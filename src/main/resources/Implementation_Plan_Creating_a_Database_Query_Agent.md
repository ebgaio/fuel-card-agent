# Spring Boot Agent — Gas Station Credit Card Customers

Sistema com **Spring Boot + MySQL + LangChain4J + Ollama (LLama)** onde um agente LLM responde perguntas sobre clientes e seus cartões de crédito exclusivos para postos de gasolina, usando APIs REST como ferramentas — nunca acessando o banco diretamente.

---

## Estrutura do Projeto

```
fuel-card-agent/
├── pom.xml                             ← Spring Boot 3.5.12 + LangChain4J 1.12.2 + WebFlux + Flyway
├── docker-compose.yml                  ← MySQL 8 + Ollama (LLama)
└── src/main/
    ├── resources/
    │   ├── application.properties      ← Configurações MySQL, Ollama, WebClient
    │   └── db/migration/V1__init.sql   ← 13 clientes + cartões GAS_STATION + dados extras
    └── java/com/example/fuelcardagent/
        ├── FuelCardAgentApplication.java
        ├── domain/
        │   ├── mapper/
        │   │   └── CreditCardMapper.java ← Mapeamento entre entidades e DTOs
        │   ├── CardType.java           ← Enum: GAS_STATION | STANDARD_CREDIT | DEBIT
        │   ├── Customer.java           ← Entidade JPA
        │   ├── CreditCard.java         ← Entidade JPA (ManyToOne → Customer)
        │   └── CustomerStatus.java     ← Enum: ACTIVE | INACTIVE | SUSPENDED | DELETED
        ├── repository/
        │   ├── CustomerRepository.java ← Busca por nome, filtro por tipo de cartão
        │   └── CreditCardRepository.java ← Filtro por tipo, por cliente, estatísticas
        ├── dto/
        │   ├── CustomerDTO.java
        │   ├── CreditCardDTO.java
        │   ├── CardStatsDTO.java       ← Record com total, média e soma de limites
        │   └── CreditCardLimitDTO.java ← DTO para limites de cartão
        ├── service/
        │   ├── CustomerService.java
        │   ├── CreditCardService.java
        │   └── AgentService.java       ← Orquestra chamadas ao agente
        ├── controller/
        │   ├── CustomerController.java  ← 4 endpoints de dados
        │   ├── CreditCardController.java ← 4 endpoints de dados
        │   └── AgentController.java     ← POST /api/agent/chat
        ├── agent/
        │   ├── DatabaseQueryTool.java  ← @Tool methods via WebClient (sem acesso direto ao BD)
        │   └── FuelCardAgent.java      ← Interface AiServices (@SystemMessage em PT-BR)
        └── config/
            ├── modelMapper/
            │   └── ModelMapperConfig.java ← Configuração do ModelMapper
            ├── WebConfig.java          ← Configuração CORS
            └── LangChain4JConfig.java  ← Bean WebClient + OllamaChatModel + AiServices
```

---

## Proposed Changes

### Domain & Persistence

#### [NEW] `Customer.java`
Entidade JPA com campos: `id`, `name`, `email`, `phone`, `cpf`, `createdAt`.

#### [NEW] `CreditCard.java`
Entidade JPA com campos: `id`, `cardNumber` (mascarado), `cardHolder`, `expirationDate`, `cardType` (enum), `creditLimit`, `customer` (ManyToOne).

#### [NEW] `CardType.java`
Enum: `GAS_STATION`, `STANDARD_CREDIT`, `DEBIT`.

#### [NEW] `CustomerStatus.java`
Enum: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `DELETED`.

#### [NEW] `CustomerRepository.java`
Herda `JpaRepository`. Métodos:
- `findByNameContainingIgnoreCase(String name)`
- `findByCards_CardType(CardType type)`

#### [NEW] `CreditCardRepository.java`
Herda `JpaRepository`. Métodos:
- `findByCardType(CardType type)`
- `findByCustomerId(Long customerId)`

### DTO Layer

#### [NEW] `CustomerDTO.java`
DTO para representação de dados de cliente.

#### [NEW] `CreditCardDTO.java`
DTO para representação de dados de cartão de crédito.

#### [NEW] `CardStatsDTO.java`
Record contendo estatísticas de cartões (total, média e soma de limites).

#### [NEW] `CreditCardLimitDTO.java`
DTO para representar informações de limite de cartão de crédito, incluindo o nome do cliente.

### Mapper Layer

#### [NEW] `CreditCardMapper.java`
Classe utilitária para mapear entre a entidade `CreditCard` e o DTO `CreditCardDTO` usando ModelMapper.

---

### API Layer (Ferramentas expostas para o Agente)

#### [NEW] `CustomerController.java`
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/customers` | Lista todos os clientes |
| GET | `/api/customers/{id}` | Busca cliente por ID |
| GET | `/api/customers/search?name=` | Busca por nome |
| GET | `/api/customers/by-card-type?type=GAS_STATION` | Clientes com cartão de posto |

#### [NEW] `CreditCardController.java`
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/credit-cards` | Lista todos os cartões |
| GET | `/api/credit-cards/gas-station` | Apenas cartões de posto |
| GET | `/api/credit-cards/customer/{customerId}` | Cartões de um cliente |
| GET | `/api/credit-cards/stats` | Estatísticas (total, limite médio) |

---

### Agent Layer (LangChain4J)

