package com.cashflowlite.service;

import com.cashflowlite.model.entity.Party;
import com.cashflowlite.repository.PartyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Suggestions for the dropdown boxes on the forms. Only the most recent rows are scanned,
 * so the lists stay fast even when the tables hold millions of rows.
 */
@Service
@Transactional(readOnly = true)
public class LookupService {

	private static final List<String> STARTER_NAMES = List.of(
			"Food", "Groceries", "Travel", "Fuel", "Bills", "Shopping",
			"Health", "Entertainment", "Rent", "Transfers", "Other");

	private final PartyRepository parties;
	private final org.springframework.jdbc.core.simple.JdbcClient jdbc;

	public LookupService(PartyRepository parties, org.springframework.jdbc.core.simple.JdbcClient jdbc) {
		this.parties = parties;
		this.jdbc = jdbc;
	}

	/** Adds the three suggestion lists the forms use. */
	public void addSuggestions(Model model) {
		model.addAttribute("nameSuggestions", nameSuggestions());
		model.addAttribute("detailSuggestions", instrumentLabels());
		model.addAttribute("partySuggestions", partyNames());
	}

	List<String> nameSuggestions() {
		Set<String> names = new TreeSet<>(STARTER_NAMES);
		names.addAll(jdbc.sql("""
						SELECT DISTINCT name FROM (
						    SELECT name FROM expenses ORDER BY expense_date DESC, id DESC LIMIT 2000
						) recent
						""")
				.query(String.class)
				.list());
		return List.copyOf(names);
	}

	List<String> instrumentLabels() {
		return jdbc.sql("SELECT label FROM payment_instruments ORDER BY label LIMIT 200")
				.query(String.class)
				.list();
	}

	List<String> partyNames() {
		return parties.findTop200ByOrderByNameAsc().stream().map(Party::getName).toList();
	}
}
