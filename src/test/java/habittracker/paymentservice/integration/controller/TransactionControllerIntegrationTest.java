package habittracker.paymentservice.integration.controller;

import com.braintreegateway.Transaction;
import habittracker.paymentservice.model.dto.TransactionInfoDTO;
import habittracker.paymentservice.service.TransactionService;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeAll
    static void localEnv() {
        String dotenvPath = new File(System.getProperty("user.dir")).getPath();

        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env.local")
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    @Test
    @DisplayName("Получение транзакции по айди")
    void testGetTransactionById() throws Exception {
        String transactionId = "valid-transaction-id";
        Transaction mockTransaction = mock(Transaction.class);
        when(mockTransaction.getId()).thenReturn(transactionId);
        when(transactionService.getTransactionByBraintree(transactionId)).thenReturn(mockTransaction);

        mockMvc.perform(get("/api/payment/show/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId));
    }

    @Test
    @DisplayName("Получение транзакции по поисковому запросу")
    void testGetTransactionBySearchRequest() throws Exception {
        String transactionId = "valid-transaction-id";

        TransactionInfoDTO mockTransactionInfoDTO = mock(TransactionInfoDTO.class);
        when(mockTransactionInfoDTO.getId()).thenReturn(transactionId);

        List<TransactionInfoDTO> mockTransactionInfoDTOList = List.of(mockTransactionInfoDTO);

        when(transactionService.getTransactionsBySearchRequest()).thenReturn(mockTransactionInfoDTOList);

        mockMvc.perform(get("/api/payment/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(transactionId));
    }
}
