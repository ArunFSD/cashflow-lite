package com.cashflowlite.repository;

import com.cashflowlite.model.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Writes and single-row reads for expenses. Lists and totals are read with SQL in the service layer.
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
