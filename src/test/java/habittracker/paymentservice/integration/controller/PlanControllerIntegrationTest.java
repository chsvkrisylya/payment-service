package habittracker.paymentservice.integration.controller;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Plan;
import com.braintreegateway.PlanGateway;
import com.braintreegateway.PlanRequest;
import com.braintreegateway.Result;
import com.braintreegateway.exceptions.NotFoundException;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import habittracker.paymentservice.service.PlanService;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PlanControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("TestDB")
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
    private BraintreeGateway braintreeGateway;

    @MockBean
    private PlanGateway planGateway;

    @MockBean
    private Plan plan;

    @Autowired
    private PlanService planService;

    @BeforeAll
    static void localEnv() {
        String dotenvPath = System.getProperty("user.dir");

        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env.local")
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    @BeforeEach
    void setUp() {

        when(braintreeGateway.plan()).thenReturn(planGateway);

        when(plan.getId()).thenReturn("testId");
        when(plan.getName()).thenReturn("Basic");
        when(plan.getCurrencyIsoCode()).thenReturn("USD");
        when(plan.getPrice()).thenReturn(new BigDecimal("9.99"));

        // Сохраняем мок в статическое поле, чтобы он использовался в сервисах
        BraintreeData.setGateway(braintreeGateway);
    }

    @Test
    void getAllPlansTest() throws Exception {

        List<Plan> plans = List.of(plan);

        when(planGateway.all()).thenReturn(plans);

        mockMvc.perform(get("/api/plan/search/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.size()").value(1),
                        jsonPath("$[0].id").value("testId"),
                        jsonPath("$[0].name").value("Basic")
                );
    }

    @Test
    void getPlanByIdIfExistTest() throws Exception {

        when(planGateway.find("\"testId\"")).thenReturn(plan);

        mockMvc.perform(get("/api/plan/search/id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"testId\""))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value("testId"),
                        jsonPath("$.name").value("Basic")
                );
    }

    @Test
    void getPlanByIdIfNotExistTest() throws Exception {

        when(planGateway.find("\"notFoundId\"")).thenThrow(new NotFoundException("Plan not found"));

        mockMvc.perform(get("/api/plan/search/id")
                        .content("\"notFoundId\""))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPlanByNameIfExistTest() throws Exception {

        Stream<Plan> mockStream = getPlanByNameMockStream();

        // Мокируем findFirst, чтобы вернуть Optional с конкретным объектом
        when(mockStream.findFirst()).thenReturn(Optional.of(plan));

        mockMvc.perform(get("/api/plan/search/name")
                        .content("\"Basic\""))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value("testId"),
                        jsonPath("$.name").value("Basic")
                );
    }

    @Test
    void getPlanByNameIfNotExistTest() throws Exception {

        Stream<Plan> mockStream = getPlanByNameMockStream();

        // Мокируем findFirst, чтобы вернуть Optional с конкретным объектом
        when(mockStream.findFirst()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/plan/search/name")
                        .content("\"notFound\""))
                .andExpect(status().isNotFound());
    }

    private Stream<Plan> getPlanByNameMockStream() {
        // Создаем мок для списка
        List<Plan> mockList = mock(List.class);
        when(planService.getAllPlans()).thenReturn(mockList);

        // Мокируем поведение метода stream(), чтобы он возвращал наш мокированный Stream
        Stream<Plan> mockStream = mock(Stream.class);
        when(mockList.stream()).thenReturn(mockStream);

        // Мокируем filter, чтобы вернуть тот же поток
        when(mockStream.filter(any())).thenReturn(mockStream);
        return mockStream;
    }

    @Test
    void createPlanTest() throws Exception {
        PlanRequest request = new PlanRequest()
                .id("newPlanId")
                .name("Premium")
                .price(new BigDecimal("30.00"))
                .currencyIsoCode("USD")
                .numberOfBillingCycles(12)
                .billingFrequency(1);

        Plan newPlan = mock(Plan.class);

        when(newPlan.getId()).thenReturn("newPlanId");
        when(newPlan.getName()).thenReturn("Premium");
        when(newPlan.getCurrencyIsoCode()).thenReturn("USD");
        when(newPlan.getPrice()).thenReturn(new BigDecimal("30.00"));

        when(planGateway.create(any(PlanRequest.class))).thenReturn(new Result<>(newPlan));

        mockMvc.perform(post("/api/plan/create")
                        .content(new ObjectMapper().writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value("newPlanId"),
                        jsonPath("$.name").value("Premium")
                );
    }

    @Test
    void createDefaultPlanTest() throws Exception {

        when(planGateway.create(any(PlanRequest.class))).thenReturn(new Result<>(plan));

        mockMvc.perform(post("/api/plan/create/default")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value("Basic"),
                        jsonPath("$.currencyIsoCode").value("USD"),
                        jsonPath("$.price").value("9.99")
                );
    }

    @Test
    void getPlanRequestTest() throws Exception {

        PlanRequestDTO requestDTO = new PlanRequestDTO(
                "Test", new BigDecimal("9.99"), "USD", 1,
                1, false, 1, Plan.DurationUnit.MONTH
        );

        mockMvc.perform(get("/api/plan/request")
                        .content(new ObjectMapper().writeValueAsString(requestDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getDefaultPlanRequestTest() throws Exception {

        mockMvc.perform(get("/api/plan/request/default")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
