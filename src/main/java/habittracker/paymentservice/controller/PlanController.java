package habittracker.paymentservice.controller;

import com.braintreegateway.Plan;
import com.braintreegateway.PlanRequest;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import habittracker.paymentservice.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Plan", description = "Subscription plan controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanService planService;

    @Operation(summary = "Get all subscription plan")
    @GetMapping("/search/all")
    public ResponseEntity<List<Plan>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @Operation(summary = "Get subscription plan by ID")
    @GetMapping("/search/id")
    public ResponseEntity<Plan> getPlanById(@RequestBody String id) {
        return planService.getPlanById(id)
                .map(ResponseEntity::ok) // Если план найден, возвращаем 200 OK с планом
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Get subscription plan by name")
    @GetMapping("/search/name")
    public ResponseEntity<Plan> getPlanByName(@RequestBody String name) {
        return planService.getPlanByName(name)
                .map(ResponseEntity::ok) // Если план найден, возвращаем 200 OK с планом
                .orElseGet(() ->  ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create subscription Plan")
    @PostMapping("/create")
    public ResponseEntity<Plan> createPlan(@RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.createPlan(request).getTarget());
    }

    @Operation(summary = "Create default subscription plan")
    @PostMapping("/create/default")
    public ResponseEntity<Plan> createDefaultPlan() {
        return ResponseEntity.ok(planService.createDefaultPlan().getTarget());
    }

    @Operation(summary = "Create subscription plan request")
    @GetMapping("/request")
    public ResponseEntity<PlanRequest> getPlanRequest(@RequestBody PlanRequestDTO requestDTO) {
        return ResponseEntity.ok(planService.createPlanRequest(requestDTO));
    }

    @Operation(summary = "Get default subscription plan")
    @GetMapping("/request/default")
    public ResponseEntity<PlanRequest> getDefaultPlanRequest() {
        return ResponseEntity.ok(planService.createDefaultPlanRequest());
    }

    @Operation(summary = "Update subscription plan by ID")
    @PostMapping("/update/id")
    public ResponseEntity<Plan> updatePlanById(@RequestBody String id, @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.updatePlanById(id, request).getTarget());
    }

    @Operation(summary = "Update subscription plan by name")
    @PostMapping("/update/name")
    public ResponseEntity<Plan> updatePlanByName(@RequestBody String name, @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.updatePlanByName(name, request).getTarget());
    }
}
