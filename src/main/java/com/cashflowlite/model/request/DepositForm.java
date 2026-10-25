package com.cashflowlite.model.request;

import com.cashflowlite.config.AppTime;
import com.cashflowlite.model.entity.Deposit;
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
 * Form behind the add / edit deposit dialog.
 */
public class DepositForm {

	/** Version of the row being edited. Empty when adding. */
	private Integer version;

	@DateTimeFormat(pattern = "dd/MM/yyyy")
	@NotNull(message = "Enter the date as DD/MM/YYYY")
	private LocalDate depositDate = LocalDate.now(AppTime.ZONE);

	@NotBlank(message = "Enter who the money came from")
	@Size(max = 150, message = "Keep the name under 150 characters")
	private String receivedFrom;

	@NotNull(message = "Enter the amount")
	@DecimalMin(value = "0.01", message = "Amount must be more than zero")
	@Digits(integer = 10, fraction = 2, message = "Use up to 2 decimal places")
	private BigDecimal amount;

	@NotNull(message = "Pick how it came")
	private PaymentMode receivedVia = PaymentMode.CASH;

	@Size(max = 100, message = "Keep this under 100 characters")
	private String paymentDetail;

	@Size(max = 500, message = "Keep the notes under 500 characters")
	private String notes;

	public static DepositForm from(Deposit deposit) {
		DepositForm form = new DepositForm();
		form.setVersion(deposit.getVersion());
		form.setDepositDate(deposit.getDepositDate());
		form.setReceivedFrom(deposit.getParty().getName());
		form.setAmount(deposit.getAmount());
		form.setReceivedVia(deposit.getPaymentMethod().getCode());
		form.setPaymentDetail(deposit.getPaymentInstrument() == null ? null : deposit.getPaymentInstrument().getLabel());
		form.setNotes(deposit.getNotes());
		return form;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public LocalDate getDepositDate() {
		return depositDate;
	}

	public void setDepositDate(LocalDate depositDate) {
		this.depositDate = depositDate;
	}

	public String getReceivedFrom() {
		return receivedFrom;
	}

	public void setReceivedFrom(String receivedFrom) {
		this.receivedFrom = receivedFrom;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentMode getReceivedVia() {
		return receivedVia;
	}

	public void setReceivedVia(PaymentMode receivedVia) {
		this.receivedVia = receivedVia;
	}

	public String getPaymentDetail() {
		return paymentDetail;
	}

	public void setPaymentDetail(String paymentDetail) {
		this.paymentDetail = paymentDetail;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
