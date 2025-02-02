package habittracker.paymentservice.controller;

import com.braintreegateway.SubscriptionRequest;
import habittracker.paymentservice.model.dto.SubscriptionRequestDTO;
import habittracker.paymentservice.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Subscription", description = "Subscription controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subService;

    @Operation(summary = "Get all subscriptions")
    @GetMapping("/search/all")
    public ResponseEntity<?> getAllSubscription() {
        return ResponseEntity.ok(subService.searchAll());
    }

    @Operation(summary = "Get subscription by ID")
    @GetMapping("/search/id")
    public ResponseEntity<?> getSubscriptionById(@RequestBody String id) {
        return ResponseEntity.ok(subService.findSubscriptionById(id));
    }

    @Operation(summary = "Get default subscription request")
    @GetMapping("/request/default")
    public ResponseEntity<?> getDefaultSubscriptionRequest(@RequestBody String nonce) {
        return ResponseEntity.ok(subService.getDefaultSubscriptionRequest(nonce));
    }

    @Operation(summary = "Create subscription request")
    @GetMapping("/request")
    public ResponseEntity<?> createSubscriptionRequest(@RequestBody SubscriptionRequestDTO requestDTO) {
        return ResponseEntity.ok(subService.createSubscriptionRequest(requestDTO));
    }

    @Operation(summary = "Create subscription")
    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(@RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subService.createSubscription(request));
    }

    @Operation(summary = "Create default subscription")
    @PostMapping("/create/default")
    public ResponseEntity<?> createDefaultSubscription(String nonce) {
        return ResponseEntity.ok(subService.createDefaultSubscription(nonce));
    }

    @Operation(summary = "Update subscription")
    @PostMapping("update")
    public ResponseEntity<?> updateSubscription(@RequestBody String id, @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subService.updateSubscription(id, request));
    }

    @Operation(summary = "Cancel subscription")
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelSubscription(@RequestBody String id) {
        return ResponseEntity.ok(subService.cancelSubscription(id));
    }

    @Operation(summary = "Delete subscription")
    @PostMapping("/delete")
    public ResponseEntity<?> deleteSubscription(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(subService.deleteSubscription(body.get("customerId"), body.get("id")));
    }
}
