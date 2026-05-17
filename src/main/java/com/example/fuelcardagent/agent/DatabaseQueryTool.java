package com.example.fuelcardagent.agent;

import com.example.fuelcardagent.repository.CustomerRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * DatabaseQueryTool — Ferramenta do agente LangChain4J.
 *
 * IMPORTANTE: Esta classe NÃO acessa o banco de dados diretamente.
 * Todas as consultas são feitas via chamadas HTTP às APIs REST internas
 * (CustomerController e CreditCardController), usando WebClient.
 *
 * O LLM decide qual método chamar com base na pergunta do usuário.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseQueryTool {

    private final WebClient webClient;
    private final CustomerRepository customerrepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Ferramentas relacionadas a CLIENTES
    // ─────────────────────────────────────────────────────────────────────────

    @Tool("Busca clientes no sistema. Você DEVE solicitar o nome ou parte dele para filtrar. " +
            "Se o usuário não fornecer um nome, informe que a lista é muito grande e peça um termo de busca.")
    public String buscarClientes(
            @P("O nome ou parte do nome do cliente para busca (opcional)") String nome,
            @P("O status do cliente: ATIVO, INATIVO, SUSPENCO ou DELETADO") String status,
            @P("Limite de resultados para exibição (padrão 10)") Integer limite,
            @P("Limite de resultados para exibição (padrão 10)") Long customerId
    ) {
        int maxResults = (limite == null || limite > 50) ? 10 : limite;

        if (nome == null || nome.isBlank()) {
             throw new RuntimeException("É necessário um nome para filtrar a busca.");
        }

        log.info("[TOOL] getCustomerCreditCards({}) → GET /api/credit-cards/customer/{}", customerId, customerId);
        List<?> result = webClient.get()
                .uri("/api/credit-cards/customer/{customerId}", customerId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        return formatResult("Cartões do cliente ID " + customerId, result);
    }

    @Tool("Lista todos os clientes cadastrados no sistema com seus dados completos (id, nome, email, telefone, CPF, data de cadastro)")
    public String getAllCustomers() {
        log.info("[TOOL] getAllCustomers → GET /api/customers");
        List<?> result = webClient.get()
                .uri("/api/customers")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        return formatResult("Todos os clientes", result);
    }

    @Tool("Busca um cliente específico pelo seu ID numérico. Use quando o usuário mencionar um ID de cliente.")
    public String getCustomerById(Long id) {
        log.info("[TOOL] getCustomerById({}) → GET /api/customers/{}", id, id);
        try {
            Object result = webClient.get()
                    .uri("/api/customers/{id}", id)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return "Cliente encontrado: " + result;
        } catch (Exception e) {
            return "Cliente com ID " + id + " não encontrado.";
        }
    }

    @Tool("Busca clientes pelo nome (busca parcial e sem distinção de maiúsculas/minúsculas). Use quando o usuário fornecer um nome ou parte de um nome.")
    public String searchCustomerByName(String name) {
        log.info("[TOOL] searchCustomerByName({}) → GET /api/customers/search?name={}", name, name);
        List<?> result = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/customers/search").queryParam("name", name).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        return formatResult("Clientes com nome contendo '" + name + "'", result);
    }

    @Tool("Retorna todos os clientes que possuem cartão de crédito exclusivo para postos de gasolina (GAS_STATION). Use para perguntas como 'quais clientes têm cartão de posto'.")
    public String getGasStationCardCustomers() {
        log.info("[TOOL] getGasStationCardCustomers → GET /api/customers/by-card-type?type=GAS_STATION");
        List<?> result = webClient.get()
                .uri("/api/customers/by-card-type?type=GAS_STATION")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        return formatResult("Clientes com cartão GAS_STATION", result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ferramentas relacionadas a CARTÕES DE CRÉDITO
    // ─────────────────────────────────────────────────────────────────────────

    @Tool("Lista todos os cartões de crédito cadastrados no sistema, incluindo cartões de posto, crédito padrão e débito.")
    public String getAllCreditCards() {
        log.info("[TOOL] getAllCreditCards → GET /api/credit-cards");
        List<?> result = webClient.get()
                .uri("/api/credit-cards")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        return formatResult("Todos os cartões", result);
    }

    @Tool("Lista apenas os cartões de crédito exclusivos para postos de gasolina (tipo GAS_STATION), com número mascarado, titular, vencimento e limite de crédito.")
    public String getGasStationCreditCards() {
        log.info("[TOOL] getGasStationCreditCards → GET /api/credit-cards/gas-station");
        List<?> result = webClient.get()
                .uri("/api/credit-cards/gas-station")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        return formatResult("Cartões GAS_STATION", result);
    }

    @Tool("Busca todos os cartões de crédito de um cliente específico pelo ID do cliente. Retorna todos os tipos de cartão daquele cliente.")
    public String getCustomerCreditCards(Long customerId) {
        log.info("[TOOL] getCustomerCreditCards({}) → GET /api/credit-cards/customer/{}", customerId, customerId);
        List<?> result = webClient.get()
                .uri("/api/credit-cards/customer/{customerId}", customerId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        return formatResult("Cartões do cliente ID " + customerId, result);
    }

    @Tool("Retorna estatísticas dos cartões de posto de gasolina: total de cartões, limite médio de crédito e limite total somado. Use para análises quantitativas.")
    public String getGasStationCardStats() {
        log.info("[TOOL] getGasStationCardStats → GET /api/credit-cards/stats");
        Object result = webClient.get()
                .uri("/api/credit-cards/stats")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return "Estatísticas dos cartões de posto de gasolina: " + result;
    }

    @Tool("""
            Busca clientes cujo limite de crédito do cartão de posto de gasolina (GAS_STATION) está dentro \
            de uma faixa percentual em relação a um valor de referência informado pelo usuário. \
            Use este método quando o usuário pedir algo como: 'clientes com limite na faixa de X% a mais e a menos de \
            [valor]' ou 'limites entre Y% acima e abaixo de [valor]'. \
            Parâmetros: referenceValue = valor base do limite (ex: 4375.00), \
            percentage = percentual de variação (ex: 20 para ±20%). \
            A faixa calculada será [referenceValue * (1 - percentage/100), referenceValue * (1 + percentage/100)].""")
    public String getCustomersByLimitRange(BigDecimal referenceValue, BigDecimal percentage) {
        log.info("[TOOL] getCustomersByLimitRange({}, {}%) → GET /api/credit-cards/by-limit-range", referenceValue, percentage);
        List<?> result = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/credit-cards/by-limit-range")
                        .queryParam("referenceValue", referenceValue)
                        .queryParam("percentage", percentage)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        BigDecimal factor = percentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal minLimit = referenceValue.multiply(BigDecimal.ONE.subtract(factor)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxLimit = referenceValue.multiply(BigDecimal.ONE.add(factor)).setScale(2, RoundingMode.HALF_UP);
        return formatResult(
                String.format("Clientes com limite de crédito entre R$ %s e R$ %s (±%s%% de R$ %s)",
                        minLimit, maxLimit, percentage, referenceValue),
                result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private String formatResult(String label, List<?> items) {
        if (items == null || items.isEmpty()) {
            return label + ": nenhum resultado encontrado.";
        }
        StringBuilder sb = new StringBuilder(label).append(" (").append(items.size()).append(" registros):\n");
        items.forEach(item -> sb.append("  - ").append(item).append("\n"));
        return sb.toString();
    }
}
