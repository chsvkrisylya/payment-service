package habittracker.paymentservice.service.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateFormatter {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    public static String dateToString(Date date) {
        return new SimpleDateFormat(DEFAULT_PATTERN).format(date);
    }
}
