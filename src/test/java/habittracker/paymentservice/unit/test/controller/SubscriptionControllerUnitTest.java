package habittracker.paymentservice.unit.test.controller;

import com.braintreegateway.Result;
import com.braintreegateway.Subscription;
import com.braintreegateway.SubscriptionRequest;
import habittracker.paymentservice.controller.SubscriptionController;
import habittracker.paymentservice.model.dto.SubscriptionInfoDTO;
import habittracker.paymentservice.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SubscriptionControllerUnitTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllSubscription() {
        List<SubscriptionInfoDTO> subscriptionList = Mockito.mock(List.class);
        when(subscriptionService.searchAll()).thenReturn(subscriptionList);
        var result = subscriptionController.getAllSubscription();
        assertThat(result).isEqualTo(ResponseEntity.ok(subscriptionList));
    }

    @Test
    void testGetSubscriptionById() {
        Subscription subscription = Mockito.mock(Subscription.class);
        when(subscriptionService.findSubscriptionById(any())).thenReturn(subscription);
        var result = subscriptionController.getSubscriptionById(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(subscription));
    }

    @Test
    void testGetDefaultSubscriptionRequest() {
        SubscriptionRequest subscriptionRequest = Mockito.mock(SubscriptionRequest.class);
        when(subscriptionService.getDefaultSubscriptionRequest(any())).thenReturn(subscriptionRequest);
        var result = subscriptionController.getDefaultSubscriptionRequest(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(subscriptionRequest));
    }

    @Test
    void testCreateSubscriptionRequest() {
        SubscriptionRequest subscriptionRequest = Mockito.mock(SubscriptionRequest.class);
        when(subscriptionService.createSubscriptionRequest(any())).thenReturn(subscriptionRequest);
        var result = subscriptionController.createSubscriptionRequest(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(subscriptionRequest));
    }

    @Test
    void testCreateSubscription() {
        Result<Subscription> subscription = Mockito.mock(Result.class);
        when(subscriptionService.createSubscription(any())).thenReturn(subscription);
        var result = subscriptionController.createSubscription(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(subscription));
    }

    @Test
    void testCreateDefaultSubscription() {
        Result<Subscription> subscription = Mockito.mock(Result.class);
        when(subscriptionService.createDefaultSubscription(any())).thenReturn(subscription);
        var result = subscriptionController.createDefaultSubscription(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(subscription));
    }

    @Test
    void testUpdateSubscription() {
        Result<Subscription> subscription = Mockito.mock(Result.class);
        when(subscriptionService.updateSubscription(any(), any())).thenReturn(subscription);
        var result = subscriptionController.updateSubscription(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(subscription));
    }

    @Test
    void testCancelSubscription() {
        Result<Subscription> subscription = Mockito.mock(Result.class);
        when(subscriptionService.cancelSubscription(any())).thenReturn(subscription);
        var result = subscriptionController.cancelSubscription(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(subscription));
    }

    @Test
    void testDeleteSubscription() {
        Result<Subscription> subscription = Mockito.mock(Result.class);
        when(subscriptionService.deleteSubscription(any(), any())).thenReturn(subscription);
        var result = subscriptionController.deleteSubscription(new HashMap<>());
        assertThat(result).isEqualTo(ResponseEntity.ok(subscription));
    }

    @Test
    void testNullGetAllSubscription() {
        when(subscriptionService.searchAll()).thenReturn(null);
        var result = subscriptionController.getAllSubscription();
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullGetSubscriptionById() {
        when(subscriptionService.findSubscriptionById(any())).thenReturn(null);
        var result = subscriptionController.getSubscriptionById(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullGetDefaultSubscriptionRequest() {
        when(subscriptionService.getDefaultSubscriptionRequest(any())).thenReturn(null);
        var result = subscriptionController.getDefaultSubscriptionRequest(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullCreateSubscriptionRequest() {
        when(subscriptionService.createSubscriptionRequest(any())).thenReturn(null);
        var result = subscriptionController.createSubscriptionRequest(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullCreateSubscription() {
        when(subscriptionService.createSubscription(any())).thenReturn(null);
        var result = subscriptionController.createSubscription(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullCreateDefaultSubscription() {

        when(subscriptionService.createDefaultSubscription(any())).thenReturn(null);
        var result = subscriptionController.createDefaultSubscription(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullUpdateSubscription() {
        when(subscriptionService.updateSubscription(any(), any())).thenReturn(null);
        var result = subscriptionController.updateSubscription(any(), any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullCancelSubscription() {
        when(subscriptionService.cancelSubscription(any())).thenReturn(null);
        var result = subscriptionController.cancelSubscription(any());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }

    @Test
    void testNullDeleteSubscription() {
        when(subscriptionService.deleteSubscription(any(), any())).thenReturn(null);
        var result = subscriptionController.deleteSubscription(new HashMap<>());
        assertThat(result).isEqualTo(ResponseEntity.ok(null));
    }
}
