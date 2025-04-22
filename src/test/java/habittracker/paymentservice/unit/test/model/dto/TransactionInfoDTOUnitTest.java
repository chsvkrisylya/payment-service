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

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getType()).isEqualTo(type);
        assertThat(dto.getAmount()).isEqualTo(amount);
        assertThat(dto.getStatus()).isEqualTo(status);
        assertThat(dto.getRefundedTransactionId()).isEqualTo(refundedTransactionId);
        assertThat(dto.isRefund()).isTrue();
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
        assertThat(dto.getId()).isEqualTo("txn_67890");
        assertThat(dto.getCreatedAt()).isEqualTo("2024-02-01T12:00:00Z");
        assertThat(dto.getType()).isEqualTo(Transaction.Type.CREDIT);
        assertThat(dto.getAmount()).isEqualTo(new BigDecimal("50.00"));
        assertThat(dto.getStatus()).isEqualTo(Transaction.Status.SUBMITTED_FOR_SETTLEMENT);
        assertThat(dto.getRefundedTransactionId()).isEqualTo("txn_67889");
        assertThat(dto.isRefund()).isTrue();  // Проверяем, что флаг возврата установлен в true
    }

    @Test
    void testSetRefundFlag() {
        // Проверка метода setRefundFlag, если передан refundedTransactionId
        TransactionInfoDTO dtoWithRefund = new TransactionInfoDTO(
                "txn_12345", "2024-01-01T10:00:00Z", Transaction.Type.SALE,
                new BigDecimal("100.00"), Transaction.Status.SETTLED, "txn_12344", false);
        dtoWithRefund.setRefundFlag("txn_12344");
        assertThat(dtoWithRefund.isRefund()).isTrue(); // Флаг возврата должен быть true

        // Проверка метода setRefundFlag, если refundedTransactionId равен null
        TransactionInfoDTO dtoWithoutRefund = new TransactionInfoDTO(
                "txn_12346", "2024-01-02T11:00:00Z", Transaction.Type.SALE,
                new BigDecimal("200.00"), Transaction.Status.SETTLED, null, false);
        dtoWithoutRefund.setRefundFlag(null);
        assertThat(dtoWithoutRefund.isRefund()).isFalse(); // Флаг возврата должен быть false
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
        assertThat(violations).isEmpty();
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
        assertThat(violations).as("DTO should fail validation due to missing/invalid fields").isNotEmpty();

        // Проверяем конкретные ошибки

        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Transaction ID cannot be blank"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Creation date cannot be blank"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Transaction type cannot be null"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Amount must be greater than zero"))).isTrue();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Transaction status cannot be null"))).isTrue();
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
        assertThat(violations).as("DTO should fail validation due to invalid amount").isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Amount must be greater than zero"))).isTrue();
     }
}
