package habittracker.paymentservice.unit.test.service.util;

import habittracker.paymentservice.service.util.NumFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NumFormatterUnitTest {

    private NumFormatter numFormatter;

    @BeforeEach
    void setUp() {
        numFormatter = new NumFormatter();
    }

    @Test
    @DisplayName("Успешное преобразование строки в Byte")
    void stringToNumShouldConvertToByte() {
        Optional<Byte> result = numFormatter.stringToNum("127", Byte.class);
        assertThat(result).isPresent().contains((byte) 127);
    }

    @Test
    @DisplayName("Успешное преобразование строки в Short")
    void stringToNumShouldConvertToShort() {
        Optional<Short> result = numFormatter.stringToNum("32767", Short.class);
        assertThat(result).isPresent().contains((short) 32767);
    }

    @Test
    @DisplayName("Успешное преобразование строки в Integer")
    void stringToNumShouldConvertToInteger() {
        Optional<Integer> result = numFormatter.stringToNum("123456", Integer.class);
        assertThat(result).isPresent().contains(123456);
    }

    @Test
    @DisplayName("Успешное преобразование строки в Long")
    void stringToNumShouldConvertToLong() {
        Optional<Long> result = numFormatter.stringToNum("9876543210", Long.class);
        assertThat(result).isPresent().contains(9876543210L);
    }

    @Test
    @DisplayName("Успешное преобразование строки в Float")
    void stringToNumShouldConvertToFloat() {
        Optional<Float> result = numFormatter.stringToNum("3.14", Float.class);
        assertThat(result).isPresent().contains(3.14f);
    }

    @Test
    @DisplayName("Успешное преобразование строки в Double")
    void stringToNumShouldConvertToDouble() {
        Optional<Double> result = numFormatter.stringToNum("2.718281828", Double.class);
        assertThat(result).isPresent().contains(2.718281828);
    }

    @Test
    @DisplayName("Успешное преобразование строки в BigInteger")
    void stringToNumShouldConvertToBigInteger() {
        Optional<BigInteger> result = numFormatter.stringToNum("12345678901234567890", BigInteger.class);
        assertThat(result).isPresent().contains(new BigInteger("12345678901234567890"));
    }

    @Test
    @DisplayName("Успешное преобразование строки в BigDecimal")
    void stringToNumShouldConvertToBigDecimal() {
        Optional<BigDecimal> result = numFormatter.stringToNum("12345.6789", BigDecimal.class);
        assertThat(result).isPresent().contains(new BigDecimal("12345.6789"));
    }

    @Test
    @DisplayName("Передана пустая строка - возвращается пустой Optional")
    void stringToNumShouldReturnEmptyWhenStringIsEmpty() {
        Optional<Integer> result = numFormatter.stringToNum(" ", Integer.class);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Передан null - возвращается пустой Optional")
    void stringToNumShouldReturnEmptyWhenStringIsNull() {
        Optional<Integer> result = numFormatter.stringToNum(null, Integer.class);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Ошибка преобразования - возвращается пустой Optional")
    void stringToNumShouldReturnEmptyWhenInvalidNumberFormat() {
        Optional<Integer> result = numFormatter.stringToNum("abc123", Integer.class);
        assertThat(result).isEmpty();
    }
}
