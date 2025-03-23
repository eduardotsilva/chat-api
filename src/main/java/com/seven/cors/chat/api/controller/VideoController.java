package com.seven.cors.chat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/video")
@Tag(name = "Video", description = "API para streaming de vídeos")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

    private final ResourceLoader resourceLoader;
    
    @Operation(summary = "Listar vídeos", description = "Retorna a lista de vídeos disponíveis")
    @GetMapping("/listar")
    public ResponseEntity<List<String>> listarVideos() {
        log.info("Listando vídeos disponíveis");
        List<String> videos = new ArrayList<>();
        
        try {
            // Verificar vídeos em resources/static/videos
            Resource videosDir = new ClassPathResource("static/videos");
            if (videosDir.exists()) {
                File dir = videosDir.getFile();
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles((file, name) -> 
                        name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".ogg"));
                    
                    if (files != null) {
                        for (File file : files) {
                            videos.add(file.getName());
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Erro ao listar vídeos: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(videos);
    }

    @Operation(summary = "Stream de vídeo", description = "Realiza streaming de um vídeo específico com suporte a range requests")
    @GetMapping("/stream/{nome}")
    public void streamVideo(
            @PathVariable String nome,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletResponse response) {
                
        try {
            log.debug("Iniciando streaming do vídeo: {}", nome);
            
            // Verificar se o vídeo existe no classpath
            Resource classpathResource = new ClassPathResource("static/videos/" + nome);
            Resource videoResource;
            
            if (classpathResource.exists()) {
                // Se existir no classpath, usamos como referência para extrair o arquivo
                log.debug("Vídeo encontrado no classpath: {}", nome);
                videoResource = classpathResource;
            } else {
                // Verificar no diretório externo (configurable via application.properties)
                Path videoPath = Paths.get(System.getProperty("user.dir"), "videos", nome);
                log.debug("Verificando vídeo no caminho externo: {}", videoPath);
                
                if (Files.exists(videoPath)) {
                    videoResource = new UrlResource(videoPath.toUri());
                    log.debug("Vídeo encontrado no caminho externo");
                } else {
                    log.warn("Vídeo não encontrado em nenhum local: {}", nome);
                    response.sendError(HttpStatus.NOT_FOUND.value(), "Vídeo não encontrado");
                    return;
                }
            }
            
            long fileSize = videoResource.contentLength();
            String contentType = determinarContentType(nome);
            log.debug("Tamanho do arquivo: {} bytes, Tipo de conteúdo: {}", fileSize, contentType);
            
            // Definir os headers para streaming
            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes");
            
            // Processar Range header para suportar seeking
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                log.debug("Processando range request: {}", rangeHeader);
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                
                long end = fileSize - 1;
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
                
                log.debug("Range calculado: bytes {}-{}/{}", start, end, fileSize);
                
                if (start >= fileSize || end >= fileSize || start > end) {
                    log.warn("Range inválido: start={}, end={}, fileSize={}", start, end, fileSize);
                    response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                    return;
                }
                
                long contentLength = end - start + 1;
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
                response.setHeader("Content-Length", String.valueOf(contentLength));
                response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
                log.debug("Enviando conteúdo parcial: {} bytes", contentLength);
                
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(videoResource.getFile(), "r")) {
                    byte[] buffer = new byte[8192];
                    randomAccessFile.seek(start);
                    
                    long bytesRemaining = contentLength;
                    int bytesRead;
                    
                    while (bytesRemaining > 0 && 
                          (bytesRead = randomAccessFile.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                        response.getOutputStream().write(buffer, 0, bytesRead);
                        bytesRemaining -= bytesRead;
                    }
                    
                    log.debug("Streaming concluído com sucesso para range {}-{}", start, end);
                }
            } else {
                log.debug("Nenhum range especificado, enviando arquivo completo");
                // Transmitir todo o conteúdo se não houver range
                response.setHeader("Content-Length", String.valueOf(fileSize));
                
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(videoResource.getFile(), "r")) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    
                    while ((bytesRead = randomAccessFile.read(buffer)) != -1) {
                        response.getOutputStream().write(buffer, 0, bytesRead);
                    }
                    
                    log.debug("Streaming do arquivo completo concluído com sucesso");
                }
            }
            
        } catch (IOException e) {
            log.error("Erro ao transmitir vídeo: {}", e.getMessage(), e);
            try {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro ao transmitir vídeo");
            } catch (IOException ex) {
                log.error("Erro ao enviar resposta de erro: {}", ex.getMessage());
            }
        }
    }
    
    @Operation(summary = "Página do player de vídeo", description = "Retorna a página HTML com o player de vídeo")
    @GetMapping(value = "/player", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String getPaginaPlayer() {
        try {
            Resource resource = new ClassPathResource("static/video-player.html");
            return new String(Files.readAllBytes(Paths.get(resource.getURI())));
        } catch (IOException e) {
            log.error("Erro ao carregar página do player: {}", e.getMessage());
            return "<html><body><h1>Erro ao carregar player de vídeo</h1></body></html>";
        }
    }
    
    private String determinarContentType(String nomeArquivo) {
        nomeArquivo = nomeArquivo.toLowerCase();
        if (nomeArquivo.endsWith(".mp4")) {
            return "video/mp4";
        } else if (nomeArquivo.endsWith(".webm")) {
            return "video/webm";
        } else if (nomeArquivo.endsWith(".ogg")) {
            return "video/ogg";
        } else {
            return "application/octet-stream";
        }
    }
} 