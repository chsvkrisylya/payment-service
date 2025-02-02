package habittracker.paymentservice.unit.test.service;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ClientTokenGateway;
import com.braintreegateway.Transaction;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.service.CheckoutServiceImpl;
import habittracker.paymentservice.service.util.NumFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    // Сервис, который тестируем
    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    // Тест 1: Проверка создания TransactionRequest с базовыми параметрами
    @Test
    @DisplayName("Проверка создания TransactionRequest с базовыми параметрами")
    void testGetNewTransactionRequest() {
        // Тестовые данные
        String amount = "10.00";
        String paymentMethodNonce = "fakeNonce";
        BigDecimal expectedAmount = new BigDecimal(amount);

        // Настройка поведения мока
        when(numFormatter.stringToNum(amount, BigDecimal.class)).thenReturn(expectedAmount);

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

        when(numFormatter.stringToNum(invalidAmount, BigDecimal.class)).thenThrow(new NumberFormatException());

        // Проверка выброса исключения
        assertThrows(NumberFormatException.class, () -> {
            checkoutService.getNewTransactionRequest(invalidAmount, paymentMethodNonce);
        });
    }

    // Тест 3: Проверка генерации нового клиентского токена
    @Test
    @DisplayName("Проверка генерации нового клиентского токена")
    void testGetNewClientToken() {
        BraintreeData.gateway = mockBraintreeGateway;
        when(mockBraintreeGateway.clientToken()).thenReturn(mockClientTokenGateway);

        String expectedToken = "test-token";
        when(mockClientTokenGateway.generate()).thenReturn(expectedToken);

        String actualToken = checkoutService.getNewClientToken();
        assertNotNull(actualToken, "Client token should not be null");
        assertEquals(expectedToken, actualToken, "The generated client token should match the expected value");
    }

    // Тест 4: Проверка списка успешных статусов транзакции
    @Test
    @DisplayName("Проверка списка успешных статусов транзакции")
    void testGetTransactionSuccessStatuses() {
        Transaction.Status[] statuses = checkoutService.getTransactionSuccessStatuses();

        assertNotNull(statuses, "Statuses should not be null");
        assertEquals(7, statuses.length, "There should be 7 status values");
        assertTrue(Arrays.asList(statuses).contains(Transaction.Status.AUTHORIZED), "Should contain AUTHORIZED status");
        assertTrue(Arrays.asList(statuses).contains(Transaction.Status.AUTHORIZING),
                "Should contain AUTHORIZING status");
        assertTrue(Arrays.asList(statuses).contains(Transaction.Status.SETTLED), "Should contain SETTLED status");
        assertTrue(Arrays.asList(statuses).contains(Transaction.Status.SETTLEMENT_CONFIRMED),
                "Should contain SETTLEMENT_CONFIRMED status");
        assertTrue(Arrays.asList(statuses).contains(Transaction.Status.SETTLEMENT_PENDING),
                "Should contain SETTLEMENT_PENDING status");
        assertTrue(Arrays.asList(statuses).contains(Transaction.Status.SETTLING), "Should contain SETTLING status");
        assertTrue(Arrays.asList(statuses).contains(Transaction.Status.SUBMITTED_FOR_SETTLEMENT),
                "Should contain SUBMITTED_FOR_SETTLEMENT status");
    }
}