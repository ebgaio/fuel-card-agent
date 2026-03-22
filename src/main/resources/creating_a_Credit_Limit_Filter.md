# Filtro de Clientes por Faixa Percentual de Limite de Crédito

Adicionar um novo filtro que permite ao LLM apresentar clientes cujo limite de crédito do cartão (GAS_STATION) esteja dentro de uma faixa percentual de ±N% em relação a um valor de referência informado pelo usuário.

**Exemplo de uso:**
> "Apresente os clientes com limites na faixa entre 20% a mais e a menos do valor 4375,00"

A faixa calculada será: `[4375 - 20%, 4375 + 20%]` → `[3500.00, 5250.00]`

---

## Proposed Changes

### Repository
#### [MODIFY] [CreditCardRepository.java](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/repository/CreditCardRepository.java)
- Adicionar query JPQL que filtra cartões cujo `creditLimit` está entre `minLimit` e `maxLimit`:
```java
@Query("SELECT c FROM CreditCard c WHERE c.cardType = :cardType AND c.creditLimit BETWEEN :minLimit AND :maxLimit")
List<CreditCard> findByCardTypeAndCreditLimitBetween(CardType cardType, BigDecimal minLimit, BigDecimal maxLimit);
```

---

### Service
#### [MODIFY] [CreditCardService.java](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/service/CreditCardService.java)
- Adicionar método `findByLimitRange(BigDecimal referenceValue, BigDecimal percentage)` que:
  1. Calcula `minLimit = referenceValue * (1 - percentage/100)`
  2. Calcula `maxLimit = referenceValue * (1 + percentage/100)`
  3. Chama o novo método do repositório com `CardType.GAS_STATION`
  4. Mapeia para [CreditCardDTO](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/dto/CreditCardDTO.java#10-36)

---

### Controller
#### [MODIFY] [CreditCardController.java](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/controller/CreditCardController.java)
- Adicionar endpoint:
```
GET /api/credit-cards/by-limit-range?referenceValue=4375.00&percentage=20
```
- Parâmetros: `referenceValue` (BigDecimal) e `percentage` (BigDecimal)
- Retorna `List<CreditCardDTO>`

---

### Agent Tool
#### [MODIFY] [DatabaseQueryTool.java](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/agent/DatabaseQueryTool.java)
- Adicionar método `@Tool` que o LLM pode invocar ao detectar a intenção de filtro por faixa de limite:
```java
@Tool("Busca clientes cujo limite de crédito do cartão de posto está dentro de uma faixa percentual em relação a um valor de referência. ...")
public String getCustomersByLimitRange(BigDecimal referenceValue, BigDecimal percentage)
```
- O método chama `GET /api/credit-cards/by-limit-range?referenceValue=...&percentage=...` via WebClient

---

## Verification Plan

### Automated Tests
- Não há testes automatizados existentes no projeto. Faremos a verificação via build + endpoint direto.
- Executar build completo:
```bash
cd /home/evandrogaio/Agents_Antigravity/fuel-card-agent && mvn clean package -DskipTests
```

### Manual Verification (via browser / curl após start da aplicação)
1. Iniciar a aplicação (se não estiver rodando): `mvn spring-boot:run`
2. Chamar o endpoint REST diretamente para validar a lógica:
   ```
   GET http://localhost:8080/api/credit-cards/by-limit-range?referenceValue=4375.00&percentage=20
   ```
   Esperado: lista de cartões com `creditLimit` entre **3500.00** e **5250.00**
3. Chamar o agente com a mensagem de linguagem natural:
   ```
   POST http://localhost:8080/api/agent/chat
   body: {"message": "Apresente os clientes com limites na faixa entre 20% a mais e a menos do valor 4375,00"}
   ```
   Esperado: resposta listando os clientes cujos limites estejam na faixa calculada.
