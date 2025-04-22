package habittracker.paymentservice.model;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;

// это временный класс для токенизации
// добавил конфигурацию через .env, можно добавить dependency injection через bean, но не уверен, что нужно
@Getter
public class BraintreeData {

    private static final Dotenv DOTENV = Dotenv
            .configure()
            .filename(".env")
            .load();

    private static final Environment ENV =
            "sandbox".equals(DOTENV.get("BRAINTREE_ENV"))
                    ? Environment.SANDBOX
                    : Environment.PRODUCTION;

    private static final String MERCH = DOTENV.get("BRAINTREE_MERCHANT_ID");
    private static final String PUB_KEY = DOTENV.get("BRAINTREE_PUBLIC_KEY");
    private static final String PR_KEY = DOTENV.get("BRAINTREE_PRIVATE_KEY");

    private BraintreeData() {
    }

    public static BraintreeGateway gateway = new BraintreeGateway(ENV, MERCH, PUB_KEY, PR_KEY);

    public static void setGateway(BraintreeGateway gateway) {
        BraintreeData.gateway = gateway;
    }

    // сайт песочницы https://sandbox.braintreegateway.com/
    // Логин: HabitTracker24
    // Пароль: KRv3bqcq#ia6$S.
}