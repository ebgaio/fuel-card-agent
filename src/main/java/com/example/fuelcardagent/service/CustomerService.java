package com.example.fuelcardagent.service;

import com.example.fuelcardagent.domain.CardType;
import com.example.fuelcardagent.dto.CustomerDTO;
import com.example.fuelcardagent.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerDTO> findAll() {
        log.debug("Finding all customers");
        return customerRepository.findAll()
                .stream()
                .map(CustomerDTO::from)
                .toList();
    }

    public Optional<CustomerDTO> findById(Long id) {
        log.debug("Finding customer by id: {}", id);
        return customerRepository.findById(id)
                .map(CustomerDTO::from);
    }

    public List<CustomerDTO> searchByName(String name) {
        log.debug("Searching customers by name: {}", name);
        return customerRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(CustomerDTO::from)
                .toList();
    }

    public List<CustomerDTO> findByCardType(CardType cardType) {
        log.debug("Finding customers with card type: {}", cardType);
        return customerRepository.findByCardType(cardType)
                .stream()
                .map(CustomerDTO::from)
                .toList();
    }
}
