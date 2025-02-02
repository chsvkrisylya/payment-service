package habittracker.paymentservice.unit.test.controller;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import habittracker.paymentservice.controller.TransactionController;
import habittracker.paymentservice.model.dto.TransactionInfoDTO;
import habittracker.paymentservice.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransactionControllerUnitTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTransactionByBraintree() {
        Transaction transaction = Mockito.mock(Transaction.class);
        when(transactionService.getTransactionByBraintree(any())).thenReturn(transaction);
        var result = transactionController.getTransactionByBraintree(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(transaction));
    }

    @Test
    void testGetTransactionsBySearchRequest() {
        List<TransactionInfoDTO> transactionList = Mockito.mock(List.class);
        when(transactionService.getTransactionsBySearchRequest()).thenReturn(transactionList);
        var result = transactionController.getTransactionsBySearchRequest();
        assertThat(result).isEqualTo(ResponseEntity.ok(transactionList));
    }

    @Test
    void testRefundTransactionByBraintree() {
        Result<Transaction> transaction = Mockito.mock(Result.class);
        when(transactionService.refundTransactionByBraintree(any())).thenReturn(transaction);
        var result = transactionController.refundTransactionByBraintree(new HashMap<>());
        assertThat(result).isEqualTo(ResponseEntity.ok(transaction));
    }

    @Test
    void testVoidTransaction() {
        Result<Transaction> transaction = Mockito.mock(Result.class);
        when(transactionService.voidTransactionById(any())).thenReturn(transaction);
        var result = transactionController.voidTransaction(new HashMap<>());
        assertThat(result).isEqualTo(ResponseEntity.ok(transaction));
    }

    @Test
    void testCancelTransaction() {
        Result<Transaction> transaction = Mockito.mock(Result.class);
        when(transactionService.cancelTransactionById(any())).thenReturn(transaction);
        var result = transactionController.cancelTransaction(new HashMap<>());
        assertThat(result).isEqualTo(ResponseEntity.ok(transaction));
    }

    @Test
    void testNullGetTransactionByBraintree() {
        when(transactionService.getTransactionByBraintree(any())).thenReturn(null);
        var result = transactionController.getTransactionByBraintree(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullGetTransactionsBySearchRequest() {
        when(transactionService.getTransactionsBySearchRequest()).thenReturn(null);
        var result = transactionController.getTransactionsBySearchRequest();
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullRefundTransactionByBraintree() {
        when(transactionService.refundTransactionByBraintree(any())).thenReturn(null);
        var result = transactionController.refundTransactionByBraintree(new HashMap<>());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullVoidTransaction() {
        when(transactionService.voidTransactionById(any())).thenReturn(null);
        var result = transactionController.voidTransaction(new HashMap<>());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullCancelTransaction() {
        when(transactionService.cancelTransactionById(any())).thenReturn(null);
        var result = transactionController.cancelTransaction(new HashMap<>());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }
}
