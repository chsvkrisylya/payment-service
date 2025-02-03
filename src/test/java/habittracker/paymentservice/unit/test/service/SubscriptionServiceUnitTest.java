package habittracker.paymentservice.unit.test.service;

import com.braintreegateway.*;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import habittracker.paymentservice.model.dto.SubscriptionRequestDTO;
import habittracker.paymentservice.service.PlanServiceImpl;
import habittracker.paymentservice.service.SubscriptionServiceImpl;
import habittracker.paymentservice.service.util.NumFormatter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

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
    private ResourceCollection<Subscription> resourceCollectionExpectedResult;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private SubscriptionRequestDTO testsubscriptionRequestDTO;
    private SubscriptionRequest testSubscriptionRequest;

    @BeforeEach
    public void setUp() {
        testSubscriptionRequest = new SubscriptionRequest();
        testsubscriptionRequestDTO = new SubscriptionRequestDTO("TestName", "10", "TestNonce",
                1, true, 2, Subscription.DurationUnit.MONTH);
        BraintreeData.gateway = braintreeGateway;
    }

    @AfterEach
    void tearDown() {
        BraintreeData.gateway = null;
    }

    @ParameterizedTest
    @CsvSource({
            "true", // С trialPeriod
            "false" // Без trialPeriod
    })
    @DisplayName("createSubscriptionRequest -> should return saved request")
    void createSubscriptionRequest(boolean trialPeriod) {

        testsubscriptionRequestDTO.setTrialPeriod(trialPeriod);

        Mockito.when(planMock.getId()).thenReturn("planId");
        Mockito.when(planService.getPlanByName(testsubscriptionRequestDTO.getName())).thenReturn(Optional.of(planMock));
        Mockito.when(numFormatter.stringToNum(testsubscriptionRequestDTO.getStrPrice(), BigDecimal.class))
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

        Mockito.when(planMock.getId()).thenReturn("Default");
        Mockito.when(planService.getPlanByName(testsubscriptionRequestDTO.getName())).thenReturn(Optional.of(planMock));

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

        SubscriptionRequest subscriptionRequest = Mockito.mock(SubscriptionRequest.class);
        Mockito.when(subscriptionGateway.create(any())).thenReturn(expectedResult);
        Mockito.when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Result<Subscription> actualResult = subscriptionService.createSubscription(subscriptionRequest);

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("createDefaultSubscription -> create default subscription")
    void createDefaultSubscription() {

        testsubscriptionRequestDTO.setName("Default");

        Mockito.when(planMock.getId()).thenReturn("Default");
        Mockito.when(planService.getPlanByName(testsubscriptionRequestDTO.getName())).thenReturn(Optional.of(planMock));
        Mockito.when(subscriptionGateway.create(any())).thenReturn(expectedResult);
        Mockito.when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Result<Subscription> actualResult = subscriptionService.createDefaultSubscription("nonce");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("searchAllSubscription -> search all subscription")
    void searchAllSubscription() {

        Mockito.when(subscriptionGateway.search(any())).thenReturn(resourceCollectionExpectedResult);
        Mockito.when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        List<SubscriptionInfoDTO> actualResult = subscriptionService.searchAll();

        Assertions.assertTrue(actualResult.isEmpty());
        Assertions.assertNotNull(actualResult);
    }

    @Test
    @DisplayName("returnFindSubscriptionById -> find subscription by id")
    void returnFindSubscriptionById() {

        Subscription subscriptionExpectedResult = Mockito.mock(Subscription.class);
        Mockito.when(subscriptionGateway.find(any())).thenReturn(subscriptionExpectedResult);
        Mockito.when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Subscription actualResult = subscriptionService.findSubscriptionById("test");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(subscriptionExpectedResult);
    }

    @Test
    @DisplayName("UpdateSubscriptionById -> update subscription by id")
    void updateSubscriptionById() {

        Mockito.when(subscriptionGateway.update(any(), any())).thenReturn(expectedResult);
        Mockito.when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);
        SubscriptionRequest subscriptionRequest = Mockito.mock(SubscriptionRequest.class);

        Result<Subscription> actualResult = subscriptionService.updateSubscription("10", subscriptionRequest);

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("cancelSubscriptionById -> cancel subscription by id")
    void cancelSubscriptionById() {

        Mockito.when(subscriptionGateway.cancel(any())).thenReturn(expectedResult);
        Mockito.when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Result<Subscription> actualResult = subscriptionService.cancelSubscription("10");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("deleteSubscriptionById -> delete subscription by id")
    void deleteSubscriptionById() {

        Mockito.when(subscriptionGateway.delete(any(), any())).thenReturn(expectedResult);
        Mockito.when(braintreeGateway.subscription()).thenReturn(subscriptionGateway);

        Result<Subscription> actualResult = subscriptionService.deleteSubscription("10", "12");

        assertThat(actualResult).usingRecursiveComparison().isEqualTo(expectedResult);
    }
}
