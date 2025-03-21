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
public class ChatMessage {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    private String userMessage;
    @Lob
    @Column(columnDefinition = "CLOB") 
    private String botResponse;
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}