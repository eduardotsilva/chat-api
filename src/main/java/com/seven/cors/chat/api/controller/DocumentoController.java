package com.seven.cors.chat.api.controller;

import com.seven.cors.chat.api.dto.DocumentoDTO;
import com.seven.cors.chat.api.model.DocumentoPDF;
import com.seven.cors.chat.api.service.DocumentoPDFService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app.api.base}/documentos")
@Tag(name = "Documentos", description = "API para gerenciamento de documentos PDF")
@RequiredArgsConstructor
@Slf4j
public class DocumentoController {

    private final DocumentoPDFService documentoService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Operation(summary = "Fazer upload de PDF", description = "Faz upload de um documento PDF para análise")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Documento PDF carregado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido ou erro de processamento")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoDTO> uploadDocumento(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            DocumentoPDF documento = documentoService.processarEArmazenarPDF(arquivo);
            return ResponseEntity.status(HttpStatus.CREATED).body(converterParaDTO(documento));
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao processar arquivo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Erro ao processar PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Listar documentos", description = "Lista todos os documentos PDF disponíveis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de documentos retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<DocumentoDTO>> listarDocumentos() {
        List<DocumentoDTO> documentos = documentoService.obterTodosDocumentos().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documentos);
    }

    @Operation(summary = "Obter documento", description = "Retorna um documento PDF específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documento encontrado"),
        @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentoDTO> obterDocumento(@PathVariable UUID id) {
        return documentoService.obterDocumentoPorId(id)
                .map(documento -> ResponseEntity.ok(converterParaDTO(documento)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Download de PDF", description = "Faz download do arquivo PDF original")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Download realizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocumento(@PathVariable UUID id) {
        return documentoService.obterDocumentoPorId(id)
                .map(documento -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", documento.getNomeArquivo());
                    headers.setContentLength(documento.getTamanhoBytes());
                    return new ResponseEntity<>(documento.getArquivoPDF(), headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Excluir documento", description = "Remove um documento PDF do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Documento excluído com sucesso"),
        @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirDocumento(@PathVariable UUID id) {
        if (documentoService.obterDocumentoPorId(id).isPresent()) {
            documentoService.excluirDocumento(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Converte um DocumentoPDF para o DTO correspondente.
     */
    private DocumentoDTO converterParaDTO(DocumentoPDF documento) {
        return DocumentoDTO.builder()
                .id(documento.getId())
                .nomeArquivo(documento.getNomeArquivo())
                .tamanhoBytes(documento.getTamanhoBytes())
                .dataUpload(documento.getDataUpload().format(DATE_FORMATTER))
                .build();
    }
} 