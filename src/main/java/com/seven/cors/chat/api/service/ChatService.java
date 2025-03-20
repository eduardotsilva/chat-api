package com.seven.cors.chat.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seven.cors.chat.api.model.ChatMessage;
import com.seven.cors.chat.api.ollama.OllamaRequest;
import com.seven.cors.chat.api.repository.ChatMessageRepository;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RestTemplate restTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public ChatMessage processUserMessage(String userMessage) {
        // Enviar para a IA
        String botResponse = sendToOllama(userMessage);

        // Criar a mensagem no banco
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUserMessage(userMessage);
        chatMessage.setBotResponse(botResponse);
        chatMessage.setTimestamp(LocalDateTime.now());

        return chatMessageRepository.save(chatMessage);
    }

    public String sendToOllama(String userMessage) {
        OllamaRequest request = new OllamaRequest("mistral", userMessage);

        // Criar headers para aceitar NDJSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_NDJSON));

        HttpEntity<OllamaRequest> entity = new HttpEntity<>(request, headers);

        // Capturar a resposta como String para evitar problemas de conversão
        ResponseEntity<String> response = restTemplate.exchange(OLLAMA_URL, HttpMethod.POST, entity, String.class);

        // Processar manualmente a resposta NDJSON
        return extractResponseFromNDJSON(response.getBody());
    }

    private String extractResponseFromNDJSON(String ndjsonResponse) {
        try {
            // Converter manualmente para UTF-8 para evitar caracteres corrompidos
            byte[] bytes = ndjsonResponse.getBytes(StandardCharsets.ISO_8859_1);
            String utf8Response = new String(bytes, StandardCharsets.UTF_8);

            List<String> jsonLines = List.of(utf8Response.split("\n"));

            List<String> responses = jsonLines.stream().map(line -> {
                try {
                    JsonNode jsonNode = objectMapper.readTree(line);
                    return jsonNode.has("response") ? jsonNode.get("response").asText() : "";
                } catch (Exception e) {
                    return "";
                }
            }).collect(Collectors.toList());

            return String.join("", responses);
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao interpretar resposta da IA.";
        }
    }
}
