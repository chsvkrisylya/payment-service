package habittracker.paymentservice.service;

import com.braintreegateway.Plan;
import com.braintreegateway.PlanRequest;
import com.braintreegateway.Result;
import com.braintreegateway.exceptions.NotFoundException;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlanServiceImpl implements PlanService {

    @Override
    public PlanRequest createPlanRequest(PlanRequestDTO requestDTO) {

        PlanRequest request = new PlanRequest()
                .id(UUID.randomUUID().toString())
                .name(requestDTO.getName())
                .price(requestDTO.getPrice())
                .currencyIsoCode(requestDTO.getCurrencyIsoCode())
                .numberOfBillingCycles(requestDTO.getNumOfCycles())
                .billingFrequency(requestDTO.getBillingFrequency())
                .trialPeriod(requestDTO.isTrialPeriod());

        if (requestDTO.isTrialPeriod()) {
            request.trialDuration(requestDTO.getTrialDuration()).trialDurationUnit(requestDTO.getDurationUnit());
        }

        return request;
    }

    @Override
    public PlanRequest createDefaultPlanRequest() {
        return new PlanRequest()
                .id(UUID.randomUUID().toString())
                .name("Default")
                .price(new BigDecimal("10.00"))
                .currencyIsoCode("USD")
                .numberOfBillingCycles(0)
                .billingFrequency(1)
                .trialPeriod(false);
    }

    @Override
    public Result<Plan> createPlan(PlanRequest request) {
        return BraintreeData.gateway.plan().create(request);
    }

    @Override
    public Result<Plan> createDefaultPlan() {
        PlanRequest request = new PlanRequest()
                .id(UUID.randomUUID().toString())
                .name("Default")
                .price(new BigDecimal("10.00"))
                .currencyIsoCode("USD")
                .numberOfBillingCycles(1)
                .billingFrequency(1)
                .trialPeriod(false);

        return BraintreeData.gateway.plan().create(request);
    }

    @Override
    public List<Plan> getAllPlans() {
        return BraintreeData.gateway.plan().all();
    }

    @Override
    public Optional<Plan> getPlanByName(String name) {
        return getAllPlans().stream().filter(plan -> plan.getName().equals(name)).findFirst();
    }

    @Override
    public Optional<Plan> getPlanById(String id) {
        try {
            return Optional.of(BraintreeData.gateway.plan().find(id));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Result<Plan> updatePlanByName(String name, PlanRequest request) {
        String id = getPlanByName(name).map(Plan::getId)
                .orElseThrow(() -> new NotFoundException("План с именем '" + name + "' не найден."));

        return BraintreeData.gateway.plan().update(id, request);
    }

    @Override
    public Result<Plan> updatePlanById(String id, PlanRequest request) {
        return BraintreeData.gateway.plan().update(id, request);
    }
}
