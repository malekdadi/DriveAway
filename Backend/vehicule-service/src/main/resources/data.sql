-- =============================================
-- Seed data for vehicule-service (H2 database)
-- =============================================

-- Categories with images
INSERT INTO categorie (nom, description, statut, image_url) VALUES ('Citadine', 'Petite voiture idéale pour la ville', true, 'https://images.unsplash.com/photo-1502877338535-766e1452684a?w=400');
INSERT INTO categorie (nom, description, statut, image_url) VALUES ('SUV', 'Sport Utility Vehicle - tout terrain confortable', true, 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=400');
INSERT INTO categorie (nom, description, statut, image_url) VALUES ('Berline', 'Véhicule de tourisme haut de gamme', true, 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=400');
INSERT INTO categorie (nom, description, statut, image_url) VALUES ('Compacte', 'Véhicule polyvalent de taille moyenne', true, 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=400');
INSERT INTO categorie (nom, description, statut, image_url) VALUES ('Utilitaire', 'Véhicule utilitaire pour le transport', true, 'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=400');

-- Vehicles with multiple images (comma-separated)
INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie, images)
VALUES ('TU-1234-AB', 'Renault', 'Clio', 2023, 15000, 'DISPONIBLE', 1,
'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=400,https://images.unsplash.com/photo-1502877338535-766e1452684a?w=400,https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?w=400');

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie, images)
VALUES ('TU-5678-CD', 'Peugeot', '3008', 2024, 8000, 'DISPONIBLE', 2,
'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=400,https://images.unsplash.com/photo-1606611013016-969c19ba27d5?w=400');

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie, images)
VALUES ('TU-9012-EF', 'Volkswagen', 'Golf', 2022, 32000, 'LOUE', 4,
'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=400,https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=400');

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie, images)
VALUES ('TU-3456-GH', 'Mercedes', 'E-Class', 2024, 5000, 'DISPONIBLE', 3,
'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=400,https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=400,https://images.unsplash.com/photo-1617531653332-bd46c24f2068?w=400');

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie, images)
VALUES ('TU-7890-IJ', 'BMW', 'X5', 2023, 20000, 'EN_MAINTENANCE', 2,
'https://images.unsplash.com/photo-1556189250-72ba954cfc2b?w=400,https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=400');

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie, images)
VALUES ('TU-1122-KL', 'Porsche', 'Macan', 2024, 3000, 'DISPONIBLE', 2,
'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=400,https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=400');

INSERT INTO vehicule (immatriculation, marque, modele, annee, kilometrage, statut, id_categorie, images)
VALUES ('TU-3344-MN', 'Range Rover', 'Sport', 2023, 18000, 'LOUE', 2,
'https://images.unsplash.com/photo-1606611013016-969c19ba27d5?w=400,https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=400');
