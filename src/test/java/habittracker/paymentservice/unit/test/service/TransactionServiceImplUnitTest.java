package habittracker.paymentservice.unit.test.service;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ResourceCollection;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionGateway;
import com.braintreegateway.Result;
import com.braintreegateway.TransactionSearchRequest;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.TransactionInfoDTO;
import habittracker.paymentservice.service.TransactionServiceImpl;
import habittracker.paymentservice.service.util.DateFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplUnitTest {

    @Mock
    private BraintreeGateway braintreeGateway;

    @Mock
    private TransactionGateway transactionGateway;

    @Mock
    private Transaction mockTransaction;

    @Mock
    private ResourceCollection<Transaction> mockCollection;

    @Mock
    private Result<Transaction> mockResult;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    public void setUp() {
        transactionService = new TransactionServiceImpl();

        BraintreeData.gateway = braintreeGateway;
        when(braintreeGateway.transaction()).thenReturn(transactionGateway);
    }

    @Test
    void testGetTransactionByBraintree() {
        //тестовые данные
        String transactionId = "test-transaction-id";
        when(transactionGateway.find(transactionId)).thenReturn(mockTransaction);

        //вызов тестируемого метода
        Transaction result = transactionService.getTransactionByBraintree(transactionId);

        //asserting
        assertNotNull(result, "Transaction not found");
        assertEquals(result, mockTransaction, "Transactions are different");
        verify(transactionGateway).find(transactionId);
    }

    @Test
    void testRefundTransactionByBraintree() {
        //given
        String transactionId = "test-transaction-id";
        when(transactionGateway.refund(transactionId)).thenReturn(mockResult);

        //calling method
        Result<Transaction> result = transactionService.refundTransactionByBraintree(transactionId);

        //asserting
        assertNotNull(result, "Transaction not found");
        assertEquals(result, mockResult, "Transactions are different");
        verify(transactionGateway).refund(transactionId);
    }

    @Test
    void testGetTransactionsBySearchRequest() {
        //given
        String transactionId = "test-transaction-id";
        Calendar createdAt = Calendar.getInstance();
        Transaction.Type type = Transaction.Type.SALE;
        BigDecimal amount = new BigDecimal(100.00);
        Transaction.Status status = Transaction.Status.SETTLED;
        String refundedTransactionId = null;

        when(mockTransaction.getId()).thenReturn(transactionId);
        when(mockTransaction.getCreatedAt()).thenReturn(createdAt);
        when(mockTransaction.getType()).thenReturn(type);
        when(mockTransaction.getAmount()).thenReturn(amount);
        when(mockTransaction.getStatus()).thenReturn(status);
        when(mockTransaction.getRefundedTransactionId()).thenReturn(refundedTransactionId);

        when(transactionGateway.search(any(TransactionSearchRequest.class))).thenReturn(mockCollection);
        doAnswer(invocation -> {
            Consumer<Transaction> consumer = invocation.getArgument(0);
            consumer.accept(mockTransaction);
            return null;
        }).when(mockCollection).forEach(any());

        //calling method
        List<TransactionInfoDTO> result = transactionService.getTransactionsBySearchRequest();

        //asserting
        verify(transactionGateway).search(any(TransactionSearchRequest.class));
        assertNotNull(result, "Transaction not found");
        assertEquals(1, result.size(), "Transactions not found");
        TransactionInfoDTO transactionInfoDTO = result.getFirst();
        assertEquals(transactionId, transactionInfoDTO.getId(), "Transaction not found");
        assertEquals(DateFormatter.dateToString(createdAt.getTime()), transactionInfoDTO.getCreatedAt(),
                "Transactions not found");
        assertEquals(type, transactionInfoDTO.getType(), "Transaction not found");
        assertEquals(amount, transactionInfoDTO.getAmount(), "Transaction not found");
        assertEquals(status, transactionInfoDTO.getStatus(), "Transaction not found");
        assertEquals(refundedTransactionId, transactionInfoDTO.getRefundedTransactionId(),
                "Transaction not found");
        assertFalse(transactionInfoDTO.isRefund(), "Transaction are not refunded");
    }

    @Test
    void testVoidTransactionById() {
        //given
        String transactionId = "test-transaction-id";
        when(transactionGateway.voidTransaction(transactionId)).thenReturn(mockResult);

        //calling method
        Result<Transaction> result = transactionService.voidTransactionById(transactionId);

        //asserting
        verify(transactionGateway).voidTransaction(transactionId);
        assertNotNull(result, "Transaction not found");
        assertEquals(result, mockResult, "Transactions are different");
    }

    @Test
    void testCancelTransactionById() {
        //given
        String transactionId = "test-transaction-id";
        when(transactionGateway.cancelRelease(transactionId)).thenReturn(mockResult);

        //calling method
        Result<Transaction> result = transactionService.cancelTransactionById(transactionId);

        //asserting
        verify(transactionGateway).cancelRelease(transactionId);
        assertNotNull(result, "Transaction not found");
        assertEquals(result, mockResult, "Transactions are different");
    }
}
