package habittracker.paymentservice.integration;


import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Transaction;
import com.braintreegateway.Result;
import com.braintreegateway.ValidationError;
import com.braintreegateway.ValidationErrors;
import com.braintreegateway.ValidationErrorCode;
import com.braintreegateway.ClientTokenGateway;
import com.braintreegateway.Transaction.Status;
import com.braintreegateway.TransactionGateway;
import com.braintreegateway.TransactionRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.service.CheckoutServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;


import java.io.File;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest
@Transactional
class CheckoutServiceImplIT {
    private static WireMockServer wireMockServer;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private CheckoutServiceImpl checkoutService;

    //Мок
    @Mock
    private ClientTokenGateway clientTokenGateway;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.auth-service-db.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.auth-service-db.username", postgresContainer::getUsername);
        registry.add("spring.datasource.auth-service-db.password", postgresContainer::getPassword);
    }

    @BeforeEach
    public void setUp() {
        // Инициализация мока BraintreeData.gateway для теста
        BraintreeData.gateway = mock(BraintreeGateway.class);
        when(BraintreeData.gateway.clientToken()).thenReturn(clientTokenGateway);
    }

    @BeforeAll
    static void loadEnv() {
        String wiremockHost = System.getProperty("WIREMOCK_HOST", "localhost");
        int wiremockPort = Integer.parseInt(System.getProperty("WIREMOCK_PORT", "8080"));
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(wiremockPort));
        wireMockServer.start();
        WireMock.configureFor(wiremockHost, wiremockPort);
        String dotenvPath = new File(System.getProperty("user.dir")).getPath();
        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env.local")
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    @AfterAll
    static void teardown() {
        wireMockServer.stop();
    }

    @Test
    void testGetNewClientToken() {
        String expectedToken = "testClientToken";
        when(BraintreeData.gateway.clientToken().generate()).thenReturn(expectedToken);
        String result = checkoutService.getNewClientToken();
        assertEquals(expectedToken, result);
        verify(clientTokenGateway, times(1)).generate();
    }

    @Test
    void testGetNewTransactionRequestValid() {

        // Входные данные
        String amount = "100.00";
        String paymentMethodNonce = "nonce123";

        // Вызов метода
        TransactionRequest transactionRequest = checkoutService.getNewTransactionRequest(amount, paymentMethodNonce);

        // Проверка
        assertNotNull(transactionRequest, "TransactionRequest не должен быть null");

        // Проверка значения amount через XML
        String xml = transactionRequest.toXML();
        assertTrue(xml.contains("<amount>100.00</amount>"), "Поле 'amount' должно быть установлено");

        // Проверка значения paymentMethodNonce через XML
        assertTrue(xml.contains("<paymentMethodNonce>nonce123</paymentMethodNonce>"),
                "Поле 'paymentMethodNonce' должно быть установлено");

        // Проверка опции submitForSettlement
        assertTrue(xml.contains("<submitForSettlement>true</submitForSettlement>"),
                "Опция 'submitForSettlement' должна быть true");
    }

    @Test
    void testGetNewTransactionRequestInvalid() {
        // Входные данные
        String invalidAmount = "invalid_amount"; // Некорректное значение
        String paymentMethodNonce = "nonce123";

        // Проверка, что вызывается исключение
        assertThrows(NumberFormatException.class, () -> {
            // Вызов метода с некорректным значением
            checkoutService.getNewTransactionRequest(invalidAmount, paymentMethodNonce);
        }, "Ожидалось исключение NumberFormatException для некорректного значения 'amount'");
    }

    @Test
    void testGetTransactionSale() {
        TransactionRequest request = mock((TransactionRequest.class));
        Result<Transaction> result = mock(Result.class);

        // Мокаем успешный результат транзакции
        Transaction testTransaction = mock(Transaction.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.getTarget()).thenReturn(testTransaction);

        TransactionGateway transactionGateway = mock(TransactionGateway.class);
        when(BraintreeData.gateway.transaction()).thenReturn(transactionGateway);
        when(transactionGateway.sale(request)).thenReturn(result);

        Result<Transaction> transactionResult = checkoutService.getTransactionSale(request);

        // Проверяем, что метод вернул успешный результат
        assertNotNull(transactionResult);
        assertTrue(transactionResult.isSuccess());
        assertEquals(testTransaction, transactionResult.getTarget());
    }

    @Test
    void testGetTransactionSuccess() {
        Status[] expectedStatuses = new Status[]{
                Transaction.Status.AUTHORIZED,
                Transaction.Status.AUTHORIZING,
                Transaction.Status.SETTLED,
                Transaction.Status.SETTLEMENT_CONFIRMED,
                Transaction.Status.SETTLEMENT_PENDING,
                Transaction.Status.SETTLING,
                Transaction.Status.SUBMITTED_FOR_SETTLEMENT
        };
        Status[] actualStatuses = checkoutService.getTransactionSuccessStatuses();
        assertNotNull(actualStatuses, "Массив статусов не должен быть null");
        assertArrayEquals(expectedStatuses, actualStatuses, "Массив статусов должен быть корректным");
    }

    @Test
    void testGetValidationErrors() {
        // Создаем фиктивные ошибки валидации
        ValidationError error1 = new ValidationError(
                "amount",
                ValidationErrorCode.CREDIT_CARD_NUMBER_IS_INVALID,
                "The credit card number is invalid"
        );
        ValidationError error2 = new ValidationError(
                "paymentMethodNonce",
                ValidationErrorCode.CREDIT_CARD_CVV_IS_INVALID,
                "The CVV is invalid"
        );

        // Имитация объекта ValidationErrors с этими ошибками
        ValidationErrors mockValidationErrors = mock(ValidationErrors.class);
        when(mockValidationErrors.getAllDeepValidationErrors()).thenReturn(List.of(error1, error2));

        // Имитация объекта Result<Transaction>, который возвращает mockValidationErrors
        Result<Transaction> mockResult = mock(Result.class);
        when(mockResult.getErrors()).thenReturn(mockValidationErrors);

        // Формируем ожидаемый результат
        String expectedErrorString =
                "Error: " + ValidationErrorCode.CREDIT_CARD_NUMBER_IS_INVALID
                        + ": The credit card number is invalid\n"
                        + "Error: " + ValidationErrorCode.CREDIT_CARD_CVV_IS_INVALID + ": The CVV is invalid\n";

        String actualErrorString = checkoutService.getValidationErrors(mockResult);

        assertNotNull(actualErrorString, "Строка ошибок не должна быть null");
        assertEquals(expectedErrorString, actualErrorString, "Строка ошибок должна быть правильно сформирована");
    }
}