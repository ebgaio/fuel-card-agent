package com.example.fuelcardagent.dto;

import com.example.fuelcardagent.domain.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CustomerDTO {

//    private Long id;
    private String name;
    private String email;
    private String phone;
    private String cpf;
    private LocalDateTime createdAt;
    private String customerStatus;

    public static CustomerDTO from(Customer customer) {
        return CustomerDTO.builder()
//                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .cpf(customer.getCpf())
                .createdAt(customer.getCreatedAt())
                .customerStatus(customer.getCustomerStatus().name())
                .build();
    }
}
