package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByFromCard(Card fromCard, Pageable pageable);

    Page<Transaction> findByToCard(Card toCard, Pageable pageable);

    Page<Transaction> findByFromCardAndToCard(Card fromCard, Card toCard, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.fromCard = :card OR t.toCard = :card ORDER BY t.transactionDate DESC")
    Page<Transaction> findByFromCardOrToCard(@Param("card") Card card, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.fromCard.owner.id = :userId OR t.toCard.owner.id = :userId ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);
}