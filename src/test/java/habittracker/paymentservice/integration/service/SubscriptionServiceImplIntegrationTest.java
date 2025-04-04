package habittracker.paymentservice.integration.service;

import com.braintreegateway.CustomerRequest;
import com.braintreegateway.PlanRequest;
import com.braintreegateway.Subscription;
import com.braintreegateway.SubscriptionRequest;
import com.braintreegateway.Result;
import com.braintreegateway.Plan;
import com.braintreegateway.exceptions.NotFoundException;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import habittracker.paymentservice.model.dto.SubscriptionRequestDTO;
import habittracker.paymentservice.service.PlanService;
import habittracker.paymentservice.service.SubscriptionServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.reflect.FieldUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Testcontainers
class SubscriptionServiceImplIntegrationTest {

    @Autowired
    private SubscriptionServiceImpl subscriptionService;

    @Autowired
    private PlanService planService;

    private static final String TEST_PLAN_ID = "test-plan-1";
    private static final String TEST_NONCE = "fake-valid-nonce";
    private static final String TEST_PLAN_NAME = "Test Plan";
    private static final String TEST_PRICE = "9.99";
    private static final int TEST_CYCLES = 1;
    private static final String NON_EXISTING_ID = "non_existing_id_123xyz";

    private static final String DEFAULT_PLAN_NAME = "Default";
    private static final String DEFAULT_PRICE = "10.00";
    private static final boolean DEFAULT_TRIAL_PERIOD = false;
    private static final boolean DEFAULT_START_IMMEDIATELY = true;
    private static final boolean DEFAULT_REVERT_SUBSCRIPTION_ON_PRORATION_FAILURE = false;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void loadEnv() {
        String dotenvPath = new File(System.getProperty("user.dir")).getParent();
        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env.dev")
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        BraintreeData.getGateway().customer()
                .create(new CustomerRequest().id("TestCustomer")
                        .paymentMethodNonce(TEST_NONCE).creditCard().token("Valid-token").done());

        BraintreeData.getGateway().plan()
                .create(new PlanRequest().id(TEST_PLAN_ID)
                        .name(TEST_PLAN_NAME).price(new BigDecimal(TEST_PRICE)).numberOfBillingCycles(TEST_CYCLES));
    }

    @AfterAll
    static void tearDown() {
        BraintreeData.getGateway().customer().delete("TestCustomer");
    }

    @Nested
    class CreateSubscriptionRequestTest {
        private SubscriptionRequestDTO requestDTO;
        private SubscriptionRequest expectedRequest;

        @BeforeEach
        void setUp() {
            requestDTO = new SubscriptionRequestDTO(
                    TEST_PLAN_NAME,
                    TEST_PRICE,
                    TEST_NONCE,
                    TEST_CYCLES,
                    false,
                    7,
                    Subscription.DurationUnit.DAY
            );
            expectedRequest = new SubscriptionRequest()
                    .planId(TEST_PLAN_ID)
                    .price(new BigDecimal(TEST_PRICE))
                    .paymentMethodNonce(TEST_NONCE)
                    .numberOfBillingCycles(TEST_CYCLES)
                    .trialPeriod(false)
                    .options()
                    .startImmediately(true)
                    .revertSubscriptionOnProrationFailure(false)
                    .done();
        }

        @ParameterizedTest
        @CsvSource({
                "true", // С trialPeriod
                "false" // Без trialPeriod
        })
        void givenValidDTOWhenCreateSubscriptionRequestThenValidRequest(boolean trialPeriod) {
            requestDTO.setTrialPeriod(trialPeriod);
            expectedRequest.trialPeriod(trialPeriod);
            if (trialPeriod) {
                expectedRequest.trialDuration(7).trialDurationUnit(Subscription.DurationUnit.DAY);
            }

            SubscriptionRequest requestEntity = subscriptionService.createSubscriptionRequest(requestDTO);

            assertThat(requestEntity).usingRecursiveComparison().isEqualTo(expectedRequest);
        }

        @Test
        void givenInvalidDTOWhenCreateSubscriptionRequestThrowNotFoundException() {
            requestDTO.setName("Invalid Plan");

            assertThatThrownBy(() -> subscriptionService.createSubscriptionRequest(requestDTO))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Plan not found");
        }

