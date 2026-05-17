# Spring Boot Agent — Gas Station Credit Card Customers

Sistema com **Spring Boot + MySQL + LangChain4J + Ollama (LLama)** onde um agente LLM responde perguntas sobre clientes e seus cartões de crédito exclusivos para postos de gasolina, usando APIs REST como ferramentas — nunca acessando o banco diretamente.

---

## Estrutura do Projeto

```
fuel-card-agent/
├── pom.xml
├── src/main/java/com/example/fuelcardagent/
│   ├── domain/
│   │   ├── Customer.java                  (entidade)
│   │   ├── CreditCard.java                (entidade)
│   │   └── CardType.java                  (enum: GAS_STATION, CREDIT, DEBIT...)
│   ├── repository/
│   │   ├── CustomerRepository.java
│   │   └── CreditCardRepository.java
│   ├── service/
│   │   ├── CustomerService.java
│   │   └── CreditCardService.java
│   ├── controller/
│   │   ├── CustomerController.java        (API de dados)
│   │   ├── CreditCardController.java      (API de dados)
│   │   └── AgentController.java           (endpoint do chat)
│   ├── agent/
│   │   ├── DatabaseQueryTool.java         (ferramenta do agente → chama APIs REST)
│   │   ├── FuelCardAgent.java             (interface AiServices)
│   │   └── AgentService.java              (orquestração do agente)
│   └── config/
│       └── LangChain4JConfig.java         (configuração Ollama + AiServices)
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/V1__init.sql          (Flyway ou init manual)
└── docker-compose.yml                     (MySQL + Ollama)
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

#### [NEW] `CustomerRepository.java`
Herda `JpaRepository`. Métodos:
- `findByNameContainingIgnoreCase(String name)`
- `findByCards_CardType(CardType type)`

#### [NEW] `CreditCardRepository.java`
Herda `JpaRepository`. Métodos:
- `findByCardType(CardType type)`
- `findByCustomerId(Long customerId)`

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
- `getCustomersByLimitRange({}, {}%)` → chama `GET /api/credit-cards/by-limit-range", referenceValue, percentage);`

> ️ O `DatabaseQueryTool` usa `WebClient` para chamar os endpoints REST — o LLM **nunca** recebe conexão direta ao banco.

#### [NEW] `FuelCardAgent.java`
Interface anotada com `@SystemMessage` descrevendo o papel do agente (especialista em cartões de posto de gasolina).

#### [NEW] `LangChain4JConfig.java`
Configura `OllamaChatModel` (aponta para `http://localhost:11434`) e registra `AiServices.builder(FuelCardAgent.class)` com o `DatabaseQueryTool`.

#### [NEW] `AgentController.java`
`POST /api/agent/chat` — recebe `{ "message": "..." }` e retorna a resposta do agente.

---

### Infraestrutura

#### [NEW] `docker-compose.yml`
Sobe MySQL 8 e Ollama com modelo `llama3.2`.

#### [NEW] `V1__init.sql`
Script com criação das tabelas e **10 clientes** com cartões do tipo `GAS_STATION` (dados fictícios realistas em português).

---

## Fluxo de Dados

```
POST /api/agent/chat
  ↓
AgentController
  ↓
AgentService → FuelCardAgent (LangChain4J AiServices)
  ↓
Ollama LLama (raciocina sobre a pergunta)
  ↓
DatabaseQueryTool.@Tool (decide qual método chamar)
  ↓
RestTemplate → GET /api/customers/** ou /api/credit-cards/**
  ↓
CustomerController / CreditCardController
  ↓
CustomerService / CreditCardService
  ↓
JPA Repository → MySQL
  ↓
Resultado sobe a cadeia → LLM analisa e responde
```

---

## Verification Plan

### 1. Subir a infraestrutura

```bash
cd fuel-card-agent
docker-compose up -d
```

Aguardar MySQL e Ollama subirem. Verificar com:
```bash
docker-compose logs -f
```

### 2. Baixar modelo LLama no Ollama

```bash
docker exec -it ollama ollama pull llama3.2
```

### 3. Build e start da aplicação

```bash
./mvnw spring-boot:run
```

### 4. Testar APIs REST diretamente

```bash
# Todos os clientes com cartão de posto
curl http://localhost:8080/api/customers/by-card-type?type=GAS_STATION

# Buscar cliente por nome
curl "http://localhost:8080/api/customers/search?name=Silva"

# Cartões de posto
curl http://localhost:8080/api/credit-cards/gas-station

# Estatísticas
curl http://localhost:8080/api/credit-cards/stats
```

### 5. Testar o Agente via chat

```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Quais clientes possuem cartão de posto de gasolina?"}'

curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Qual é o limite médio dos cartões de posto cadastrados?"}'

curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Me fale sobre os cartões do cliente com ID 1"}'
  
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Apresente os clientes com limites na faixa entre 13,6% a mais e a menos do valor 2570,00"}'
```

### 6. Validar que o LLM não acessa o banco diretamente

Verificar nos logs da aplicação (`--debug` ou via Actuator) que toda chamada de dados passa pelos endpoints REST e não há chamadas JDBC originadas do contexto do agente.
