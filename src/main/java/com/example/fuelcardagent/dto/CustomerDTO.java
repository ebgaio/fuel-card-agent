package com.example.fuelcardagent.dto;

import com.example.fuelcardagent.domain.Customer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String cpf;
    private LocalDateTime createdAt;

    public static CustomerDTO from(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .cpf(customer.getCpf())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
