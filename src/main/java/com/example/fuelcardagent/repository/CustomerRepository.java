package com.example.fuelcardagent.repository;

import com.example.fuelcardagent.domain.CardType;
import com.example.fuelcardagent.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT c FROM Customer c JOIN c.cards card WHERE card.cardType = :cardType")
    List<Customer> findByCardType(@Param("cardType") CardType cardType);
}
