package com.seven.cors.chat.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class WebController {
    
    @GetMapping("/")
    public RedirectView redirectToChat() {
        return new RedirectView("/chat.html");
    }
} 