#### [NEW] `DatabaseQueryTool.java`
Classe anotada com `@Tool` (LangChain4J). Métodos:
- `getAllGasStationCustomers()` → chama `GET /api/customers/by-card-type?type=GAS_STATION`
- `getCustomerById(Long id)` → chama `GET /api/customers/{id}`
- `searchCustomerByName(String name)` → chama `GET /api/customers/search?name=`
- `getGasStationCreditCards()` → chama `GET /api/credit-cards/gas-station`
- `getCustomerCards(Long customerId)` → chama `GET /api/credit-cards/customer/{customerId}`
- `getCardStats()` → chama `GET /api/credit-cards/stats`

> ️ O `DatabaseQueryTool` usa `WebClient` para chamar os endpoints REST — o LLM **nunca** recebe conexão direta ao banco.

#### [NEW] `FuelCardAgent.java`
Interface anotada com `@SystemMessage` descrevendo o papel do agente (especialista em cartões de posto de gasolina) em português.

#### [NEW] `LangChain4JConfig.java`
Configura `OllamaChatModel` (aponta para `http://localhost:11434`) e registra `AiServices.builder(FuelCardAgent.class)` com o `DatabaseQueryTool`, incluindo a configuração do `WebClient` e `ModelMapper`.

#### [NEW] `AgentController.java`
`POST /api/agent/chat` — recebe `{ "message": "..." }` e retorna a resposta do agente.

### Config Layer

#### [NEW] `WebConfig.java`
Configuração Spring para habilitar CORS (Cross-Origin Resource Sharing) para endpoints `/api/**`, permitindo requisições de `http://localhost:4200`.

#### [NEW] `ModelMapperConfig.java`
Configuração Spring para fornecer um bean `ModelMapper` para facilitar o mapeamento entre DTOs e entidades.

---

### Infraestrutura

#### [NEW] `docker-compose.yml`
Sobe MySQL 8 e Ollama com modelo `llama3.2`.

#### [NEW] `V1__init.sql`
Script com criação das tabelas e **13 clientes** com cartões do tipo `GAS_STATION` (dados fictícios realistas em português) e dados extras.

---

## Fluxo de Dados

```
POST /api/agent/chat {"message": "..."}
        ↓
  AgentController → AgentService
        ↓
  FuelCardAgent.chat() — LangChain4J AiServices
        ↓
  OllamaChatModel (LLama via Ollama)
        ↓  raciocina → decide qual @Tool chamar
  DatabaseQueryTool.método()
        ↓  WebClient HTTP
  GET /api/customers/** ou /api/credit-cards/**
        ↓
  CustomerService / CreditCardService → JPA → MySQL
        ↓
  Resultado sobe → LLM analisa → responde em PT-BR
```

> ⚠️ O LLM nunca recebe uma conexão JDBC. Ele apenas recebe strings de texto com os dados retornados pelas APIs.

---

## Como executar

### 1. Subir infraestrutura
```bash
cd fuel-card-agent
docker-compose up -d
```

### 2. Baixar o modelo LLama
```bash
docker exec -it ollama ollama pull llama3.2
```

### 3. Iniciar a aplicação
```bash
./mvnw spring-boot:run
```
> O Flyway cria automaticamente as tabelas e insere os dados ao subir.

---

## Testando as APIs de dados

```bash
# Clientes com cartão de posto
curl "http://localhost:8080/api/customers/by-card-type?type=GAS_STATION"

# Buscar por nome
curl "http://localhost:8080/api/customers/search?name=Silva"

# Cartões de posto
curl "http://localhost:8080/api/credit-cards/gas-station"

# Estatísticas
curl "http://localhost:8080/api/credit-cards/stats"

# Cartões do cliente 1
curl "http://localhost:8080/api/credit-cards/customer/1"
```

---

## Testando o Agente

```bash
# Listar clientes com cartão de posto
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Quais clientes possuem cartão de posto de gasolina?"}'

# Análise de limite médio
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Qual é o limite médio dos cartões de posto cadastrados?"}'

# Dados de cliente específico
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Me mostre os cartões do cliente com ID 3"}'

# Análise comparativa
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Quem tem o maior limite de crédito no cartão de posto?"}'
```

---

## Dados de teste (V1__init.sql)

| Cliente | CPF | Cartão GAS_STATION | Limite        |
|---------|-----|--------------------|---------------|
| Carlos Eduardo Silva | 123.456.789-00 | **** **** **** 1001 | R$ 3.500      |
| Ana Paula Ferreira | 234.567.890-11 | **** **** **** 2002 | R$ 2.800      |
| Roberto Alves Mendes | 345.678.901-22 | **** **** **** 3003 | R$ 5.000      |
| Juliana Costa Ramos | 456.789.012-33 | **** **** **** 4004 | R$ 4.200      |
| Marcos Vinicius Oliveira | 567.890.123-44 | **** **** **** 5005 | R$ 1.500      |
| Fernanda Lima Torres | 678.901.234-55 | **** **** **** 6006 | R$ 6.000      |
| André Luiz Barbosa | 789.012.345-66 | **** **** **** 7007 | R$ 2.200      |
| Patrícia Souza Nunes | 890.123.456-77 | **** **** **** 8008 | R$ 3.800      |
| Ricardo Pereira Gomes | 901.234.567-88 | **** **** **** 9009 | R$ 4.500      |
| Luciana Martins Cardoso | 012.345.678-99 | **** **** **** 0010 | R$ 7.000      |
| Evandro Barroso Gaio | 456.123.979-00 | **** **** **** 1234| R$ 31.330.00  |
| Angelita Simoes | 789.321.646-00 | **** **** **** 3456 | R$ 300.000.00 |
| Elaine Gaio | 456.123.789-00 | **** **** **** 5678 | R$ 332.420.00 |