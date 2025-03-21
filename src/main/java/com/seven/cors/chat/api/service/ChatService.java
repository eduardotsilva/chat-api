package com.seven.cors.chat.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seven.cors.chat.api.model.ChatMessage;
import com.seven.cors.chat.api.ollama.OllamaRequest;
import com.seven.cors.chat.api.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String URL_OLLAMA = "http://localhost:11434/api/generate";

    private final RestTemplate restTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper conversorJson = new ObjectMapper();

    public ChatMessage processarMensagemUsuario(String mensagemUsuario) {
        String respostaIa = enviarParaIa(mensagemUsuario, false);
        ChatMessage mensagem = criarMensagemChat(mensagemUsuario, respostaIa);
        return chatMessageRepository.save(mensagem);
    }

    public ResponseBodyEmitter processarStreaming(String mensagemUsuario) {
        ResponseBodyEmitter emissor = new ResponseBodyEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                OllamaRequest requisicao = new OllamaRequest("mistral", mensagemUsuario, true);
                HttpEntity<OllamaRequest> entidade = construirHttpEntity(requisicao);

                RequestCallback callback = restTemplate.httpEntityCallback(entidade, String.class);

                restTemplate.execute(URL_OLLAMA, HttpMethod.POST, callback, resposta -> {
                    try (BufferedReader leitor = new BufferedReader(new InputStreamReader(resposta.getBody()))) {
                        String linha;
                        while ((linha = leitor.readLine()) != null) {
                            enviarFragmentoParaCliente(emissor, linha);
                        }
                        emissor.complete();
                    } catch (Exception e) {
                        emissor.completeWithError(e);
                    }
                    return null;
                });

            } catch (Exception e) {
                tratarErroStreaming(emissor, "Erro ao processar IA.", e);
            } finally {
                executor.shutdown();
            }
        });

        return emissor;
    }

    private String enviarParaIa(String mensagemUsuario, boolean streaming) {
        OllamaRequest requisicao = new OllamaRequest("mistral", mensagemUsuario, streaming);
        HttpEntity<OllamaRequest> entidade = construirHttpEntity(requisicao);

        try {
            ResponseEntity<String> resposta = restTemplate.exchange(URL_OLLAMA, HttpMethod.POST, entidade, String.class);
            return extrairTextoDaRespostaNDJSON(resposta.getBody());
        } catch (Exception e) {
            return "Erro ao se comunicar com a IA.";
        }
    }

    private HttpEntity<OllamaRequest> construirHttpEntity(OllamaRequest requisicao) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_JSON);
        cabecalhos.setAccept(List.of(MediaType.APPLICATION_NDJSON));
        return new HttpEntity<>(requisicao, cabecalhos);
    }

    private ChatMessage criarMensagemChat(String mensagemUsuario, String respostaIa) {
        ChatMessage mensagem = new ChatMessage();
        mensagem.setUserMessage(mensagemUsuario);
        mensagem.setBotResponse(respostaIa);
        mensagem.setTimestamp(LocalDateTime.now());
        return mensagem;
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

    private void enviarFragmentoParaCliente(ResponseBodyEmitter emissor, String linha) throws IOException {
        String fragmento = extrairFragmentoTexto(linha);
        if (!fragmento.isBlank()) {
            emissor.send(fragmento);
        }
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
