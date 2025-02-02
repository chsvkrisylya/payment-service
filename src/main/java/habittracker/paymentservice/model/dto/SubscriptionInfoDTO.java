package habittracker.paymentservice.model.dto;

import com.braintreegateway.Subscription.Status;
import com.braintreegateway.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class SubscriptionInfoDTO {

    @NotBlank(message = "ID cannot be blank")
    String id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must be less than or equal to 100 characters")
    String name;

    @NotBlank(message = "Merchant Account ID cannot be blank")
    @Size(max = 50, message = "Merchant Account ID must be less than or equal to 50 characters")
    String merchantAccountId;

    @NotBlank(message = "Plan ID cannot be blank")
    @Size(max = 50, message = "Plan ID must be less than or equal to 50 characters")
    String planId;

    @NotNull(message = "Status cannot be null")
    Status status;

    @NotNull(message = "Transactions list cannot be null")
    List<Transaction> transactions;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    BigDecimal price;

    @NotBlank(message = "Payment method token cannot be blank")
    @Size(max = 50, message = "Payment method token must be less than or equal to 50 characters")
    String paymentMethodToken;

    @NotNull(message = "Number of billing cycles cannot be null")
    @DecimalMin(value = "1", message = "Number of billing cycles must be at least 1")
    Integer numberOfBillingCycles;

    @NotBlank(message = "Next billing date cannot be blank")
    String nextBillingDate;

    @NotBlank(message = "First billing date cannot be blank")
    String firstBillingDate;

    @NotNull(message = "Current billing cycle cannot be null")
    @DecimalMin(value = "1", message = "Current billing cycle must be at least 1")
    Integer currentBillingCycle;

    @NotBlank(message = "Created date cannot be blank")
    String createdAt;

    @NotBlank(message = "Updated date cannot be blank")
    String updatedAt;

    @NotBlank(message = "Billing period start date cannot be blank")
    String billingPeriodStartDate;

    @NotBlank(message = "Billing period end date cannot be blank")
    String billingPeriodEndDate;
}
