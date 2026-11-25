package tn.driveaway.billing;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
class KonnectClient {
    record Session(String reference, String url) {}
    record Details(boolean completed, boolean expired, String token, long amount, long reached,
                   boolean successfulTransaction, String link) {}

    private final RestClient api;
    private final String key;
    private final String wallet;
    private final String webhook;

    KonnectClient(@Value("${driveaway.konnect.base}") String base, @Value("${driveaway.konnect.key}") String key,
            @Value("${driveaway.konnect.wallet}") String wallet, @Value("${driveaway.konnect.webhook}") String webhook) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(8000);
        this.api = RestClient.builder().baseUrl(base).requestFactory(factory).defaultHeader("x-api-key", key).build();
        this.key = key;
        this.wallet = wallet;
        this.webhook = webhook;
    }

    Session start(BillingService.Invoice invoice, BigDecimal amount) {
        configured();
        try {
            JsonNode result = api.post().uri("/payments/init-payment").body(Map.of(
                "receiverWalletId", wallet, "amount", millimes(amount), "token", "TND", "type", "immediate",
                "description", "Facture " + invoice.numero(), "email", invoice.email(),
                "orderId", invoice.numero(), "webhook", webhook, "lifespan", 30,
                "acceptedPaymentMethods", List.of("bank_card", "e-DINAR")
            )).retrieve().body(JsonNode.class);
            String reference = result == null ? "" : result.path("paymentRef").asText();
            if (!reference.matches("[a-zA-Z0-9_-]{1,100}")) throw unavailable();
            return new Session(reference, checkoutUrl(result.path("payUrl").asText()));
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    Details details(String reference) {
        configured();
        try {
            JsonNode result = api.get().uri("/payments/{reference}", reference).retrieve().body(JsonNode.class);
            if (result == null || !result.path("payment").isObject()) throw unavailable();
            JsonNode payment = result.path("payment");
            boolean successful = StreamSupport.stream(payment.path("transactions").spliterator(), false)
                .anyMatch(transaction -> "success".equals(transaction.path("status").asText()));
            return new Details("completed".equals(payment.path("status").asText()),
                expired(payment.path("expirationDate").asText()), payment.path("token").asText(),
                payment.path("amount").asLong(-1), payment.path("reachedAmount").asLong(-1),
                successful, payment.path("link").asText());
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    String checkoutUrl(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if ("https".equals(uri.getScheme()) && uri.getUserInfo() == null && host != null
                    && (host.equals("konnect.network") || host.endsWith(".konnect.network") || host.equals("knct.me"))) {
                return url;
            }
        } catch (IllegalArgumentException exception) {
            throw unavailable();
        }
        throw unavailable();
    }

    static long millimes(BigDecimal amount) {
        return amount.movePointRight(3).longValueExact();
    }

    boolean isConfigured() {
        return !key.isBlank() && !wallet.isBlank() && webhook.startsWith("https://");
    }

    private void configured() {
        if (!isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Paiement Konnect à configurer.");
        }
    }

    private static boolean expired(String value) {
        try {
            return Instant.parse(value).plus(1, ChronoUnit.MINUTES).isBefore(Instant.now());
        } catch (java.time.format.DateTimeParseException exception) {
            return false;
        }
    }

    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Réponse Konnect indisponible ou invalide.");
    }
}
