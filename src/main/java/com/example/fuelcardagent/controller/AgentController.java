package com.example.fuelcardagent.controller;

import com.example.fuelcardagent.service.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final AgentService agentService;

    /**
     * POST /api/agent/chat
     * Endpoint principal do agente. Recebe uma pergunta em linguagem natural
     * e retorna a resposta gerada pelo LLM (via Ollama/LLama),
     * que internamente usa as ferramentas (APIs) para buscar dados.
     *
     * Exemplo de body:
     * { "message": "Quais clientes possuem cartão de posto de gasolina?" }
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Campo 'message' é obrigatório."));
        }

        log.info("[AGENT CONTROLLER] POST /api/agent/chat | message: {}", message);
        String response = agentService.chat(message);

        return ResponseEntity.ok(Map.of(
                "message", message,
                "response", response
        ));
    }
}
