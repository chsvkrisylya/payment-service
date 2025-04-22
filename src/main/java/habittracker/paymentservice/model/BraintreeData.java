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

    private static final Dotenv DOTENV = Dotenv
            .configure()
            .filename(".env")
            .load();

    private static final String MERCH = "ntdd8c9v7v6jhtpn";
    // Merchant id - идентификатор продавца

    private static final String MERCH = DOTENV.get("BRAINTREE_MERCHANT_ID");
    private static final String PUB_KEY = DOTENV.get("BRAINTREE_PUBLIC_KEY");
    private static final String PR_KEY = DOTENV.get("BRAINTREE_PRIVATE_KEY");

    private BraintreeData() {
    }

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
