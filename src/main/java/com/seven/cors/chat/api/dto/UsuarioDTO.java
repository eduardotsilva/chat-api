package com.seven.cors.chat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    private UUID id;
    private String email;
    private String nome;
    private String sobrenome;
    private String fotoUrl;
    private Set<String> papeis;
} 