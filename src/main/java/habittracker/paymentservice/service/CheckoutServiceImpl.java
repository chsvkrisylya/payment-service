package habittracker.paymentservice.service;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.Transaction.Status;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.service.util.NumFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final NumFormatter numFormatter;

    @Override
    public String getNewClientToken() {
        return BraintreeData.gateway.clientToken().generate();
    }

    @Override
    public TransactionRequest getNewTransactionRequest(String amount, String paymentMethodNonce) {
        return new TransactionRequest()
                .amount(numFormatter.stringToNum(amount, BigDecimal.class).orElseThrow(
                        () -> new NumberFormatException("Некорректный формат суммы: " + amount)
                ))
                .paymentMethodNonce(paymentMethodNonce)
                .options()
                .submitForSettlement(true)
                .done();
    }

    @Override
    public Result<Transaction> getTransactionSale(TransactionRequest request) {
        return BraintreeData.gateway.transaction().sale(request);
    }

    @Override
    public Status[] getTransactionSuccessStatuses() {
        return new Status[]{
                Transaction.Status.AUTHORIZED,
                Transaction.Status.AUTHORIZING,
                Transaction.Status.SETTLED,
                Transaction.Status.SETTLEMENT_CONFIRMED,
                Transaction.Status.SETTLEMENT_PENDING,
                Transaction.Status.SETTLING,
                Transaction.Status.SUBMITTED_FOR_SETTLEMENT
        };
    }

    @Override
    public String getValidationErrors(Result<Transaction> result) {
        StringBuilder errorString = new StringBuilder();
        for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
            errorString.append("Error: ").append(error.getCode()).append(": ").append(error.getMessage()).append("\n");
        }
        return errorString.toString();
    }
}
