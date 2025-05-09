package habittracker.paymentservice.integration.controller;

import com.braintreegateway.Result;
import com.braintreegateway.Subscription;
import com.braintreegateway.SubscriptionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import habittracker.paymentservice.model.dto.SubscriptionRequestDTO;
import habittracker.paymentservice.service.SubscriptionService;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        String dotenvPath = System.getProperty("user.dir");

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
                        List.of(),
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

    @Test
    void testGetSubscriptionById() throws Exception {
        Subscription mockSubscription = Mockito.mock(Subscription.class);
        when(subscriptionService.findSubscriptionById("1")).thenReturn(mockSubscription);

        mockMvc.perform(get("/api/subscription/search/id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"1\""))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDefaultSubscriptionRequest() throws Exception {
        SubscriptionRequest mockRequest = new SubscriptionRequest();
        when(subscriptionService.getDefaultSubscriptionRequest("nonce123")).thenReturn(mockRequest);

        mockMvc.perform(get("/api/subscription/request/default")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"nonce123\""))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateSubscriptionRequest() throws Exception {
        SubscriptionRequestDTO requestDTO = new SubscriptionRequestDTO(
                "Test Subscription",
                "100.00",
                "nonce123",
                12,
                false,
                0,
                Subscription.DurationUnit.MONTH
        );

        SubscriptionRequest mockRequest = Mockito.mock(SubscriptionRequest.class);
        when(subscriptionService.createSubscriptionRequest(requestDTO)).thenReturn(mockRequest);

        mockMvc.perform(get("/api/subscription/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateSubscription() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest();
        Result<Subscription> mockResult = new Result<>();
        when(subscriptionService.createSubscription(request)).thenReturn(mockResult);

        mockMvc.perform(post("/api/subscription/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateDefaultSubscription() throws Exception {
        Result<Subscription> mockResult = new Result<>();
        when(subscriptionService.createDefaultSubscription("nonce123")).thenReturn(mockResult);

        mockMvc.perform(post("/api/subscription/create/default")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"nonce123\""))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelSubscription() throws Exception {
        Result<Subscription> mockResult = new Result<>();
        when(subscriptionService.cancelSubscription("1")).thenReturn(mockResult);

        mockMvc.perform(post("/api/subscription/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteSubscription() throws Exception {
        Result<Subscription> mockResult = new Result<>();
        when(subscriptionService.deleteSubscription("customer123", "1")).thenReturn(mockResult);

        mockMvc.perform(post("/api/subscription/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(Map.of("customerId", "customer123", "id", "1"))))
                .andExpect(status().isOk());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}