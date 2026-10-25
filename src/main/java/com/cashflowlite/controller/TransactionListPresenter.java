package com.cashflowlite.controller;

import com.cashflowlite.config.AppTime;
import com.cashflowlite.model.response.EntryType;
import com.cashflowlite.model.response.SearchField;
import com.cashflowlite.model.response.TransactionFilter;
import com.cashflowlite.model.response.TransactionPage;
import com.cashflowlite.service.TransactionService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Fills the model for every list page: the current page of rows, the filter values, and the
 * links for the pager. Shared by the expense, deposit and all-transactions controllers.
 */
@Component
public class TransactionListPresenter {

	public static final int PAGE_SIZE = 50;

	private final TransactionService transactions;

	public TransactionListPresenter(TransactionService transactions) {
		this.transactions = transactions;
	}

	public void show(Model model, ListRequest request) {
		LocalDate[] range = range(request.from(), request.to());
		LocalDate from = range[0];
		LocalDate to = range[1];
		List<SearchField> fields = normalise(request.fields());
		String query = request.query() == null ? "" : request.query().trim();
		EntryType kind = request.kind() == null ? EntryType.ALL : request.kind();

		TransactionPage result = transactions.page(
				new TransactionFilter(from, to, kind, fields, query), request.page(), PAGE_SIZE);

		String fromText = from == null ? "" : from.toString();
		String toText = to == null ? "" : to.toString();
		StringBuilder queryString = new StringBuilder()
				.append("from=").append(fromText)
				.append("&to=").append(toText);
		for (SearchField field : fields) {
			queryString.append("&field=").append(field.name());
		}
		queryString.append("&q=").append(encode(query));
		if ("/transactions".equals(request.base()) && kind != EntryType.ALL) {
			queryString.append("&kind=").append(kind.name());
		}

		model.addAttribute("result", result);
		model.addAttribute("from", fromText);
		model.addAttribute("to", toText);
		model.addAttribute("kind", kind);
		model.addAttribute("fields", fields);
		model.addAttribute("fieldsLabel", fieldsLabel(fields));
		model.addAttribute("q", query);
		model.addAttribute("searchFields", SearchField.values());
		model.addAttribute("entryTypes", EntryType.values());
		model.addAttribute("baseUrl", request.base());
		model.addAttribute("pagePrefix", request.base() + "?" + queryString);
	}

	/**
	 * Works out the dates to filter by. When neither date is given, this month is used.
	 * A blank date means no limit on that side. If the dates are reversed, they are swapped.
	 */
	public static LocalDate[] range(String fromText, String toText) {
		if (fromText == null && toText == null) {
			YearMonth now = YearMonth.now(AppTime.ZONE);
			return new LocalDate[] {now.atDay(1), now.atEndOfMonth()};
		}
		LocalDate from = parseDate(fromText);
		LocalDate to = parseDate(toText);
		if (from != null && to != null && from.isAfter(to)) {
			LocalDate swap = from;
			from = to;
			to = swap;
		}
		return new LocalDate[] {from, to};
	}

	/** After saving or deleting, go back to the list showing the whole month of that entry. */
	public static String redirectToMonthOf(String base, LocalDate date) {
		YearMonth month = YearMonth.from(date);
		return "redirect:" + base + "?from=" + month.atDay(1) + "&to=" + month.atEndOfMonth();
	}

	/** No columns ticked, or "All fields" ticked, means every column. */
	static List<SearchField> normalise(List<SearchField> requested) {
		if (requested == null) {
			return List.of(SearchField.ALL);
		}
		List<SearchField> chosen = requested.stream().filter(Objects::nonNull).distinct().toList();
		if (chosen.isEmpty() || chosen.contains(SearchField.ALL)) {
			return List.of(SearchField.ALL);
		}
		return chosen;
	}

	static String fieldsLabel(List<SearchField> fields) {
		if (fields.size() == 1 && fields.get(0) == SearchField.ALL) {
			return SearchField.ALL.getLabel();
		}
		return fields.stream().map(SearchField::getLabel).collect(Collectors.joining(", "));
	}

	private static LocalDate parseDate(String text) {
		if (text == null || text.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(text.trim());
		} catch (DateTimeParseException ex) {
			return null;
		}
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
