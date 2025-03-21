# Chat-API com Integração de PDFs e Streaming

Um assistente virtual com capacidade de analisar documentos PDF e responder perguntas com base em seu conteúdo, utilizando o modelo Mistral do Ollama para geração de respostas em streaming.

![7Cors Chat API](https://img.shields.io/badge/7Cors-Chat%20API-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green)
![Java](https://img.shields.io/badge/Java-17-orange)
![Ollama](https://img.shields.io/badge/Ollama-Mistral-purple)

## 📋 Funcionalidades

- **Chat com IA**: Conversa em tempo real com o assistente usando Mistral via Ollama
- **Streaming de Respostas**: Visualização da resposta sendo gerada em tempo real
- **Upload e Análise de PDFs**: Faça upload de documentos PDF para consultar seu conteúdo
- **Chat Contextualizado**: Faça perguntas específicas sobre o conteúdo dos PDFs
- **Histórico de Conversas**: Acesse todas as conversas anteriores
- **Interface Moderna**: Design responsivo com suporte a tema escuro
- **Persistência em Banco de Dados**: Armazenamento de mensagens e documentos

## 🏗️ Arquitetura

O projeto é construído usando:

- **Spring Boot**: Framework backend para APIs REST
- **H2 Database**: Banco de dados em memória
- **Apache PDFBox**: Para extração de texto de documentos PDF
- **Ollama**: Para acesso ao modelo Mistral de IA
- **JavaScript Vanilla**: Frontend simples e eficiente sem frameworks pesados
- **Streaming SSE**: Server-sent events para streaming de respostas em tempo real

## 🔧 Pré-requisitos

1. Java 17 ou superior
2. [Ollama](https://ollama.com/) instalado e configurado com o modelo Mistral
3. Maven ou Wrapper (incluído no projeto)
4. Porta 8080 disponível para o servidor web

## 🚀 Como Executar

### Instalando o Ollama e o modelo Mistral

1. Instale o Ollama seguindo as instruções em [ollama.com](https://ollama.com/)
2. Baixe o modelo Mistral com o comando:
   ```bash
   ollama pull mistral
   ```
3. Inicie o servidor Ollama:
   ```bash
   ollama serve
   ```

### Executando a aplicação

#### Opção 1: Usando Maven
```bash
mvn clean spring-boot:run
```

#### Opção 2: Usando o Maven Wrapper (Windows)
```bash
.\mvnw.cmd clean spring-boot:run
```

#### Opção 3: Usando o Maven Wrapper (Linux/Mac)
```bash
./mvnw clean spring-boot:run
```

O servidor estará disponível em: http://localhost:8080

## 📱 Usando o Sistema

1. **Acesse a interface**: Abra http://localhost:8080 no seu navegador
2. **Chat normal**: Digite mensagens e receba respostas do assistente
3. **Upload de PDF**:
   - Clique no ícone de PDF no canto superior direito
   - Faça upload de um documento PDF
   - Selecione o documento na lista
4. **Consulte o PDF**: Com o PDF selecionado, faça perguntas específicas sobre seu conteúdo
5. **Histórico**: Veja conversas anteriores clicando no ícone de histórico

## 📚 API Endpoints

### Chat
- `POST /api/v1/chat`: Envia uma mensagem e recebe resposta síncrona
- `POST /api/v1/chat/stream`: Envia uma mensagem e recebe resposta em streaming
- `GET /api/v1/chat/historico`: Obtém o histórico de conversas

### Documentos PDF
- `POST /api/v1/documentos`: Faz upload de um documento PDF
- `GET /api/v1/documentos`: Lista todos os documentos disponíveis
- `GET /api/v1/documentos/{id}`: Obtém detalhes de um documento específico
- `GET /api/v1/documentos/{id}/download`: Baixa o arquivo PDF original
- `DELETE /api/v1/documentos/{id}`: Remove um documento

## 🔒 Limitações

- O texto extraído dos PDFs é limitado a 10.000 caracteres para evitar exceder o limite de tokens do modelo
- Documentos com imagens ou formatos complexos podem ter extração de texto limitada
- O servidor Ollama deve estar rodando localmente na porta 11434

## 🛠️ Configurações Avançadas

As configurações podem ser ajustadas no arquivo `application.yml`:

- Timeouts de streaming
- Conexões com banco de dados
- Configurações de CORS
- Mapeamentos de API

## 📜 Licença

Este projeto é licenciado sob os termos da Licença MIT.

---

Desenvolvido por 7Cors

