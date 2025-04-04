package habittracker.paymentservice.unit.test.service.util;

import habittracker.paymentservice.service.util.DateFormatter;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DateFormatterUnitTest {

    @Test
    void dateToStringShouldReturnFormattedDateWhenDateIsValid() {
        Date date = new Date(1633020293000L); // пример времени в миллисекундах (можно взять любой момент времени)

        String formattedDate = DateFormatter.dateToString(date);

        SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String expectedDate = expectedFormat.format(date);
        assertThat(formattedDate).isEqualTo(expectedDate);
    }

    @Test
    void dateToStringShouldReturnFormattedDateWhenDateIsMinValue() {
        Date date = new Date(Long.MIN_VALUE);

        String formattedDate = DateFormatter.dateToString(date);

        SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String expectedDate = expectedFormat.format(date);
        assertThat(formattedDate).isEqualTo(expectedDate);
    }

    @Test
    void dateToStringShouldReturnFormattedDateWhenDateIsMaxValue() {
        Date date = new Date(Long.MAX_VALUE);

        String formattedDate = DateFormatter.dateToString(date);

        SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String expectedDate = expectedFormat.format(date);
        assertThat(formattedDate).isEqualTo(expectedDate);
    }

    @Test
    void dateToStringShouldHandleDateWithLeapYearCorrectly() {
        //Проверка преобразования 29 февраля
        Date date = new Date(1582934400000L);

        String formattedDate = DateFormatter.dateToString(date);

        SimpleDateFormat expectedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String expectedDate = expectedFormat.format(date);
        assertThat(formattedDate).isEqualTo(expectedDate);
    }

    @Test
    void dateToStringShouldThrowNullPointerExceptionWhenDateIsNull() {
        Date date = null;

        assertThatThrownBy(() -> DateFormatter.dateToString(date))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("date must not be null");
    }
}
