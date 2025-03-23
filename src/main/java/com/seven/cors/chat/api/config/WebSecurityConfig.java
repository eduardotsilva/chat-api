package com.seven.cors.chat.api.config;

import com.seven.cors.chat.api.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final UsuarioService usuarioService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configurando SecurityFilterChain");
        
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                // URLs públicas que não requerem autenticação
                .requestMatchers(
                    // Páginas públicas de login e recursos estáticos
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/login"),
                    new AntPathRequestMatcher("/login.html"),
                    new AntPathRequestMatcher("/oauth2/**"),
                    new AntPathRequestMatcher("/login/oauth2/code/**"),
                    
                    // Todos os recursos estáticos
                    new AntPathRequestMatcher("/css/**"),
                    new AntPathRequestMatcher("/js/**"),
                    new AntPathRequestMatcher("/images/**"),
                    new AntPathRequestMatcher("/fonts/**"),
                    new AntPathRequestMatcher("/favicon.ico"),
                    new AntPathRequestMatcher("/static/**"),
                    new AntPathRequestMatcher("/*.js"),
                    new AntPathRequestMatcher("/*.css"),
                    new AntPathRequestMatcher("/*.png"),
                    new AntPathRequestMatcher("/*.jpg"),
                    new AntPathRequestMatcher("/*.svg"),
                    
                    // APIs públicas
                    new AntPathRequestMatcher("/auth/**"),
                    new AntPathRequestMatcher("/api/v1/public/**"),
                    
                    // Documentação e console H2
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/h2-console/**")
                ).permitAll()
                
                // URLs que requerem autenticação
                .requestMatchers(
                    new AntPathRequestMatcher("/chat.html"),
                    new AntPathRequestMatcher("/chat"),
                    new AntPathRequestMatcher("/api/v1/chat/**"),
                    new AntPathRequestMatcher("/api/v1/documento/**"),
                    new AntPathRequestMatcher("/api/v1/conversa/**")
                ).authenticated()
                
                // Qualquer outra URL requer autenticação
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login.html")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(usuarioService)
                )
                .defaultSuccessUrl("/auth/oauth2/success", true)
                .failureUrl("/login.html?error=authentication_failed")
                .permitAll()
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );
            
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "X-Session-ID"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 