        @Test
        void givenValidDTOWithInvalidPriceWhenCreateSubscriptionRequestThenValidRequestWithDefaultPrice() {
            requestDTO.setStrPrice("abc.xyz");

            SubscriptionRequest requestEntity = subscriptionService.createSubscriptionRequest(requestDTO);

            assertThat(requestEntity).usingRecursiveComparison().isEqualTo(expectedRequest);
        }

        @Test
        void createDefaultSubscriptionRequestWhenDefaultPlanExistsThenValidRequestWithDefaultNonce() {
            String planId = planService.getPlanByName(DEFAULT_PLAN_NAME)
                    .map(Plan::getId)
                    .orElseGet(() -> planService.createPlan(
                                    new PlanRequest()
                                            .name(DEFAULT_PLAN_NAME)
                                            .price(new BigDecimal(DEFAULT_PRICE))
                                            .numberOfBillingCycles(TEST_CYCLES))
                            .getTarget()
                            .getId());
            SubscriptionRequest request = subscriptionService.getDefaultSubscriptionRequest(TEST_NONCE);
            SubscriptionRequest expectedDefaultRequest = new SubscriptionRequest()
                    .planId(planId)
                    .price(new BigDecimal(DEFAULT_PRICE))
                    .paymentMethodNonce(TEST_NONCE)
                    .numberOfBillingCycles(TEST_CYCLES)
                    .trialPeriod(DEFAULT_TRIAL_PERIOD)
                    .options()
                    .startImmediately(DEFAULT_START_IMMEDIATELY)
                    .revertSubscriptionOnProrationFailure(DEFAULT_REVERT_SUBSCRIPTION_ON_PRORATION_FAILURE)
                    .done();

            assertThat(request).usingRecursiveComparison().isEqualTo(expectedDefaultRequest);
        }
    }

    @Nested
    class SubscriptionManagementTests {
        private static final AtomicInteger PRICE_COUNTER = new AtomicInteger(10);
        private Subscription testSubscription;

        @BeforeEach
        void createTestSubscription() {
            testSubscription = createSubscription();
        }

        @AfterEach
        void cancelTestSubscription() {
            subscriptionService.cancelSubscription(testSubscription.getId());
        }

        Subscription createSubscription() {
            Result<Subscription> result = subscriptionService.createSubscription(
                    new SubscriptionRequest()
                            .planId(TEST_PLAN_ID)
                            .price(new BigDecimal(PRICE_COUNTER.getAndIncrement()))
                            .paymentMethodNonce(getValidPaymentNonce())
            );
            assertTrue(result.isSuccess());
            return result.getTarget();
        }

        String getValidPaymentNonce() {
            return BraintreeData.getGateway().paymentMethodNonce()
                    .create("Valid-token")
                    .getTarget()
                    .getNonce();
        }

        SubscriptionRequest getTestSubscriptionRequest() {
            SubscriptionRequestDTO requestDTO = new SubscriptionRequestDTO(
                    TEST_PLAN_NAME,
                    TEST_PRICE,
                    getValidPaymentNonce(),
                    TEST_CYCLES,
                    false,
                    7,
                    Subscription.DurationUnit.DAY
            );

            return subscriptionService.createSubscriptionRequest(requestDTO);
        }

        @Test
        void givenValidSubscriptionRequestWhenCreateSubscriptionThenSuccess() throws IllegalAccessException {
            SubscriptionRequest request = getTestSubscriptionRequest();
            Result<Subscription> result = subscriptionService.createSubscription(request);

            Subscription subscription = result.getTarget();

            // Используется рефлексию для чтения полей у SubscriptionRequest
            String requestPlanId = (String) FieldUtils.readField(request, "planId", true);
            BigDecimal requestPrice = (BigDecimal) FieldUtils.readField(request, "price", true);
            Integer requestBillingCycles = (Integer) FieldUtils.readField(request, "numberOfBillingCycles", true);
            Boolean requestHasTrialPeriod = (Boolean) FieldUtils.readField(request, "hasTrialPeriod", true);

            assertThat(subscription.getPlanId()).isEqualTo(requestPlanId);
            assertThat(subscription.getPrice()).isEqualTo(requestPrice);
            assertThat(subscription.getPaymentMethodToken()).isNotNull();
            assertThat(subscription.getNumberOfBillingCycles()).isEqualTo(requestBillingCycles);
            assertThat(subscription.hasTrialPeriod()).isEqualTo(requestHasTrialPeriod);
        }

