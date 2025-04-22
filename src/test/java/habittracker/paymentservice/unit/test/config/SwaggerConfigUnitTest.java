package habittracker.paymentservice.unit.test.config;

import static org.assertj.core.api.Assertions.assertThat;

import habittracker.paymentservice.config.SwaggerConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class SwaggerConfigUnitTest {

    @Test
    void testCusromOpenAPIBean() {
        try (var context = new AnnotationConfigApplicationContext(SwaggerConfig.class)) {

            // Проверяем, что бин не null
            OpenAPI openAPI = context.getBean(OpenAPI.class);
            assertThat(openAPI).as("OpenAPI bean должен быть создан").isNotNull();

            // Проверяем Info на корректность
            Info info = openAPI.getInfo();
            assertThat(info).as("Info должен быть установлен").isNotNull();
            assertThat(info.getTitle()).isEqualTo("Payment Service API");
            assertThat(info.getDescription()).isEqualTo("Api для управления сервисом оплаты");
            assertThat(info.getVersion()).isEqualTo("1.0.0");

            // Проверяем схему безопасности
            Components components = openAPI.getComponents();
            assertThat(components).as("Components должны быть установлены").isNotNull();
            SecurityScheme securityScheme = components.getSecuritySchemes().get("bearerAuth");
            assertThat(securityScheme).as("SecurityScheme 'bearerAuth' должен быть установлен").isNotNull();
            assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
            assertThat(securityScheme.getScheme()).isEqualTo("bearer");
            assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
            assertThat(securityScheme.getIn()).isEqualTo(SecurityScheme.In.HEADER);
            assertThat(securityScheme.getName()).isEqualTo("Authorization");
        }
    }
}
