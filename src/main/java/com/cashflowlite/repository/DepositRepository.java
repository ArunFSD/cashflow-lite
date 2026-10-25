package com.cashflowlite.repository;

import com.cashflowlite.model.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Writes and single-row reads for deposits. Lists and totals are read with SQL in the service layer.
 */
public interface DepositRepository extends JpaRepository<Deposit, Long> {
}
