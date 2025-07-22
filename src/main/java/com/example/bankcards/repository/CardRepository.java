package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByOwner(User owner, Pageable pageable);
    List<Card> findByOwnerAndStatus(User owner, CardStatus status);
    Optional<Card> findByCardNumber(String cardNumber);


    @Query("SELECT c FROM Card c WHERE c.owner = :owner " +
                  "AND (:status IS NULL OR c.status = :status)")
    Page<Card> findByOwnerWithStatusFilter(@Param("owner") User owner,
                                           @Param("status") CardStatus status,
                                           Pageable pageable);


    @Query("SELECT COUNT(c) FROM Card c WHERE c.owner = :owner AND c.status = 'ACTIVE'")
    long countActiveCardsByOwner(@Param("owner") User owner);

}
