package habittracker.paymentservice.unit.test.model.dto;

import com.braintreegateway.Subscription.DurationUnit;
import habittracker.paymentservice.model.dto.SubscriptionRequestDTO;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionRequestDTOUnitTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testConstructorAndGetters() {
        // Подготовка тестовых данных
        String name = "Test Subscription";
        String strPrice = "99.99";
        String nonce = "sampleNonce";
        int numOfBillingCycles = 12;
        boolean trialPeriod = true;
        int trialDuration = 14;
        DurationUnit durationUnit = DurationUnit.MONTH;

        // Создание объекта SubscriptionRequestDTO с использованием конструктора
        SubscriptionRequestDTO dto = new SubscriptionRequestDTO(
                name, strPrice, nonce, numOfBillingCycles, trialPeriod, trialDuration, durationUnit);

        // Проверка значений через геттеры
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getStrPrice()).isEqualTo(strPrice);
        assertThat(dto.getNonce()).isEqualTo(nonce);
        assertThat(dto.getNumOfBillingCycles()).isEqualTo(numOfBillingCycles);
        assertThat(dto.isTrialPeriod()).isTrue();
        assertThat(dto.getTrialDuration()).isEqualTo(trialDuration);
        assertThat(dto.getDurationUnit()).isEqualTo(durationUnit);
    }

    @Test
    void testSetters() {
        // Создание объекта SubscriptionRequestDTO с использованием конструктора без параметров
        SubscriptionRequestDTO dto = new SubscriptionRequestDTO(null, null, null, 0, false, 0, null);

        // Установка значений через сеттеры
        dto.setName("New Subscription");
        dto.setStrPrice("49.99");
        dto.setNonce("newSampleNonce");
        dto.setNumOfBillingCycles(6);
        dto.setTrialPeriod(false);
        dto.setTrialDuration(7);
        dto.setDurationUnit(DurationUnit.DAY);

        // Проверка значений через геттеры
        assertThat(dto.getName()).isEqualTo("New Subscription");
        assertThat(dto.getStrPrice()).isEqualTo("49.99");
        assertThat(dto.getNonce()).isEqualTo("newSampleNonce");
        assertThat(dto.getNumOfBillingCycles()).isEqualTo(6);
        assertThat(dto.isTrialPeriod()).isFalse();
        assertThat(dto.getTrialDuration()).isEqualTo(7);
        assertThat(dto.getDurationUnit()).isEqualTo(DurationUnit.DAY);
    }

    @Test
    void testEmptyConstructor() {
        // Проверка того, что объект создается с пустыми значениями по умолчанию
        SubscriptionRequestDTO dto = new SubscriptionRequestDTO(null, null, null, 0, false, 0, null);

        assertThat(dto.getName()).isNull();
        assertThat(dto.getStrPrice()).isNull();
        assertThat(dto.getNonce()).isNull();
        assertThat(dto.getNumOfBillingCycles()).isZero();
        assertThat(dto.isTrialPeriod()).isFalse();
        assertThat(dto.getTrialDuration()).isZero();
        assertThat(dto.getDurationUnit()).isNull();
    }

    @Test
    void testValidSubscriptionRequestDTO() {
        SubscriptionRequestDTO dto = new SubscriptionRequestDTO(
                "Premium Plan",
                "19.99",
                "nonce123",
                12,
                true,
                30,
                DurationUnit.MONTH
        );

        Set<ConstraintViolation<SubscriptionRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).as("DTO should pass validation").isEmpty();
    }

    @Test
    void testInvalidSubscriptionRequestDTOMissingFields() {
        SubscriptionRequestDTO dto = new SubscriptionRequestDTO(
                "", // Имя пустое
                "", // Цена пустая
                "", // Nonce пустое
                -1, // Неверное количество циклов
                false,
                -5, // Неверная длительность пробного периода
                null // Единица измерения отсутствует
        );

        Set<ConstraintViolation<SubscriptionRequestDTO>> violations = validator.validate(dto);
        assertThat(violations).as("DTO should pass validation").isNotEmpty();

        // Проверяем конкретные ошибки
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("Name cannot be blank"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("Price cannot be blank"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("Nonce cannot be blank"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("Number of billing cycles must be at least 1"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("Trial duration must be at least 1"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("Duration unit cannot be null"))).isTrue();
    }

    @Test
    void testInvalidPrice() {
        SubscriptionRequestDTO dto = new SubscriptionRequestDTO(
                "Premium Plan",
                "invalidPrice", // Неверный формат цены
                "nonce123",
                12,
                true,
                30,
                DurationUnit.MONTH
        );

        Set<ConstraintViolation<SubscriptionRequestDTO>> violations = validator.validate(dto);
        assertThat(violations)
                .as("DTO should fail validation due to invalid price format")
                .isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().equals("Price must be a valid number with up to two decimal places"))).isTrue();
    }
}
