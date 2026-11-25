package tn.driveaway.billing;

import java.io.IOException;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
class BillingController {
    private final BillingService billing;
    private final InvoiceDocuments documents;
    private final KonnectClient konnect;

    BillingController(BillingService billing, InvoiceDocuments documents, KonnectClient konnect) {
        this.billing = billing;
        this.documents = documents;
        this.konnect = konnect;
    }

    record PaymentAvailability(boolean konnectConfigured) {}

    @GetMapping("/paiements/disponibilite")
    PaymentAvailability paymentAvailability() { return new PaymentAvailability(konnect.isConfigured()); }

    @GetMapping("/vehicules")
    List<BillingService.Vehicle> vehicles() { return billing.vehicles(); }

    @GetMapping("/factures")
    List<BillingService.Invoice> list(@RequestParam(defaultValue = "") String q) { return billing.list(q); }

    @GetMapping("/factures/stats")
    BillingService.Stats stats() { return billing.stats(); }

    @GetMapping("/factures/{id}")
    BillingService.Invoice get(@PathVariable long id) { return billing.get(id); }

    @PostMapping("/factures")
    @ResponseStatus(HttpStatus.CREATED)
    BillingService.Invoice create(@Valid @RequestBody BillingService.Input input) { return billing.create(input); }

    @PutMapping("/factures/{id}")
    BillingService.Invoice update(@PathVariable long id, @Valid @RequestBody BillingService.Input input) {
        return billing.update(id, input);
    }

    @DeleteMapping("/factures/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable long id) { billing.delete(id); }

    @GetMapping("/cautions")
    List<BillingService.Advance> advances() { return billing.advances(); }

    @GetMapping("/cautions/{id}")
    BillingService.Advance getAdvance(@PathVariable long id) { return billing.getAdvance(id); }

    @PostMapping("/cautions")
    @ResponseStatus(HttpStatus.CREATED)
    BillingService.Advance register(@Valid @RequestBody BillingService.Deposit deposit) { return billing.register(deposit); }


    @PostMapping("/factures/{id}/checkout")
    BillingService.Checkout checkout(@PathVariable long id) { return billing.checkout(id); }

    @PostMapping("/factures/{id}/paiements/especes")
    BillingService.Invoice receiveCash(@PathVariable long id, @Valid @RequestBody BillingService.CashPayment payment) {
        return billing.receiveCash(id, payment);
    }

    @PostMapping("/factures/{id}/paiements/verifier")
    BillingService.Invoice refresh(@PathVariable long id) { return billing.refresh(id); }

    @GetMapping("/konnect/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void webhook(@RequestParam("payment_ref") String reference) { billing.notice(reference); }

    @GetMapping("/factures/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable long id) throws IOException {
        var invoice = billing.get(id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(invoice.numero() + ".pdf").build().toString())
            .body(documents.pdf(invoice));
    }

}
