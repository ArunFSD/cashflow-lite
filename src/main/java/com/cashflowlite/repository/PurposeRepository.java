package com.cashflowlite.repository;

import com.cashflowlite.model.entity.ExpenseType;
import com.cashflowlite.model.entity.Purpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurposeRepository extends JpaRepository<Purpose, Integer> {

	Optional<Purpose> findByCode(ExpenseType code);
}
