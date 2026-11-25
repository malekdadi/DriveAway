CREATE TABLE IF NOT EXISTS `facture` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `numero` varchar(50) NOT NULL,
  `email_destinataire` varchar(150) NOT NULL,
  `modele_vehicule` varchar(100) NOT NULL,
  `date_emission` datetime NOT NULL DEFAULT current_timestamp(),
  `nombre_jours` int(11) NOT NULL,
  `prix_journalier` decimal(10,2) NOT NULL,
  `montant_total` decimal(10,2) NOT NULL,
  `statut_envoi_email` varchar(30) NOT NULL DEFAULT 'EN_ATTENTE',
  `reste_a_payer` decimal(10,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero` (`numero`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `paiement` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `facture_id` bigint(20) unsigned NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `date_paiement` datetime NOT NULL DEFAULT current_timestamp(),
  `mode_paiement` varchar(50) NOT NULL,
  `reference_transaction` varchar(100) DEFAULT NULL,
  `statut` varchar(30) NOT NULL DEFAULT 'EN_ATTENTE',
  PRIMARY KEY (`id`),
  KEY `fk_paiement_facture` (`facture_id`),
  CONSTRAINT `fk_paiement_facture` FOREIGN KEY (`facture_id`) REFERENCES `facture` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `caution` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `paiement_id` bigint(20) unsigned NOT NULL,
  `montant_caution` decimal(10,2) NOT NULL,
  `date_depot` datetime NOT NULL DEFAULT current_timestamp(),
  `statut` varchar(30) NOT NULL DEFAULT 'BLOQUEE',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_caution_paiement` (`paiement_id`),
  CONSTRAINT `fk_caution_paiement` FOREIGN KEY (`paiement_id`) REFERENCES `paiement` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `caution_avance` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `modele_vehicule` varchar(100) NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `date_depot` datetime NOT NULL,
  `paiement_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_caution_avance_paiement` (`paiement_id`),
  CONSTRAINT `fk_caution_avance_paiement` FOREIGN KEY (`paiement_id`) REFERENCES `paiement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `facture_location` (
  `facture_id` bigint(20) unsigned NOT NULL,
  `date_depart` date NOT NULL,
  `date_retour` date NOT NULL,
  PRIMARY KEY (`facture_id`),
  CONSTRAINT `fk_facture_location_facture` FOREIGN KEY (`facture_id`) REFERENCES `facture` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

