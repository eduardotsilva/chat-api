package com.seven.cors.chat.api.repository;

import com.seven.cors.chat.api.model.DocumentoPDF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentoPDFRepository extends JpaRepository<DocumentoPDF, UUID> {
    // Métodos personalizados podem ser adicionados aqui se necessário
} 