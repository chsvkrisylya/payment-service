package habittracker.paymentservice.unit.test.model.dto;

import com.braintreegateway.Plan.DurationUnit;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanRequestDTOUnitTest {

  private static Validator validator;
  ConstraintViolation<PlanRequestDTO> violation;

  @InjectMocks
  PlanRequestDTO planRequestDto;

  @BeforeAll
  static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @BeforeEach
  public void setUp() {
    planRequestDto = PlanRequestDTO.builder()
            .name("Test plan")
            .price(new BigDecimal("49.99"))
            .currencyIsoCode("USD")
            .numOfCycles(12)
            .billingFrequency(1)
            .trialPeriod(true)
            .trialDuration(14)
            .durationUnit(DurationUnit.DAY)
            .build();
  }

  @Test
  void testConstructorAndGetters() {
    String name = "Test Plan";
    BigDecimal price = new BigDecimal("49.99");
    String currencyIsoCode = "USD";
    int numOfCycles = 12;
    int billingFrequency = 1;
    boolean trialPeriod = true;
    int trialDuration = 14;
    DurationUnit durationUnit = DurationUnit.DAY;

    PlanRequestDTO dto = new PlanRequestDTO(
            name, price, currencyIsoCode, numOfCycles, billingFrequency,
            trialPeriod, trialDuration, durationUnit);

    assertEquals(name, dto.getName());
    assertEquals(price, dto.getPrice());
    assertEquals(currencyIsoCode, dto.getCurrencyIsoCode());
    assertEquals(numOfCycles, dto.getNumOfCycles());
    assertEquals(billingFrequency, dto.getBillingFrequency());
    assertTrue(dto.isTrialPeriod());
    assertEquals(trialDuration, dto.getTrialDuration());
    assertEquals(durationUnit, dto.getDurationUnit());
  }

  @Test
  void testSetters() {
    PlanRequestDTO dto = new PlanRequestDTO(null, null, null, 0, 0, false, 0, null);

    dto.setName("New Plan");
    dto.setPrice(new BigDecimal("99.99"));
    dto.setCurrencyIsoCode("EUR");
    dto.setNumOfCycles(6);
    dto.setBillingFrequency(2);
    dto.setTrialPeriod(false);
    dto.setTrialDuration(7);
    dto.setDurationUnit(DurationUnit.MONTH);

    assertEquals("New Plan", dto.getName());
    assertEquals(new BigDecimal("99.99"), dto.getPrice());
    assertEquals("EUR", dto.getCurrencyIsoCode());
    assertEquals(6, dto.getNumOfCycles());
    assertEquals(2, dto.getBillingFrequency());
    assertFalse(dto.isTrialPeriod());
    assertEquals(7, dto.getTrialDuration());
    assertEquals(DurationUnit.MONTH, dto.getDurationUnit());
  }

  // --- Тесты валидации ---
  @Test
  void testValidPlanRequestDTO() {
    Set<ConstraintViolation<PlanRequestDTO>> violations = validator.validate(planRequestDto);
    assertTrue(violations.isEmpty(), "Expected no validation errors");
  }

  @Test
  void testInvalidName() {
    planRequestDto.setName("");
    violation = validator.validate(planRequestDto).iterator().next();
    assertEquals("The range name of 1 to 100", violation.getMessage());
  }

  @Test
  void testInvalidPrice() {
    planRequestDto.setPrice(BigDecimal.valueOf(-1.0));
    violation = validator.validate(planRequestDto).iterator().next();
    assertEquals("Price must be greater than zero", violation.getMessage());
  }

  @Test
  void testInvalidCurrencyIsoCode() {
    planRequestDto.setCurrencyIsoCode("USDT");
    violation = validator.validate(planRequestDto).iterator().next();
    assertEquals("Currency ISO Code must be 3 characters", violation.getMessage());
  }

  @Test
  void testInvalidNumOfCycles() {
    planRequestDto.setNumOfCycles(-1);
    violation = validator.validate(planRequestDto).iterator().next();
    assertEquals("Number of cycles must be at least 1", violation.getMessage());
  }

  @Test
  void testInvalidBillingFrequency() {
    planRequestDto.setBillingFrequency(-1);
    violation = validator.validate(planRequestDto).iterator().next();
    assertEquals("Billing frequency must be at least 1", violation.getMessage());
  }

  @Test
  void testInvalidTrialDuration() {
    planRequestDto.setTrialDuration(-1);
    violation = validator.validate(planRequestDto).iterator().next();
    assertEquals("Trial duration must be at least 0", violation.getMessage());
  }

  @Test
  void testInvalidDurationUnit() {
    planRequestDto.setDurationUnit(null);
    violation = validator.validate(planRequestDto).iterator().next();
    assertEquals("Duration unit cannot be null", violation.getMessage());
  }
}
