package com.seven.cors.chat.api.service;

import com.seven.cors.chat.api.model.ChatMessage;
import com.seven.cors.chat.api.provider.IAProvider;
import com.seven.cors.chat.api.provider.StreamingResponseWrapper;
import com.seven.cors.chat.api.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    @Qualifier("ollama")
    private final IAProvider iaProvider;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Obtém todas as conversas salvas no banco de dados.
     * 
     * @return Lista de todas as mensagens
     */
    public List<ChatMessage> obterHistoricoConversas() {
        return chatMessageRepository.findAll();
    }

    public ChatMessage processarMensagemUsuario(String mensagemUsuario) {
        String respostaIa = iaProvider.responder(mensagemUsuario);
        ChatMessage mensagem = new ChatMessage();
        mensagem.setUserMessage(mensagemUsuario);
        mensagem.setBotResponse(respostaIa);
        mensagem.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(mensagem);
    }

    public ResponseBodyEmitter processarStreaming(String mensagemUsuario) {
        StreamingResponseWrapper wrapper = iaProvider.responderStreaming(mensagemUsuario);
        
        // Processar a resposta completa quando estiver disponível
        CompletableFuture<String> respostaFuture = wrapper.getRespostaCompletaFuture();
        respostaFuture.thenAccept(respostaCompleta -> {
            try {
                // Salvar a mensagem no banco de dados quando a resposta estiver completa
                ChatMessage mensagem = new ChatMessage();
                mensagem.setUserMessage(mensagemUsuario);
                mensagem.setBotResponse(respostaCompleta);
                mensagem.setTimestamp(LocalDateTime.now());
                chatMessageRepository.save(mensagem);
                log.info("Conversa de streaming salva no banco de dados. ID: {}", mensagem.getId());
            } catch (Exception e) {
                log.error("Erro ao salvar conversa de streaming no banco de dados", e);
            }
        });
        
        // Retornar apenas o emissor para o controller
        return wrapper.getEmissor();
    }
}
