package com.cashflowlite.api;

import com.cashflowlite.service.DepositNotFoundException;
import com.cashflowlite.service.ExpenseNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns API errors into JSON problem responses (RFC 9457). Only applies to the /api controllers.
 */
@RestControllerAdvice(basePackages = "com.cashflowlite.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler({ExpenseNotFoundException.class, DepositNotFoundException.class})
	public ProblemDetail notFound() {
		return problem(HttpStatus.NOT_FOUND, "Not found", "No entry with that id exists.");
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ProblemDetail conflict() {
		return problem(HttpStatus.CONFLICT, "Changed by someone else",
				"The entry was changed after you loaded it. Fetch it again and retry. Nothing was saved.");
	}

	@ExceptionHandler(InvalidEntryException.class)
	public ProblemDetail rule(InvalidEntryException ex) {
		return invalid(ex.problems());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail beanCheck(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(error.getField(), error.getDefaultMessage());
		}
		return invalid(errors);
	}

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ProblemDetail badInput() {
		return problem(HttpStatus.BAD_REQUEST, "Bad request",
				"A value has the wrong format. Dates are YYYY-MM-DD and amounts are numbers.");
	}

	@ExceptionHandler(DataAccessException.class)
	public ProblemDetail database(DataAccessException ex) {
		log.error("Database error in API", ex);
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Database error",
				"Nothing was changed. The details are in the application log.");
	}

	private static ProblemDetail invalid(Map<String, String> errors) {
		ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Invalid entry", "Some fields need attention.");
		detail.setProperty("errors", errors);
		return detail;
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		return problem;
	}
}
