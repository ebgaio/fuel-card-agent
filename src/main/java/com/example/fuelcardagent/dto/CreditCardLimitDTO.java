package com.example.fuelcardagent.dto;

import com.example.fuelcardagent.domain.CreditCard;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CreditCardLimitDTO {

//    private Long id;
    private String customerName;
    private LocalDate expirationDate;
    private BigDecimal creditLimit;

    public static CreditCardLimitDTO from(CreditCard card) {
        return CreditCardLimitDTO.builder()
//                .id(card.getId())
                .customerName(card.getCustomer().getName())
                .expirationDate(card.getExpirationDate())
                .creditLimit(card.getCreditLimit())
                .build();
    }
}
