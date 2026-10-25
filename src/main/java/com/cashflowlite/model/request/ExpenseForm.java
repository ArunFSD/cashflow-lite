package com.cashflowlite.model.request;

import com.cashflowlite.config.AppTime;
import com.cashflowlite.model.entity.Expense;
import com.cashflowlite.model.entity.ExpenseType;
import com.cashflowlite.model.entity.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Form behind the add / edit expense dialog. Field rules that depend on the choices
 * (card needed, friend needed, note needed) are checked by EntryValidator.
 */
public class ExpenseForm {

	/** Version of the row being edited. Empty when adding. */
	private Integer version;

	@DateTimeFormat(pattern = "dd/MM/yyyy")
	@NotNull(message = "Enter the date as DD/MM/YYYY")
	private LocalDate expenseDate = LocalDate.now(AppTime.ZONE);

	@NotBlank(message = "Enter a name for this expense")
	@Size(max = 60, message = "Keep the name under 60 characters")
	private String name;

	@NotNull(message = "Enter the amount")
	@DecimalMin(value = "0.01", message = "Amount must be more than zero")
	@Digits(integer = 10, fraction = 2, message = "Use up to 2 decimal places")
	private BigDecimal amount;

	@NotNull(message = "Pick how you paid")
	private PaymentMode paymentMode = PaymentMode.CASH;

	@Size(max = 100, message = "Keep this under 100 characters")
	private String paymentDetail;

	@NotNull(message = "Pick what this payment was for")
	private ExpenseType expenseType = ExpenseType.PURCHASE;

	@Size(max = 150, message = "Keep the name under 150 characters")
	private String partyName;

	@Size(max = 100, message = "Keep this under 100 characters")
	private String purposeNote;

	@Size(max = 500, message = "Keep the notes under 500 characters")
	private String notes;

	public static ExpenseForm from(Expense expense) {
		ExpenseForm form = new ExpenseForm();
		form.setVersion(expense.getVersion());
		form.setExpenseDate(expense.getExpenseDate());
		form.setName(expense.getName());
		form.setAmount(expense.getAmount());
		form.setPaymentMode(expense.getPaymentMethod().getCode());
		form.setPaymentDetail(expense.getPaymentInstrument() == null ? null : expense.getPaymentInstrument().getLabel());
		form.setExpenseType(expense.getPurpose().getCode());
		form.setPartyName(expense.getParty() == null ? null : expense.getParty().getName());
		form.setPurposeNote(expense.getPurposeNote());
		form.setNotes(expense.getNotes());
		return form;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public LocalDate getExpenseDate() {
		return expenseDate;
	}

	public void setExpenseDate(LocalDate expenseDate) {
		this.expenseDate = expenseDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentMode getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(PaymentMode paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getPaymentDetail() {
		return paymentDetail;
	}

	public void setPaymentDetail(String paymentDetail) {
		this.paymentDetail = paymentDetail;
	}

	public ExpenseType getExpenseType() {
		return expenseType;
	}

	public void setExpenseType(ExpenseType expenseType) {
		this.expenseType = expenseType;
	}

	public String getPartyName() {
		return partyName;
	}

	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}

	public String getPurposeNote() {
		return purposeNote;
	}

	public void setPurposeNote(String purposeNote) {
		this.purposeNote = purposeNote;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
