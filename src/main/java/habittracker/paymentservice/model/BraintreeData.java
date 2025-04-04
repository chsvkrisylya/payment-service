package habittracker.paymentservice.model;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import lombok.EqualsAndHashCode;
import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicReference;

// это временный класс для токенизации
@UtilityClass
@EqualsAndHashCode
public class BraintreeData {

    private static final Environment ENV = Environment.SANDBOX;
    // настройка транзакций sandbox для песочницы, production для реальных продаж

    private static final String MERCH = "ntdd8c9v7v6jhtpn";
    // Merchant id - идентификатор продавца

    private static final String PUB_KEY = "npgkpwc74ntxjtwr";
    // Public key - открытый ключ (ключ api braintree)

    private static final String PRIV_KEY = "ffa8548e17e3dd2b64135b2bd66d24d6";
    // Private key - закрытый ключ (ключ сервера, наш сервер делает изменения используя аутентификацию по этому ключу)

    private static final AtomicReference<BraintreeGateway> GATEWAY =
            new AtomicReference<>(new BraintreeGateway(ENV, MERCH, PUB_KEY, PRIV_KEY));

    public static BraintreeGateway getGateway() {
        return GATEWAY.get();
    }

    public static void setGateway(BraintreeGateway newGateway) {
        GATEWAY.set(newGateway);
    }

    // сайт песочницы https://sandbox.braintreegateway.com/
    // Логин: HabitTracker24
    // Пароль: KRv3bqcq#ia6$S.
}
