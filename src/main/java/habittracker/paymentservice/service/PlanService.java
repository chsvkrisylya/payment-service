package habittracker.paymentservice.service;

import com.braintreegateway.Plan;
import com.braintreegateway.PlanRequest;
import com.braintreegateway.Result;
import habittracker.paymentservice.model.dto.PlanRequestDTO;

import java.util.List;

public interface PlanService {

    PlanRequest createPlanRequest(PlanRequestDTO requestDTO);

    PlanRequest createDefaultPlanRequest();

    Result<Plan> createPlan(PlanRequest request);

    Result<Plan> createDefaultPlan();

    List<Plan> getAllPlans();

    Plan getPlanByName(String name);

    Plan getPlanById(String id);

    Result<Plan> updatePlanByName(String name, PlanRequest request);

    Result<Plan> updatePlanById(String id, PlanRequest request);
}
