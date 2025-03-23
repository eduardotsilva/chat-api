package com.seven.cors.chat.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Controller
@Slf4j
public class WebController {

    @GetMapping("/")
    public String index() {
        log.info("Acessando a página inicial (raiz)");
        return "redirect:/login.html";
    }

    @GetMapping("/login")
    public String login() {
        log.info("Acessando a página de login via /login");
        return "redirect:/login.html";
    }

    @GetMapping("/chat")
    public String chat() {
        log.info("Acessando a página de chat via /chat");
        return "redirect:/chat.html";
    }

    // Manipulação do redirecionamento do OAuth2
    @GetMapping("/login/oauth2/code/google")
    public String handleOAuth2CodeRedirect(
            HttpServletRequest request,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) {
        
        log.info("Recebido redirecionamento OAuth2 do Google");
        log.debug("URL da requisição: {}", request.getRequestURL());
        log.debug("Query string: {}", request.getQueryString());
        log.debug("Parâmetros - code: {} (tamanho: {}), state: {}", 
                  code != null ? "presente" : "ausente", 
                  code != null ? code.length() : 0, 
                  state);
        
        return "redirect:/auth/oauth2/success";
    }
    
    // Fallback para servir páginas HTML diretamente como resposta String
    @GetMapping(value = "/login.html", produces = "text/html")
    @ResponseBody
    public String getLoginPage() throws IOException {
        log.info("Servindo a página de login.html diretamente");
        return readResourceAsString("static/login.html");
    }
    
    @GetMapping(value = "/chat.html", produces = "text/html")
    @ResponseBody
    public String getChatPage() throws IOException {
        log.info("Servindo a página de chat.html diretamente");
        return readResourceAsString("static/chat.html");
    }
    
    // Método para ler recursos como String
    private String readResourceAsString(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
} 