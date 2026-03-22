package com.example.fuelcardagent.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Interface do agente gerenciada pelo LangChain4J AiServices.
 *
 * O framework gera automaticamente a implementação desta interface,
 * conectando o LLM (Ollama/LLama) com as ferramentas do DatabaseQueryTool.
 */
public interface FuelCardAgent {

    @SystemMessage("""
            Você é um assistente especialista em cartões de crédito exclusivos para postos de 
            gasolina. Seu papel é ajudar usuários a consultarem informações sobre clientes e 
            seus cartões de crédito do tipo GAS_STATION cadastrados no sistema.
            
            Regras importantes:
            1. Sempre use as ferramentas disponíveis para buscar dados antes de responder.
            2. Nunca invente dados — responda apenas com base no que as ferramentas retornarem.
            3. Apresente os resultados de forma clara, organizada e em português do Brasil.
            4. Quando apresentar listas de clientes ou cartões, formate-os de maneira legível.
            5. Para análises, destaque informações relevantes como limites de crédito e vencimentos.
            6. Se o usuário pedir algo que exija múltiplas consultas, faça todas antes de responder.
            """)
    String chat(@UserMessage String message);
}
