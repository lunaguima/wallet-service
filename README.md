# wallet-service

Microsserviço de carteira digital, responsável por manter o saldo de usuários e processar operações de **débito** e **crédito**. Desenvolvido em Java com Spring Boot, seguindo boas práticas de separação em camadas, tratamento centralizado de exceções e testes automatizados (unitários e de integração).

## ✨ Funcionalidades

- Consultar o saldo de uma carteira por `userId`
- Debitar um valor da carteira (com validação de saldo suficiente)
- Creditar um valor na carteira
- Tratamento padronizado de erros (carteira não encontrada, saldo insuficiente, validação de payload)

## 🛠️ Tecnologias

- **Java 17**
- **Spring Boot 4.1.1** (Web MVC, Data JPA, Validation)
- **PostgreSQL** como banco de dados
- **Lombok** para reduzir boilerplate
- **JUnit 5 + Mockito** para testes unitários
- **Testcontainers** para testes de integração com um Postgres real
- **Maven** (com Maven Wrapper) como build tool

## 📂 Estrutura do projeto

```
src/main/java/com/microsservicos/wallet_service/
├── controller/       # Endpoints REST (WalletController)
├── domain/            # Entidade Wallet e regras de negócio (debit/credit)
├── dto/               # Records de entrada e saída (OperationRequest, WalletResponse)
├── exception/         # Exceções de domínio + GlobalExceptionHandler
├── repository/        # WalletRepository (Spring Data JPA)
└── service/           # WalletService (regras de aplicação)

src/test/java/com/microsservicos/wallet_service/
├── WalletServiceTest.java              # Testes unitários (Mockito)
└── WalletServiceApplicationTests.java  # Teste de integração (Testcontainers)
```

## 🔌 Endpoints

| Método | Rota                          | Descrição                          |
|--------|-------------------------------|-------------------------------------|
| GET    | `/api/wallets/{userId}`       | Retorna os dados da carteira        |
| POST   | `/api/wallets/{userId}/debit` | Debita um valor da carteira         |
| POST   | `/api/wallets/{userId}/credit`| Credita um valor na carteira        |

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

| Status | Situação                                  |
|--------|--------------------------------------------|
| 404    | Carteira não encontrada para o `userId`     |
| 422    | Saldo insuficiente para o débito solicitado |
| 400    | Payload inválido (ex: valor nulo ou ≤ 0)    |

## ▶️ Como executar

Pré-requisitos: **JDK 17** e **Docker** (usado pelos testes de integração com Testcontainers).

```bash
# subir a aplicação
./mvnw spring-boot:run
```

A aplicação inicia por padrão em `http://localhost:8080`.

## ✅ Como rodar os testes

```bash
./mvnw clean test
```

Isso executa:
- Os testes unitários de `WalletServiceTest` (regras de débito/crédito com Mockito, sem banco real)
- O teste de integração `WalletServiceApplicationTests`, que sobe o contexto Spring completo com um PostgreSQL real via Testcontainers (por isso é necessário o Docker em execução)

## 🗺️ Próximos passos

- Criar um segundo serviço (`transfer-service`) que consome esta API via HTTP, para demonstrar comunicação real entre microsserviços
- Adicionar containerização própria (Dockerfile) e orquestração via Docker Compose