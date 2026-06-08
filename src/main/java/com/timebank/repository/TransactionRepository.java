package com.timebank.repository;

import com.timebank.entity.Transaction;
import com.timebank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(User sender, User receiver);
}