package habittracker.paymentservice.unit.test.model.dto;

import com.braintreegateway.Transaction;
import com.braintreegateway.Subscription.Status;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.mockito.Mockito;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SubscriptionInfoDTOUnitTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testConstructorAndGetters() {
        // Подготовка тестовых данных
        String id = "sub_12345";
        String name = "Test Subscription";
        String merchantAccountId = "merchant_123";
        String planId = "plan_12345";
        Status status = Status.ACTIVE;  // Статус - который существует в вашей версии библиотеки
        List<Transaction> transactions = Collections.emptyList();
        BigDecimal price = new BigDecimal("49.99");
        String paymentMethodToken = "pm_12345";
        Integer numberOfBillingCycles = 12;
        String nextBillingDate = "2024-12-01";
        String firstBillingDate = "2024-01-01";
        Integer currentBillingCycle = 1;
        String createdAt = "2024-01-01T10:00:00Z";
        String updatedAt = "2024-01-01T12:00:00Z";
        String billingPeriodStartDate = "2024-01-01";
        String billingPeriodEndDate = "2024-12-31";

        // Создание объекта SubscriptionInfoDTO с использованием конструктора
        SubscriptionInfoDTO dto = new SubscriptionInfoDTO(
                id, name, merchantAccountId, planId, status, transactions, price,
                paymentMethodToken, numberOfBillingCycles, nextBillingDate, firstBillingDate,
                currentBillingCycle, createdAt, updatedAt, billingPeriodStartDate, billingPeriodEndDate);

        // Проверка значений через геттеры
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(merchantAccountId, dto.getMerchantAccountId());
        assertEquals(planId, dto.getPlanId());
        assertEquals(status, dto.getStatus());
        assertEquals(transactions, dto.getTransactions());
        assertEquals(price, dto.getPrice());
        assertEquals(paymentMethodToken, dto.getPaymentMethodToken());
        assertEquals(numberOfBillingCycles, dto.getNumberOfBillingCycles());
        assertEquals(nextBillingDate, dto.getNextBillingDate());
        assertEquals(firstBillingDate, dto.getFirstBillingDate());
        assertEquals(currentBillingCycle, dto.getCurrentBillingCycle());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
        assertEquals(billingPeriodStartDate, dto.getBillingPeriodStartDate());
        assertEquals(billingPeriodEndDate, dto.getBillingPeriodEndDate());
    }

    @Test
    void testSetters() {
        // Создание объекта SubscriptionInfoDTO с использованием конструктора без параметров
        SubscriptionInfoDTO dto = new SubscriptionInfoDTO(
                "sub_12345", "Test Subscription", "merchant_123", "plan_12345", Status.ACTIVE,
                Collections.emptyList(), new BigDecimal("49.99"), "pm_12345", 12,
                "2024-12-01", "2024-01-01", 1, "2024-01-01T10:00:00Z", "2024-01-01T12:00:00Z",
                "2024-01-01", "2024-12-31");

        // Установка значений через сеттеры
        dto.setStatus(Status.ACTIVE);  // Существующий статус
        dto.setPrice(new BigDecimal("29.99"));

        // Проверка значений через геттеры
        assertEquals(Status.ACTIVE, dto.getStatus());
        assertEquals(new BigDecimal("29.99"), dto.getPrice());
    }

    @Test
    void testValidSubscriptionInfoDTO() {
        // Создаем мок для Transaction
        Transaction mockTransaction = Mockito.mock(Transaction.class);

        SubscriptionInfoDTO dto = new SubscriptionInfoDTO(
                "12345",
                "Premium Plan",
                "merchant123",
                "plan123",
                Status.ACTIVE,
                List.of(mockTransaction), // Используем мок
                BigDecimal.valueOf(19.99),
                "token123",
                12,
                "2024-12-31",
                "2024-01-01",
                1,
                "2023-12-01",
                "2024-11-30",
                "2023-12-01",
                "2024-12-01"
        );

        Set<ConstraintViolation<SubscriptionInfoDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "DTO should pass validation");
    }

    @Test
    void testInvalidSubscriptionInfoDTOMissingFields() {
        SubscriptionInfoDTO dto = new SubscriptionInfoDTO(
                null, // ID отсутствует
                "", // Имя пустое
                "merchant123",
                "plan123",
                null, // Статус отсутствует
                null, // Транзакции отсутствуют
                BigDecimal.ZERO, // Цена неверная
                null, // Токен платежа отсутствует
                0, // Неверное количество циклов
                "", // Пустая дата
                "2024-01-01",
                null, // Текущий цикл отсутствует
                "2023-12-01",
                "2024-11-30",
                null, // Период отсутствует
                "" // Пустая дата
        );

        Set<ConstraintViolation<SubscriptionInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "DTO should fail validation");

        // Проверяем конкретные ошибки
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("ID cannot be blank")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Name cannot be blank")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Price must be greater than zero")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Status cannot be null")));
    }

    @Test
    void testInvalidPrice() {
        SubscriptionInfoDTO dto = new SubscriptionInfoDTO(
                "12345",
                "Premium Plan",
                "merchant123",
                "plan123",
                Status.ACTIVE,
                null,
                BigDecimal.valueOf(-5.0), // Неверная цена
                "token123",
                12,
                "2024-12-31",
                "2024-01-01",
                1,
                "2023-12-01",
                "2024-11-30",
                "2023-12-01",
                "2024-12-01"
        );

        Set<ConstraintViolation<SubscriptionInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "DTO should fail validation due to invalid price");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Price must be greater than zero")));
    }

    @Test
    void testInvalidNumberOfBillingCycles() {
        SubscriptionInfoDTO dto = new SubscriptionInfoDTO(
                "12345",
                "Premium Plan",
                "merchant123",
                "plan123",
                Status.ACTIVE,
                null,
                BigDecimal.valueOf(19.99),
                "token123",
                -1, // Неверное количество циклов
                "2024-12-31",
                "2024-01-01",
                1,
                "2023-12-01",
                "2024-11-30",
                "2023-12-01",
                "2024-12-01"
        );

        Set<ConstraintViolation<SubscriptionInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "DTO should fail validation due to invalid number of billing cycles");
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals("Number of billing cycles must be at least 1")));
    }
}