        @Test
        void givenValidNonceWhenCreateDefaultSubscriptionThenSuccess() {
            Result<Subscription> result = subscriptionService.createDefaultSubscription(getValidPaymentNonce());
            assertTrue(result.isSuccess());
            Subscription subscription = result.getTarget();
            assertThat(subscription.getPlanId()).isNotNull();
            assertThat(subscription.getPrice()).isEqualTo(new BigDecimal(DEFAULT_PRICE));
            assertThat(subscription.getPaymentMethodToken()).isNotNull();
            assertThat(subscription.getNumberOfBillingCycles()).isEqualTo(TEST_CYCLES);
            assertFalse(subscription.hasTrialPeriod());
        }

        @Test
        void givenActiveSubscriptionsWhenSearchAllThenReturnList() {
            List<SubscriptionInfoDTO> list = subscriptionService.searchAll();

            System.out.println("Subscriptions found:");
            list.forEach(sub -> System.out.println("Subscription ID: " + sub.getId() + ", Status: " + sub.getStatus()));

            // Проверяем, что тестовая подписка найдена
            SubscriptionInfoDTO foundSubscription = list.stream()
                    .filter(sub -> sub.getId().equals(testSubscription.getId()))
                    .findFirst()
                    .orElse(null);

            assertThat(foundSubscription).isNotNull();
            assertThat(foundSubscription.getId()).isEqualTo(testSubscription.getId());

        }

        @Test
        void givenExistingSubscriptionIdWhenFindByIdThenReturnSubscription() {
            Subscription subscription = subscriptionService.findSubscriptionById(testSubscription.getId());

            assertThat(subscription).usingRecursiveComparison().isEqualTo(testSubscription);
        }

        @Test
        void givenValidUpdateRequestWhenUpdateSubscriptionThenSuccess() {
            SubscriptionRequest request = new SubscriptionRequest().price(new BigDecimal("15.00"));

            Result<Subscription> result = subscriptionService.updateSubscription(testSubscription.getId(), request);

            assertTrue(result.isSuccess());
            assertEquals(new BigDecimal("15.00"), result.getTarget().getPrice());
            assertThat(result.getTarget()).usingRecursiveComparison().ignoringFields(
                    "price", "nextBillAmount", "nextBillingPeriodAmount", "statusHistory", "updatedAt")
                    .isEqualTo(testSubscription);
        }

        @Test
        void givenInValidUpdateRequestWhenUpdateSubscriptionThenNotSuccess() {
            SubscriptionRequest request = new SubscriptionRequest()
                    .paymentMethodToken("invalid_token")
                    .planId("invalid_merchant")
                    .merchantAccountId("invalid_merchant")
                    .numberOfBillingCycles(0);

            Result<Subscription> result = subscriptionService.updateSubscription(testSubscription.getId(), request);

            assertFalse(result.isSuccess());
            assertThat(result.getMessage())
                    .contains("Payment method token is invalid")
                    .contains("Plan ID is invalid")
                    .contains("Merchant Account ID is invalid")
                    .contains("Number Of Billing Cycles must be greater than zero");
        }

        @Test
        void givenActiveSubscriptionWhenCancelThenReturnSuccess() {
            Result<Subscription> result = subscriptionService.cancelSubscription(testSubscription.getId());

            assertTrue(result.isSuccess());
            assertEquals(Subscription.Status.CANCELED, result.getTarget().getStatus());
        }

        @Test
        void givenCanceledSubscriptionWhenCancelAgainThenReturnError() {
            // Предварительно отменяем подписку
            assertTrue(subscriptionService.cancelSubscription(testSubscription.getId()).isSuccess());

            Result<Subscription> result = subscriptionService.cancelSubscription(testSubscription.getId());

            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("Subscription has already been canceled"));
        }
    }

    @Test
    void givenNonExistingSubscriptionIdWhenFindByIdThenThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> subscriptionService.findSubscriptionById(NON_EXISTING_ID));
    }

    @Test
    void givenNonExistingSubscriptionIdWhenUpdateThenThrowNotFoundException() {
        SubscriptionRequest request = new SubscriptionRequest().price(new BigDecimal("20.00"));

        assertThrows(NotFoundException.class, () -> subscriptionService.updateSubscription(NON_EXISTING_ID, request));
    }

    @Test
    void givenNonExistingSubscriptionIdWhenCancelThenThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> subscriptionService.cancelSubscription(NON_EXISTING_ID));
    }
}