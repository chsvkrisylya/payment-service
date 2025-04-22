package habittracker.paymentservice.model.dto;

import com.braintreegateway.Plan.DurationUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class PlanRequestDTO {
    @Size(min = 1, max = 100, message = "The range name of 1 to 100")
    String name;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    BigDecimal price;

    @NotBlank(message = "Currency ISO Code cannot be blank")
    @Size(min = 3, max = 3, message = "Currency ISO Code must be 3 characters")
    String currencyIsoCode;

    @Min(value = 1, message = "Number of cycles must be at least 1")
    int numOfCycles;

    @Min(value = 1, message = "Billing frequency must be at least 1")
    int billingFrequency;

    boolean trialPeriod;

    @Min(value = 0, message = "Trial duration must be at least 0")
    int trialDuration;

    @NotNull(message = "Duration unit cannot be null")
    DurationUnit durationUnit;
}
