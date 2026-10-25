package com.cashflowlite.repository;

import com.cashflowlite.model.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartyRepository extends JpaRepository<Party, Long> {

	Optional<Party> findFirstByNameIgnoreCase(String name);

	List<Party> findTop200ByOrderByNameAsc();
}
