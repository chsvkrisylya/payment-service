package habittracker.paymentservice.unit.test.model.dto;

import com.braintreegateway.Transaction;
import habittracker.paymentservice.model.dto.TransactionInfoDTO;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TransactionInfoDTOUnitTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testConstructorAndGetters() {
        // Подготовка тестовых данных
        String id = "txn_12345";
        String createdAt = "2024-01-01T10:00:00Z";
        Transaction.Type type = Transaction.Type.SALE;
        BigDecimal amount = new BigDecimal("100.00");
        Transaction.Status status = Transaction.Status.SETTLED;
        String refundedTransactionId = "txn_12344";
        boolean isRefund = true;

        // Создание объекта TransactionInfoDTO с использованием конструктора
        TransactionInfoDTO dto = new TransactionInfoDTO(
                id, createdAt, type, amount, status, refundedTransactionId, isRefund);

        // Проверка значений через геттеры
        assertEquals(id, dto.getId());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(type, dto.getType());
        assertEquals(amount, dto.getAmount());
        assertEquals(status, dto.getStatus());
        assertEquals(refundedTransactionId, dto.getRefundedTransactionId());
        assertTrue(dto.isRefund());
    }

    @Test
    void testSetters() {
        // Создание объекта TransactionInfoDTO с использованием конструктора без параметров
        TransactionInfoDTO dto = new TransactionInfoDTO(null, null, null, null, null, null, false);

        // Установка значений через сеттеры
        dto.setId("txn_67890");
        dto.setCreatedAt("2024-02-01T12:00:00Z");
        dto.setType(Transaction.Type.CREDIT);
        dto.setAmount(new BigDecimal("50.00"));
        dto.setStatus(Transaction.Status.SUBMITTED_FOR_SETTLEMENT);
        dto.setRefundedTransactionId("txn_67889");
        dto.setRefundFlag("txn_67889");  // Вызываем метод для установки флага возврата

        // Проверка значений через геттеры
        assertEquals("txn_67890", dto.getId());
        assertEquals("2024-02-01T12:00:00Z", dto.getCreatedAt());
        assertEquals(Transaction.Type.CREDIT, dto.getType());
        assertEquals(new BigDecimal("50.00"), dto.getAmount());
        assertEquals(Transaction.Status.SUBMITTED_FOR_SETTLEMENT, dto.getStatus());
        assertEquals("txn_67889", dto.getRefundedTransactionId());
        assertTrue(dto.isRefund());  // Проверяем, что флаг возврата установлен в true
    }

    @Test
    void testSetRefundFlag() {
        // Проверка метода setRefundFlag, если передан refundedTransactionId
        TransactionInfoDTO dtoWithRefund = new TransactionInfoDTO(
                "txn_12345", "2024-01-01T10:00:00Z", Transaction.Type.SALE,
                new BigDecimal("100.00"), Transaction.Status.SETTLED, "txn_12344", false);
        dtoWithRefund.setRefundFlag("txn_12344");
        assertTrue(dtoWithRefund.isRefund());  // Флаг возврата должен быть true

        // Проверка метода setRefundFlag, если refundedTransactionId равен null
        TransactionInfoDTO dtoWithoutRefund = new TransactionInfoDTO(
                "txn_12346", "2024-01-02T11:00:00Z", Transaction.Type.SALE,
                new BigDecimal("200.00"), Transaction.Status.SETTLED, null, false);
        dtoWithoutRefund.setRefundFlag(null);
        assertFalse(dtoWithoutRefund.isRefund());  // Флаг возврата должен быть false
    }

    @Test
    void testValidTransactionInfoDTO() {
        TransactionInfoDTO dto = new TransactionInfoDTO(
                "TXN123456",
                "2024-12-01T12:00:00",
                Transaction.Type.SALE,
                new BigDecimal("100.50"),
                Transaction.Status.AUTHORIZED,
                null, // Нет возврата
                false
        );

        Set<ConstraintViolation<TransactionInfoDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "DTO should pass validation");
    }

    @Test
    void testInvalidTransactionInfoDTOMissingFields() {
        TransactionInfoDTO dto = new TransactionInfoDTO(
                "", // Пустой ID
                "", // Пустая дата
                null, // Null тип транзакции
                new BigDecimal("-1"), // Неверная сумма
                null, // Null статус
                "TXN654321", // Возврат
                true
        );

        Set<ConstraintViolation<TransactionInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "DTO should fail validation due to missing/invalid fields");

        // Проверяем конкретные ошибки
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Transaction ID cannot be blank")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Creation date cannot be blank")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Transaction type cannot be null")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Amount must be greater than zero")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Transaction status cannot be null")));
    }

    @Test
    void testInvalidAmount() {
        TransactionInfoDTO dto = new TransactionInfoDTO(
                "TXN123456",
                "2024-12-01T12:00:00",
                Transaction.Type.SALE,
                new BigDecimal("0"), // Неверная сумма
                Transaction.Status.AUTHORIZED,
                null, // Нет возврата
                false
        );

        Set<ConstraintViolation<TransactionInfoDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "DTO should fail validation due to invalid amount");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Amount must be greater than zero")));
    }
}
