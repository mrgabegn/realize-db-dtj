# Digital Bank API

API REST simplificada para um banco digital, desenvolvida com Java e Spring Boot.

O objetivo do projeto é permitir o cadastro básico de contas, transferência de valores entre contas, consulta de movimentações financeiras e simulação de notificação após uma transferência concluída com sucesso.

A aplicação foi construída com foco em consistência transacional, controle de concorrência, versionamento de banco de dados e documentação via Swagger.

## Tecnologias Utilizadas

* Java 21 ou superior
* Spring Boot
* Spring Web
* Spring Data JPA
* PostgreSQL
* Flyway
* Maven
* Docker Compose
* Swagger / OpenAPI
* JUnit 5
* Mockito

## Funcionalidades

* Cadastro de contas
* Consulta de conta por ID
* Transferência de valores entre contas
* Registro de movimentações financeiras
* Consulta de movimentações por conta
* Simulação de notificação após transferência concluída
* Versionamento do banco com Flyway
* Banco PostgreSQL via Docker Compose
* Documentação da API com Swagger
* Testes unitários

## Estrutura do Projeto

```txt id="a4k9gd"
src/main/java
  controller
    AccountController
    TransferController

  service
    AccountService
    TransferService
    NotificationService

  repository
    AccountRepository
    MovementRepository
    TransferRepository
    NotificationOutboxRepository

  domain
    Account
    Movement
    Transfer
    NotificationOutbox

  dto
    CreateAccountRequest
    TransferRequest
    AccountResponse
    MovementResponse
    TransferResponse

  event
    TransferCompletedEvent

  exception
    AccountNotFoundException
    InsufficientBalanceException
    InvalidTransferException

src/main/resources
  db/migration
    V1__create_tables.sql
    V2__insert_initial_data.sql
```

## Pré-requisitos

Antes de executar o projeto, é necessário ter instalado:

* Java 21+
* Maven
* Docker
* Docker Compose

## Como Executar o Projeto

### 1. Clonar o repositório

```bash id="e2li0y"
git clone https://github.com/mrgabegn/realize-db-dtj.git
cd realize-db-dtj
```

### 2. Subir o PostgreSQL com Docker Compose

O projeto possui um arquivo `docker-compose.yml` na raiz.

Execute:

```bash id="wx15as"
docker compose up -d
```

Esse comando irá subir um container PostgreSQL com a seguinte configuração:

```txt id="rmq8q0"
Database: digital_bank
User: postgres
Password: postgres
Port: 5432
```

### 3. Executar a aplicação

```bash id="yjsvb1"
mvn clean spring-boot:run
```

A aplicação ficará disponível em:

```txt id="jvqfyp"
http://localhost:8080
```

## Swagger

Após iniciar a aplicação, a documentação Swagger estará disponível em:

```txt id="by1hiq"
http://localhost:8080/swagger-ui.html
```

O JSON OpenAPI estará disponível em:

```txt id="i65mfm"
http://localhost:8080/api-docs
```

## Banco de Dados

O banco utilizado é PostgreSQL.

As configurações principais ficam no arquivo `application.properties`:

```properties id="cykho2"
spring.datasource.url=jdbc:postgresql://localhost:5432/digital_bank
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
```

## Flyway

O projeto utiliza Flyway para versionamento e criação do banco de dados.

As migrations ficam no diretório:

```txt id="u4w8jp"
src/main/resources/db/migration
```

Migrations atuais:

```txt id="ro6xw4"
V1__create_tables.sql
V2__insert_initial_data.sql
```

O Flyway é executado automaticamente ao iniciar a aplicação.

A configuração do Hibernate está definida como:

```properties id="sszgit"
spring.jpa.hibernate.ddl-auto=validate
```

Isso significa que o Hibernate apenas valida se as entidades Java estão compatíveis com as tabelas existentes. Ele não cria nem altera tabelas automaticamente.

A responsabilidade de criar e versionar o schema do banco é do Flyway.

## Dados Iniciais

A migration `V2__insert_initial_data.sql` pré-carrega algumas contas para facilitar os testes da API.

| Nome               | ID da Conta                            | Saldo Inicial |
| ------------------ | -------------------------------------- | ------------: |
| Gabriel Nascimento | `8b445638-3ee8-44f7-81e8-a50d828705d9` |       1000.00 |
| Himari Nori        | `f02be693-6cc5-4a2e-96cc-46a93b4fcb9a` |        750.00 |
| Clara Nascimento   | `a4a7f65f-fb7e-4f4e-95a9-73e28b8dbf12` |        500.00 |
| Luigi Nascimento   | `7cd8fda2-9e10-4df6-b14c-3a4a0d23fa34` |       1250.00 |

## Endpoints

### Criar Conta

```http id="abnr6a"
POST /accounts
```

Exemplo de requisição:

```json id="hkt5dw"
{
  "name": "Novo Cliente",
  "initialBalance": 500.00
}
```

Exemplo de resposta:

```json id="h8m5w0"
{
  "id": "bf91b173-7fa6-4ef1-b054-c6de8f4b3c23",
  "name": "Novo Cliente",
  "balance": 500.00
}
```

### Consultar Conta por ID

```http id="m5fy99"
GET /accounts/{id}
```

Exemplo de resposta:

```json id="lryx29"
{
  "id": "8b445638-3ee8-44f7-81e8-a50d828705d9",
  "name": "Gabriel Nascimento",
  "balance": 1000.00
}
```

### Consultar Movimentações da Conta

```http id="ar4j30"
GET /accounts/{id}/movements
```

Exemplo de resposta:

