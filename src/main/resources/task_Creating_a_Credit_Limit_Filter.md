# Task: Credit Limit Percentage Range Filter

## Planning
- [x] Read project structure and relevant files
- [x] Write implementation plan

## Execution
- [ ] Add JPQL query to [CreditCardRepository](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/repository/CreditCardRepository.java#12-24)
- [ ] Add service method `findByLimitRange` to [CreditCardService](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/service/CreditCardService.java#15-61)
- [ ] Add REST endpoint `GET /api/credit-cards/by-limit-range` to [CreditCardController](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/controller/CreditCardController.java#13-61)
- [ ] Add `@Tool` method `getCustomersByLimitRange` to [DatabaseQueryTool](file:///home/evandrogaio/Agents_Antigravity/fuel-card-agent/src/main/java/com/example/fuelcardagent/agent/DatabaseQueryTool.java#23-143)

## Verification
- [ ] Build the project with Maven
- [ ] Browser test the new endpoint via the agent chat
