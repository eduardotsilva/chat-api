package com.seven.cors.chat.api.provider;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * Wrapper para encapsular a resposta por streaming e a resposta completa quando finalizada.
 */
public class StreamingResponseWrapper {
    private final ResponseBodyEmitter emissor;
    private final CompletableFuture<String> respostaCompletaFuture;

    public StreamingResponseWrapper(ResponseBodyEmitter emissor, CompletableFuture<String> respostaCompletaFuture) {
        this.emissor = emissor;
        this.respostaCompletaFuture = respostaCompletaFuture;
    }

    /**
     * Obtém o emissor para enviar a resposta em tempo real ao cliente.
     */
    public ResponseBodyEmitter getEmissor() {
        return emissor;
    }

    /**
     * Obtém um CompletableFuture que será concluído quando a resposta estiver completa.
     */
    public CompletableFuture<String> getRespostaCompletaFuture() {
        return respostaCompletaFuture;
    }
} 