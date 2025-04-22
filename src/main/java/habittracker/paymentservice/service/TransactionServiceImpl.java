package habittracker.paymentservice.service;

import com.braintreegateway.ResourceCollection;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionSearchRequest;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.TransactionInfoDTO;
import habittracker.paymentservice.service.util.DateFormatter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Override
    public Transaction getTransactionByBraintree(String transactionId) {
        return BraintreeData.getGateway().transaction().find(transactionId);
    }

    @Override
    public Result<Transaction> refundTransactionByBraintree(String transactionId) {
        return BraintreeData.getGateway().transaction().refund(transactionId);
    }

    @Override
    public List<TransactionInfoDTO> getTransactionsBySearchRequest() {
        TransactionSearchRequest request = new TransactionSearchRequest().creditCardNumber().startsWith("4111");
        ResourceCollection<Transaction> collection = BraintreeData.getGateway().transaction().search(request);

        List<TransactionInfoDTO> transactionList = new ArrayList<>();
        collection.forEach(transaction -> {
            TransactionInfoDTO transactionInfo = new TransactionInfoDTO(
                    transaction.getId(),
                    transaction.getCreatedAt() == null ? null : DateFormatter.dateToString(transaction.getCreatedAt()
                            .getTime()),
                    transaction.getType(),
                    transaction.getAmount(),
                    transaction.getStatus(),
                    transaction.getRefundedTransactionId(),
                    false);

            transactionInfo.setRefundFlag(transaction.getRefundedTransactionId());
            transactionList.add(transactionInfo);
        });

        return transactionList;
    }

    @Override
    public Result<Transaction> voidTransactionById(String transactionId) {
        return BraintreeData.getGateway().transaction().voidTransaction(transactionId);
    }

    @Override
    public Result<Transaction> cancelTransactionById(String transactionId) {
        return BraintreeData.getGateway().transaction().cancelRelease(transactionId);
    }
}