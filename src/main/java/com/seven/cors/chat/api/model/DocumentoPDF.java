package com.seven.cors.chat.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoPDF {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    private String nomeArquivo;
    
    @Lob
    @Column(columnDefinition = "CLOB")
    private String conteudoTexto;
    
    private LocalDateTime dataUpload;
    
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] arquivoPDF;
    
    private long tamanhoBytes;
    
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (dataUpload == null) {
            dataUpload = LocalDateTime.now();
        }
    }
} 