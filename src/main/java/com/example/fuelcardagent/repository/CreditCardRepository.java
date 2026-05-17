package com.example.fuelcardagent.repository;

import com.example.fuelcardagent.domain.CardType;
import com.example.fuelcardagent.domain.CreditCard;
import com.example.fuelcardagent.dto.CreditCardLimitDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findByCardType(CardType cardType);

    List<CreditCard> findByCustomerId(Long customerId);

    @Query("SELECT AVG(c.creditLimit) FROM CreditCard c WHERE c.cardType = :cardType")
    BigDecimal averageCreditLimitByCardType(CardType cardType);

    long countByCardType(CardType cardType);

    @Query("SELECT new com.example.fuelcardagent.dto.CreditCardLimitDTO(customer.name, creditCard.expirationDate, creditCard.creditLimit) " +
           "FROM CreditCard creditCard " +
           "JOIN Customer customer " +
           "ON creditCard.customer.id = customer.id " +
           "WHERE creditCard.cardType = :cardType " +
           "AND creditCard.creditLimit BETWEEN :minLimit AND :maxLimit")
    List<CreditCardLimitDTO> findByCardTypeAndCreditLimitBetween(CardType cardType, BigDecimal minLimit, BigDecimal maxLimit);

    // Busca cartões onde a data de expiração é anterior à data fornecida (Vencidos)
    @Query("SELECT c FROM CreditCard c WHERE c.expirationDate < :date AND c.id = :creditCarId")
    List<CreditCard> findByExpirationDateBeforeOrderByExpirationDateDesc(LocalDate date, Long creditCarId);

    // Busca cartões que expiram entre duas datas (Próximos 2 meses)
    @Query("SELECT c FROM CreditCard c WHERE c.expirationDate BETWEEN :start AND :end")
    List<CreditCard> findByExpirationDateBetweenOrderByExpirationDateAsc(LocalDate start, LocalDate end);
}
