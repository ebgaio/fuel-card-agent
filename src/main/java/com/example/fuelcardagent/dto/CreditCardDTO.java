package com.example.fuelcardagent.dto;

import com.example.fuelcardagent.domain.CreditCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CreditCardDTO {

//    private Long id;
    private String cardNumber;
    private String cardHolder;
    private LocalDate expirationDate;
    private String cardType;
    private BigDecimal creditLimit;
    private Long customerId;
    private String customerName;

    public static CreditCardDTO from(CreditCard card) {
        return CreditCardDTO.builder()
//                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .cardHolder(card.getCardHolder())
                .expirationDate(card.getExpirationDate())
                .cardType(card.getCardType().name())
                .creditLimit(card.getCreditLimit())
                .customerId(card.getCustomer().getId())
                .customerName(card.getCustomer().getName())
                .build();
    }
}
