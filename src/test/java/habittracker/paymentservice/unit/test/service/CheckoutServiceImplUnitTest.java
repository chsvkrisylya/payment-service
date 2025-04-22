package habittracker.paymentservice.unit.test.service;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ClientTokenGateway;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionGateway;
import com.braintreegateway.Result;
import com.braintreegateway.ValidationError;
import com.braintreegateway.ValidationErrors;
import com.braintreegateway.ValidationErrorCode;
import com.braintreegateway.TransactionRequest;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.service.CheckoutServiceImpl;
import habittracker.paymentservice.service.util.NumFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplUnitTest {

    // Моки для первого теста (NumFormatter)
    @Mock
    private NumFormatter numFormatter;

    // Моки для второго теста (BraintreeGateway и ClientTokenGateway)
    @Mock
    private BraintreeGateway mockBraintreeGateway;

    @Mock
    private ClientTokenGateway mockClientTokenGateway;

    @Mock
    private TransactionGateway mockTransactionGateway;

    @Mock
    private Result<Transaction> expectedResult;

    // Сервис, который тестируем
    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    @BeforeEach
    void setUp() {
        BraintreeData.setGateway(mockBraintreeGateway);
    }

    // Тест 1: Проверка создания TransactionRequest с базовыми параметрами
    @Test
    @DisplayName("Проверка создания TransactionRequest с базовыми параметрами")
    void testGetNewTransactionRequest() {
        // Тестовые данные
        String amount = "10.00";
        String paymentMethodNonce = "fakeNonce";
        BigDecimal expectedAmount = new BigDecimal(amount);

        // Настройка поведения мока
        when(numFormatter.stringToNum(amount, BigDecimal.class)).thenReturn(Optional.of(expectedAmount));

        //Вызов тестируемого метода
        checkoutService.getNewTransactionRequest(amount, paymentMethodNonce);

        // Проверки
        verify(numFormatter).stringToNum(amount, BigDecimal.class);
    }

    // Тест 2: Проверка на ошибку NumberFormatException при некорректном формате суммы
    @Test
    @DisplayName("Проверка на ошибку NumberFormatException при некорректном формате суммы")
    void testGetNewTransactionRequestWithInvalidAmount() {
        // Некорректная сумма
        String invalidAmount = "invalid";
        String paymentMethodNonce = "fakeNonce";

        when(numFormatter.stringToNum(invalidAmount, BigDecimal.class)).thenReturn(Optional.empty());

        // Проверка выброса исключения
        assertThatThrownBy(() -> checkoutService.getNewTransactionRequest(invalidAmount, paymentMethodNonce))
                .isInstanceOf(NumberFormatException.class)
                .hasMessage("Некорректный формат суммы: " + invalidAmount);
    }

    // Тест 3: Проверка генерации нового клиентского токена
    @Test
    @DisplayName("Проверка генерации нового клиентского токена")
    void testGetNewClientToken() {
        BraintreeData.setGateway(mockBraintreeGateway);
        when(mockBraintreeGateway.clientToken()).thenReturn(mockClientTokenGateway);

        String expectedToken = "test-token";
        when(mockClientTokenGateway.generate()).thenReturn(expectedToken);

        String actualToken = checkoutService.getNewClientToken();
        assertThat(actualToken)
                .as("The generated client token should match the expected value")
                .isEqualTo(expectedToken)
                .as("Client token should not be null")
                .isNotNull();
    }

    // Тест 4: Проверка списка успешных статусов транзакции
    @Test
    @DisplayName("Проверка списка успешных статусов транзакции")
    void testGetTransactionSuccessStatuses() {
        Transaction.Status[] statuses = checkoutService.getTransactionSuccessStatuses();
        //There must be 7 status values and must not be null
        assertThat(statuses).hasSize(7).isNotNull();
        //Contain AUTHORIZED status
        assertThat(Arrays.asList(statuses)).contains(Transaction.Status.AUTHORIZED);
        //Contain AUTHORIZING status
        assertThat(Arrays.asList(statuses)).contains(Transaction.Status.AUTHORIZING);
        //Contain SETTLED status
        assertThat(Arrays.asList(statuses)).contains(Transaction.Status.SETTLED);
        //Contain SETTLEMENT_CONFIRMED status
        assertThat(Arrays.asList(statuses)).contains(Transaction.Status.SETTLEMENT_CONFIRMED);
        //Contain SETTLEMENT_PENDING status
        assertThat(Arrays.asList(statuses)).contains(Transaction.Status.SETTLEMENT_PENDING);
        //Contain SETTLING status
        assertThat(Arrays.asList(statuses)).contains(Transaction.Status.SETTLING);
        //Contain SUBMITTED_FOR_SETTLEMENT status
        assertThat(Arrays.asList(statuses)).contains(Transaction.Status.SUBMITTED_FOR_SETTLEMENT);
    }

    @Test
    @DisplayName("Проверка списка возвращаемых ошибок")
    void testGetValidationErrorsShouldReturnErrors() {
        var error1 = new ValidationError("amount",
                ValidationErrorCode.SUBSCRIPTION_MODIFICATION_AMOUNT_CANNOT_BE_BLANK,
                "Amount cannot be blank");
        var error2 = new ValidationError("id",
                ValidationErrorCode.CUSTOMER_ID_IS_TOO_LONG,
                "Customer ID is too long");
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.addError(error1);
        validationErrors.addError(error2);
        Result<Transaction> result = new Result<>(validationErrors);

        String errors = checkoutService.getValidationErrors(result);
        assertThat(errors)
                .contains("Error: SUBSCRIPTION_MODIFICATION_AMOUNT_CANNOT_BE_BLANK: Amount cannot be blank")
                .contains("Error: CUSTOMER_ID_IS_TOO_LONG: Customer ID is too long");
    }

    @Test
    @DisplayName("Проверка создания успешной транзакции")
    void testGetTransactionSaleSuccess() {
        TransactionRequest request = new TransactionRequest();
        when(mockBraintreeGateway.transaction()).thenReturn(mockTransactionGateway);
        when(mockTransactionGateway.sale(request)).thenReturn(expectedResult);

        Result<Transaction> result = checkoutService.getTransactionSale(request);

        assertThat(result).isSameAs(expectedResult).isNotNull();

        verify(mockTransactionGateway, times(1)).sale(request);
    }
}