package com.example.fuelcardagent.repository;

import com.example.fuelcardagent.domain.CardType;
import com.example.fuelcardagent.domain.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findByCardType(CardType cardType);

    List<CreditCard> findByCustomerId(Long customerId);

    @Query("SELECT AVG(c.creditLimit) FROM CreditCard c WHERE c.cardType = :cardType")
    BigDecimal averageCreditLimitByCardType(CardType cardType);

    long countByCardType(CardType cardType);

    @Query("SELECT c FROM CreditCard c WHERE c.cardType = :cardType AND c.creditLimit BETWEEN :minLimit AND :maxLimit")
    List<CreditCard> findByCardTypeAndCreditLimitBetween(CardType cardType, BigDecimal minLimit, BigDecimal maxLimit);
}
