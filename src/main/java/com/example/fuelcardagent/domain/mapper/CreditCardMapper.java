package com.example.fuelcardagent.domain.mapper;

import com.example.fuelcardagent.config.modelMapper.ModelMapperConfig;
import com.example.fuelcardagent.domain.CreditCard;
import com.example.fuelcardagent.dto.CreditCardDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class CreditCardMapper{

    private final ModelMapperConfig modelMapper;

    public ModelMapper modelMapper() {

        ModelMapper modelMapper = this.modelMapper.modelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);

        return modelMapper;
    }

    public CreditCardDTO toJson(CreditCard creditCard) {
        return modelMapper().map(creditCard, CreditCardDTO.class);
    }

    public CreditCard toEntity(CreditCardDTO creditCardDTO) {
        return modelMapper().map(creditCardDTO, CreditCard.class);
    }

    public List<CreditCard> toCollectionEntity(List<CreditCardDTO> creditCardDTO) {
        return creditCardDTO.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
