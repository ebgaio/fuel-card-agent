package com.example.fuelcardagent.service;

import com.example.fuelcardagent.domain.CardType;
import com.example.fuelcardagent.domain.Customer;
import com.example.fuelcardagent.dto.CustomerDTO;
import com.example.fuelcardagent.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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
        if (Objects.isNull(id)) {
            return Optional.empty();
        }

        Optional<Customer> customer = customerRepository.findById(id);
        if(customer.isEmpty()){
            return Optional.empty();
        }
        return customer.map(CustomerDTO::from);
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

    @Transactional
    public CustomerDTO create(CustomerDTO dto) {
        log.info("Creating customer: {}", dto.getName());
        Customer customer = Customer.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .cpf(dto.getCpf())
                .build();
        Customer saved = customerRepository.save(customer);
        return CustomerDTO.from(saved);
    }

    @Transactional
    public Optional<CustomerDTO> update(Long id, CustomerDTO dto) {
        log.info("Updating customer id: {}", id);
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return customerRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setEmail(dto.getEmail());
                    existing.setPhone(dto.getPhone());
                    existing.setCpf(dto.getCpf());
                    return CustomerDTO.from(customerRepository.save(existing));
                });
    }

    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting customer id: {}", id);
        if (Objects.isNull(id)) {
            return false;
        }
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
