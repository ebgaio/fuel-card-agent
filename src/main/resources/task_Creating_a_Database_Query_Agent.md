# Task: Spring Boot Agent - Gas Station Credit Card Customers

## Planning
- [/] Create implementation plan
- [ ] Present plan to user for approval

## Project Setup
- [ ] Create Spring Boot project structure (Maven)
- [ ] Configure pom.xml (Spring Boot, JPA, MySQL, LangChain4J, Ollama)
- [ ] Configure application.properties
- [ ] Create MySQL init script with sample data

## Domain Layer
- [ ] Create `Customer` entity
- [ ] Create `CreditCard` entity (type = GAS_STATION)
- [ ] Create Repositories (CustomerRepository, CreditCardRepository)

## Service Layer
- [ ] Create `CustomerService`
- [ ] Create `CreditCardService`

## API Layer (Tools for the Agent)
- [ ] `GET /api/customers` — list all customers
- [ ] `GET /api/customers/{id}` — find by ID
- [ ] `GET /api/customers/search?name=` — search by name
- [ ] `GET /api/customers/card-type/{type}` — filter by card type
- [ ] `GET /api/credit-cards` — list all cards
- [ ] `GET /api/credit-cards/gas-station` — list gas station cards only
- [ ] `GET /api/credit-cards/customer/{customerId}` — list cards by customer

## Agent Layer (LangChain4J)
- [ ] Create `DatabaseQueryTool` (calls REST APIs)
- [ ] Configure Ollama/LLama integration
- [ ] Create `AgentService` using AiServices
- [ ] Create `AgentController` — expose chat endpoint

## Verification
- [ ] Test REST APIs with curl/Postman
- [ ] Test agent chat endpoint
- [ ] Validate that LLM uses tools (APIs) and does NOT hit DB directly
