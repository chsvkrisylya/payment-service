package habittracker.paymentservice.unit.test.service;


import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Plan;
import com.braintreegateway.PlanGateway;
import com.braintreegateway.PlanRequest;
import com.braintreegateway.Result;
import com.braintreegateway.exceptions.NotFoundException;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import habittracker.paymentservice.service.PlanServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlanServiceImplUnitTest {

  @Mock
  private Plan planMock;

  @Mock
  private PlanGateway planGateway;

  @Mock
  private BraintreeGateway braintreeGateway;

  @Mock
  private Result<Plan> expectedResultPlan;

  @Mock
  private List<Plan> expectedListPlan;

  @InjectMocks
  private PlanServiceImpl planService;
  private PlanRequestDTO requestDto;

  @Mock
  private PlanRequest testPlanRequest;

  @BeforeEach
  public void setUp() {
    testPlanRequest = new PlanRequest();
    planService = new PlanServiceImpl();
    requestDto = new PlanRequestDTO("Basic Plan", BigDecimal.valueOf(9.99), "USD",
                12, 1, false, 0, null);
    BraintreeData.gateway = braintreeGateway;
  }

  @Test
  @DisplayName("Should create PlanRequest from PlanRequestDTO correctly")
  void testCreatePlanRequestWithoutTrialPeriod() {
    var expectedValue = testPlanRequest
            .id("9ddd8ffa-c8a1-4201-a71a-78615c880109")
            .name("Basic Plan")
            .price(BigDecimal.valueOf(9.99))
            .currencyIsoCode("USD")
            .numberOfBillingCycles(12)
            .billingFrequency(1)
            .trialPeriod(false);

    PlanRequest result = planService.createPlanRequest(requestDto);
    result.id("9ddd8ffa-c8a1-4201-a71a-78615c880109");
    // Сравнение объектов на равенство
    assertThat(result).usingRecursiveComparison().isEqualTo(expectedValue);
  }

  @Test
  @DisplayName("Should create PlanRequest from PlanRequestDTO correctly")
  void testCreatePlanRequestWithTrialPeriod() {
    requestDto.setTrialPeriod(true);
    requestDto.setTrialDuration(30);
    requestDto.setDurationUnit(Plan.DurationUnit.DAY);
    var expectedValue = testPlanRequest
            .id("9ddd8ffa-c8a1-4201-a71a-78615c880109")
            .name("Basic Plan")
            .price(BigDecimal.valueOf(9.99))
            .currencyIsoCode("USD")
            .numberOfBillingCycles(12)
            .billingFrequency(1)
            .trialPeriod(true)
            .trialDuration(30)
            .trialDurationUnit(Plan.DurationUnit.DAY);

    PlanRequest result = planService.createPlanRequest(requestDto);
    result.id("9ddd8ffa-c8a1-4201-a71a-78615c880109");
    assertThat(result).usingRecursiveComparison().isEqualTo(expectedValue);
  }

  @Test
  @DisplayName("getDefaultPlanRequest -> create default plan request")
  void testCreateDefaultPlanRequestWithoutTrialPeriod() {
    var expectedValue = testPlanRequest
            .id("9ddd8ffa-c8a1-4201-a71a-78615c880109")
            .name("Default")
            .price(new BigDecimal("10.00"))
            .currencyIsoCode("USD")
            .numberOfBillingCycles(0)
            .billingFrequency(1)
            .trialPeriod(false);

    PlanRequest result = planService.createDefaultPlanRequest();
    result.id("9ddd8ffa-c8a1-4201-a71a-78615c880109");

    // Падает с assertEquals поэтому с сравнение c использованием AssertThat.
    // Видимо параметры по-другому не сравнить
    assertThat(result).usingRecursiveComparison().isEqualTo(expectedValue);
  }

  @Test
  @DisplayName("createPlan -> create plan")
  void testCreatePlan() {
    PlanRequest planRequest = mock(PlanRequest.class);
    when(planGateway.create(any())).thenReturn(expectedResultPlan);
    when(braintreeGateway.plan()).thenReturn(planGateway);

    Result<Plan> result = planService.createPlan(planRequest);

    assertThat(result).usingRecursiveComparison().isEqualTo(expectedResultPlan);
  }

  @Test
  @DisplayName("createDefaultPlan -> create default plan")
  void testCreateDefaultPlan() {
    when(planGateway.create(any())).thenReturn(expectedResultPlan);
    when(braintreeGateway.plan()).thenReturn(planGateway);

    Result<Plan> result = planService.createDefaultPlan();

    assertThat(result).usingRecursiveComparison().isEqualTo(expectedResultPlan);
  }

  @Test
  @DisplayName("getAllPlans -> get all plans")
  void testGetAllPlans() {
    when(planGateway.all()).thenReturn(expectedListPlan);
    when(braintreeGateway.plan()).thenReturn(planGateway);

    List<Plan> result = planService.getAllPlans();

    assertEquals(result, expectedListPlan);
  }

  @Test
  @DisplayName("getPlanById should return Plan when found")
  void testGetPlanByIdWhenPlanFound() {
    String planId = "testId";
    when(planGateway.find(planId)).thenReturn(planMock);
    when(braintreeGateway.plan()).thenReturn(planGateway);

    Plan result = planService.getPlanById(planId);

    assertNotNull(result, "План не должен быть null");
    assertEquals(planMock, result, "Возвращаемый план должен совпадать");
    verify(braintreeGateway.plan()).find(planId);
  }

  @Test
  @DisplayName("getPlanById shouldn't return Plan")
  void testGetPlanByIdWhenPlanNotFound() {
    String planId = "testId";
    when(planGateway.find(planId)).thenThrow(new NotFoundException("Plan not found"));
    when(braintreeGateway.plan()).thenReturn(planGateway);

    Plan result = planService.getPlanById(planId);

    assertNull(result, "План должен быть null");
    verify(braintreeGateway.plan()).find(planId);
  }

  @Test
  @DisplayName("updatePlanById should return Plan when found")
  void testUpdatePlanByIdWithoutException() {
    when(planGateway.update("testId", testPlanRequest)).thenReturn(expectedResultPlan);
    when(braintreeGateway.plan()).thenReturn(planGateway);

    Result<Plan> result = planService.updatePlanById("testId", testPlanRequest);

    assertNotNull(result, "План не должен быть null");
    assertEquals(expectedResultPlan, result, "Result<Plan> должен совпадать");
    verify(braintreeGateway.plan()).update("testId", testPlanRequest);
  }

  @Test
  @DisplayName("getPlanById should return Plan when found")
  void testUpdatePlanByIdWithException() {
    // Проходит тест только на проверку RuntimeException
    when(braintreeGateway.plan()).thenThrow(new RuntimeException("Can't change plan by id"));

    Result<Plan> result = planService.updatePlanById("testId", testPlanRequest);

    assertNull(result, "Result<Plan> должен быть null");
  }
}
