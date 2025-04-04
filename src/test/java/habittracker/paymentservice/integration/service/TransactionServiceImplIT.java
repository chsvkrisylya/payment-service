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

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
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
        String wiremockHost = System.getProperty("WIREMOCK_HOST", "localhost");
        int wiremockPort = Integer.parseInt(System.getProperty("WIREMOCK_PORT", "8081"));
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(wiremockPort));
        wireMockServer.start();
        WireMock.configureFor(wiremockHost, wiremockPort);
        String dotenvPath = new File(System.getProperty("user.dir")).getParent();
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
    void testGetTransactionByBraintree() {
        Transaction mockTransaction = mock(Transaction.class);
        when(BraintreeData.getGateway().transaction().find("123")).thenReturn(mockTransaction);

        Transaction result = transactionService.getTransactionByBraintree("123");

        assertNotNull(result, "Transaction should not be null");
        verify(BraintreeData.getGateway().transaction()).find("123");
    }

    @Test
    void testRefundTransactionByBraintree() {
        Result<Transaction> mockResult = mock(Result.class);
        when(BraintreeData.getGateway().transaction().refund("123")).thenReturn(mockResult);

        Result<Transaction> result = transactionService.refundTransactionByBraintree("123");

        assertNotNull(result, "Refund result should not be null");
        verify(BraintreeData.getGateway().transaction()).refund("123");
    }

    @Test
    void testGetTransactionsBySearchRequest() {
        BraintreeGateway mockGateway = new BraintreeGateway(
                new Environment(
                        "http://localhost:8081", // baseURL
                        "http://localhost:8081", // authURL
                        new String[] {},         // Пустой массив сертификатов
                        "TEST_ENV"               // Имя окружения (можно любое)
                ),
                "fake_merchant_id",
                "fake_public_key",
                "fake_private_key"
        );
        BraintreeData.setGateway(mockGateway);
        Transaction mockTransaction = mock(Transaction.class);
        LocalDate timestamp = LocalDate.now();
        Calendar calendar = Calendar.getInstance();
        calendar.set(timestamp.getYear(), timestamp.getMonthValue() - 1, timestamp.getDayOfMonth());

        when(mockTransaction.getId()).thenReturn("txn_123");
        when(mockTransaction.getCreatedAt()).thenReturn(calendar);

        stubFor(post(urlPathMatching("/merchants/.*/transactions/advanced_search_ids"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/xml")
                        .withBody(
                                "<search-results>"
                                        + "    <page-size>50</page-size>"
                                        + "    <ids>"
                                        + "        <item>txn_123</item>"
                                        + "    </ids>"
                                        + "</search-results>"
                        )));

        stubFor(post(urlPathMatching("/merchants/.*/transactions/advanced_search"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/xml")
                        .withBody(
                                "<credit-card-transactions>"
                                        + "    <transaction>"
                                        + "        <id>txn_123</id>"
                                        + "        <amount>100</amount>"
                                        + "        <status>SETTLED</status>"
                                        + "    </transaction>"
                                        + "</credit-card-transactions>"
                        )));

        List<TransactionInfoDTO> transactions = transactionService.getTransactionsBySearchRequest();

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
        when(BraintreeData.getGateway().transaction().voidTransaction("123")).thenReturn(mockResult);

        Result<Transaction> result = transactionService.voidTransactionById("123");

        assertNotNull(result, "Void transaction result should not be null");
        verify(BraintreeData.getGateway().transaction()).voidTransaction("123");
    }

    @Test
    void testCancelTransactionById() {
        Result<Transaction> mockResult = mock(Result.class);
        when(BraintreeData.getGateway().transaction().cancelRelease("123")).thenReturn(mockResult);

        Result<Transaction> result = transactionService.cancelTransactionById("123");

        assertNotNull(result, "Cancel transaction result should not be null");
        verify(BraintreeData.getGateway().transaction()).cancelRelease("123");
    }

}
