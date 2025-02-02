package habittracker.paymentservice.controller;

import com.braintreegateway.PlanRequest;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import habittracker.paymentservice.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Plan", description = "Subscription plan controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanService planService;

    @Operation(summary = "Get all subscription plan")
    @GetMapping("/search/all")
    public ResponseEntity<?> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @Operation(summary = "Get subscription plan by ID")
    @GetMapping("/search/id")
    public ResponseEntity<?> getPlanById(@RequestBody String id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @Operation(summary = "Get subscription plan by name")
    @GetMapping("/search/name")
    public ResponseEntity<?> getPlanByName(@RequestBody String name) {
        return ResponseEntity.ok(planService.getPlanByName(name));
    }

    @Operation(summary = "Create subscription Plan")
    @PostMapping("/create")
    public ResponseEntity<?> createPlan(@RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.createPlan(request));
    }

    @Operation(summary = "Create default subscription plan")
    @PostMapping("/create/default")
    public ResponseEntity<?> createDefaultPlan() {
        return ResponseEntity.ok(planService.createDefaultPlan());
    }

    @Operation(summary = "Create subscription plan request")
    @GetMapping("/request")
    public ResponseEntity<?> getPlanRequest(@RequestBody PlanRequestDTO requestDTO) {
        return ResponseEntity.ok(planService.createPlanRequest(requestDTO));
    }

    @Operation(summary = "Get default subscription plan")
    @GetMapping("/request/default")
    public ResponseEntity<?> getDefaultPlanRequest() {
        return ResponseEntity.ok(planService.createDefaultPlanRequest());
    }

    @Operation(summary = "Update subscription plan by ID")
    @PostMapping("/update/id")
    public ResponseEntity<?> updatePlanById(@RequestBody String id, @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.updatePlanById(id, request));
    }

    @Operation(summary = "Update subscription plan by name")
    @PostMapping("/update/name")
    public ResponseEntity<?> updatePlanByName(@RequestBody String name, @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.updatePlanByName(name, request));
    }
}
