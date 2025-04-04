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
import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(dto.getName()).isEqualTo(name);
    assertThat(dto.getPrice()).isEqualTo(price);
    assertThat(dto.getCurrencyIsoCode()).isEqualTo(currencyIsoCode);
    assertThat(dto.getNumOfCycles()).isEqualTo(numOfCycles);
    assertThat(dto.getBillingFrequency()).isEqualTo(billingFrequency);
    assertThat(dto.isTrialPeriod()).isTrue();
    assertThat(dto.getTrialDuration()).isEqualTo(trialDuration);
    assertThat(dto.getDurationUnit()).isEqualTo(durationUnit);
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

    assertThat(dto.getName()).isEqualTo("New Plan");
    assertThat(dto.getPrice()).isEqualTo(new BigDecimal("99.99"));
    assertThat(dto.getCurrencyIsoCode()).isEqualTo("EUR");
    assertThat(dto.getNumOfCycles()).isEqualTo(6);
    assertThat(dto.getBillingFrequency()).isEqualTo(2);
    assertThat(dto.isTrialPeriod()).isFalse();
    assertThat(dto.getTrialDuration()).isEqualTo(7);
    assertThat(dto.getDurationUnit()).isEqualTo(DurationUnit.MONTH);
  }

  // --- Тесты валидации ---
  @Test
  void testValidPlanRequestDTO() {
    Set<ConstraintViolation<PlanRequestDTO>> violations = validator.validate(planRequestDto);
    assertThat(violations).as("Expected no validation errors").isEmpty();
  }

  @Test
  void testInvalidName() {
    planRequestDto.setName("");
    violation = validator.validate(planRequestDto).iterator().next();
    assertThat(violation.getMessage()).isEqualTo("The range name of 1 to 100");
  }

  @Test
  void testInvalidPrice() {
    planRequestDto.setPrice(BigDecimal.valueOf(-1.0));
    violation = validator.validate(planRequestDto).iterator().next();
    assertThat(violation.getMessage()).isEqualTo("Price must be greater than zero");
  }

  @Test
  void testInvalidCurrencyIsoCode() {
    planRequestDto.setCurrencyIsoCode("USDT");
    violation = validator.validate(planRequestDto).iterator().next();
    assertThat(violation.getMessage()).isEqualTo("Currency ISO Code must be 3 characters");
  }

  @Test
  void testInvalidNumOfCycles() {
    planRequestDto.setNumOfCycles(-1);
    violation = validator.validate(planRequestDto).iterator().next();
    assertThat(violation.getMessage()).isEqualTo("Number of cycles must be at least 1");
  }

  @Test
  void testInvalidBillingFrequency() {
    planRequestDto.setBillingFrequency(-1);
    violation = validator.validate(planRequestDto).iterator().next();
    assertThat(violation.getMessage()).isEqualTo("Billing frequency must be at least 1");
  }

  @Test
  void testInvalidTrialDuration() {
    planRequestDto.setTrialDuration(-1);
    violation = validator.validate(planRequestDto).iterator().next();
    assertThat(violation.getMessage()).isEqualTo("Trial duration must be at least 0");
  }

  @Test
  void testInvalidDurationUnit() {
    planRequestDto.setDurationUnit(null);
    violation = validator.validate(planRequestDto).iterator().next();
    assertThat(violation.getMessage()).isEqualTo("Duration unit cannot be null");
  }

  @Test
  void testBuilder() {
    // Используем Builder для создания объекта
    PlanRequestDTO planRequestDTO = PlanRequestDTO.builder()
            .name("Test Plan")
            .price(new BigDecimal("99.99"))
            .currencyIsoCode("USD")
            .numOfCycles(12)
            .billingFrequency(1)
            .trialPeriod(true)
            .trialDuration(30)
            .durationUnit(DurationUnit.MONTH)
            .build();

    // Проверяем, что объект был правильно создан
    assertThat(planRequestDTO).isNotNull();
    assertThat(planRequestDTO.getName()).isEqualTo("Test Plan");
    assertThat(planRequestDTO.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    assertThat(planRequestDTO.getCurrencyIsoCode()).isEqualTo("USD");
    assertThat(planRequestDTO.getNumOfCycles()).isEqualTo(12);
    assertThat(planRequestDTO.getBillingFrequency()).isEqualTo(1);
    assertThat(planRequestDTO.isTrialPeriod()).isTrue();
    assertThat(planRequestDTO.getTrialDuration()).isEqualTo(30);
    assertThat(planRequestDTO.getDurationUnit()).isEqualTo(DurationUnit.MONTH);
  }
}
