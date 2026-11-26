-- =====================================================================
-- Donnees initiales pour agence-service (Base H2 dediee)
-- Alignees avec les agences de demonstration du Frontend Angular
-- =====================================================================

INSERT INTO agence (nom, adresse, ville, telephone, heure_ouverture, heure_fermeture, ouvert_24h, jours_ouverture, statut, latitude, longitude)
VALUES ('Tunis-Carthage', 'Aéroport International Tunis-Carthage, Zone Arrivées', 'Tunis', '+216 71 755 000', '00:00:00', '23:59:59', true, 'LUN,MAR,MER,JEU,VEN,SAM,DIM', 'ACTIVE', 36.8510, 10.2272);

INSERT INTO agence (nom, adresse, ville, telephone, heure_ouverture, heure_fermeture, ouvert_24h, jours_ouverture, statut, latitude, longitude)
VALUES ('La Marsa', '14 Avenue Habib Bourguiba, La Marsa', 'Tunis', '+216 71 742 310', '08:00:00', '20:00:00', false, 'LUN,MAR,MER,JEU,VEN,SAM', 'ACTIVE', 36.8784, 10.3247);

INSERT INTO agence (nom, adresse, ville, telephone, heure_ouverture, heure_fermeture, ouvert_24h, jours_ouverture, statut, latitude, longitude)
VALUES ('Sousse', 'Avenue de la Corniche, Boujaafar', 'Sousse', '+216 73 228 450', '08:00:00', '19:00:00', false, 'LUN,MAR,MER,JEU,VEN,SAM', 'ACTIVE', 35.8256, 10.6369);

INSERT INTO agence (nom, adresse, ville, telephone, heure_ouverture, heure_fermeture, ouvert_24h, jours_ouverture, statut, latitude, longitude)
VALUES ('Sfax Centre', '22 Avenue Majida Boulila, Sfax', 'Sfax', '+216 74 401 920', '08:30:00', '18:30:00', false, 'LUN,MAR,MER,JEU,VEN', 'ACTIVE', 34.7406, 10.7603);

INSERT INTO agence (nom, adresse, ville, telephone, heure_ouverture, heure_fermeture, ouvert_24h, jours_ouverture, statut, latitude, longitude)
VALUES ('Hammamet Nord', 'Route Touristique Zone Mrezga, Hammamet', 'Hammamet', '+216 72 281 110', '09:00:00', '18:00:00', false, 'LUN,MAR,MER,JEU,VEN,SAM', 'FERMEE_TEMPORAIREMENT', 36.4000, 10.6167);

-- Points de retrait rattaches aux agences
INSERT INTO point_retrait (nom, adresse, type_point, retrait_autorise, retour_autorise, autre_ville_autorise, actif, capacite_max, instructions_acces, latitude, longitude, agence_id)
VALUES ('Terminal 1 — Hall Arrivées', 'Aéroport Tunis-Carthage, Terminal 1, Guichet n°4', 'AEROPORT', true, true, true, true, 40, 'À droite après la sortie douane, parking dédié niveau P1.', 36.8512, 10.2275, 1);

INSERT INTO point_retrait (nom, adresse, type_point, retrait_autorise, retour_autorise, autre_ville_autorise, actif, capacite_max, instructions_acces, latitude, longitude, agence_id)
VALUES ('Parking Express P2', 'Aéroport Tunis-Carthage, Parking P2 Allée B', 'AEROPORT', true, true, true, true, 25, 'Accès direct zone loueurs, boîte à clés 24h/24.', 36.8501, 10.2289, 1);

INSERT INTO point_retrait (nom, adresse, type_point, retrait_autorise, retour_autorise, autre_ville_autorise, actif, capacite_max, instructions_acces, latitude, longitude, agence_id)
VALUES ('Agence Corniche La Marsa', '14 Avenue Habib Bourguiba, La Marsa Plage', 'CENTRE_VILLE', true, true, true, true, 18, 'Stationnement réservé devant l''agence.', 36.8784, 10.3247, 2);

INSERT INTO point_retrait (nom, adresse, type_point, retrait_autorise, retour_autorise, autre_ville_autorise, actif, capacite_max, instructions_acces, latitude, longitude, agence_id)
VALUES ('Relais Les Berges du Lac 2', 'Rue de la Feuille d''Érable, Lac 2, Tunis', 'HOTEL', true, false, false, true, 12, 'Retrait uniquement sur réservation confirmée, accueil conciergerie.', 36.8435, 10.2681, 2);

INSERT INTO point_retrait (nom, adresse, type_point, retrait_autorise, retour_autorise, autre_ville_autorise, actif, capacite_max, instructions_acces, latitude, longitude, agence_id)
VALUES ('Corniche Boujaafar', 'Avenue de la Corniche, face hôtel Riadh Palms', 'CENTRE_VILLE', true, true, true, true, 22, 'Guichet principal au rez-de-chaussée.', 35.8256, 10.6369, 3);

INSERT INTO point_retrait (nom, adresse, type_point, retrait_autorise, retour_autorise, autre_ville_autorise, actif, capacite_max, instructions_acces, latitude, longitude, agence_id)
VALUES ('Marina Port El Kantaoui', 'Zone Portuaire El Kantaoui, Hammam Sousse', 'PORT', true, true, false, true, 15, 'Parking capitainerie, retour même ville uniquement.', 35.8925, 10.5944, 3);

INSERT INTO point_retrait (nom, adresse, type_point, retrait_autorise, retour_autorise, autre_ville_autorise, actif, capacite_max, instructions_acces, latitude, longitude, agence_id)
VALUES ('Gare SNCFT Sfax', 'Place de la Gare, Centre-ville Sfax', 'GARE', true, true, true, true, 14, 'Comptoir DriveAway à la sortie principale de la gare.', 34.7378, 10.7618, 4);
