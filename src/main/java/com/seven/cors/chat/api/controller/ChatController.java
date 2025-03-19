package com.seven.cors.chat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.ChatRequest;

import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("${app.api.base}/chat")
@Tag(name = "Chat", description = "API para gerenciamento de mensagens de chat")
public class ChatController {

    @Operation(summary = "Obter mensagens do chat", description = "Retorna uma lista de mensagens do chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de mensagens retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @GetMapping("/messages")
    public List<String> getMessages() {
        return Arrays.asList("Olá!", "Bem-vindo ao chat!", "Como posso ajudar?");
    }
    
    
    
    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok("Resposta do chatbot aqui...");
    }
    
}
