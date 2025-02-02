package habittracker.paymentservice.unit.test.controller;

import com.braintreegateway.Plan;
import com.braintreegateway.PlanRequest;
import com.braintreegateway.Result;
import habittracker.paymentservice.controller.PlanController;
import habittracker.paymentservice.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PlanControllerUnitTest {

    @Mock
    private PlanService planService;

    @InjectMocks
    private PlanController planController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllPlans() {
        List<Plan> plan = Mockito.mock(List.class);
        when(planService.getAllPlans()).thenReturn(plan);
        var result = planController.getAllPlans();
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testGetPlanById() {
        Plan plan = Mockito.mock(Plan.class);
        when(planService.getPlanById(any())).thenReturn(plan);
        var result = planController.getPlanById(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testGetPlanByName() {
        Plan plan = Mockito.mock(Plan.class);
        when(planService.getPlanByName(any())).thenReturn(plan);
        var result = planController.getPlanByName(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testCreatePlan() {
        Result<Plan> plan = Mockito.mock(Result.class);
        when(planService.createPlan(any())).thenReturn(plan);
        var result = planController.createPlan(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testCreateDefaultPlan() {
        Result<Plan> plan = Mockito.mock(Result.class);
        when(planService.createDefaultPlan()).thenReturn(plan);
        var result = planController.createDefaultPlan();
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testGetPlanRequest() {
        PlanRequest planRequest = Mockito.mock(PlanRequest.class);
        when(planService.createPlanRequest(any())).thenReturn(planRequest);
        var result = planController.getPlanRequest(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(planRequest));
    }

    @Test
    void testGetDefaultPlanRequest() {
        PlanRequest planRequest = Mockito.mock(PlanRequest.class);
        when(planService.createDefaultPlanRequest()).thenReturn(planRequest);
        var result = planController.getDefaultPlanRequest();
        assertThat(result).isEqualTo(ResponseEntity.ok(planRequest));
    }

    @Test
    void testUpdatePlanById() {
        Result<Plan> plan = Mockito.mock(Result.class);
        when(planService.updatePlanById(any(), any())).thenReturn(plan);
        var result = planController.updatePlanById(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(plan));
    }

    @Test
    void testUpdatePlanByName() {
        Result<Plan> plan = Mockito.mock(Result.class);
        when(planService.updatePlanByName(any(), any())).thenReturn(plan);
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
    void testNullGetPlanById() {
        when(planService.getPlanById(any())).thenReturn(null);
        var result = planController.getPlanById(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullGetPlanByName() {
        when(planService.getPlanByName(any())).thenReturn(null);
        var result = planController.getPlanByName(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullCreatePlan() {
        when(planService.createPlan(any())).thenReturn(null);
        var result = planController.createPlan(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullCreateDefaultPlan() {
        when(planService.createDefaultPlan()).thenReturn(null);
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
        when(planService.updatePlanById(any(), any())).thenReturn(null);
        var result = planController.updatePlanById(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullUpdatePlanByName() {
        when(planService.updatePlanByName(any(), any())).thenReturn(null);
        var result = planController.updatePlanByName(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }
}
