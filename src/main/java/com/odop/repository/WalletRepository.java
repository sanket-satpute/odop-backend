package com.odop.repository;

import com.odop.model.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface WalletRepository extends MongoRepository<Wallet, String> {

    // Find wallet by customer ID
    Optional<Wallet> findByCustomerId(String customerId);

    // Check if wallet exists for customer
    boolean existsByCustomerId(String customerId);

    // Find wallets by active status
    List<Wallet> findByIsActive(boolean isActive);

    // Find locked wallets
    List<Wallet> findByIsLocked(boolean isLocked);

    // Find wallets with balance greater than
    @Query("{ 'balance': { $gte: ?0 } }")
    List<Wallet> findByBalanceGreaterThanEqual(double amount);

    // Find wallets with balance less than
    @Query("{ 'balance': { $lte: ?0 } }")
    List<Wallet> findByBalanceLessThanEqual(double amount);

    // Count active wallets
    long countByIsActive(boolean isActive);

    // Find wallets with recent transactions
    @Query("{ 'transactions.0.timestamp': { $gte: ?0 } }")
    List<Wallet> findWalletsWithRecentTransactions(java.util.Date since);
}
