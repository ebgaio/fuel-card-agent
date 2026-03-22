package com.example.fuelcardagent.controller;

import com.example.fuelcardagent.domain.CardType;
import com.example.fuelcardagent.dto.CustomerDTO;
import com.example.fuelcardagent.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * GET /api/customers
     * Lista todos os clientes cadastrados.
     */
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> findAll() {
        log.info("[API] GET /api/customers");
        return ResponseEntity.ok(customerService.findAll());
    }

    /**
     * GET /api/customers/{id}
     * Retorna um cliente pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> findById(@PathVariable Long id) {
        log.info("[API] GET /api/customers/{}", id);
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/customers/search?name=Silva
     * Busca clientes pelo nome (busca parcial, case-insensitive).
     */
    @GetMapping("/search")
    public ResponseEntity<List<CustomerDTO>> searchByName(@RequestParam String name) {
        log.info("[API] GET /api/customers/search?name={}", name);
        return ResponseEntity.ok(customerService.searchByName(name));
    }

    /**
     * GET /api/customers/by-card-type?type=GAS_STATION
     * Retorna clientes que possuem ao menos um cartão do tipo informado.
     */
    @GetMapping("/by-card-type")
    public ResponseEntity<List<CustomerDTO>> findByCardType(@RequestParam CardType type) {
        log.info("[API] GET /api/customers/by-card-type?type={}", type);
        return ResponseEntity.ok(customerService.findByCardType(type));
    }
}
