package com.cashflowlite.controller;

import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;

import java.util.List;

/**
 * The parameters of a list page: where it lives, what kind of rows, and the filters.
 * from and to are raw text ("yyyy-MM-dd"). Null for both means the current month.
 * fields may hold several columns to search in.
 */
public record ListRequest(
		String base,
		EntryType kind,
		String from,
		String to,
		List<SearchField> fields,
		String query,
		int page) {
}
