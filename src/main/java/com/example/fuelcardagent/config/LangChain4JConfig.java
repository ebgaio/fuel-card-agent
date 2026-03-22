package com.example.fuelcardagent.config;

import com.example.fuelcardagent.agent.DatabaseQueryTool;
import com.example.fuelcardagent.agent.FuelCardAgent;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class LangChain4JConfig {

    @Value("${langchain4j.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.model-name}")
    private String ollamaModelName;

    @Value("${langchain4j.ollama.temperature:0.3}")
    private Double temperature;

    @Value("${langchain4j.ollama.timeout:120s}")
    private String timeout;

    @Value("${app.api.base-url}")
    private String apiBaseUrl;

    /**
     * WebClient configurado para chamar as APIs REST internas.
     * É o único canal pelo qual o DatabaseQueryTool acessa dados.
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer
                .build();
    }

    /**
     * Configura o modelo de linguagem via Ollama (LLama rodando localmente).
     */
    @Bean
    public OllamaChatModel ollamaChatModel() {
        // Extrai o valor numérico de "120s" → 120
        long timeoutSeconds = Long.parseLong(timeout.replace("s", "").trim());

        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    /**
     * Cria o agente via AiServices do LangChain4J.
     * O framework gera a implementação da interface FuelCardAgent,
     * conectando o LLM ao DatabaseQueryTool (@Tool methods).
     */
    @Bean
    public FuelCardAgent fuelCardAgent(OllamaChatModel chatModel, DatabaseQueryTool databaseQueryTool) {
        return AiServices.builder(FuelCardAgent.class)
                .chatModel(chatModel)
                .tools(databaseQueryTool)
                .build();
    }
}
