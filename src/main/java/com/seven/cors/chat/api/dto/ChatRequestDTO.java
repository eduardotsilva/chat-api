package com.seven.cors.chat.api.dto;

import lombok.Data;

@Data
public class ChatRequestDTO {
    private String message;
    private boolean stream;
}