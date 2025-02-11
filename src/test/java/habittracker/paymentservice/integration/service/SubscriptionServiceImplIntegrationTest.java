package habittracker.paymentservice.integration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import habittracker.paymentservice.controller.PlanController;
import habittracker.paymentservice.model.dto.PlanRequestDTO;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;


import java.io.File;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("local")
class SubscriptionServiceImplIntegrationTest {

    @Autowired
    private PlanController planController;

    @BeforeAll
    static void loadEnv() {
        // Загружаем переменные окружения
        String dotenvPath = new File(System.getProperty("user.dir")).getPath();
        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env.local")
                .load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

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

