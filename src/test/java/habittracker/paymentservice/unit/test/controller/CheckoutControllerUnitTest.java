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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(checkoutController.root()).isEqualTo("redirect:payment/checkouts");
    }

    @Test
    @DisplayName("Отображение формы оплаты с клиентским токеном")
    void testDisplayCheckoutForm() {
        String token = "some-token";
        when(checkoutService.getNewClientToken()).thenReturn(token);

        Model model = new ExtendedModelMap();
        String viewName = checkoutController.checkout(model);

        assertThat(viewName).isEqualTo("checkouts/new");
        assertThat(model.getAttribute("clientToken")).isEqualTo(token);
        assertThatCode(() -> verify(checkoutService, times(1)).getNewClientToken())
                .doesNotThrowAnyException();
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
        assertThat(result).isEqualTo("redirect:/payment/checkouts/12345");
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
        assertThat(result).isEqualTo("redirect:/payment/checkouts/54321");
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
        assertThat(result).isEqualTo("redirect:/payment/checkouts");

        String actualKey = "errorDetails";
        String expectedValue = "some validation error";
        assertThat((Map<String, String>) redirectAttributes.getFlashAttributes())
                .containsEntry(actualKey, expectedValue);
    }

    @Test
    @DisplayName("Ошибка формата суммы: отображение соответствующей ошибки")
    void testNumberFormatException() {
        String amount = "errorAmount";
        String nonce = "postForm";

        when(checkoutService.getNewTransactionRequest(amount, nonce)).thenThrow(new NumberFormatException());

        String result = checkoutController.postForm(amount, nonce, redirectAttributes);
        assertThat(result).isEqualTo("redirect:/payment/checkouts");

        String actualKey = "errorDetails";
        String expectedValue = "Error: 81503: Amount is an invalid format.";
        assertThat((Map<String, String>) redirectAttributes.getFlashAttributes())
                .containsEntry(actualKey, expectedValue);
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
        assertThat(viewName).isEqualTo("checkouts/show");
        assertThat(model.getAttribute("isSuccess")).isEqualTo(true);
        assertThat(model.getAttribute("transaction")).isEqualTo(transaction);
        assertThat(model.getAttribute("createdAt"))
                .isEqualTo(DateFormatter.dateToString(createdAt.getTime()));
        assertThat(model.getAttribute("updatedAt"))
                .isEqualTo(DateFormatter.dateToString(updatedAt.getTime()));
    }
}
