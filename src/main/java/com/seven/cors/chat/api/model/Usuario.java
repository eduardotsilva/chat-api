package com.seven.cors.chat.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String nome;
    private String sobrenome;
    private String fotoUrl;
    
    @Column(unique = true)
    private String googleId;
    
    private LocalDateTime ultimoLogin;
    private LocalDateTime criadoEm;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_papeis",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "papel_id")
    )
    private Set<Papel> papeis = new HashSet<>();
    
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return papeis.stream()
                .map(papel -> new SimpleGrantedAuthority(papel.getNome()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return ""; // Não usamos senha para autenticação social
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
} 