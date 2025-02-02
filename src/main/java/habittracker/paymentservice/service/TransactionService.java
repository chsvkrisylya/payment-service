package habittracker.paymentservice.service;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import habittracker.paymentservice.model.dto.TransactionInfoDTO;

import java.util.List;

public interface TransactionService {
    Transaction getTransactionByBraintree(String transactionId);

    Result<Transaction> refundTransactionByBraintree(String transactionId);

    List<TransactionInfoDTO> getTransactionsBySearchRequest();

    Result<Transaction> voidTransactionById(String transactionId);

    Result<Transaction> cancelTransactionById(String transactionId);
}
