# 7cors-api 🧠💭  
**Chatbot baseado no modelo Mistral, integrado ao Spring Boot e Ollama**  

## 🚀 Visão Geral  
O **7cors-api** é um backend desenvolvido em **Spring Boot** que utiliza o modelo de linguagem **Mistral** via **Ollama** para fornecer respostas inteligentes em conversas automatizadas.  

## 🛠️ Recursos  
- ✔️ API REST para comunicação com o chatbot  
- ✔️ Integração com o servidor Ollama para geração de respostas  
- ✔️ Estrutura modular e escalável com Spring Boot  
- ✔️ Suporte para armazenamento e histórico de conversas (opcional)  
- ✔️ Possível integração com WebSockets, mensageria ou plataformas de chat  

## ⚙️ Tecnologias Utilizadas  
- Java 17+  
- Spring Boot  
- Ollama (Mistral AI)  
- Banco de Dados (opcional: PostgreSQL, MySQL, MongoDB)  
- Docker (para deploy opcional)  

## 📌 Instalação e Configuração  
1. Clone este repositório:  
   ```sh
   git clone https://github.com/seu-usuario/7cors-api.git
   ```  
2. Instale e configure o **Ollama**:  
   ```sh
   ollama pull mistral
   ```  
3. Inicie o servidor Ollama localmente:  
   ```sh
   ollama serve
   ```  
4. Configure as variáveis de ambiente conforme necessário.  
5. Execute a aplicação Spring Boot:  
   ```sh
   mvn spring-boot:run
   ```  

## 💽 Endpoints Principais  
| Método | Endpoint | Descrição |
|--------|---------|------------|
| `POST` | `/chat` | Envia uma mensagem ao chatbot e recebe a resposta. |
| `GET` | `/health` | Verifica o status da API. |

## 🔧 Contribuição  
Contribuições são bem-vindas! Sinta-se à vontade para abrir **issues** ou enviar um **pull request**.  

---  
Se precisar de ajustes, me avise! 🚀

