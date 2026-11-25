package tn.driveaway.billing;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import jakarta.validation.constraints.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BillingService {
    record Vehicle(String id, String modele, int prix, String image) {}
    record Input(@NotBlank @Email @Size(max=150) String email, @NotBlank String vehicule,
                 @Min(1) @Max(365) Integer jours, @Positive Long cautionId, LocalDate depart, LocalDate retour) {}
    record Deposit(@NotBlank String vehicule, @NotNull @DecimalMin("0.01") @Digits(integer=8, fraction=2) BigDecimal montant) {}
    record CashPayment(@NotNull @DecimalMin("0.01") @Digits(integer=8, fraction=2) BigDecimal montant,
                       @NotNull UUID reference) {}
    record Advance(long id, String modele, BigDecimal montant, LocalDateTime date, Long factureId) {}
    record Invoice(long id, String numero, String email, String modele, LocalDateTime date,
                   int jours, BigDecimal prix, BigDecimal total, BigDecimal solde,
                   String emailStatut, BigDecimal caution, String cautionId, long operations,
                   LocalDate depart, LocalDate retour) {}
    record Stats(int factures, long reglees, BigDecimal total, BigDecimal cautions, BigDecimal solde) {}
    record Payment(long id, long factureId, BigDecimal montant, String mode, String statut,
                   String reference) {}
    record Checkout(String url) {}

    private static final List<Vehicle> VEHICLES = List.of(
        new Vehicle("clio", "Renault Clio", 145, "clio"),
        new Vehicle("3008", "Peugeot 3008", 285, "3008"),
        new Vehicle("golf", "Volkswagen Golf", 215, "golf"),
        new Vehicle("classe-e", "Mercedes-Benz Classe E", 490, "classe-e"),
        new Vehicle("x5", "BMW X5", 690, "x5"),
        new Vehicle("macan", "Porsche Macan", 850, "macan"),
        new Vehicle("range-rover", "Range Rover Sport", 990, "range-rover"));
    private static final String INVOICES = """
        SELECT f.*,
          COALESCE((SELECT SUM(c.montant_caution) FROM caution c JOIN paiement p ON p.id=c.paiement_id
            WHERE p.facture_id=f.id AND p.statut='CONFIRME' AND c.statut='BLOQUEE'),0) AS caution_versee,
          (SELECT COALESCE(CONCAT('CAU-', MAX(a.id)), CAST(MAX(c.id) AS CHAR))
            FROM caution c JOIN paiement p ON p.id=c.paiement_id LEFT JOIN caution_avance a ON a.paiement_id=p.id
            WHERE p.facture_id=f.id AND p.statut='CONFIRME' AND c.statut='BLOQUEE') AS caution_id,
          (SELECT COUNT(*) FROM paiement p WHERE p.facture_id=f.id AND p.statut<>'EXPIRE') AS operations,
          l.date_depart, l.date_retour
        FROM facture f LEFT JOIN facture_location l ON l.facture_id=f.id
        """;
    private final JdbcTemplate jdbc;
    private final KonnectClient konnect;

    BillingService(JdbcTemplate jdbc, KonnectClient konnect) {
        this.jdbc = jdbc;
        this.konnect = konnect;
    }

    List<Vehicle> vehicles() {
        return VEHICLES;
    }

    List<Invoice> list(String search) {
        String term = search.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return jdbc.query(INVOICES + """
            WHERE CONCAT_WS(' ', f.numero, f.modele_vehicule, f.email_destinataire) LIKE ? ESCAPE '!'
            ORDER BY f.date_emission DESC, f.id DESC
            """, this::invoice, "%" + term + "%");
    }

    Stats stats() {
        return jdbc.queryForObject("""
            SELECT COUNT(*) n, COALESCE(SUM(reste_a_payer=0),0) paid,
                COALESCE(SUM(montant_total),0) total, COALESCE(SUM(reste_a_payer),0) balance,
                (SELECT COALESCE(SUM(c.montant_caution),0) FROM caution c JOIN paiement p ON p.id=c.paiement_id
                    WHERE c.statut='BLOQUEE' AND p.statut='CONFIRME') deposits
            FROM facture
            """, (rs, row) -> new Stats(rs.getInt("n"), rs.getLong("paid"), rs.getBigDecimal("total"),
                rs.getBigDecimal("deposits"), rs.getBigDecimal("balance")));
    }

    Invoice get(long id) {
        try {
            return jdbc.queryForObject(INVOICES + " WHERE f.id=?", this::invoice, id);
        } catch (EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture introuvable.");
        }
    }

    private static final String ADVANCES = """
        SELECT a.*, p.facture_id FROM caution_avance a LEFT JOIN paiement p ON p.id=a.paiement_id
        """;

    List<Advance> advances() {
        return jdbc.query(ADVANCES + " ORDER BY a.id DESC", this::advance);
    }

    Advance getAdvance(long id) {
        return jdbc.query(ADVANCES + " WHERE a.id=?", this::advance, id).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caution introuvable."));
    }

    @Transactional
    public Advance register(Deposit input) {
        Vehicle vehicle = vehicle(input.vehicule());
        long id = insert("INSERT INTO caution_avance (modele_vehicule,montant,date_depot) VALUES (?,?,?)",
            vehicle.modele(), input.montant(), now());
        return getAdvance(id);
    }

    private Advance advance(ResultSet rs, int row) throws SQLException {
        return new Advance(rs.getLong("id"), rs.getString("modele_vehicule"), rs.getBigDecimal("montant"),
            rs.getObject("date_depot", LocalDateTime.class), rs.getObject("facture_id", Long.class));
    }

    private void applyAdvance(long factureId, Long advanceId, Vehicle vehicle, BigDecimal total) {
        if (advanceId == null) return;
        Advance advance = getAdvance(advanceId);
        if (advance.factureId() != null) throw conflict("Cette caution est déjà rattachée à une facture.");
        if (!advance.modele().equals(vehicle.modele())) throw conflict("Cette caution correspond à un autre modèle.");
        if (advance.montant().compareTo(total) > 0) throw conflict("La caution dépasse le total de la facture.");
        long paymentId = recordPayment(factureId, advance.montant(), "CAUTION", null, "CONFIRME");
        if (jdbc.update("UPDATE caution_avance SET paiement_id=? WHERE id=? AND paiement_id IS NULL",
                paymentId, advanceId) != 1) throw conflict("Cette caution vient d’être utilisée.");
        jdbc.update("INSERT INTO caution (paiement_id,montant_caution,date_depot,statut) VALUES (?,?,?,'BLOQUEE')",
            paymentId, advance.montant(), advance.date());
        reduceBalance(factureId, advance.montant());
    }

    @Transactional
    public Invoice create(Input input) {
        Vehicle vehicle = vehicle(input.vehicule());
        int days = rentalDays(input);
        BigDecimal total = BigDecimal.valueOf((long) vehicle.prix() * days);
        String numero = "DA-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
        long id = insert("""
            INSERT INTO facture (numero,email_destinataire,modele_vehicule,date_emission,nombre_jours,
                prix_journalier,montant_total,reste_a_payer) VALUES (?,?,?,?,?,?,?,?)
            """, numero, input.email(), vehicle.modele(), now(), days, vehicle.prix(), total, total);
        saveDates(id, input);
        applyAdvance(id, input.cautionId(), vehicle, total);
        return get(id);
    }

    @Transactional
    public Invoice update(long id, Input input) {
        Invoice current = lock(id);
        editable(current);
        Vehicle vehicle = vehicle(input.vehicule());
        int days = rentalDays(input);
        BigDecimal total = BigDecimal.valueOf((long) vehicle.prix() * days);
        jdbc.update("""
            UPDATE facture SET email_destinataire=?,modele_vehicule=?,nombre_jours=?,prix_journalier=?,
                montant_total=?,reste_a_payer=?,statut_envoi_email='EN_ATTENTE' WHERE id=?
            """, input.email(), vehicle.modele(), days, vehicle.prix(), total, total, id);
        saveDates(id, input);
        applyAdvance(id, input.cautionId(), vehicle, total);
        return get(id);
    }

    private static int rentalDays(Input input) {
        if (input.depart() == null && input.retour() == null && input.jours() != null) return input.jours();
        if (input.depart() != null && input.retour() != null) {
            long days = ChronoUnit.DAYS.between(input.depart(), input.retour());
            if (days >= 1 && days <= 365 && input.depart().getYear() >= 1000 && input.retour().getYear() <= 9999) {
                return (int) days;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Choisissez des dates de location comprises entre 1 et 365 jours.");
    }

    private void saveDates(long id, Input input) {
        if (input.depart() == null) {
            jdbc.update("DELETE FROM facture_location WHERE facture_id=?", id);
            return;
        }
        jdbc.update("""
            INSERT INTO facture_location (facture_id,date_depart,date_retour) VALUES (?,?,?)
            ON DUPLICATE KEY UPDATE date_depart=?, date_retour=?
            """, id, input.depart(), input.retour(), input.depart(), input.retour());
    }

    @Transactional
    public void delete(long id) {
        editable(lock(id));
        jdbc.update("DELETE FROM facture WHERE id=?", id);
    }

    @Transactional
    public Checkout checkout(long id) {
        Invoice invoice = lock(id);
        List<Payment> pending = pending(id);
        if (!pending.isEmpty()) {
            Payment previous = pending.get(0);
            KonnectClient.Details details = konnect.details(previous.reference());
            settle(previous, details);
            if (!details.completed() && !details.expired()) {
                return new Checkout(konnect.checkoutUrl(details.link()));
            }
            if (details.completed()) return new Checkout(null);
        }
        BigDecimal amount = invoice.solde();
        if (amount.signum() <= 0) throw conflict("La facture est déjà réglée.");
        KonnectClient.Session session = konnect.start(invoice, amount);
        recordPayment(id, amount, "KONNECT", session.reference(), "EN_ATTENTE");
        return new Checkout(session.url());
    }

    @Transactional
    public Invoice receiveCash(long id, CashPayment payment) {
        Invoice invoice = lock(id);
        String reference = payment.reference().toString();
        List<BigDecimal> recorded = jdbc.queryForList("""
            SELECT montant FROM paiement WHERE facture_id=? AND mode_paiement='ESPECES' AND reference_transaction=?
            """, BigDecimal.class, id, reference);
        BigDecimal amount = payment.montant();
        if (!recorded.isEmpty()) {
            if (recorded.get(0).compareTo(amount) != 0) throw conflict("Cette référence correspond à un autre montant.");
            return invoice;
        }
        if (!pending(id).isEmpty()) {
            throw conflict("Un paiement Konnect est en cours. Vérifiez son statut avant d’enregistrer des espèces.");
        }
        if (amount.compareTo(invoice.solde()) > 0) {
            throw conflict("Le montant reçu dépasse le solde de la facture.");
        }
        recordPayment(id, amount, "ESPECES", reference, "CONFIRME");
        reduceBalance(id, amount);
        return get(id);
    }

    @Transactional
    public Invoice refresh(long id) {
        lock(id);
        for (Payment payment : pending(id)) {
            settle(payment, konnect.details(payment.reference()));
        }
        return get(id);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void notice(String reference) {
        if (!reference.matches("[a-zA-Z0-9_-]{1,100}")) return;
        List<Payment> matching = jdbc.query("""
            SELECT * FROM paiement WHERE reference_transaction=? AND mode_paiement IN ('KONNECT','KONNECT_CAUTION')
            """, this::payment, reference);
        if (matching.isEmpty()) return;
        long id = matching.get(0).factureId();
        lock(id);
        for (Payment payment : pending(id)) {
            if (payment.reference().equals(reference)) settle(payment, konnect.details(reference));
        }
    }

    Invoice lock(long id) {
        if (jdbc.queryForList("SELECT id FROM facture WHERE id=? FOR UPDATE", Long.class, id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture introuvable.");
        }
        return get(id);
    }

    void emailStatus(long id, String status) {
        jdbc.update("UPDATE facture SET statut_envoi_email=? WHERE id=?", status, id);
    }

    List<Long> queuedEmails() {
        return jdbc.queryForList("""
            SELECT id FROM facture WHERE statut_envoi_email='A_ENVOYER' AND reste_a_payer=0
            ORDER BY id LIMIT 20
            """, Long.class);
    }

    private void settle(Payment payment, KonnectClient.Details details) {
        if (details.completed()) {
            if (!"TND".equals(details.token()) || details.amount() != KonnectClient.millimes(payment.montant())
                    || details.reached() != details.amount() || !details.successfulTransaction()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Paiement Konnect incohérent.");
            }
            jdbc.update("UPDATE paiement SET statut='CONFIRME',date_paiement=? WHERE id=?", now(), payment.id());
            jdbc.update("UPDATE caution SET statut='BLOQUEE',date_depot=? WHERE paiement_id=?", now(), payment.id());
            reduceBalance(payment.factureId(), payment.montant());
        } else if (details.expired()) {
            jdbc.update("UPDATE paiement SET statut='EXPIRE' WHERE id=?", payment.id());
            jdbc.update("UPDATE caution SET statut='EXPIREE' WHERE paiement_id=?", payment.id());
        }
    }

    private void editable(Invoice invoice) {
        if (invoice.operations() > 0) throw conflict("La facture possède un paiement ou une caution.");
    }

    private List<Payment> pending(long id) {
        return jdbc.query("""
            SELECT * FROM paiement WHERE facture_id=? AND statut='EN_ATTENTE'
                AND mode_paiement IN ('KONNECT','KONNECT_CAUTION') ORDER BY id
            """, this::payment, id);
    }

    private long recordPayment(long id, BigDecimal amount, String mode, String ref, String status) {
        return insert("""
            INSERT INTO paiement (facture_id,montant,date_paiement,mode_paiement,reference_transaction,statut)
                VALUES (?,?,?,?,?,?)
            """, id, amount, now(), mode, ref, status);
    }

    private void reduceBalance(long id, BigDecimal amount) {
        if (jdbc.update("""
                UPDATE facture SET statut_envoi_email=CASE WHEN reste_a_payer=? THEN 'A_ENVOYER' ELSE statut_envoi_email END,
                    reste_a_payer=reste_a_payer-? WHERE id=? AND reste_a_payer>=?
                """,
                amount, amount, id, amount) != 1) throw conflict("Le solde a changé.");
    }

    private static Vehicle vehicle(String id) {
        return VEHICLES.stream().filter(item -> item.id().equals(id)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sélectionnez un véhicule du catalogue."));
    }

    private static LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("Africa/Tunis"));
    }

    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private long insert(String sql, Object... values) {
        var key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        }, key);
        return key.getKey().longValue();
    }

    private Invoice invoice(ResultSet rs, int row) throws SQLException {
        return new Invoice(rs.getLong("id"), rs.getString("numero"), rs.getString("email_destinataire"),
            rs.getString("modele_vehicule"), rs.getObject("date_emission", LocalDateTime.class),
            rs.getInt("nombre_jours"), rs.getBigDecimal("prix_journalier"), rs.getBigDecimal("montant_total"),
            rs.getBigDecimal("reste_a_payer"), rs.getString("statut_envoi_email"), rs.getBigDecimal("caution_versee"),
            rs.getString("caution_id"), rs.getLong("operations"),
            rs.getObject("date_depart", LocalDate.class), rs.getObject("date_retour", LocalDate.class));
    }

    private Payment payment(ResultSet rs, int row) throws SQLException {
        return new Payment(rs.getLong("id"), rs.getLong("facture_id"), rs.getBigDecimal("montant"),
            rs.getString("mode_paiement"), rs.getString("statut"), rs.getString("reference_transaction"));
    }
}

