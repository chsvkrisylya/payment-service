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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertEquals(name, dto.getName());
        assertEquals(strPrice, dto.getStrPrice());
        assertEquals(nonce, dto.getNonce());
        assertEquals(numOfBillingCycles, dto.getNumOfBillingCycles());
        assertTrue(dto.isTrialPeriod());
        assertEquals(trialDuration, dto.getTrialDuration());
        assertEquals(durationUnit, dto.getDurationUnit());
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
        assertEquals("New Subscription", dto.getName());
        assertEquals("49.99", dto.getStrPrice());
        assertEquals("newSampleNonce", dto.getNonce());
        assertEquals(6, dto.getNumOfBillingCycles());
        assertFalse(dto.isTrialPeriod());
        assertEquals(7, dto.getTrialDuration());
        assertEquals(DurationUnit.DAY, dto.getDurationUnit());
    }

    @Test
    void testEmptyConstructor() {
        // Проверка того, что объект создается с пустыми значениями по умолчанию
        SubscriptionRequestDTO dto = new SubscriptionRequestDTO(null, null, null, 0, false, 0, null);

        assertNull(dto.getName());
        assertNull(dto.getStrPrice());
        assertNull(dto.getNonce());
        assertEquals(0, dto.getNumOfBillingCycles());
        assertFalse(dto.isTrialPeriod());
        assertEquals(0, dto.getTrialDuration());
        assertNull(dto.getDurationUnit());
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
        assertTrue(violations.isEmpty(), "DTO should pass validation");
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
        assertFalse(violations.isEmpty(), "DTO should fail validation due to missing/invalid fields");

        // Проверяем конкретные ошибки
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Name cannot be blank")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Price cannot be blank")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Nonce cannot be blank")));
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals("Number of billing cycles must be at least 1")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Trial duration must be at least 1")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Duration unit cannot be null")));
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
        assertFalse(violations.isEmpty(),
                "DTO should fail validation due to invalid price format");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage()
                .equals("Price must be a valid number with up to two decimal places")));
    }
}
