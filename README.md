# 7Cors Chat API

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Ollama](https://img.shields.io/badge/Ollama-latest-blue.svg)
![JWT](https://img.shields.io/badge/JWT-Auth-yellow.svg)
![OAuth2](https://img.shields.io/badge/OAuth2-Google-red.svg)

API para chat com inteligência artificial que utiliza streaming para respostas em tempo real. Integra-se com o Ollama usando o modelo Mistral para processamento das solicitações. Inclui suporte para análise de PDFs e autenticação social via Google.

## Arquitetura

O projeto utiliza as seguintes tecnologias:

- **Spring Boot 3.4.3**: Framework Java para desenvolvimento da API
- **H2 Database**: Banco de dados em memória para armazenamento
- **Ollama**: Servidor local para execução de modelos de IA
- **Mistral**: Modelo de IA para processamento de linguagem natural
- **OAuth2/JWT**: Autenticação social com Google e tokens JWT
- **Thymeleaf**: Motor de templates para páginas HTML (opcional)

A arquitetura segue o padrão MVC (Model-View-Controller), com separação clara de responsabilidades.

## Pré-requisitos

- Java 17 ou superior
- Ollama instalado e configurado com o modelo Mistral
- Maven (ou use o wrapper incluído)
- Porta 8080 disponível
- Credenciais OAuth2 do Google (para login social)

## Instalação

### 1. Ollama e Mistral

1. Instale o Ollama seguindo as instruções em [https://ollama.ai/](https://ollama.ai/)
2. Baixe e inicie o modelo Mistral:
   ```bash
   ollama pull mistral
   ```
3. Verifique se o Ollama está rodando na porta padrão (11434)

### 2. Configuração do OAuth2 (Google)

1. Acesse o [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um novo projeto
3. Configure a tela de consentimento OAuth
4. Crie credenciais OAuth 2.0
5. Adicione as seguintes URIs de redirecionamento autorizados:
   - `http://localhost:8080/login/oauth2/code/google`
   - `http://localhost:8080/auth/oauth2/code/google`
   - `http://localhost:8080/oauth2/success`
6. Anote o Client ID e Client Secret
7. Faça download do arquivo JSON das credenciais

### 3. Configuração da aplicação

1. Clone o repositório:
   ```bash
   git clone [URL_DO_REPOSITORIO]
   cd chat-api
   ```

2. Configure as credenciais do Google no `application.yml` ou use variáveis de ambiente:
   ```yaml
   spring:
     security:
       oauth2:
         client:
           registration:
             google:
               client-id: seu-client-id-aqui
               client-secret: seu-client-secret-aqui
   ```

3. Configure o JWT secret (recomendado usar variável de ambiente em produção):
   ```yaml
   jwt:
     secret: ${JWT_SECRET:chave-secreta-para-jwt-deve-ser-longa-e-segura-em-ambiente-de-producao}
   ```

### 4. Execução

Você pode iniciar a aplicação de várias formas:

#### Usando Maven:
```bash
mvn spring-boot:run
```

#### Usando o Wrapper:
```bash
./mvnw spring-boot:run  # Linux/Mac
mvnw.cmd spring-boot:run  # Windows
```

#### Como jar executável:
```bash
mvn clean package
java -jar target/chat-api-0.0.1-SNAPSHOT.jar
```

## Uso

Acesse `http://localhost:8080` no navegador. Você será redirecionado para a página de login.

### Autenticação Social

1. Clique no botão "Entrar com Google"
2. Faça login com sua conta Google
3. Conceda as permissões solicitadas
4. Você será redirecionado para o chat após autenticação bem-sucedida

### Chat Normal

1. Digite uma mensagem na área de entrada no final da tela
2. Clique no botão enviar ou pressione Enter
3. A resposta será mostrada em tempo real com streaming

### Upload e Consulta de PDFs

1. Clique no botão de documentos no menu lateral
2. Faça upload de um PDF
3. Selecione o PDF para ativá-lo como contexto
4. Faça perguntas sobre o conteúdo do PDF 

### Histórico

Clique no botão de histórico para ver suas conversas anteriores.

## API Endpoints

### Autenticação

- `GET /auth/login` - Redireciona para login OAuth2 do Google
- `GET /auth/oauth2/success` - Endpoint de callback após login bem-sucedido
- `GET /auth/usuario` - Obtém informações do usuário logado
- `GET /auth/token/validar` - Verifica se o token JWT é válido
- `POST /auth/logout` - Realiza logout

### Chat

- `GET /api/v1/chat/mensagens` - Lista mensagens de exemplo
- `GET /api/v1/chat/historico` - Obtém histórico de conversas
- `POST /api/v1/chat` - Envia uma mensagem e recebe resposta
- `POST /api/v1/chat/stream` - Envia uma mensagem e recebe resposta em streaming

### Documentos PDF

- `POST /api/v1/documentos` - Faz upload de um documento PDF
- `GET /api/v1/documentos` - Lista documentos disponíveis
- `GET /api/v1/documentos/{id}` - Obtém metadados de um documento
- `GET /api/v1/documentos/{id}/download` - Baixa o documento PDF
- `DELETE /api/v1/documentos/{id}` - Remove um documento

## Limitações

- Processamento de PDFs limitado a 10.000 caracteres para evitar exceder limites de tokens
- Documentos com tabelas, imagens ou formatos complexos podem não ser processados corretamente
- O Ollama deve estar disponível na porta 11434
- Tokens JWT têm validade de 24 horas

## Configurações Avançadas

Você pode ajustar diversos parâmetros no arquivo `application.yml`:

- Timeouts de conexão e leitura
- Expiração do token JWT
- Limites de upload de arquivos
- Configurações do banco de dados

## Resolução de Problemas

### Problemas de Autenticação OAuth2

Se você encontrar problemas com a autenticação OAuth2:

1. Verifique se as URIs de redirecionamento no Google Cloud Console correspondem exatamente às configuradas na aplicação
2. Certifique-se de que a tela de consentimento OAuth está configurada corretamente
3. Verifique se o client ID e client secret estão corretos no `application.yml`
4. Analise os logs da aplicação para mensagens de erro específicas

---

Desenvolvido por 7Cors &copy; 2025

