package habittracker.paymentservice.controller;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import habittracker.paymentservice.service.CheckoutService;
import habittracker.paymentservice.service.TransactionService;
import habittracker.paymentservice.service.util.DateFormatter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@Tag(name = "checkout", description = "Checkout controller")
@RequestMapping("/payment")
@RequiredArgsConstructor
public class CheckoutController {

    private static final String REDIRECT_CHECKOUTS = "redirect:/payment/checkouts";
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Redirects to /checkouts")
    public String root() {
        return "redirect:payment/checkouts";
    }

    @GetMapping("/checkouts")
    @Operation(summary = "Displays payment form")
    public String checkout(Model model) {
        String clientToken = checkoutService.getNewClientToken();
        model.addAttribute("clientToken", clientToken);

        return "checkouts/new";
    }

    @PostMapping("/checkouts")
    @Operation(summary = "Posts checkout form")
    public String postForm(@RequestParam("amount") String amount,
                           @RequestParam("payment_method_nonce") String nonce,
                           final RedirectAttributes redirectAttributes) {
        try {
            Result<Transaction> result = checkoutService
                    .getTransactionSale(checkoutService.getNewTransactionRequest(amount, nonce));

            if (result.isSuccess()) {
                return "redirect:/payment/checkouts/" + result.getTarget().getId();
            } else if (result.getTransaction() != null) {
                return "redirect:/payment/checkouts/" + result.getTransaction().getId();
            } else {
                redirectAttributes.addFlashAttribute("errorDetails", checkoutService.getValidationErrors(result));
                return REDIRECT_CHECKOUTS;
            }

        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorDetails", "Error: 81503: Amount is an invalid format.");
            return REDIRECT_CHECKOUTS;
        }
    }

    @GetMapping("/checkouts/{transactionId}")
    @Operation(summary = "Displays transaction details")
    public String getTransaction(@PathVariable String transactionId, Model model) {

        try {
            Transaction transaction = transactionService.getTransactionByBraintree(transactionId);

            model.addAttribute("isSuccess", Arrays.asList(checkoutService.getTransactionSuccessStatuses())
                    .contains(transaction.getStatus()));
            model.addAttribute("transaction", transaction);
            model.addAttribute("creditCard", transaction.getCreditCard());
            model.addAttribute("customer", transaction.getCustomer());
            model.addAttribute("createdAt", DateFormatter.dateToString(transaction.getCreatedAt().getTime()));
            model.addAttribute("updatedAt", DateFormatter.dateToString(transaction.getUpdatedAt().getTime()));
        } catch (Exception e) {
            LOGGER.error("Error occurred:", e);
            return REDIRECT_CHECKOUTS;
        }

        return "checkouts/show";
    }
}
