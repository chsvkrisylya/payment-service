package habittracker.paymentservice.integration.controller;

import com.braintreegateway.Subscription;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import habittracker.paymentservice.service.SubscriptionService;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
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
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.math.BigDecimal;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SubscriptionControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;

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
    void testGetAllSubscriptions() throws Exception {
        List<SubscriptionInfoDTO> mockSubscriptions = List.of(
                new SubscriptionInfoDTO(
                        "1",
                        "Test Description",
                        "Merchant123",
                        "PlanA",
                        Subscription.Status.ACTIVE,
                        List.of(), // пустой список транзакций
                        BigDecimal.valueOf(100.0),
                        "Token123",
                        12,
                        "2023-01-01",
                        "2023-01-01",
                        1,
                        "2023-01-01",
                        "2023-01-01",
                        "2023-01-01",
                        "2023-01-01"
                )
        );

        when(subscriptionService.searchAll()).thenReturn(mockSubscriptions);

        mockMvc.perform(get("/api/subscription/search/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Test Description"))
                .andExpect(jsonPath("$[0].merchantAccountId").value("Merchant123"))
                .andExpect(jsonPath("$[0].planId").value("PlanA"))
                .andExpect(jsonPath("$[0].status").value(Subscription.Status.ACTIVE.name()))
                // Проверяем строковое имя перечисления
                .andExpect(jsonPath("$[0].price").value(100.0))
                .andExpect(jsonPath("$[0].paymentMethodToken").value("Token123"))
                .andExpect(jsonPath("$[0].numberOfBillingCycles").value(12))
                .andExpect(jsonPath("$[0].nextBillingDate").value("2023-01-01"))
                .andExpect(jsonPath("$[0].firstBillingDate").value("2023-01-01"))
                .andExpect(jsonPath("$[0].currentBillingCycle").value(1))
                .andExpect(jsonPath("$[0].createdAt").value("2023-01-01"))
                .andExpect(jsonPath("$[0].updatedAt").value("2023-01-01"))
                .andExpect(jsonPath("$[0].billingPeriodStartDate").value("2023-01-01"))
                .andExpect(jsonPath("$[0].billingPeriodEndDate").value("2023-01-01"));
    }
}