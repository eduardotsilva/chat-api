package com.seven.cors.chat.api.repository;

import com.seven.cors.chat.api.model.Papel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PapelRepository extends JpaRepository<Papel, UUID> {
    Optional<Papel> findByNome(String nome);
} 