package habittracker.paymentservice.model.dto;

import com.braintreegateway.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
public class TransactionInfoDTO {

    @NotBlank(message = "Transaction ID cannot be blank")
    private String id;

    @NotBlank(message = "Creation date cannot be blank")
    private String createdAt;

    @NotNull(message = "Transaction type cannot be null")
    private Transaction.Type type;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Transaction status cannot be null")
    private Transaction.Status status;

    private String refundedTransactionId;

    private boolean isRefund;

    public void setRefundFlag(String refundedTransactionId) {
        if (refundedTransactionId != null) {
            this.isRefund = true;
        } else {
            this.isRefund = false;
        }
    }
}
