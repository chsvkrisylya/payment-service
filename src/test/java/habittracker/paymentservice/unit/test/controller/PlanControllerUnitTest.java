package habittracker.paymentservice.unit.test.controller;

import com.braintreegateway.Plan;
import com.braintreegateway.PlanRequest;
import com.braintreegateway.Result;
import habittracker.paymentservice.controller.PlanController;
import habittracker.paymentservice.service.PlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanControllerUnitTest {

    @Mock
    private PlanService planService;

    @Mock
    private Result<Plan> planResult;

    @Mock
    private Plan plan;

    @Mock
    private List<Plan> planList;

    @Mock
    private PlanRequest planRequest;

    @InjectMocks
    private PlanController planController;

    @Test
    void testGetAllPlans() {
        when(planService.getAllPlans()).thenReturn(planList);
        var result = planController.getAllPlans();
        assertThat(result).isEqualTo(ResponseEntity.ok(planList));
    }

    @Test
    void testGetPlanById() {
        when(planService.getPlanById(any())).thenReturn(Optional.of(plan));
        var result = planController.getPlanById(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testGetPlanByName() {
        when(planService.getPlanByName(any())).thenReturn(Optional.of(plan));
        var result = planController.getPlanByName(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testCreatePlan() {
        when(planService.createPlan(any())).thenReturn(planResult);
        when(planResult.getTarget()).thenReturn(plan);
        var result = planController.createPlan(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testCreateDefaultPlan() {
        when(planService.createDefaultPlan()).thenReturn(planResult);
        when(planResult.getTarget()).thenReturn(plan);
        var result = planController.createDefaultPlan();
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testGetPlanRequest() {
        when(planService.createPlanRequest(any())).thenReturn(planRequest);
        var result = planController.getPlanRequest(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(planRequest));
    }

    @Test
    void testGetDefaultPlanRequest() {
        when(planService.createDefaultPlanRequest()).thenReturn(planRequest);
        var result = planController.getDefaultPlanRequest();
        assertThat(result).isEqualTo(ResponseEntity.ok(planRequest));
    }

    @Test
    void testUpdatePlanById() {
        when(planService.updatePlanById(any(), any())).thenReturn(planResult);
        when(planResult.getTarget()).thenReturn(plan);
        var result = planController.updatePlanById(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testUpdatePlanByName() {
        when(planService.updatePlanByName(any(), any())).thenReturn(planResult);
        when(planResult.getTarget()).thenReturn(plan);
        var result = planController.updatePlanByName(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testNullGetAllPlans() {
        when(planService.getAllPlans()).thenReturn(null);
        var result = planController.getAllPlans();
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testOptionalEmptyGetPlanById() {
        when(planService.getPlanById(any())).thenReturn(Optional.empty());
        var result = planController.getPlanById(any());
        assertThat(result).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    void testOptionalEmptyGetPlanByName() {
        when(planService.getPlanByName(any())).thenReturn(Optional.empty());
        var result = planController.getPlanByName(any());
        assertThat(result).isEqualTo(ResponseEntity.notFound().build());
    }

    @Test
    void testNullCreatePlan() {
        when(planService.createPlan(any())).thenReturn(planResult);
        when(planResult.getTarget()).thenReturn(null);
        var result = planController.createPlan(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullCreateDefaultPlan() {
        when(planService.createDefaultPlan()).thenReturn(planResult);
        when(planResult.getTarget()).thenReturn(null);
        var result = planController.createDefaultPlan();
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullGetPlanRequest() {
        when(planService.createPlanRequest(any())).thenReturn(null);
        var result = planController.getPlanRequest(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullGetDefaultPlanRequest() {
        when(planService.createDefaultPlanRequest()).thenReturn(null);
        var result = planController.getDefaultPlanRequest();
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullUpdatePlanById() {
        when(planService.updatePlanById(any(), any())).thenReturn(planResult);
        when(planResult.getTarget()).thenReturn(null);
        var result = planController.updatePlanById(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullUpdatePlanByName() {
        when(planService.updatePlanByName(any(), any())).thenReturn(planResult);
        when(planResult.getTarget()).thenReturn(null);
        var result = planController.updatePlanByName(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }
}
