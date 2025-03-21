package com.seven.cors.chat.api.service;

import com.seven.cors.chat.api.model.ChatMessage;
import com.seven.cors.chat.api.provider.IAProvider;
import com.seven.cors.chat.api.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final IAProvider iaProvider;
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage processarMensagemUsuario(String mensagemUsuario) {
        String respostaIa = iaProvider.responder(mensagemUsuario);
        ChatMessage mensagem = new ChatMessage();
        mensagem.setUserMessage(mensagemUsuario);
        mensagem.setBotResponse(respostaIa);
        mensagem.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(mensagem);
    }

    public ResponseBodyEmitter processarStreaming(String mensagemUsuario) {
        return iaProvider.responderStreaming(mensagemUsuario);
    }
}
