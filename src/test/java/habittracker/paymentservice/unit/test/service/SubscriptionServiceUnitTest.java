package habittracker.paymentservice.unit.test.service;

import com.braintreegateway.Plan;
import com.braintreegateway.ResourceCollection;
import com.braintreegateway.Result;
import com.braintreegateway.Subscription;
import com.braintreegateway.SubscriptionRequest;
import com.braintreegateway.SubscriptionSearchRequest;
import com.braintreegateway.SubscriptionGateway;
import com.braintreegateway.BraintreeGateway;

import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import habittracker.paymentservice.model.dto.SubscriptionRequestDTO;
import habittracker.paymentservice.service.PlanServiceImpl;
import habittracker.paymentservice.service.SubscriptionServiceImpl;
import habittracker.paymentservice.service.util.NumFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Collections;


import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceUnitTest {

    @Mock
    private PlanServiceImpl planService;

    @Mock
    private NumFormatter numFormatter;

    @Mock
    private Plan planMock;

    @Mock
    private SubscriptionGateway subscriptionGateway;

    @Mock
    private BraintreeGateway braintreeGateway;

    @Mock
    private Result<Subscription> expectedResult;

    @Mock
    private ResourceCollection<Subscription> resourceCollection;

    @Mock
    private Subscription subscription;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private SubscriptionRequestDTO testsubscriptionRequestDTO;
    private SubscriptionRequest testSubscriptionRequest;

    @BeforeEach
    public void setUp() {
        testSubscriptionRequest = new SubscriptionRequest();
        testsubscriptionRequestDTO = new SubscriptionRequestDTO("TestName", "10", "TestNonce",
                1, true, 2, Subscription.DurationUnit.MONTH);
        BraintreeData.setGateway(braintreeGateway);
    }

    @AfterEach
    void tearDown() {
        BraintreeData.setGateway(null);
    }

    @ParameterizedTest
    @CsvSource({
            "true", // С trialPeriod
            "false" // Без trialPeriod
    })
    @DisplayName("createSubscriptionRequest -> should return saved request")
    void createSubscriptionRequest(boolean trialPeriod) {

        testsubscriptionRequestDTO.setTrialPeriod(trialPeriod);

        when(planMock.getId()).thenReturn("planId");
        when(planService.getPlanByName(testsubscriptionRequestDTO.getName())).thenReturn(Optional.of(planMock));
        when(numFormatter.stringToNum(testsubscriptionRequestDTO.getStrPrice(), BigDecimal.class))
                .thenReturn(Optional.of(BigDecimal.valueOf(10)));

        var expectedResultSubscription = testSubscriptionRequest
                .planId("planId")
                .price(BigDecimal.valueOf(10))
                .paymentMethodNonce("TestNonce")
                .numberOfBillingCycles(1)
                .trialPeriod(trialPeriod)
                .options()
                .startImmediately(true)
                .revertSubscriptionOnProrationFailure(false)
                .done();
        if (trialPeriod) {
            testSubscriptionRequest.trialDuration(2).trialDurationUnit(Subscription.DurationUnit.MONTH);
        }

        SubscriptionRequest actualResult = subscriptionService.createSubscriptionRequest(testsubscriptionRequestDTO);

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResultSubscription);
    }

    @Test
    @DisplayName("getDefaultSubscriptionRequest -> create default subscription request")
    void getDefaultSubscriptionRequest() {

        testsubscriptionRequestDTO.setName("Default");

        when(planMock.getId()).thenReturn("Default");
        when(planService.getPlanByName(testsubscriptionRequestDTO.getName())).thenReturn(Optional.of(planMock));

        var expectedResultDefaultSubscription = new SubscriptionRequest()
                .planId("Default")
                .price(new BigDecimal("10.00"))
                .paymentMethodNonce("TestNonce")
                .numberOfBillingCycles(1)
                .trialPeriod(false)
                .options()
                .startImmediately(true)
                .revertSubscriptionOnProrationFailure(false)
                .done();

        SubscriptionRequest actualResult = subscriptionService.getDefaultSubscriptionRequest("TestNonce");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResultDefaultSubscription);
    }

    @Test
    @DisplayName("createSubscription -> create subscription")
    void createSubscription() {

        SubscriptionRequest subscriptionRequest = mock(SubscriptionRequest.class);
        when(subscriptionGateway.create(any())).thenReturn(expectedResult);
        when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Result<Subscription> actualResult = subscriptionService.createSubscription(subscriptionRequest);

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("createDefaultSubscription -> create default subscription")
    void createDefaultSubscription() {

        testsubscriptionRequestDTO.setName("Default");

        when(planMock.getId()).thenReturn("Default");
        when(planService.getPlanByName(testsubscriptionRequestDTO.getName())).thenReturn(Optional.of(planMock));
        when(subscriptionGateway.create(any())).thenReturn(expectedResult);
        when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Result<Subscription> actualResult = subscriptionService.createDefaultSubscription("nonce");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("searchAllSubscription -> search all subscription")
    void searchAllSubscription() {
        when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);
        when(subscriptionGateway.search(any(SubscriptionSearchRequest.class))).thenReturn(resourceCollection);
        when(resourceCollection.iterator())
                .thenAnswer(invocation -> Collections.singletonList(subscription).iterator());

        when(subscription.getId()).thenReturn("sub_123");
        when(subscription.getDescription()).thenReturn("Test Subscription");
        when(subscription.getMerchantAccountId()).thenReturn("habittracker");
        when(subscription.getPlanId()).thenReturn("plan_456");
        when(subscription.getStatus()).thenReturn(Subscription.Status.ACTIVE);
        when(subscription.getPrice()).thenReturn(new BigDecimal("19.99"));
        when(subscription.getPaymentMethodToken()).thenReturn("token_789");
        when(subscription.getNumberOfBillingCycles()).thenReturn(12);

        List<SubscriptionInfoDTO> result = subscriptionService.searchAll();
        assertThat(result).hasSize(1).isNotNull();
        assertThat(result.getFirst().getId()).isEqualTo("sub_123");
    }

    @Test
    @DisplayName("returnFindSubscriptionById -> find subscription by id")
    void returnFindSubscriptionById() {

        Subscription subscriptionExpectedResult = mock(Subscription.class);
        when(subscriptionGateway.find(any())).thenReturn(subscriptionExpectedResult);
        when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Subscription actualResult = subscriptionService.findSubscriptionById("test");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(subscriptionExpectedResult);
    }

    @Test
    @DisplayName("UpdateSubscriptionById -> update subscription by id")
    void updateSubscriptionById() {

        when(subscriptionGateway.update(any(), any())).thenReturn(expectedResult);
        when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);
        SubscriptionRequest subscriptionRequest = mock(SubscriptionRequest.class);

        Result<Subscription> actualResult = subscriptionService.updateSubscription("10", subscriptionRequest);

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("cancelSubscriptionById -> cancel subscription by id")
    void cancelSubscriptionById() {

        when(subscriptionGateway.cancel(any())).thenReturn(expectedResult);
        when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Result<Subscription> actualResult = subscriptionService.cancelSubscription("10");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("deleteSubscriptionById -> delete subscription by id")
    void deleteSubscriptionById() {

        when(subscriptionGateway.delete(any(), any())).thenReturn(expectedResult);
        when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Result<Subscription> actualResult = subscriptionService.deleteSubscription("10", "12");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }
}
