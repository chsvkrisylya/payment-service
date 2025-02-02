package habittracker.paymentservice.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Component
public class NumFormatter {
    public boolean hasError = false;

    public <T extends Number> T stringToNum(String num, Class<T> clazz) {
        hasError = false;
        try {
            if (clazz == Integer.class) {
                return clazz.cast(Integer.parseInt(num));
            } else if (clazz == Double.class) {
                return clazz.cast(Double.parseDouble(num));
            } else if (clazz == Float.class) {
                return clazz.cast(Float.parseFloat(num));
            } else if (clazz == Long.class) {
                return clazz.cast(Long.parseLong(num));
            } else if (clazz == Short.class) {
                return clazz.cast(Short.parseShort(num));
            } else if (clazz == BigInteger.class) {
                return clazz.cast(new BigInteger(num));
            } else if (clazz == BigDecimal.class) {
                return clazz.cast(new BigDecimal(num));
            } else {
                hasError = true;
                log.warn("класс '" + clazz + "' не поддерживается");
                return null;
            }
        } catch (NumberFormatException e) {
            hasError = true;
            log.warn("Нельзя преобразовать '" + num + "'");
            return null;

        }
    }
}
