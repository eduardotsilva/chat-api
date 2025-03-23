package com.seven.cors.chat.api.service;

import com.seven.cors.chat.api.dto.UsuarioDTO;
import com.seven.cors.chat.api.model.Papel;
import com.seven.cors.chat.api.model.Usuario;
import com.seven.cors.chat.api.repository.PapelRepository;
import com.seven.cors.chat.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService implements UserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UsuarioRepository usuarioRepository;
    private final PapelRepository papelRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            log.info("Iniciando autenticação OAuth2");
            
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            
            // Processar os atributos do usuário OAuth2
            Map<String, Object> attributes = oAuth2User.getAttributes();
            log.info("Atributos OAuth2: {}", attributes);
            
            String email = (String) attributes.get("email");
            String nome = (String) attributes.get("given_name");
            String sobrenome = (String) attributes.get("family_name");
            String fotoUrl = (String) attributes.get("picture");
            String googleId = (String) attributes.get("sub");
            
            log.info("Processando OAuth2 para usuário com email: {}", email);
            
            if (email == null) {
                log.error("Email não encontrado no perfil Google");
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_email"), "Email não encontrado no perfil Google");
            }
            
            // Verificar se o usuário já existe
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);
            Usuario usuario;
            
            if (usuarioExistente.isPresent()) {
                // Atualizar usuário existente
                log.info("Atualizando usuário existente: {}", email);
                usuario = usuarioExistente.get();
                usuario.setNome(nome != null ? nome : usuario.getNome());
                usuario.setSobrenome(sobrenome != null ? sobrenome : usuario.getSobrenome());
                usuario.setFotoUrl(fotoUrl != null ? fotoUrl : usuario.getFotoUrl());
                usuario.setGoogleId(googleId);
                usuario.setUltimoLogin(LocalDateTime.now());
            } else {
                // Criar novo usuário
                log.info("Criando novo usuário: {}", email);
                Papel papelUsuario = papelRepository.findByNome("ROLE_USER")
                        .orElseGet(() -> {
                            log.info("Criando novo papel ROLE_USER");
                            Papel novoPapel = new Papel();
                            novoPapel.setNome("ROLE_USER");
                            return papelRepository.save(novoPapel);
                        });
                
                usuario = Usuario.builder()
                        .email(email)
                        .nome(nome)
                        .sobrenome(sobrenome)
                        .fotoUrl(fotoUrl)
                        .googleId(googleId)
                        .ultimoLogin(LocalDateTime.now())
                        .criadoEm(LocalDateTime.now())
                        .papeis(Collections.singleton(papelUsuario))
                        .build();
            }
            
            usuario = usuarioRepository.save(usuario);
            
            log.info("Usuário autenticado com Google: {}", email);
            
            // Retorna o usuário criado/atualizado como OAuth2User para compatibilidade com Spring Security
            // Este é um wrapper que adiciona os attributes do OAuth2 ao objeto Usuario
            return new UsuarioOAuth2UserWrapper(usuario, attributes);
            
        } catch (Exception e) {
            log.error("Erro durante autenticação OAuth2: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException(new OAuth2Error("authentication_error"), "Erro de autenticação: " + e.getMessage());
        }
    }
    
    /**
     * Converte um usuário para DTO
     */
    public UsuarioDTO converterParaDTO(Usuario usuario) {
        Set<String> papeis = usuario.getPapeis().stream()
                .map(Papel::getNome)
                .collect(Collectors.toSet());
        
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nome(usuario.getNome())
                .sobrenome(usuario.getSobrenome())
                .fotoUrl(usuario.getFotoUrl())
                .papeis(papeis)
                .build();
    }
    
    /**
     * Classe wrapper para combinar Usuario com OAuth2User
     */
    private static class UsuarioOAuth2UserWrapper extends Usuario implements OAuth2User {
        private final Map<String, Object> attributes;
        
        public UsuarioOAuth2UserWrapper(Usuario usuario, Map<String, Object> attributes) {
            super(usuario.getId(), usuario.getEmail(), usuario.getNome(), usuario.getSobrenome(), 
                  usuario.getFotoUrl(), usuario.getGoogleId(), usuario.getUltimoLogin(), 
                  usuario.getCriadoEm(), usuario.getPapeis());
            this.attributes = attributes;
        }
        
        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }
        
        @Override
        public String getName() {
            return getEmail();
        }
    }
} 