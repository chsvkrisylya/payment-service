package habittracker.paymentservice.integration.service;

import com.braintreegateway.PlanRequest;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Plan;
import com.braintreegateway.PlanGateway;
import com.braintreegateway.Result;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.service.PlanService;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("local")
class PlanServiceImplIT {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("password");

    @Autowired
    private PlanService planService;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.auth-service-db.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.auth-service-db.username", postgresContainer::getUsername);
        registry.add("spring.datasource.auth-service-db.password", postgresContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Мокируем Braintree Gateway
        BraintreeGateway mockGateway = mock(BraintreeGateway.class);
        PlanGateway mockPlanGateway = mock(PlanGateway.class);  // Мокируем PlanGateway

        // Настроим метод plan() так, чтобы он возвращал mockPlanGateway
        when(mockGateway.plan()).thenReturn(mockPlanGateway);

        // Создаем мок для метода create() в PlanGateway
        Plan mockPlan = mock(Plan.class);
        // Устанавливаем значение id в mockPlan
        when(mockPlan.getId()).thenReturn("plan_123");
        when(mockPlan.getName()).thenReturn("Test Plan");
        when(mockPlan.getPrice()).thenReturn(new BigDecimal("19.99"));
        when(mockPlan.getCurrencyIsoCode()).thenReturn("USD");

        when(mockPlanGateway.create(any(PlanRequest.class))).thenReturn(new Result<>(mockPlan));

        // Сохраняем мок в статическое поле, чтобы он использовался в сервисах
        BraintreeData.setGateway(mockGateway);
    }

    @BeforeAll
    static void loadEnv() {
        // Загружаем переменные окружения
        String dotenvPath = System.getProperty("user.dir");
        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env.local")
                .load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    @Test
    @DisplayName("Create a plan and retrieve it successfully")
    void testCreateAndRetrievePlan() {
        // Создаем запрос на создание плана
        PlanRequest request = new PlanRequest()
                .id("plan_123")
                .name("Test Plan")
                .price(new BigDecimal("19.99"))
                .currencyIsoCode("USD");

        // Вызываем метод для создания плана
        Result<Plan> result = planService.createPlan(request);

        // Проверяем, что результат успешный
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isSuccess(), "Result should indicate success");

        // Получаем созданный план
        Plan createdPlan = result.getTarget();
        assertNotNull(createdPlan, "Created plan should not be null");
        assertEquals("plan_123", createdPlan.getId(), "Plan ID should match");
        assertEquals("Test Plan", createdPlan.getName(), "Plan name should match");
        assertEquals(BigDecimal.valueOf(19.99), createdPlan.getPrice(), "Plan price should match");
        assertEquals("USD", createdPlan.getCurrencyIsoCode(), "Plan currency should match");
    }
}






