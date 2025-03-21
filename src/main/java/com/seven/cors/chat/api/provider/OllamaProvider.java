package com.seven.cors.chat.api.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seven.cors.chat.api.ollama.OllamaRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Qualifier("ollama")
public class OllamaProvider implements IAProvider {

    private static final String URL_OLLAMA = "http://localhost:11434/api/generate";
    private final RestTemplate restTemplate;
    private final ObjectMapper conversorJson = new ObjectMapper();

    @Override
    public String responder(String pergunta) {
        OllamaRequest requisicao = new OllamaRequest("mistral", pergunta, false);
        HttpEntity<OllamaRequest> entidade = construirHttpEntity(requisicao);

        try {
            ResponseEntity<String> resposta = restTemplate.exchange(URL_OLLAMA, HttpMethod.POST, entidade, String.class);
            return extrairTextoDaRespostaNDJSON(resposta.getBody());
        } catch (Exception e) {
            return "Erro ao se comunicar com a IA.";
        }
    }

    @Override
    public StreamingResponseWrapper responderStreaming(String pergunta) {
        // Configurar o emissor com timeout mais longo (2 minutos = 120000ms)
        ResponseBodyEmitter emissor = new ResponseBodyEmitter(120000L);
        CompletableFuture<String> respostaCompletaFuture = new CompletableFuture<>();
        StringBuilder respostaCompleta = new StringBuilder();
        
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                OllamaRequest requisicao = new OllamaRequest("mistral", pergunta, true);
                HttpEntity<OllamaRequest> entidade = construirHttpEntity(requisicao);
                RequestCallback callback = restTemplate.httpEntityCallback(entidade, String.class);

                // Configurar temporizador para enviar pulsos keep-alive a cada 20 segundos
                ExecutorService keepAliveExecutor = Executors.newSingleThreadExecutor();
                keepAliveExecutor.execute(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            Thread.sleep(20000); // 20 segundos
                            emissor.send("", MediaType.TEXT_EVENT_STREAM); // Enviar pulso vazio como keep-alive
                        }
                    } catch (Exception e) {
                        // Thread interrompida, ignorar
                    }
                });

                restTemplate.execute(URL_OLLAMA, HttpMethod.POST, callback, resposta -> {
                    try (BufferedReader leitor = new BufferedReader(new InputStreamReader(resposta.getBody()))) {
                        String linha;
                        while ((linha = leitor.readLine()) != null) {
                            String fragmento = extrairFragmentoTexto(linha);
                            if (!fragmento.isBlank()) {
                                respostaCompleta.append(fragmento);
                                enviarFragmentoParaCliente(emissor, fragmento);
                            }
                        }
                        emissor.complete();
                        keepAliveExecutor.shutdownNow(); // Interromper o keep-alive após terminar
                        respostaCompletaFuture.complete(respostaCompleta.toString());
                    } catch (Exception e) {
                        emissor.completeWithError(e);
                        keepAliveExecutor.shutdownNow(); // Interromper o keep-alive em caso de erro
                        respostaCompletaFuture.completeExceptionally(e);
                    }
                    return null;
                });
            } catch (Exception e) {
                tratarErroStreaming(emissor, "Erro ao processar IA.", e);
                respostaCompletaFuture.completeExceptionally(e);
            } finally {
                executor.shutdown();
            }
        });

        return new StreamingResponseWrapper(emissor, respostaCompletaFuture);
    }

    private HttpEntity<OllamaRequest> construirHttpEntity(OllamaRequest requisicao) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_JSON);
        cabecalhos.setAccept(List.of(MediaType.APPLICATION_NDJSON));
        return new HttpEntity<>(requisicao, cabecalhos);
    }

    private String extrairTextoDaRespostaNDJSON(String respostaNdjson) {
        try {
            String respostaUtf8 = new String(respostaNdjson.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            List<String> linhas = List.of(respostaUtf8.split("\n"));
            return linhas.stream()
                    .map(this::extrairFragmentoTexto)
                    .collect(Collectors.joining());
        } catch (Exception e) {
            return "Erro ao interpretar resposta da IA.";
        }
    }

    private String extrairFragmentoTexto(String linha) {
        try {
            JsonNode json = conversorJson.readTree(linha);
            return json.has("response") ? json.get("response").asText() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void enviarFragmentoParaCliente(ResponseBodyEmitter emissor, String fragmento) throws IOException {
        emissor.send(fragmento);
    }

    private void tratarErroStreaming(ResponseBodyEmitter emissor, String mensagemErro, Exception excecaoOriginal) {
        try {
            emissor.send(mensagemErro);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem de erro para o cliente: " + e.getMessage());
        } finally {
            emissor.completeWithError(excecaoOriginal);
        }
    }
}