package com.cashflowlite.controller;

import com.cashflowlite.service.DepositNotFoundException;
import com.cashflowlite.service.ExpenseNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Turns errors into a readable page (templates/error.html) instead of the default white label page.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler({ExpenseNotFoundException.class, DepositNotFoundException.class})
	public ModelAndView notFound(HttpServletRequest request, HttpServletResponse response) {
		log.warn("Entry not found for {} {}", request.getMethod(), request.getRequestURI());
		return show(response, HttpStatus.NOT_FOUND, "That entry was not found",
				"It may have been deleted already. Go back to the list and try again.");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ModelAndView noResource(HttpServletResponse response) {
		return show(response, HttpStatus.NOT_FOUND, "Page not found",
				"That address does not exist. Use the links below to continue.");
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ModelAndView conflict(HttpServletResponse response) {
		return show(response, HttpStatus.CONFLICT, "This entry was changed by someone else",
				"Nothing was saved. Open the list again to see the latest version, then make your change.");
	}

	@ExceptionHandler(DataAccessException.class)
	public ModelAndView database(DataAccessException ex, HttpServletRequest request, HttpServletResponse response) {
		log.error("Database error during {} {}", request.getMethod(), request.getRequestURI(), ex);
		return show(response, HttpStatus.INTERNAL_SERVER_ERROR, "Could not save to the database",
				"Nothing was changed. Check the entry and try again. The details are in the application log.");
	}

	@ExceptionHandler(Exception.class)
	public ModelAndView unexpected(Exception ex, HttpServletRequest request, HttpServletResponse response) {
		log.error("Unexpected error during {} {}", request.getMethod(), request.getRequestURI(), ex);
		return show(response, HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong",
				"The error has been logged. Try again, or go back to the list.");
	}

	private static ModelAndView show(HttpServletResponse response, HttpStatus status, String title, String message) {
		response.setStatus(status.value());
		ModelAndView view = new ModelAndView("error");
		view.addObject("title", title);
		view.addObject("message", message);
		view.addObject("status", status.value());
		return view;
	}
}
