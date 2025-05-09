package habittracker.paymentservice.integration.service;

import com.braintreegateway.Environment;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionGateway;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.TransactionInfoDTO;
import habittracker.paymentservice.service.TransactionServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class TransactionServiceImplIT {

    private static WireMockServer wireMockServer;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private TransactionServiceImpl transactionService;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.auth-service-db.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.auth-service-db.username", postgresContainer::getUsername);
        registry.add("spring.datasource.auth-service-db.password", postgresContainer::getPassword);
    }

    @BeforeEach
    void setUp() {

        BraintreeGateway mockGateway = mock(BraintreeGateway.class);
        TransactionGateway mockTransactionGateway = mock(TransactionGateway.class);
        when(mockGateway.transaction()).thenReturn(mockTransactionGateway);
        BraintreeData.setGateway(mockGateway);
    }

    @BeforeAll
    static void loadEnv() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        String dotenvPath = System.getProperty("user.dir");
        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env.local")
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        System.setProperty("WIREMOCK_PORT", String.valueOf(wireMockServer.port()));
    }

    @AfterAll
    static void teardown() {
        wireMockServer.stop();
    }

    @Test
    void testGetTransactionByBraintree() {
        Transaction mockTransaction = mock(Transaction.class);
        when(BraintreeData.gateway.transaction().find("123")).thenReturn(mockTransaction);

        Transaction result = transactionService.getTransactionByBraintree("123");

        assertNotNull(result, "Transaction should not be null");
        verify(BraintreeData.gateway.transaction()).find("123");
    }

    @Test
    void testRefundTransactionByBraintree() {
        Result<Transaction> mockResult = mock(Result.class);
        when(BraintreeData.gateway.transaction().refund("123")).thenReturn(mockResult);

        Result<Transaction> result = transactionService.refundTransactionByBraintree("123");

        assertNotNull(result, "Refund result should not be null");
        verify(BraintreeData.gateway.transaction()).refund("123");
    }

    @Test
    void testGetTransactionsBySearchRequest() {
        // 1. Настройка mock Gateway с динамическим портом WireMock
        BraintreeGateway mockGateway = new BraintreeGateway(
                new Environment(
                        "http://localhost:" + wireMockServer.port(),
                        "http://localhost:" + wireMockServer.port(),
                        new String[]{},
                        "TEST_ENV"
                ),
                "integration_merchant_id",
                "fake_public_key",
                "fake_private_key"
        );
        BraintreeData.setGateway(mockGateway);

        // 2. Настройка заглушек для аутентификации
        stubFor(post(urlPathMatching("/merchants/.*/transactions/advanced_search_ids"))
                .withHeader("Authorization", containing("Basic"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(
                                "<search-results>"
                                        + "<page-size>50</page-size>"
                                        + "<ids>"
                                        + "<item>txn_123</item>"
                                        + "</ids>"
                                        + "</search-results>"
                        )));

        stubFor(post(urlPathMatching("/merchants/.*/transactions/advanced_search"))
                .withHeader("Authorization", containing("Basic"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(
                                "<credit-card-transactions>"
                                        + "<transaction>"
                                        + "<id>txn_123</id>"
                                        + "<amount>100</amount>"
                                        + "<status>SETTLED</status>"
                                        + "</transaction>"
                                        + "</credit-card-transactions>"
                        )));

        // 3. Вызов тестируемого метода
        List<TransactionInfoDTO> transactions = transactionService.getTransactionsBySearchRequest();

        // 4. Проверки
        assertNotNull(transactions, "Transactions list should not be null");
        assertEquals(1, transactions.size(), "Transactions list size should be 1");

        TransactionInfoDTO transactionInfo = transactions.get(0);
        assertEquals("txn_123", transactionInfo.getId(), "Transaction ID should match");
        assertEquals(BigDecimal.valueOf(100), transactionInfo.getAmount(), "Transaction amount should match");
        assertEquals(Transaction.Status.SETTLED, transactionInfo.getStatus(), "Transaction status should match");
    }

    @Test
    void testVoidTransactionById() {
        Result<Transaction> mockResult = mock(Result.class);
        when(BraintreeData.gateway.transaction().voidTransaction("123")).thenReturn(mockResult);

        Result<Transaction> result = transactionService.voidTransactionById("123");

        assertNotNull(result, "Void transaction result should not be null");
        verify(BraintreeData.gateway.transaction()).voidTransaction("123");
    }

    @Test
    void testCancelTransactionById() {
        Result<Transaction> mockResult = mock(Result.class);
        when(BraintreeData.gateway.transaction().cancelRelease("123")).thenReturn(mockResult);

        Result<Transaction> result = transactionService.cancelTransactionById("123");

        assertNotNull(result, "Cancel transaction result should not be null");
        verify(BraintreeData.gateway.transaction()).cancelRelease("123");
    }

}
