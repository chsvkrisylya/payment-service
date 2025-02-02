package habittracker.paymentservice.unit.test.controller;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import habittracker.paymentservice.controller.CheckoutController;
import habittracker.paymentservice.service.CheckoutService;
import habittracker.paymentservice.service.TransactionService;

import habittracker.paymentservice.service.util.DateFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerUnitTest {

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private CheckoutController checkoutController;

    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    @Test
    @DisplayName("Проверка перенаправления на /payment/checkouts")
    void testRedirectToCheckouts() {
        assertEquals("redirect:payment/checkouts", checkoutController.root());
    }

    @Test
    @DisplayName("Отображение формы оплаты с клиентским токеном")
    void testDisplayCheckoutForm() {
        String token = "some-token";
        when(checkoutService.getNewClientToken()).thenReturn(token);

        Model model = new ExtendedModelMap();

        String viewName = checkoutController.checkout(model);
        assertEquals("checkouts/new", viewName);

        assertEquals(token, model.getAttribute("clientToken"));

        verify(checkoutService, times(1)).getNewClientToken();
    }

    @Test
    @DisplayName("Успешная транзакция: перенаправление на детали транзакции")
    void testSuccessfulTransaction() {
        String amount = "123";
        String nonce = "postForm";
        BigDecimal bigDecimalAmount = new BigDecimal(amount);

        TransactionRequest transactionRequest = new TransactionRequest()
                .amount(bigDecimalAmount)
                .paymentMethodNonce(nonce)
                .options()
                .submitForSettlement(true)
                .done();

        when(checkoutService.getNewTransactionRequest(amount, nonce)).thenReturn(transactionRequest);
        Result<Transaction> successfulResult = mock(Result.class);
        Transaction successfulTransaction = mock(Transaction.class);

        when(successfulResult.isSuccess()).thenReturn(true);
        when(successfulResult.getTarget()).thenReturn(successfulTransaction);
        when(successfulResult.getTarget().getId()).thenReturn("12345");

        when(checkoutService.getTransactionSale(transactionRequest)).thenReturn(successfulResult);

        String result = checkoutController.postForm(amount, nonce, redirectAttributes);
        assertEquals("redirect:/payment/checkouts/12345", result);
    }

    @Test
    @DisplayName("Неуспешная транзакция: перенаправление с ID транзакции")
    void testUnsuccessfulTransaction() {
        String amount = "100";
        String nonce = "postForm";
        BigDecimal bigDecimalAmount = new BigDecimal(amount);

        TransactionRequest transactionRequest = new TransactionRequest()
                .amount(bigDecimalAmount)
                .paymentMethodNonce(nonce)
                .options()
                .submitForSettlement(true)
                .done();

        when(checkoutService.getNewTransactionRequest(amount, nonce)).thenReturn(transactionRequest);
        Result<Transaction> unSuccessfulResult = mock(Result.class);
        Transaction unSuccessfulTransaction = mock(Transaction.class);

        when(unSuccessfulResult.isSuccess()).thenReturn(false);
        when(unSuccessfulResult.getTransaction()).thenReturn(unSuccessfulTransaction);
        when(unSuccessfulResult.getTransaction().getId()).thenReturn("54321");

        when(checkoutService.getTransactionSale(transactionRequest)).thenReturn(unSuccessfulResult);

        String result = checkoutController.postForm(amount, nonce, redirectAttributes);
        assertEquals("redirect:/payment/checkouts/54321", result);
    }

    @Test
    @DisplayName("Ошибка валидации транзакции: отображение ошибки")
    void testValidationErrorTransaction() {
        String amount = "999";
        String nonce = "postForm";
        BigDecimal bigDecimalAmount = new BigDecimal(amount);

        TransactionRequest transactionRequest = new TransactionRequest()
                .amount(bigDecimalAmount)
                .paymentMethodNonce(nonce)
                .options()
                .submitForSettlement(true)
                .done();

        when(checkoutService.getNewTransactionRequest(amount, nonce)).thenReturn(transactionRequest);

        Result<Transaction> validationErrorResult = mock(Result.class);

        when(validationErrorResult.isSuccess()).thenReturn(false);
        when(validationErrorResult.getTransaction()).thenReturn(null);

        when(checkoutService.getTransactionSale(transactionRequest)).thenReturn(validationErrorResult);
        when(checkoutService.getValidationErrors(validationErrorResult)).thenReturn("some validation error");

        String result = checkoutController.postForm(amount, nonce, redirectAttributes);
        assertEquals("redirect:/payment/checkouts", result);

        assertEquals("some validation error", redirectAttributes.getFlashAttributes().get("errorDetails"));
    }

    @Test
    @DisplayName("Ошибка формата суммы: отображение соответствующей ошибки")
    void testNumberFormatException() {
        String amount = "errorAmount";
        String nonce = "postForm";

        when(checkoutService.getNewTransactionRequest(amount, nonce)).thenThrow(new NumberFormatException());

        String result = checkoutController.postForm(amount, nonce, redirectAttributes);
        assertEquals("redirect:/payment/checkouts", result);
        assertEquals("Error: 81503: Amount is an invalid format.",
                redirectAttributes.getFlashAttributes().get("errorDetails"));
    }

    @Test
    @DisplayName("Получение данных транзакции с успешным статусом")
    void getTransaction() {
        String transactionId = "12345";
        Model model = new ExtendedModelMap();

        Transaction transaction = mock(Transaction.class);
        when(transactionService.getTransactionByBraintree(transactionId)).thenReturn(transaction);

        Transaction.Status[] successStatus = {
                Transaction.Status.AUTHORIZED,
                Transaction.Status.AUTHORIZING,
                Transaction.Status.SETTLED,
                Transaction.Status.SETTLEMENT_CONFIRMED,
                Transaction.Status.SETTLEMENT_PENDING,
                Transaction.Status.SETTLING,
                Transaction.Status.SUBMITTED_FOR_SETTLEMENT
        };
        when(checkoutService.getTransactionSuccessStatuses()).thenReturn(successStatus);
        when(transaction.getStatus()).thenReturn(Transaction.Status.AUTHORIZED);

        Calendar createdAt = Calendar.getInstance();
        Calendar updatedAt = Calendar.getInstance();
        when(transaction.getCreatedAt()).thenReturn(createdAt);
        when(transaction.getUpdatedAt()).thenReturn(updatedAt);

        String viewName = checkoutController.getTransaction(transactionId, model);
        assertEquals("checkouts/show", viewName);

        assertEquals(true, model.getAttribute("isSuccess"));
        assertEquals(transaction, model.getAttribute("transaction"));
        assertEquals(DateFormatter.dateToString(createdAt.getTime()), model.getAttribute("createdAt"));
        assertEquals(DateFormatter.dateToString(updatedAt.getTime()), model.getAttribute("updatedAt"));
    }
}
