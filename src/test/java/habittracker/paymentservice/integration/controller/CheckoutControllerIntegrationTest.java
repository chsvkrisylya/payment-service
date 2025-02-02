package habittracker.paymentservice.integration.controller;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Testcontainers
@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CheckoutControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void localEnv() {
        String dotenvPath = new File(System.getProperty("user.dir")).getParent();

        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvPath)
                .filename(".env.local")
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    @Test
    void testRootRedirectToCheckouts() throws Exception {
        mockMvc.perform(get("/payment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("payment/checkouts"));
    }

    @Test
    void testPostFormWithValidationErrors() throws Exception {
        String amount = "invalid";
        String nonce = "mock-nonce";

        mockMvc.perform(post("/payment/checkouts")
                        .param("amount", amount)
                        .param("payment_method_nonce", nonce))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorDetails"))
                .andExpect(redirectedUrl("/payment/checkouts"));
    }

    @Test
    void testSuccessfulTransaction() throws Exception {
        String validAmount = "100.00";
        String validNonce = "mock-valid-nonce";

        mockMvc.perform(post("/payment/checkouts")
                        .param("amount", validAmount)
                        .param("payment_method_nonce", validNonce))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payment/checkouts"));
    }

    @Test
    void testDisplayCheckoutForm() throws Exception {
        mockMvc.perform(get("/payment/checkouts"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("clientToken"))
                .andExpect(view().name("checkouts/new"));
    }

}