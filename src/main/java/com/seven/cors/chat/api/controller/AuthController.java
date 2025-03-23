package com.seven.cors.chat.api.controller;

import com.seven.cors.chat.api.config.JwtUtil;
import com.seven.cors.chat.api.dto.JwtResponseDTO;
import com.seven.cors.chat.api.dto.UsuarioDTO;
import com.seven.cors.chat.api.model.Usuario;
import com.seven.cors.chat.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "API para gerenciar autenticação de usuários")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    @Operation(summary = "Informação do usuário", description = "Retorna informações do usuário atualmente logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @GetMapping("/usuario")
    public ResponseEntity<UsuarioDTO> getUsuarioInfo(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(usuarioService.converterParaDTO(usuario));
    }

    @Operation(summary = "Login", description = "Endpoint para login social")
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        log.info("Iniciando processo de login com Google");
        response.sendRedirect("/oauth2/authorization/google");
    }

    @Operation(summary = "Sucesso no login", description = "Endpoint chamado após login bem-sucedido")
    @GetMapping("/oauth2/success")
    public void oauth2Success(HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("Processo de autenticação OAuth2 concluído, gerando token JWT");
        
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Obtendo autenticação do SecurityContextHolder");
        }
        
        if (authentication == null) {
            log.error("Autenticação é nula no contexto de segurança");
            response.sendRedirect("/login.html?error=authentication_failed");
            return;
        }
        
        log.info("Tipo de autenticação: {}", authentication.getClass().getName());
        log.info("Principal type: {}", authentication.getPrincipal().getClass().getName());
        log.info("Authorities: {}", authentication.getAuthorities());
        
        try {
            Object principal = authentication.getPrincipal();
            String email = null;
            
            if (principal instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) principal;
                Map<String, Object> attributes = oAuth2User.getAttributes();
                log.info("Atributos OAuth2: {}", attributes);
                email = (String) attributes.get("email");
            } else if (principal instanceof Usuario) {
                email = ((Usuario) principal).getEmail();
            } else {
                log.error("Tipo de principal não reconhecido: {}", principal.getClass().getName());
                response.sendRedirect("/login.html?error=unknown_principal_type");
                return;
            }
            
            log.info("Email obtido da autenticação: {}", email);
            
            if (email == null) {
                log.error("Email não encontrado na autenticação");
                response.sendRedirect("/login.html?error=email_not_found");
                return;
            }
            
            Usuario usuario = (Usuario) usuarioService.loadUserByUsername(email);
            log.info("Usuário carregado do banco: {}", usuario.getUsername());
            
            String token = jwtUtil.generateToken(usuario);
            log.info("Token JWT gerado com sucesso");
            
            // Armazenar o token em cookie
            Cookie jwtCookie = new Cookie("jwt_token", token);
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(86400); // 24 horas em segundos
            response.addCookie(jwtCookie);
            
            // Redirecionar para a página principal após login bem-sucedido
            response.sendRedirect("/chat.html");
        } catch (Exception e) {
            log.error("Erro ao processar autenticação: {}", e.getMessage(), e);
            response.sendRedirect("/login.html?error=authentication_processing_error");
        }
    }
    
    @Operation(summary = "Redirecionamento OAuth2", description = "Endpoint para redirecionamento após autenticação OAuth2")
    @GetMapping("/oauth2/code/google")
    public void oauth2Redirect(HttpServletResponse response) throws IOException {
        log.info("Recebido redirecionamento OAuth2 do Google");
        response.sendRedirect("/auth/oauth2/success");
    }

    @Operation(summary = "Validar token", description = "Verifica se um token JWT é válido")
    @GetMapping("/token/validar")
    public ResponseEntity<Map<String, Boolean>> validarToken(HttpServletRequest request) {
        // Extrair o token do cabeçalho
        String token = null;
        String headerAuth = request.getHeader("Authorization");
        
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            token = headerAuth.substring(7);
        }
        
        // Verificar também nos cookies
        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        // Verificar se o token é válido
        Map<String, Boolean> response = new HashMap<>();
        
        if (token != null) {
            try {
                String username = jwtUtil.extractUsername(token);
                Usuario usuario = (Usuario) usuarioService.loadUserByUsername(username);
                boolean isValid = jwtUtil.validateToken(token, usuario);
                response.put("valid", isValid);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                log.warn("Erro ao validar token: {}", e.getMessage());
                response.put("valid", false);
                return ResponseEntity.ok(response);
            }
        }
        
        response.put("valid", false);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Realiza o logout do usuário")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Remover o cookie JWT
        Cookie jwtCookie = new Cookie("jwt_token", "");
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        return ResponseEntity.ok().build();
    }
} 