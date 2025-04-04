package habittracker.paymentservice.service;

import com.braintreegateway.Plan;
import com.braintreegateway.ResourceCollection;
import com.braintreegateway.Result;
import com.braintreegateway.Subscription;
import com.braintreegateway.SubscriptionRequest;
import com.braintreegateway.SubscriptionSearchRequest;
import com.braintreegateway.exceptions.NotFoundException;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import habittracker.paymentservice.model.dto.SubscriptionRequestDTO;
import habittracker.paymentservice.service.util.DateFormatter;
import habittracker.paymentservice.service.util.NumFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final PlanServiceImpl planService;
    private final NumFormatter numFormatter;

    @Override
    public SubscriptionRequest createSubscriptionRequest(SubscriptionRequestDTO requestDTO) {
        Plan plan = planService.getPlanByName(requestDTO.getName())
                .orElseThrow(() -> new NotFoundException("Plan not found"));

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest()
                .planId(plan.getId())
                .price(numFormatter.stringToNum(requestDTO.getStrPrice(), BigDecimal.class)
                        .orElseGet(plan::getPrice))
                .paymentMethodNonce(requestDTO.getNonce())
                .numberOfBillingCycles(requestDTO.getNumOfBillingCycles())
                .trialPeriod(requestDTO.isTrialPeriod())
                .options()
                .startImmediately(true)
                .revertSubscriptionOnProrationFailure(false)
                .done();

        if (requestDTO.isTrialPeriod()) {
            subscriptionRequest.trialDuration(requestDTO.getTrialDuration())
                    .trialDurationUnit(requestDTO.getDurationUnit());
        }
        return subscriptionRequest;
    }

    @Override
    public SubscriptionRequest getDefaultSubscriptionRequest(String nonce) {
        String planId = planService.getPlanByName("Default")
                .map(Plan::getId)
                .orElseGet(() -> planService.createDefaultPlan().getTarget().getId());

        return new SubscriptionRequest()
                .planId(planId)
                .price(new BigDecimal("10.00"))
                .paymentMethodNonce(nonce)
                .numberOfBillingCycles(1)
                .trialPeriod(false)
                .options()
                .startImmediately(true)
                .revertSubscriptionOnProrationFailure(false)
                .done();
    }

    @Override
    public Result<Subscription> createSubscription(SubscriptionRequest request) {
        return BraintreeData.getGateway().subscription().create(request);
    }

    @Override
    public Result<Subscription> createDefaultSubscription(String nonce) {
        return BraintreeData.getGateway().subscription().create(getDefaultSubscriptionRequest(nonce));
    }

    @Override
    public List<SubscriptionInfoDTO> searchAll() {
        SubscriptionSearchRequest searchRequest = new SubscriptionSearchRequest()
                .merchantAccountId().is("habittracker");

        ResourceCollection<Subscription> collection = BraintreeData.getGateway()
                .subscription()
                .search(searchRequest);

        List<SubscriptionInfoDTO> subscriptionList = new ArrayList<>();

        for (Subscription subscription : collection) {
            // Безопасное форматирование дат
            String nextBillingDate = formatDate(subscription.getNextBillingDate());
            String firstBillingDate = formatDate(subscription.getFirstBillingDate());
            String createdAt = formatDate(subscription.getCreatedAt());
            String updatedAt = formatDate(subscription.getUpdatedAt());
            String billingPeriodStartDate = formatDate(subscription.getBillingPeriodStartDate());
            String billingPeriodEndDate = formatDate(subscription.getBillingPeriodEndDate());

            SubscriptionInfoDTO subscriptionInfo = new SubscriptionInfoDTO(
                    subscription.getId(),
                    subscription.getDescription(),
                    subscription.getMerchantAccountId(),
                    subscription.getPlanId(),
                    subscription.getStatus(),
                    subscription.getTransactions(),
                    subscription.getPrice(),
                    subscription.getPaymentMethodToken(),
                    subscription.getNumberOfBillingCycles(),
                    nextBillingDate,
                    firstBillingDate,
                    subscription.getCurrentBillingCycle(),
                    createdAt,
                    updatedAt,
                    billingPeriodStartDate,
                    billingPeriodEndDate
            );
            subscriptionList.add(subscriptionInfo);
        }

        return subscriptionList;
    }

    @Override
    public Subscription findSubscriptionById(String id) {
        return BraintreeData.getGateway().subscription().find(id);
    }

    @Override
    public Result<Subscription> updateSubscription(String id, SubscriptionRequest request) {
        return BraintreeData.getGateway().subscription().update(id, request);
    }

    @Override
    public Result<Subscription> cancelSubscription(String id) {
        return BraintreeData.getGateway().subscription().cancel(id);
    }

    @Override
    public Result<Subscription> deleteSubscription(String customerId, String id) {
        return BraintreeData.getGateway().subscription().delete(customerId, id);
    }

    private String formatDate(Calendar date) {
        return date != null ? DateFormatter.dateToString(date.getTime()) : "N/A";
    }
}
