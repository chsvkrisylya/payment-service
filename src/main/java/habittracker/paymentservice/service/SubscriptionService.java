package habittracker.paymentservice.service;

import com.braintreegateway.Result;
import com.braintreegateway.Subscription;
import com.braintreegateway.SubscriptionRequest;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import habittracker.paymentservice.model.dto.SubscriptionRequestDTO;

import java.util.List;

public interface SubscriptionService {

    SubscriptionRequest createSubscriptionRequest(SubscriptionRequestDTO requestDTO);

    SubscriptionRequest getDefaultSubscriptionRequest(String nonce);

    Result<Subscription> createSubscription(SubscriptionRequest request);

    Result<Subscription> createDefaultSubscription(String nonce);

    List<SubscriptionInfoDTO> searchAll();

    Subscription findSubscriptionById(String id);

    Result<Subscription> updateSubscription(String id, SubscriptionRequest request);

    Result<Subscription> cancelSubscription(String id);

    Result<Subscription> deleteSubscription(String customerId, String id);
}
