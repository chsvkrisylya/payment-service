package habittracker.paymentservice.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import habittracker.paymentservice.controller.PlanController;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SubscriptionServiceImplIntegrationTest {

    @Autowired
    private PlanController planController;

    @Test
    void createPlanAndSubscriptionViaController() {
        // Создание тестового DTO
        PlanRequestDTO planRequestDTO = PlanRequestDTO.builder()
                .name("TestPlan")
                .price(new BigDecimal("20.00"))
                .numOfCycles(12)
                .trialPeriod(true)
                .trialDuration(14)
                .build();

        // Вызов метода контроллера
        ResponseEntity<?> planCreationResponse = planController.getPlanRequest(planRequestDTO);

        // Проверки
        assertNotNull(planCreationResponse);
        assertNotNull(planCreationResponse.getBody());
        assertEquals(200, planCreationResponse.getStatusCode().value());
    }
}

