package com.driveaway.reservation.service;

import com.driveaway.reservation.model.Contrat;
import com.driveaway.reservation.model.Reservation;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Construit le document PDF d'un contrat de location (OpenPDF). */
@Component
public class ContratPdfGenerator {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color GRIS_CLAIR = new Color(235, 235, 235);

    private final Font titre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private final Font sousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private final Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private final Font gras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    /** Le contrat et sa réservation doivent être chargés (appel dans une transaction). */
    public byte[] generer(Contrat c) {
        Reservation r = c.getReservation();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Paragraph entete = new Paragraph("DriveAway — Contrat de location", titre);
            entete.setAlignment(Element.ALIGN_CENTER);
            doc.add(entete);
            Paragraph numero = new Paragraph("N° " + c.getNumeroContrat(), sousTitre);
            numero.setAlignment(Element.ALIGN_CENTER);
            numero.setSpacingAfter(20);
            doc.add(numero);

            doc.add(section("Informations générales"));
            PdfPTable infos = tableau();
            ligne(infos, "Statut du contrat", String.valueOf(c.getStatut()));
            ligne(infos, "Date de signature", date(c.getDateSignature()));
            ligne(infos, "Réservation n°", String.valueOf(r.getIdReservation()));
            ligne(infos, "Client (id)", String.valueOf(r.getClientId()));
            ligne(infos, "Véhicule (id)", String.valueOf(r.getVehiculeId()));
            ligne(infos, "Agence de retrait (id)", String.valueOf(r.getAgenceRetraitId()));
            ligne(infos, "Agence de retour (id)", String.valueOf(r.getAgenceRetourId()));
            doc.add(infos);

            doc.add(section("Dates"));
            PdfPTable dates = tableau();
            ligne(dates, "Début prévu", date(r.getDateDebut()));
            ligne(dates, "Début effectif", date(c.getDateDebutEffective()));
            ligne(dates, "Fin prévue", date(c.getDateFinPrevue()));
            ligne(dates, "Retour effectif", date(c.getDateRetourEffective()));
            if (c.getKilometrageDepart() != null) {
                ligne(dates, "Kilométrage départ", c.getKilometrageDepart() + " km");
            }
            if (c.getKilometrageRetour() != null) {
                ligne(dates, "Kilométrage retour", c.getKilometrageRetour() + " km");
            }
            doc.add(dates);

            doc.add(section("Montants"));
            PdfPTable montants = tableau();
            ligne(montants, "Prix journalier", montant(r.getPrixJournalier()));
            ligne(montants, "Montant total", montant(c.getMontantTotal()));
            ligne(montants, "Caution", montant(c.getCaution()));
            ligne(montants, "Frais de retard", montant(c.getFraisRetard()));
            if (c.getMontantFinal() != null) {
                ligne(montants, "Montant final", montant(c.getMontantFinal()));
            }
            doc.add(montants);

            Paragraph pied = new Paragraph("Document généré le " + LocalDate.now().format(FORMAT_DATE), normal);
            pied.setSpacingBefore(30);
            pied.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pied);
        } catch (DocumentException e) {
            throw new IllegalStateException("Erreur lors de la génération du PDF du contrat " + c.getIdContrat(), e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    private Paragraph section(String texte) {
        Paragraph p = new Paragraph(texte, sousTitre);
        p.setSpacingBefore(12);
        p.setSpacingAfter(6);
        return p;
    }

    private PdfPTable tableau() {
        PdfPTable t = new PdfPTable(new float[]{2, 3});
        t.setWidthPercentage(100);
        return t;
    }

    private void ligne(PdfPTable t, String libelle, String valeur) {
        PdfPCell cLibelle = new PdfPCell(new Phrase(libelle, gras));
        cLibelle.setBackgroundColor(GRIS_CLAIR);
        cLibelle.setPadding(5);
        PdfPCell cValeur = new PdfPCell(new Phrase(valeur, normal));
        cValeur.setPadding(5);
        t.addCell(cLibelle);
        t.addCell(cValeur);
    }

    private static String date(LocalDate d) {
        return d != null ? d.format(FORMAT_DATE) : "—";
    }

    private static String montant(BigDecimal m) {
        return m != null ? m.setScale(2, java.math.RoundingMode.HALF_UP) + " TND" : "—";
    }
}
