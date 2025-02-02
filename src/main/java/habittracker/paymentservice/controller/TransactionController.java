package habittracker.paymentservice.controller;

import habittracker.paymentservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction", description = "Transaction controller")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/show/{transactionId}")
    @Operation(summary = "Get transaction by id from Braintree Data Base")
    public ResponseEntity<?> getTransactionByBraintree(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionByBraintree(transactionId));
    }

    @GetMapping("/search")
    @Operation(summary = "Get all transactions where a card number starting with \"4111\" from Braintree Data Base")
    public ResponseEntity<?> getTransactionsBySearchRequest() {
        return ResponseEntity.ok(transactionService.getTransactionsBySearchRequest());
    }

    @PostMapping("/refund")
    @Operation(summary = "Make a refund of the transaction")
    public ResponseEntity<?> refundTransactionByBraintree(@RequestBody Map<String, String> body) {
        String transactionId = body.get("id");
        log.info("возврат по id = " + transactionId);
        return ResponseEntity.ok(transactionService.refundTransactionByBraintree(transactionId));
    }

    @PostMapping("/void")
    @Operation(summary = "Void the transaction")
    public ResponseEntity<?> voidTransaction(@RequestBody Map<String, String> body) {
        String transactionId = body.get("id");
        log.info("анулирование по id = " + transactionId);
        return ResponseEntity.ok(transactionService.voidTransactionById(transactionId));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel the transaction")
    public ResponseEntity<?> cancelTransaction(@RequestBody Map<String, String> body) {
        String transactionId = body.get("id");
        log.info("отмена по id = " + transactionId);
        return ResponseEntity.ok(transactionService.cancelTransactionById(transactionId));
    }

}
