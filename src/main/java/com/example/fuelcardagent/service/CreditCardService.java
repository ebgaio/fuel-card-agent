package com.example.fuelcardagent.service;

import com.example.fuelcardagent.domain.CardType;
import com.example.fuelcardagent.dto.CardStatsDTO;
import com.example.fuelcardagent.dto.CreditCardDTO;
import com.example.fuelcardagent.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;

    public List<CreditCardDTO> findAll() {
        log.debug("Finding all credit cards");
        return creditCardRepository.findAll()
                .stream()
                .map(CreditCardDTO::from)
                .toList();
    }

    public List<CreditCardDTO> findGasStationCards() {
        log.debug("Finding all GAS_STATION cards");
        return creditCardRepository.findByCardType(CardType.GAS_STATION)
                .stream()
                .map(CreditCardDTO::from)
                .toList();
    }

    public List<CreditCardDTO> findByCustomerId(Long customerId) {
        log.debug("Finding cards for customer id: {}", customerId);
        return creditCardRepository.findByCustomerId(customerId)
                .stream()
                .map(CreditCardDTO::from)
                .toList();
    }

    public CardStatsDTO getGasStationStats() {
        log.debug("Computing GAS_STATION card statistics");
        long total = creditCardRepository.countByCardType(CardType.GAS_STATION);
        BigDecimal avg = creditCardRepository.averageCreditLimitByCardType(CardType.GAS_STATION);
        if (avg == null) avg = BigDecimal.ZERO;

        List<CreditCardDTO> cards = findGasStationCards();
        BigDecimal totalLimit = cards.stream()
                .map(CreditCardDTO::getCreditLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CardStatsDTO(total, avg.setScale(2, java.math.RoundingMode.HALF_UP), totalLimit);
    }

    /**
     * Retorna os cartões GAS_STATION cujo limite de crédito está dentro de uma
     * faixa percentual em relação a um valor de referência.
     *
     * @param referenceValue valor de referência do limite (ex: 4375.00)
     * @param percentage     percentual de variação (ex: 20 para ±20%)
     * @return lista de cartões dentro da faixa [referenceValue*(1-pct/100), referenceValue*(1+pct/100)]
     */
    public List<CreditCardDTO> findByLimitRange(BigDecimal referenceValue, BigDecimal percentage) {
        log.debug("Finding GAS_STATION cards with limit range: {}% around {}", percentage, referenceValue);

        BigDecimal factor = percentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal minLimit = referenceValue.multiply(BigDecimal.ONE.subtract(factor)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxLimit = referenceValue.multiply(BigDecimal.ONE.add(factor)).setScale(2, RoundingMode.HALF_UP);

        log.debug("Credit limit range: [{}, {}]", minLimit, maxLimit);

        return creditCardRepository
                .findByCardTypeAndCreditLimitBetween(CardType.GAS_STATION, minLimit, maxLimit)
                .stream()
                .map(CreditCardDTO::from)
                .toList();
    }
}
