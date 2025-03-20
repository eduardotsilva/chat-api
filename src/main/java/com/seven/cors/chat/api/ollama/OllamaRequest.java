package com.seven.cors.chat.api.ollama;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OllamaRequest {
    private String model;
    private String prompt;
}