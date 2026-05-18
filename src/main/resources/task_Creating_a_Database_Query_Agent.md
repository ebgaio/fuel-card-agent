# Task: Spring Boot Agent - Gas Station Credit Card Customers

## Planning
- [x] Create implementation plan
- [x] Present plan to user for approval

## Project Setup
- [x] Create Spring Boot project structure (Maven)
- [x] Configure pom.xml (Spring Boot, JPA, MySQL, LangChain4J, WebFlux, Flyway, ModelMapper)
- [x] Configure application.properties (MySQL, Ollama, WebClient)
- [x] Create MySQL init script with sample data (V1__init.sql)
- [x] Configure CORS (`WebConfig.java`)
- [x] Configure ModelMapper (`ModelMapperConfig.java`)

## Domain Layer
- [x] Create `Customer` entity
- [x] Create `CreditCard` entity (ManyToOne to Customer)
- [x] Create `CardType` enum (GAS_STATION, STANDARD_CREDIT, DEBIT)
- [x] Create `CustomerStatus` enum (ACTIVE, INACTIVE, SUSPENDED, DELETED)
- [x] Create `CustomerRepository` (JpaRepository)
- [x] Create `CreditCardRepository` (JpaRepository)

## DTO Layer
- [x] Create `CustomerDTO`
- [x] Create `CreditCardDTO`
- [x] Create `CardStatsDTO` (Record)
- [x] Create `CreditCardLimitDTO`

## Mapper Layer
- [x] Create `CreditCardMapper` (using ModelMapper)

## Service Layer
- [x] Create `CustomerService`
- [x] Create `CreditCardService`
- [x] Create `AgentService` (Orchestrates agent calls)

## API Layer (Tools for the Agent)
- [x] Create `CustomerController`
    - [x] `GET /api/customers` — list all customers
    - [x] `GET /api/customers/{id}` — find by ID
    - [x] `GET /api/customers/search?name=` — search by name
    - [x] `GET /api/customers/by-card-type?type=GAS_STATION` — filter by card type
- [x] Create `CreditCardController`
    - [x] `GET /api/credit-cards` — list all cards
    - [x] `GET /api/credit-cards/gas-station` — list gas station cards only
    - [x] `GET /api/credit-cards/customer/{customerId}` — list cards by customer
    - [x] `GET /api/credit-cards/stats` — get card statistics

## Agent Layer (LangChain4J)
- [x] Create `DatabaseQueryTool` (annotated with @Tool, calls REST APIs via WebClient)
- [x] Create `FuelCardAgent` interface (AiServices, @SystemMessage in PT-BR)
- [x] Configure `LangChain4JConfig` (Bean WebClient + OllamaChatModel + AiServices)
- [x] Create `AgentController` — expose chat endpoint (`POST /api/agent/chat`)

## Verification
- [x] Subir infraestrutura (`docker-compose up -d`)
- [x] Baixar modelo LLama no Ollama (`docker exec -it ollama ollama pull llama3.2`)
- [x] Build e start da aplicação (`./mvnw spring-boot:run`)
- [x] Test REST APIs directly with curl
- [x] Test agent chat endpoint with curl
- [x] Validate that LLM uses tools (APIs) and does NOT hit DB directly (check logs)
