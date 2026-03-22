package com.example.fuelcardagent.service;

import com.example.fuelcardagent.agent.FuelCardAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AgentService — orquestra chamadas ao agente LangChain4J.
 * Ponto de entrada para o AgentController.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final FuelCardAgent fuelCardAgent;

    public String chat(String userMessage) {
        log.info("[AGENT] Received message: {}", userMessage);
        String response = fuelCardAgent.chat(userMessage);
        log.info("[AGENT] Response: {}", response);
        return response;
    }
}
