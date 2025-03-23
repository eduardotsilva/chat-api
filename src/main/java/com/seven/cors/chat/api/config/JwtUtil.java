package com.seven.cors.chat.api.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            log.debug("Username extraído do token: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Erro ao extrair username do token: {}", e.getMessage());
            throw e;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            log.debug("Processando token JWT: {}", token.substring(0, Math.min(10, token.length())) + "...");
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("Claims extraídos do token: {}", claims);
            return claims;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expirado: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("Erro ao processar JWT: {}", e.getMessage());
            throw e;
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean isExpired = expiration.before(new Date());
            log.debug("Token expira em: {}, expirado: {}", expiration, isExpired);
            return isExpired;
        } catch (ExpiredJwtException e) {
            log.warn("Token já expirado: {}", e.getMessage());
            return true;
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername());
        log.info("Token JWT gerado para usuário: {}", userDetails.getUsername());
        log.debug("Token gerado: {}", token.substring(0, Math.min(10, token.length())) + "...");
        return token;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        log.debug("Criando token para '{}' válido até {}", subject, expiryDate);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            log.debug("Validando token para usuário: {}, válido: {}", username, isValid);
            return isValid;
        } catch (JwtException e) {
            log.error("Erro ao validar token JWT: {}", e.getMessage());
            return false;
        }
    }
} 