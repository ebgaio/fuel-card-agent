package com.example.fuelcardagent.controller;

import com.example.fuelcardagent.dto.CardStatsDTO;
import com.example.fuelcardagent.dto.CreditCardDTO;
import com.example.fuelcardagent.service.CreditCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/credit-cards")
@RequiredArgsConstructor
@Slf4j
public class CreditCardController {

    private final CreditCardService creditCardService;

    /**
     * GET /api/credit-cards
     * Lista todos os cartões cadastrados (todos os tipos).
     */
    @GetMapping
    public ResponseEntity<List<CreditCardDTO>> findAll() {
        log.info("[API] GET /api/credit-cards");
        return ResponseEntity.ok(creditCardService.findAll());
    }

    /**
     * GET /api/credit-cards/gas-station
     * Lista apenas os cartões exclusivos para postos de gasolina.
     */
    @GetMapping("/gas-station")
    public ResponseEntity<List<CreditCardDTO>> findGasStationCards() {
        log.info("[API] GET /api/credit-cards/gas-station");
        return ResponseEntity.ok(creditCardService.findGasStationCards());
    }

    /**
     * GET /api/credit-cards/customer/{customerId}
     * Lista todos os cartões de um cliente específico.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CreditCardDTO>> findByCustomer(@PathVariable Long customerId) {
        log.info("[API] GET /api/credit-cards/customer/{}", customerId);
        return ResponseEntity.ok(creditCardService.findByCustomerId(customerId));
    }

    /**
     * GET /api/credit-cards/stats
     * Retorna estatísticas dos cartões de posto (total, limite médio, limite total).
     */
    @GetMapping("/stats")
    public ResponseEntity<CardStatsDTO> getStats() {
        log.info("[API] GET /api/credit-cards/stats");
        return ResponseEntity.ok(creditCardService.getGasStationStats());
    }

    /**
     * GET /api/credit-cards/by-limit-range?referenceValue=4375.00&percentage=20
     * Retorna cartões GAS_STATION cujo limite está na faixa de ±percentage% do referenceValue.
     * Exemplo: referenceValue=4375.00 e percentage=20 → faixa [3500.00, 5250.00]
     */
    @GetMapping("/by-limit-range")
    public ResponseEntity<List<CreditCardDTO>> findByLimitRange(
            @RequestParam BigDecimal referenceValue,
            @RequestParam BigDecimal percentage) {
        log.info("[API] GET /api/credit-cards/by-limit-range?referenceValue={}&percentage={}", referenceValue, percentage);
        return ResponseEntity.ok(creditCardService.findByLimitRange(referenceValue, percentage));
    }
}
