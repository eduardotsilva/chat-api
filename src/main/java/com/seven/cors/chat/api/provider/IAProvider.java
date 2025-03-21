package com.seven.cors.chat.api.provider;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface IAProvider {
    String responder(String pergunta);
    ResponseBodyEmitter responderStreaming(String pergunta);
}