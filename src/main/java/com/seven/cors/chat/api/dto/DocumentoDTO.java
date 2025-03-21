package com.seven.cors.chat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para transferência de dados de documentos PDF.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDTO {
    private UUID id;
    private String nomeArquivo;
    private long tamanhoBytes;
    private String dataUpload;
    
    // Tamanho formatado para exibição
    public String getTamanhoFormatado() {
        if (tamanhoBytes < 1024) {
            return tamanhoBytes + " bytes";
        } else if (tamanhoBytes < 1024 * 1024) {
            return String.format("%.2f KB", tamanhoBytes / 1024.0);
        } else {
            return String.format("%.2f MB", tamanhoBytes / (1024.0 * 1024.0));
        }
    }
} 