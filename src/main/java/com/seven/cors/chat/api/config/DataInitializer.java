package com.seven.cors.chat.api.config;

import com.seven.cors.chat.api.model.Papel;
import com.seven.cors.chat.api.repository.PapelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PapelRepository papelRepository;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar papéis padrão
        criarPapelSeNaoExistir("ROLE_USER");
        criarPapelSeNaoExistir("ROLE_ADMIN");
        
        log.info("Inicialização de dados concluída");
    }
    
    private void criarPapelSeNaoExistir(String nome) {
        if (!papelRepository.findByNome(nome).isPresent()) {
            Papel papel = new Papel();
            papel.setNome(nome);
            papelRepository.save(papel);
            log.info("Papel criado: {}", nome);
        }
    }
} 