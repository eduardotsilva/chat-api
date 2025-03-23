package com.seven.cors.chat.api.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                try {
                    String username = jwtUtil.extractUsername(jwt);
                    log.debug("Processando token JWT para usuário: {}", username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        if (jwtUtil.validateToken(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("Usuário autenticado via JWT: {}", username);
                        } else {
                            log.warn("Token JWT inválido para usuário: {}", username);
                        }
                    }
                } catch (ExpiredJwtException e) {
                    log.warn("Token JWT expirado: {}", e.getMessage());
                } catch (MalformedJwtException e) {
                    log.warn("Token JWT inválido: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Não foi possível realizar a autenticação: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        // Verificar primeiro no cabeçalho de autorização
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        // Se não encontrar no cabeçalho, verificar nos cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (StringUtils.hasText(token)) {
                        log.debug("Token JWT encontrado em cookie");
                        return token;
                    }
                }
            }
        }

        return null;
    }
} 