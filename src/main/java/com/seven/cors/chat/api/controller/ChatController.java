package com.seven.cors.chat.api.controller;

import com.seven.cors.chat.api.dto.ChatRequestDTO;
import com.seven.cors.chat.api.model.ChatMessage;
import com.seven.cors.chat.api.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("${app.api.base}/chat")
@Tag(name = "Chat", description = "API para interação com o assistente virtual")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Operation(summary = "Conversas salvas", description = "Retorna uma lista fixa de mensagens de exemplo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mensagens retornadas com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/mensagens")
    public List<String> listarMensagens() {
        return Arrays.asList("Olá!", "Bem-vindo ao chat!", "Como posso ajudar?");
    }

    @Operation(summary = "Histórico de conversas", description = "Retorna todas as conversas salvas no banco de dados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/historico")
    public ResponseEntity<List<ChatMessage>> historicoConversas() {
        List<ChatMessage> historico = chatService.obterHistoricoConversas();
        return ResponseEntity.ok(historico);
    }

    @Operation(summary = "Conversa via mensagem", description = "Envia uma mensagem ao assistente virtual e recebe a resposta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mensagem processada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping
    public ResponseEntity<ChatMessage> conversar(@RequestBody ChatRequestDTO requisicao) {
        ChatMessage resposta = chatService.processarMensagemUsuario(requisicao.getMessage());
        return ResponseEntity.ok(resposta);
    }

    @Operation(summary = "Conversa via streaming", description = "Realiza uma conversa com o assistente utilizando resposta contínua (stream)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Streaming iniciado com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter conversarStreaming(@RequestBody String mensagemUsuario) {
        // Configurar um timeout mais longo (2 minutos)
        return chatService.processarStreaming(mensagemUsuario);
    }
}
