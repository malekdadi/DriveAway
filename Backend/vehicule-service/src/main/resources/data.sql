-- =============================================
-- Seed data for vehicule-service (H2 database)
-- =============================================

-- Categories
INSERT INTO categorie (nom, description, statut) VALUES ('Citadine', 'Petite voiture idéale pour la ville', true);
INSERT INTO categorie (nom, description, statut) VALUES ('SUV', 'Sport Utility Vehicle - tout terrain confortable', true);
INSERT INTO categorie (nom, description, statut) VALUES ('Berline', 'Véhicule de tourisme haut de gamme', true);
INSERT INTO categorie (nom, description, statut) VALUES ('Compacte', 'Véhicule polyvalent de taille moyenne', true);
INSERT INTO categorie (nom, description, statut) VALUES ('Utilitaire', 'Véhicule utilitaire pour le transport', true);

-- Vehicles
INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie)
VALUES ('TU-1234-AB', 'Renault', 'Clio', 2023, 15000, 'DISPONIBLE', 1);

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie)
VALUES ('TU-5678-CD', 'Peugeot', '3008', 2024, 8000, 'DISPONIBLE', 2);

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie)
VALUES ('TU-9012-EF', 'Volkswagen', 'Golf', 2022, 32000, 'LOUE', 4);

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie)
VALUES ('TU-3456-GH', 'Mercedes', 'E-Class', 2024, 5000, 'DISPONIBLE', 3);

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie)
VALUES ('TU-7890-IJ', 'BMW', 'X5', 2023, 20000, 'EN_MAINTENANCE', 2);

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie)
VALUES ('TU-1122-KL', 'Porsche', 'Macan', 2024, 3000, 'DISPONIBLE', 2);

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie)
VALUES ('TU-3344-MN', 'Range Rover', 'Sport', 2023, 18000, 'LOUE', 2);
