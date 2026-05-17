package com.example.fuelcardagent.service;

import com.example.fuelcardagent.domain.CardType;
import com.example.fuelcardagent.domain.CreditCard;
import com.example.fuelcardagent.domain.Customer;
import com.example.fuelcardagent.domain.mapper.CreditCardMapper;
import com.example.fuelcardagent.dto.CardStatsDTO;
import com.example.fuelcardagent.dto.CreditCardDTO;
import com.example.fuelcardagent.dto.CreditCardLimitDTO;
import com.example.fuelcardagent.repository.CreditCardRepository;
import com.example.fuelcardagent.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final CustomerRepository customerRepository;
    private final CreditCardMapper creditCardMapper;
    private final ModelMapper modelMapper;

    public List<CreditCardDTO> findAll() {
        log.debug("Finding all credit cards");
        return creditCardRepository.findAll()
                .stream()
                .map(CreditCardDTO::from)
                .toList();
    }

    public Optional<CreditCardDTO> findById(Long id) {
        log.debug("Finding credit card by id: {}", id);
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return creditCardRepository.findById(id)
                .map(CreditCardDTO::from);
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
        if (Objects.isNull(customerId)) {
            return List.of();
        }
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
    public List<CreditCardLimitDTO> findByLimitRange(BigDecimal referenceValue, BigDecimal percentage) {
        log.debug("Finding GAS_STATION cards with limit range: {}% around {}", percentage, referenceValue);

        BigDecimal factor = percentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal minLimit = referenceValue.multiply(BigDecimal.ONE.subtract(factor)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxLimit = referenceValue.multiply(BigDecimal.ONE.add(factor)).setScale(2, RoundingMode.HALF_UP);

        log.debug("Credit limit range: [{}, {}]", minLimit, maxLimit);

        List<CreditCardLimitDTO> creditCardLimitDTO = creditCardRepository
                .findByCardTypeAndCreditLimitBetween(CardType.GAS_STATION, minLimit, maxLimit)
                .stream()
                .toList();

        return creditCardLimitDTO;
    }

    public List<CreditCard> getExpiredCards(Long creditCardId) {
        return creditCardRepository.findByExpirationDateBeforeOrderByExpirationDateDesc(LocalDate.now(), creditCardId);
    }

    public List<CreditCard> getCardsExpiringSoon() {
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsFromNow = today.plusMonths(2);
        return creditCardRepository.findByExpirationDateBetweenOrderByExpirationDateAsc(today, twoMonthsFromNow);
    }

    @Transactional
    public CreditCardDTO create(CreditCardDTO dto) {
        Long customerId;
        log.info("Creating credit card for customer: {}", dto.getCustomerId());
        if (Objects.isNull(dto.getCustomerId())) {
            throw new IllegalArgumentException("O ID do cliente não pode ser nulo.");
        } else {
            customerId = dto.getCustomerId();
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + customerId));

        dto.setCustomerId(customer.getId());
        dto.setCustomerName(customer.getName());
        CreditCard creditCard = creditCardMapper.toEntity(dto);
        CreditCard creditCardSaved = creditCardRepository.save(creditCard);
        return creditCardMapper.toJson(creditCardSaved);
    }

    @Transactional
    public CreditCardDTO update(Long id, CreditCardDTO dto) {
        log.info("Updating credit card id: {}", id);
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("O ID do cartão não pode ser nulo.");
        }

        if (Objects.isNull(dto)) {
            throw new IllegalArgumentException("O DTO do cartão não pode ser nulo.");
        }

        Long customerId = dto.getCustomerId();
        if (Objects.isNull(customerId)) {
            throw new IllegalArgumentException("O ID do cliente não pode ser nulo.");
        }

        customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado" + customerId));

        creditCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado: " + id));

        CreditCard creditCard = creditCardMapper.toEntity(dto);
        CreditCard creditCardSaved = creditCardRepository.save(creditCard);
        return creditCardMapper.toJson(creditCardSaved);
    }

    @Transactional
    public boolean delete(Long id) {
        log.info("Deleting credit card id: {}", id);
        if (Objects.isNull(id)) {
            return false;
        }
        if (creditCardRepository.existsById(id)) {
            creditCardRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
