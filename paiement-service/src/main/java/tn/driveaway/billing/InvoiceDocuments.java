package tn.driveaway.billing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.mail.MessagingException;

@Service
class InvoiceDocuments {
    private final BillingService billing;
    private final JavaMailSender mail;
    private final String sender;
    private final TransactionTemplate transactions;

    InvoiceDocuments(BillingService billing, JavaMailSender mail, PlatformTransactionManager manager,
              @Value("${driveaway.mail.from}") String sender) {
        this.billing = billing;
        this.mail = mail;
        this.sender = sender;
        this.transactions = new TransactionTemplate(manager);
    }

    byte[] pdf(BillingService.Invoice invoice) throws IOException {
        try (var doc = new PDDocument(); var output = new ByteArrayOutputStream()) {
            var page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            try (var content = new PDPageContentStream(doc, page)) {
                var bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                var normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                content.setNonStrokingColor(0.76f, 0.02f, 0.17f);
                content.addRect(48, 696, 499, 88);
                content.fill();
                content.setNonStrokingColor(1f);
                line(content, bold, 26, 66, 751, "DriveAway");
                line(content, normal, 14, 66, 722, "Facture de location");
                content.setNonStrokingColor(0.10f, 0.15f, 0.22f);
                line(content, bold, 13, 48, 662, invoice.numero());
                line(content, normal, 12, 48, 636, "Émise le " + invoice.date().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                line(content, normal, 10, 48, 612, invoice.email());
                line(content, bold, 16, 48, 560, invoice.modele());
                line(content, normal, 12, 48, 534, invoice.jours() + " jour(s) à " + money(invoice.prix()));
                if (invoice.depart() != null && invoice.retour() != null) {
                    var format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    line(content, normal, 12, 48, 512, "Du " + invoice.depart().format(format)
                        + " au " + invoice.retour().format(format));
                }
                line(content, normal, 12, 48, 488, "Total location : " + money(invoice.total()));
                if (invoice.cautionId() != null) {
                    line(content, normal, 12, 48, 462,
                        "Caution " + invoice.cautionId() + " déduite : " + money(invoice.caution()));
                }
                line(content, normal, 12, 48, 436, "Autres paiements : " +
                    money(invoice.total().subtract(invoice.caution()).subtract(invoice.solde())));
                line(content, bold, 18, 48, 390, "Reste à payer : " + money(invoice.solde()));
            }
            doc.save(output);
            return output.toByteArray();
        }
    }

    @Scheduled(initialDelayString = "${driveaway.mail.delay:5000}", fixedDelayString = "${driveaway.mail.delay:5000}")
    void sendPaidInvoices() {
        if (sender.isBlank()) return;
        for (long id : billing.queuedEmails()) {
            transactions.executeWithoutResult(status -> sendIfQueued(id));
        }
    }

    private void sendIfQueued(long id) {
        BillingService.Invoice invoice = billing.lock(id);
        if (invoice.solde().signum() != 0 || !"A_ENVOYER".equals(invoice.emailStatut())) return;
        try {
            var message = new MimeMessageHelper(mail.createMimeMessage(), true, "UTF-8");
            message.setFrom(sender);
            message.setTo(invoice.email());
            message.setSubject("DriveAway · Facture " + invoice.numero());
            message.setText("Bonjour,\n\nVotre paiement a été confirmé. Votre facture acquittée est jointe à ce message.\n\nDriveAway");
            message.addAttachment(invoice.numero() + ".pdf", new ByteArrayResource(pdf(invoice)));
            mail.send(message.getMimeMessage());
            billing.emailStatus(id, "ENVOYE");
        } catch (MessagingException | MailException | IOException exception) {
            billing.emailStatus(id, "ECHEC");
        }
    }

    private static String money(BigDecimal amount) {
        return amount.toPlainString() + " DT";
    }

    private void line(PDPageContentStream content, PDType1Font font, int size, int x, int y, String value)
            throws IOException {
        StringBuilder printable = new StringBuilder();
        value.codePoints().forEach(code -> {
            String character = Character.toString(code);
            try {
                font.encode(character);
                printable.append(character);
            } catch (IllegalArgumentException ignored) {
                printable.append('?');
            } catch (IOException exception) {
                throw new java.io.UncheckedIOException(exception);
            }
        });
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(printable.toString());
        content.endText();
    }
}