```json id="77ndd2"
[
  {
    "id": "8ccfa581-98d5-4d0d-8554-872d87c98266",
    "accountId": "8b445638-3ee8-44f7-81e8-a50d828705d9",
    "type": "DEBIT",
    "amount": 150.00,
    "transferId": "df83d915-19a7-44c2-9af4-fad4a45f17d2",
    "createdAt": "2026-06-08T12:00:00Z"
  }
]
```

### Realizar Transferência

```http id="ieb9jh"
POST /transfers
```

Exemplo de requisição:

```json id="nmhaye"
{
  "fromAccountId": "8b445638-3ee8-44f7-81e8-a50d828705d9",
  "toAccountId": "f02be693-6cc5-4a2e-96cc-46a93b4fcb9a",
  "amount": 150.00
}
```

Exemplo de resposta:

```json id="d0uqef"
{
  "transferId": "df83d915-19a7-44c2-9af4-fad4a45f17d2",
  "status": "TRANSFER_COMPLETED"
}
```

## Decisões de Design e Arquitetura

### Arquitetura em Camadas

O projeto segue uma arquitetura simples em camadas:

* `Controller`: recebe as requisições HTTP e retorna as respostas da API.
* `Service`: concentra as regras de negócio.
* `Repository`: realiza o acesso ao banco de dados.
* `Domain`: representa as entidades principais do domínio.
* `DTO`: define os contratos de entrada e saída da API.

Essa divisão facilita manutenção, testes e evolução do projeto.

### Transferência Transacional

A transferência de valores é executada dentro de uma única transação.

Durante uma transferência, o sistema:

1. Valida a requisição.
2. Busca e bloqueia as contas envolvidas.
3. Debita o valor da conta de origem.
4. Credita o valor na conta de destino.
5. Registra a transferência.
6. Registra as movimentações financeiras.
7. Publica um evento de transferência concluída.

Caso qualquer etapa falhe, a transação é revertida e nenhuma movimentação parcial é persistida.

### Controle de Concorrência

As contas envolvidas na transferência são carregadas com lock pessimista.

Isso evita que duas transações concorrentes alterem o saldo da mesma conta ao mesmo tempo, prevenindo inconsistências em cenários de alta concorrência.

As contas são bloqueadas em ordem determinística por ID, reduzindo o risco de deadlocks.

### Valores Monetários

Os valores financeiros são representados com `BigDecimal`.

Essa escolha evita problemas de precisão que podem ocorrer com tipos como `double` ou `float`.

### Registro de Movimentações

Cada transferência concluída gera duas movimentações:

* Uma movimentação do tipo `DEBIT` para a conta de origem.
* Uma movimentação do tipo `CREDIT` para a conta de destino.

Dessa forma, cada conta possui seu próprio histórico financeiro.

### Notificações

Após uma transferência ser concluída com sucesso, o sistema publica um evento `TransferCompletedEvent`.

A notificação é simulada por meio de logs da aplicação.

O projeto também possui uma tabela `notification_outbox`, preparando a arquitetura para uma futura implementação mais resiliente, com envio assíncrono e possibilidade de retentativas.

### Versionamento do Banco

O schema do banco é versionado com Flyway.

Isso torna a criação do banco reproduzível, rastreável e consistente entre ambientes.

### Documentação da API

A documentação da API é gerada automaticamente com SpringDoc OpenAPI e disponibilizada via Swagger UI.

## Executando os Testes

Para executar os testes:

```bash id="skyqj3"
mvn test
```

Os testes devem cobrir cenários como:

* Criação de conta com sucesso
* Transferência com sucesso
* Transferência com saldo insuficiente
* Transferência com valor inválido
* Transferência para a mesma conta
* Transferência com conta inexistente
* Registro das movimentações financeiras
* Publicação do evento de notificação
* Transferências concorrentes

## Resetando o Banco de Dados

Para parar o container e remover os dados persistidos:

```bash id="kl8yye"
docker compose down -v
```

Depois, suba o banco novamente:

```bash id="h4eaeu"
docker compose up -d
```

Ao iniciar a aplicação novamente, o Flyway irá recriar as tabelas e inserir os dados iniciais.

## Possíveis Melhorias Futuras

* Adicionar autenticação e autorização com Spring Security
* Implementar idempotência para evitar transferências duplicadas
* Processar notificações de forma assíncrona usando Outbox Pattern
* Adicionar paginação na consulta de movimentações
* Adicionar auditoria de operações
* Criar testes de integração com Testcontainers
* Criar Dockerfile para a aplicação
* Adicionar métricas e tracing para observabilidade

## Solução de Problemas

### A aplicação inicia e encerra logo em seguida

Verifique se a dependência `spring-boot-starter-web` está presente no `pom.xml`.

```xml id="hstq2y"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Sem essa dependência, a aplicação pode iniciar como aplicação não-web e encerrar após carregar o contexto.

### Erro: Schema validation: missing table [accounts]

Esse erro indica que o Hibernate tentou validar a entidade `Account`, mas a tabela `accounts` não existia no banco.

Verifique se:

* O PostgreSQL está rodando.
* O Flyway está configurado.
* As migrations estão em `src/main/resources/db/migration`.
* Os arquivos seguem o padrão `V1__descricao.sql`, com dois underlines.
* As dependências do Flyway estão no `pom.xml`.

Dependências recomendadas:

```xml id="uqjpuw"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>

<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### Swagger não abre

Verifique se a dependência do SpringDoc está no `pom.xml`:

```xml id="omj53c"
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.3</version>
</dependency>
```

Depois acesse:

```txt id="bmfdt8"
http://localhost:8080/swagger-ui.html
```

## Repositório

Repositório público:

```txt id="utnb0v"
https://github.com/mrgabegn/realize-db-dtj
```
