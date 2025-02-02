package habittracker.paymentservice.service;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;

public interface CheckoutService {
    String getNewClientToken();

    TransactionRequest getNewTransactionRequest(String amount, String paymentMethodNonce);

    Result<Transaction> getTransactionSale(TransactionRequest request);

    Transaction.Status[] getTransactionSuccessStatuses();

    String getValidationErrors(Result<Transaction> result);
}
