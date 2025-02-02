package habittracker.paymentservice.service.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    private DateFormatter() {
    }

    public static String dateToString(Date date) {
        return new SimpleDateFormat(DEFAULT_PATTERN).format(date);
    }
}
