package habittracker.paymentservice.model.dto;

import com.braintreegateway.Subscription.DurationUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SubscriptionRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Price cannot be blank")
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Price must be a valid number with up to two decimal places")
    private String strPrice;

    @NotBlank(message = "Nonce cannot be blank")
    private String nonce;

    @Min(value = 1, message = "Number of billing cycles must be at least 1")
    private int numOfBillingCycles;

    private boolean trialPeriod;

    @Min(value = 1, message = "Trial duration must be at least 1")
    private int trialDuration;

    @NotNull(message = "Duration unit cannot be null")
    private DurationUnit durationUnit;
}
