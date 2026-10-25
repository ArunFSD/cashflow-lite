package com.cashflowlite.controller;

import com.cashflowlite.config.AppTime;
import com.cashflowlite.model.response.MonthlySummary;
import com.cashflowlite.service.MonthlyExpenseService;
import com.cashflowlite.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * The monthly report page.
 */
@Controller
@RequestMapping("/monthly")
public class MonthlyController {

	private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM yyyy");

	private final MonthlyExpenseService monthly;

	public MonthlyController(MonthlyExpenseService monthly) {
		this.monthly = monthly;
	}

	@GetMapping
	public String report(@RequestParam(name = "month", required = false) String month, Model model) {
		YearMonth selected = TransactionService.parseMonth(month);
		if (selected == null) {
			selected = YearMonth.now(AppTime.ZONE);
		}
		MonthlySummary summary = monthly.summarize(selected);
		model.addAttribute("summary", summary);
		model.addAttribute("month", selected.toString());
		model.addAttribute("monthLabel", selected.format(MONTH_LABEL));
		return "monthly";
	}
}
