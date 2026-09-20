# wallet-service

Microsserviço de carteira digital, responsável por manter o saldo de usuários e processar operações de **débito** e **crédito**. Desenvolvido em Java com Spring Boot, seguindo boas práticas de separação em camadas, tratamento centralizado de exceções, testes automatizados (unitários e de integração) e containerização com Docker.

## ✨ Funcionalidades

- Criar uma carteira para um `userId` (saldo inicial zero)
- Consultar o saldo de uma carteira por `userId`
- Debitar um valor da carteira (com validação de saldo suficiente)
- Creditar um valor na carteira
- Tratamento padronizado de erros (carteira não encontrada, carteira já existente, saldo insuficiente, validação de payload)

## 🛠️ Tecnologias

- **Java 17**
- **Spring Boot 4.1.1** (Web MVC, Data JPA, Validation)
- **PostgreSQL** como banco de dados
- **Lombok** para reduzir boilerplate
- **JUnit 5 + Mockito** para testes unitários
- **Testcontainers** para testes de integração com um Postgres real
- **Docker + Docker Compose** para empacotar e executar a aplicação com o banco
- **Maven** (com Maven Wrapper) como build tool

## 📂 Estrutura do projeto

```
src/main/java/com/microsservicos/wallet_service/
├── controller/       # Endpoints REST (WalletController)
├── domain/           # Entidade Wallet e regras de negócio (debit/credit)
├── dto/              # Records de entrada e saída (CreateWalletRequest, OperationRequest, WalletResponse)
├── exception/        # Exceções de domínio + GlobalExceptionHandler
├── repository/       # WalletRepository (Spring Data JPA)
└── service/          # WalletService (regras de aplicação)

src/test/java/com/microsservicos/wallet_service/
├── WalletServiceTest.java              # Testes unitários (Mockito)
└── WalletServiceApplicationTests.java  # Teste de integração (Testcontainers)

Dockerfile              # Build multi-stage da aplicação
docker-compose.yml      # Aplicação + PostgreSQL
```

## 🔌 Endpoints

| Método | Rota                           | Descrição                          |
|--------|--------------------------------|-------------------------------------|
| POST   | `/api/wallets`                 | Cria uma carteira (saldo zero)      |
| GET    | `/api/wallets/{userId}`        | Retorna os dados da carteira        |
| POST   | `/api/wallets/{userId}/debit`  | Debita um valor da carteira         |
| POST   | `/api/wallets/{userId}/credit` | Credita um valor na carteira        |

### Exemplo de requisição (criar carteira)

```http
POST /api/wallets
Content-Type: application/json

{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

### Exemplo de requisição (débito)

```http
POST /api/wallets/3fa85f64-5717-4562-b3fc-2c963f66afa6/debit
Content-Type: application/json

{
  "amount": 40.00
}
```

### Exemplo de resposta

```json
{
  "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "balance": 60.00
}
```

### Erros tratados

| Status | Situação                                     |
|--------|-----------------------------------------------|
| 400    | Payload inválido (ex: valor nulo ou ≤ 0)      |
| 404    | Carteira não encontrada para o `userId`       |
| 409    | Já existe uma carteira para o `userId`        |
| 422    | Saldo insuficiente para o débito solicitado   |

## ▶️ Como executar

Pré-requisito: **Docker** com Docker Compose.

```bash
docker compose up --build
```

Isso sobe o PostgreSQL e a aplicação. A API fica disponível em `http://localhost:8080`.

Para parar e remover os containers:

```bash
docker compose down
```

Para remover também os dados do banco (volume):

```bash
docker compose down -v
```

### Testando a API

Exemplo com `curl` (Linux/macOS/Git Bash):

```bash
# criar carteira
curl -X POST http://localhost:8080/api/wallets \
  -H "Content-Type: application/json" \
  -d '{"userId":"3fa85f64-5717-4562-b3fc-2c963f66afa6"}'

# creditar 100
curl -X POST http://localhost:8080/api/wallets/3fa85f64-5717-4562-b3fc-2c963f66afa6/credit \
  -H "Content-Type: application/json" \
  -d '{"amount":100.00}'

# consultar
curl http://localhost:8080/api/wallets/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

## ✅ Como rodar os testes

Pré-requisitos: **JDK 17** e **Docker** em execução (usado pelo Testcontainers).

```bash
./mvnw clean test
```

Isso executa:
- Os testes unitários de `WalletServiceTest` (criação, débito e crédito com Mockito, sem banco real)
- O teste de integração `WalletServiceApplicationTests`, que sobe o contexto Spring completo com um PostgreSQL real via Testcontainers (por isso é necessário o Docker em execução)

## 🗺️ Próximos passos

- Criar um segundo serviço (`transfer-service`) que consome esta API via HTTP, para demonstrar comunicação real entre microsserviços
- Orquestrar os dois serviços no mesmo Docker Compose, com o `transfer-service` chamando `http://wallet-service:8080`
- Tratar concorrência nas operações de saldo (ex: lock pessimista ou `@Version`)