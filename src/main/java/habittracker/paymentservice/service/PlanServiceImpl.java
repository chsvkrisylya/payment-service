package habittracker.paymentservice.service;

import com.braintreegateway.Plan;
import com.braintreegateway.PlanRequest;
import com.braintreegateway.Result;
import com.braintreegateway.exceptions.NotFoundException;
import habittracker.paymentservice.model.BraintreeData;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
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
                .numberOfBillingCycles(0)
                .billingFrequency(1)
                .trialPeriod(false);

        return BraintreeData.gateway.plan().create(request);
    }

    @Override
    public List<Plan> getAllPlans() {
        return BraintreeData.gateway.plan().all();
    }

    @Override
    public Plan getPlanByName(String name) {
        List<Plan> plans = getAllPlans();

        Optional<Plan> foundPlan = plans.stream()
                .filter(plan -> plan.getName().equals(name))
                .findFirst();

        if (foundPlan.isEmpty()) {
            log.warn("План с названием '" + name + "' не найден.");
        }

        return foundPlan.orElse(null);
    }

    @Override
    public Plan getPlanById(String id) {
        try {
            return BraintreeData.gateway.plan().find(id);
        } catch (NotFoundException e) {
            log.warn("План с id '" + id + "' не найден.");
            return null;
        }
    }

    @Override
    public Result<Plan> updatePlanByName(String name, PlanRequest request) {
        try {
            String id = getPlanByName(name).getId();

            if (id.isEmpty()) {
                throw new NotFoundException();
            }

            return BraintreeData.gateway.plan().update(id, request);
        } catch (NotFoundException e) {
            log.warn("Не возможно изменить план по имени");
            return null;
        }
    }

    @Override
    public Result<Plan> updatePlanById(String id, PlanRequest request) {
        try {
            return BraintreeData.gateway.plan().update(id, request);
        } catch (Exception e) {
            log.warn("Не возможно изменить план по id");
            return null;
        }
    }
}
