package habittracker.paymentservice.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Component
public class NumFormatter {
    private static final Map<Class<? extends Number>, Function<String, ? extends Number>> NUMBER_PARSERS;

    static {
        NUMBER_PARSERS = Map.of(
                Byte.class, Byte::valueOf,
                Short.class, Short::valueOf,
                Integer.class, Integer::valueOf,
                Long.class, Long::valueOf,
                Float.class, Float::valueOf,
                Double.class, Double::valueOf,
                BigInteger.class, BigInteger::new,
                BigDecimal.class, BigDecimal::new
        );
    }

    /**
     * Преобразует строку в число указанного типа.
     *
     * @param numberString Строка для преобразования
     * @param targetType   Целевой числовой тип (например, Integer.class)
     * @return Optional с числом или пустой, если преобразование невозможно
     */
    public <T extends Number> Optional<T> stringToNum(String numberString, Class<T> targetType) {
        if (isBlank(numberString)) {
            log.warn("Передана пустая строка или null: '{}'", numberString);
            return Optional.empty();
        }

        Function<String, ? extends Number> parser = NUMBER_PARSERS.get(targetType);
        if (parser == null) {
            log.warn("Неподдерживаемый тип: {}", targetType.getSimpleName());
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(targetType.cast(parser.apply(numberString.trim())));
        } catch (NumberFormatException e) {
            log.warn("Ошибка преобразования '{}' в {}", numberString, targetType.getSimpleName(), e);
            return Optional.empty();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
