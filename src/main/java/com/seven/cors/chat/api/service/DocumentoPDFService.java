package com.seven.cors.chat.api.service;

import com.seven.cors.chat.api.model.DocumentoPDF;
import com.seven.cors.chat.api.repository.DocumentoPDFRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoPDFService {

    private final DocumentoPDFRepository documentoRepository;

    /**
     * Processa um arquivo PDF, extraindo seu texto, e salvando no banco de dados.
     * 
     * @param arquivo O arquivo PDF enviado pelo usuário
     * @return O documento PDF processado e salvo
     * @throws IOException Se ocorrer erro ao processar o PDF
     */
    public DocumentoPDF processarEArmazenarPDF(MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo PDF vazio");
        }

        // Verificar se é um PDF
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("O arquivo deve ser um PDF");
        }

        // Ler o conteúdo do PDF
        byte[] conteudoPDF = arquivo.getBytes();
        String textoExtraido = extrairTextoDoPDF(conteudoPDF);

        // Criar e salvar o documento
        DocumentoPDF documento = new DocumentoPDF();
        documento.setNomeArquivo(arquivo.getOriginalFilename());
        documento.setConteudoTexto(textoExtraido);
        documento.setArquivoPDF(conteudoPDF);
        documento.setTamanhoBytes(arquivo.getSize());
        documento.setDataUpload(LocalDateTime.now());

        log.info("Documento PDF processado: {}, tamanho: {} bytes", documento.getNomeArquivo(), documento.getTamanhoBytes());
        
        return documentoRepository.save(documento);
    }

    /**
     * Extrai o texto de um arquivo PDF.
     *
     * @param conteudoPDF O conteúdo do PDF em bytes
     * @return O texto extraído do PDF
     * @throws IOException Se ocorrer erro ao ler o PDF
     */
    private String extrairTextoDoPDF(byte[] conteudoPDF) throws IOException {
        try (PDDocument document = PDDocument.load(conteudoPDF)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Obtém todos os documentos PDF armazenados.
     *
     * @return Lista de documentos PDF
     */
    public List<DocumentoPDF> obterTodosDocumentos() {
        return documentoRepository.findAll();
    }

    /**
     * Obtém um documento PDF pelo ID.
     *
     * @param id O ID do documento
     * @return O documento, se encontrado
     */
    public Optional<DocumentoPDF> obterDocumentoPorId(UUID id) {
        return documentoRepository.findById(id);
    }

    /**
     * Exclui um documento PDF pelo ID.
     *
     * @param id O ID do documento a ser excluído
     */
    public void excluirDocumento(UUID id) {
        documentoRepository.deleteById(id);
        log.info("Documento PDF excluído: ID {}", id);
    }
} 