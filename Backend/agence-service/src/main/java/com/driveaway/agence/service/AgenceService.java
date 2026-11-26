package com.driveaway.agence.service;

import com.driveaway.agence.dto.*;
import com.driveaway.agence.exception.BadRequestException;
import com.driveaway.agence.exception.ConflictException;
import com.driveaway.agence.exception.ResourceNotFoundException;
import com.driveaway.agence.mapper.AgenceMapper;
import com.driveaway.agence.model.Agence;
import com.driveaway.agence.model.PointRetrait;
import com.driveaway.agence.model.StatutAgence;
import com.driveaway.agence.model.TypePointRetrait;
import com.driveaway.agence.repository.AgenceRepository;
import com.driveaway.agence.repository.PointRetraitRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AgenceService {

    public static final ZoneId TUNIS_ZONE = ZoneId.of("Africa/Tunis");
    private static final String DEFAULT_JOURS_OUVERTURE = "LUN,MAR,MER,JEU,VEN,SAM";
    private static final double EARTH_RADIUS_KM = 6371.0;

    private static final List<String> CANONICAL_DAYS = List.of("LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM");
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "nom", "ville", "statut", "heureOuverture", "heureFermeture", "ouvert24h"
    );

    private static final Map<DayOfWeek, String> DAY_CODES = Map.of(
            DayOfWeek.MONDAY, "LUN",
            DayOfWeek.TUESDAY, "MAR",
            DayOfWeek.WEDNESDAY, "MER",
            DayOfWeek.THURSDAY, "JEU",
            DayOfWeek.FRIDAY, "VEN",
            DayOfWeek.SATURDAY, "SAM",
            DayOfWeek.SUNDAY, "DIM"
    );

    private static final Map<DayOfWeek, String> DAY_LABELS_FR = Map.of(
            DayOfWeek.MONDAY, "lundi",
            DayOfWeek.TUESDAY, "mardi",
            DayOfWeek.WEDNESDAY, "mercredi",
            DayOfWeek.THURSDAY, "jeudi",
            DayOfWeek.FRIDAY, "vendredi",
            DayOfWeek.SATURDAY, "samedi",
            DayOfWeek.SUNDAY, "dimanche"
    );

    private final AgenceRepository agenceRepository;
    private final PointRetraitRepository pointRetraitRepository;

    public AgenceService(AgenceRepository agenceRepository, PointRetraitRepository pointRetraitRepository) {
        this.agenceRepository = agenceRepository;
        this.pointRetraitRepository = pointRetraitRepository;
    }

    // ==================== CRUD & Recherche / Tri / Pagination ====================

    @Transactional(readOnly = true)
    public List<AgenceResponse> findAll(String ville,
                                        String nom,
                                        StatutAgence statut,
                                        Boolean ouvert24h,
                                        Boolean ouverteMaintenant,
                                        TypePointRetrait typePoint,
                                        Boolean retraitAutorise,
                                        Boolean retourAutorise,
                                        Boolean autreVilleAutorise,
                                        String sortBy,
                                        String direction) {
        Sort sort = buildSort(sortBy, direction);
        Specification<Agence> spec = buildSpecification(
                ville, nom, statut, ouvert24h, typePoint, retraitAutorise, retourAutorise, autreVilleAutorise
        );

        LocalDateTime now = LocalDateTime.now(TUNIS_ZONE);
        return agenceRepository.findAll(spec, sort).stream()
                .map(agence -> toResponse(agence, now))
                .filter(response -> ouverteMaintenant == null || response.ouverteMaintenant() == ouverteMaintenant)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AgenceResponse> findPage(String ville,
                                         String nom,
                                         StatutAgence statut,
                                         Boolean ouvert24h,
                                         TypePointRetrait typePoint,
                                         Boolean retraitAutorise,
                                         Boolean retourAutorise,
                                         Boolean autreVilleAutorise,
                                         int page,
                                         int size,
                                         String sortBy,
                                         String direction) {
        if (page < 0) {
            throw new BadRequestException("Le numéro de page doit être supérieur ou égal à 0.");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("La taille de page doit être comprise entre 1 et 100.");
        }

        Sort sort = buildSort(sortBy, direction);
        Specification<Agence> spec = buildSpecification(
                ville, nom, statut, ouvert24h, typePoint, retraitAutorise, retourAutorise, autreVilleAutorise
        );

        LocalDateTime now = LocalDateTime.now(TUNIS_ZONE);
        return agenceRepository.findAll(spec, PageRequest.of(page, size, sort))
                .map(agence -> toResponse(agence, now));
    }

    @Transactional(readOnly = true)
    public AgenceResponse findById(Long id) {
        Agence agence = getAgenceOrThrow(id);
        return toResponse(agence, LocalDateTime.now(TUNIS_ZONE));
    }

    @Transactional
    public AgenceResponse create(AgenceRequest request) {
        String nom = request.nom().trim();
        String ville = request.ville().trim();

        if (agenceRepository.existsByNomIgnoreCaseAndVilleIgnoreCase(nom, ville)) {
            throw new ConflictException("Une agence nommée '" + nom + "' existe déjà dans la ville '" + ville + "'.");
        }

        Agence agence = new Agence();
        applyAgenceRequest(agence, request);

        Agence saved = agenceRepository.save(agence);
        return toResponse(saved, LocalDateTime.now(TUNIS_ZONE));
    }

    @Transactional
    public AgenceResponse update(Long id, AgenceRequest request) {
        Agence agence = getAgenceOrThrow(id);
        String nom = request.nom().trim();
        String ville = request.ville().trim();

        if (agenceRepository.existsByNomIgnoreCaseAndVilleIgnoreCaseAndIdNot(nom, ville, id)) {
            throw new ConflictException("Une autre agence nommée '" + nom + "' existe déjà dans la ville '" + ville + "'.");
        }

        applyAgenceRequest(agence, request);
        Agence saved = agenceRepository.save(agence);
        return toResponse(saved, LocalDateTime.now(TUNIS_ZONE));
    }

    @Transactional
    public void delete(Long id) {
        Agence agence = getAgenceOrThrow(id);
        long countPoints = pointRetraitRepository.countByAgenceId(id);
        if (countPoints > 0) {
            throw new ConflictException(
                    "Impossible de supprimer l'agence '" + agence.getNom() + "' car elle possède encore "
                            + countPoints + " point(s) de retrait rattaché(s)."
            );
        }
        agenceRepository.delete(agence);
    }

    // ==================== Metiers Simples ====================

    @Transactional
    public AgenceResponse updateHoraires(Long id, AgenceHorairesPatchRequest request) {
        if (request.heureOuverture() == null && request.heureFermeture() == null
                && request.ouvert24h() == null && request.joursOuverture() == null) {
            throw new BadRequestException("Au moins un champ d'horaire doit être fourni pour la modification.");
        }

        Agence agence = getAgenceOrThrow(id);
        boolean ouvert24h = request.ouvert24h() != null ? request.ouvert24h() : Boolean.TRUE.equals(agence.getOuvert24h());
        LocalTime ouverture = request.heureOuverture() != null ? request.heureOuverture() : agence.getHeureOuverture();
        LocalTime fermeture = request.heureFermeture() != null ? request.heureFermeture() : agence.getHeureFermeture();
        String jours = request.joursOuverture() != null
                ? normalizeAndValidateDays(request.joursOuverture())
                : agence.getJoursOuverture();

        if (ouvert24h) {
            ouverture = LocalTime.MIDNIGHT;
            fermeture = LocalTime.of(23, 59, 59);
        } else {
            validateScheduleWindow(ouverture, fermeture);
        }

        agence.setOuvert24h(ouvert24h);
        agence.setHeureOuverture(ouverture);
        agence.setHeureFermeture(fermeture);
        agence.setJoursOuverture(jours);

        Agence saved = agenceRepository.save(agence);
        return toResponse(saved, LocalDateTime.now(TUNIS_ZONE));
    }

    @Transactional
    public AgenceResponse updateStatut(Long id, StatutAgence statut) {
        Agence agence = getAgenceOrThrow(id);
        agence.setStatut(statut);
        Agence saved = agenceRepository.save(agence);
        return toResponse(saved, LocalDateTime.now(TUNIS_ZONE));
    }

    @Transactional(readOnly = true)
    public OuvertureResponse verifierOuverture(Long id, LocalDateTime dateHeure) {
        Agence agence = getAgenceOrThrow(id);
        LocalDateTime target = dateHeure != null ? dateHeure : LocalDateTime.now(TUNIS_ZONE);
        OpeningEvaluation evaluation = evaluateOpening(agence, target);

        return new OuvertureResponse(
                agence.getId(),
                agence.getNom(),
                agence.getStatut(),
                target,
                evaluation.ouverte(),
                evaluation.raison(),
                agence.getHeureOuverture(),
                agence.getHeureFermeture(),
                agence.getOuvert24h(),
                agence.getJoursOuverture()
        );
    }

    @Transactional(readOnly = true)
    public RetourAutoriseResponse verifierRetourAutorise(Long id, boolean autreVille) {
        Agence agence = getAgenceOrThrow(id);
        boolean agenceActive = agence.getStatut() == StatutAgence.ACTIVE;

        if (!agenceActive) {
            String raison = agence.getStatut() == StatutAgence.FERMEE_TEMPORAIREMENT
                    ? "Retour refusé : l'agence est fermée temporairement."
                    : "Retour refusé : l'agence est fermée définitivement.";
            return new RetourAutoriseResponse(
                    agence.getId(), agence.getNom(), agence.getVille(),
                    false, false, false, 0, raison, List.of()
            );
        }

        List<PointRetrait> allPoints = pointRetraitRepository.findByAgenceIdOrderByIdAsc(id);
        List<PointRetrait> pointsRetour = allPoints.stream()
                .filter(p -> Boolean.TRUE.equals(p.getActif()) && Boolean.TRUE.equals(p.getRetourAutorise()))
                .toList();

        List<PointRetrait> pointsAutreVille = pointsRetour.stream()
                .filter(p -> Boolean.TRUE.equals(p.getAutreVilleAutorise()))
                .toList();

        List<PointRetrait> compatibles = autreVille ? pointsAutreVille : pointsRetour;
        boolean autorise = !compatibles.isEmpty();

        String raison;
        if (autorise) {
            raison = autreVille
                    ? "Retour inter-villes autorisé sur " + compatibles.size() + " point(s) actif(s)."
                    : "Retour autorisé sur " + compatibles.size() + " point(s) actif(s).";
        } else {
            raison = autreVille
                    ? "Aucun point actif de cette agence n'autorise le retour inter-villes."
                    : "Aucun point actif de cette agence n'autorise le retour de véhicule.";
        }

        return new RetourAutoriseResponse(
                agence.getId(),
                agence.getNom(),
                agence.getVille(),
                true,
                autorise,
                !pointsAutreVille.isEmpty(),
                compatibles.size(),
                raison,
                compatibles.stream().map(AgenceMapper::toPointResponse).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<AgenceProximiteResponse> findProximite(double latitude,
                                                       double longitude,
                                                       double rayonKm,
                                                       boolean activeUniquement) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new BadRequestException("La latitude doit être comprise entre -90 et 90.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new BadRequestException("La longitude doit être comprise entre -180 et 180.");
        }
        if (rayonKm <= 0.0 || rayonKm > 1000.0) {
            throw new BadRequestException("Le rayon de recherche doit être compris entre 0 (exclu) et 1000 km.");
        }

        LocalDateTime now = LocalDateTime.now(TUNIS_ZONE);
        return agenceRepository.findByLatitudeIsNotNullAndLongitudeIsNotNull().stream()
                .filter(a -> !activeUniquement || a.getStatut() == StatutAgence.ACTIVE)
                .map(a -> {
                    double distance = round2(haversineKm(latitude, longitude, a.getLatitude(), a.getLongitude()));
                    return new AgenceProximiteResponse(distance, toResponse(a, now));
                })
                .filter(item -> item.distanceKm() <= rayonKm)
                .sorted(Comparator.comparingDouble(AgenceProximiteResponse::distanceKm))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReseauStatistiquesResponse getStatistiques() {
        long totalAgences = agenceRepository.count();
        long actives = agenceRepository.countByStatut(StatutAgence.ACTIVE);
        long fermeesTemp = agenceRepository.countByStatut(StatutAgence.FERMEE_TEMPORAIREMENT);
        long fermeesDef = agenceRepository.countByStatut(StatutAgence.FERMEE_DEFINITIVEMENT);

        long totalPoints = pointRetraitRepository.count();
        long pointsActifs = pointRetraitRepository.countByActifTrue();
        long pointsRetrait = pointRetraitRepository.countByActifTrueAndRetraitAutoriseTrue();
        long pointsRetour = pointRetraitRepository.countByActifTrueAndRetourAutoriseTrue();
        long pointsAutreVille = pointRetraitRepository.countByActifTrueAndAutreVilleAutoriseTrue();
        long capaciteTotale = pointRetraitRepository.sumCapaciteActive();

        Map<String, Long> agencesParVille = new LinkedHashMap<>();
        for (Object[] row : agenceRepository.countGroupByVille()) {
            agencesParVille.put((String) row[0], (Long) row[1]);
        }

        Map<String, Long> pointsParType = new LinkedHashMap<>();
        for (TypePointRetrait type : TypePointRetrait.values()) {
            pointsParType.put(type.name(), 0L);
        }
        for (Object[] row : pointRetraitRepository.countGroupByTypePoint()) {
            TypePointRetrait type = (TypePointRetrait) row[0];
            pointsParType.put(type.name(), (Long) row[1]);
        }

        return new ReseauStatistiquesResponse(
                totalAgences, actives, fermeesTemp, fermeesDef,
                totalPoints, pointsActifs, pointsRetrait, pointsRetour, pointsAutreVille, capaciteTotale,
                agencesParVille, pointsParType
        );
    }

    // ==================== Helpers internes ====================

    public Agence getAgenceOrThrow(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence introuvable avec l'identifiant : " + id));
    }

    private void applyAgenceRequest(Agence agence, AgenceRequest request) {
        validateCoordinatesPair(request.latitude(), request.longitude());

        boolean ouvert24h = Boolean.TRUE.equals(request.ouvert24h());
        LocalTime ouverture = request.heureOuverture();
        LocalTime fermeture = request.heureFermeture();

        if (ouvert24h) {
            ouverture = LocalTime.MIDNIGHT;
            fermeture = LocalTime.of(23, 59, 59);
        } else {
            validateScheduleWindow(ouverture, fermeture);
        }

        String jours = (request.joursOuverture() == null || request.joursOuverture().isBlank())
                ? DEFAULT_JOURS_OUVERTURE
                : normalizeAndValidateDays(request.joursOuverture());

        StatutAgence statut = request.statut() != null ? request.statut() : StatutAgence.ACTIVE;

        agence.setNom(request.nom().trim());
        agence.setAdresse(request.adresse().trim());
        agence.setVille(request.ville().trim());
        agence.setTelephone(request.telephone().trim());
        agence.setHeureOuverture(ouverture);
        agence.setHeureFermeture(fermeture);
        agence.setOuvert24h(ouvert24h);
        agence.setJoursOuverture(jours);
        agence.setStatut(statut);
        agence.setLatitude(request.latitude());
        agence.setLongitude(request.longitude());
    }

    private void validateScheduleWindow(LocalTime ouverture, LocalTime fermeture) {
        if (ouverture == null || fermeture == null) {
            throw new BadRequestException("Les heures d'ouverture et de fermeture sont obligatoires.");
        }
        if (!fermeture.isAfter(ouverture)) {
            throw new BadRequestException("L'heure de fermeture (" + fermeture + ") doit être strictement postérieure à l'heure d'ouverture (" + ouverture + ").");
        }
    }

    private void validateCoordinatesPair(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new BadRequestException("La latitude et la longitude doivent être renseignées ensemble.");
        }
    }

    private String normalizeAndValidateDays(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("La liste des jours d'ouverture ne peut pas être vide.");
        }
        Set<String> tokens = new HashSet<>();
        for (String part : raw.split(",")) {
            String code = part.trim().toUpperCase(Locale.ROOT);
            if (code.isEmpty()) {
                continue;
            }
            if (!CANONICAL_DAYS.contains(code)) {
                throw new BadRequestException("Jour d'ouverture invalide : '" + code + "'. Valeurs autorisées : " + String.join(",", CANONICAL_DAYS));
            }
            tokens.add(code);
        }
        if (tokens.isEmpty()) {
            throw new BadRequestException("Au moins un jour d'ouverture valide doit être spécifié.");
        }
        return CANONICAL_DAYS.stream().filter(tokens::contains).reduce((a, b) -> a + "," + b).orElseThrow();
    }

    private OpeningEvaluation evaluateOpening(Agence agence, LocalDateTime dateTime) {
        if (agence.getStatut() == StatutAgence.FERMEE_TEMPORAIREMENT) {
            return new OpeningEvaluation(false, "L'agence est fermée temporairement.");
        }
        if (agence.getStatut() == StatutAgence.FERMEE_DEFINITIVEMENT) {
            return new OpeningEvaluation(false, "L'agence est fermée définitivement.");
        }

        DayOfWeek day = dateTime.getDayOfWeek();
        String dayCode = DAY_CODES.get(day);
        Set<String> openDays = Arrays.stream(agence.getJoursOuverture().split(","))
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        if (!openDays.contains(dayCode)) {
            return new OpeningEvaluation(false, "L'agence est fermée le " + DAY_LABELS_FR.get(day) + ".");
        }

        if (Boolean.TRUE.equals(agence.getOuvert24h())) {
            return new OpeningEvaluation(true, "L'agence est ouverte 24h/24.");
        }

        LocalTime time = dateTime.toLocalTime();
        boolean insideWindow = !time.isBefore(agence.getHeureOuverture()) && !time.isAfter(agence.getHeureFermeture());
        if (insideWindow) {
            return new OpeningEvaluation(true, "L'agence est ouverte au créneau demandé ("
                    + agence.getHeureOuverture() + " – " + agence.getHeureFermeture() + ").");
        }
        return new OpeningEvaluation(false, "En dehors des horaires d'ouverture ("
                + agence.getHeureOuverture() + " – " + agence.getHeureFermeture() + ").");
    }

    private Sort buildSort(String sortBy, String direction) {
        String field = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy.trim();
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new BadRequestException("Champ de tri invalide : '" + field + "'. Valeurs autorisées : " + ALLOWED_SORT_FIELDS);
        }
        String dir = (direction == null || direction.isBlank()) ? "asc" : direction.trim().toLowerCase(Locale.ROOT);
        if (!dir.equals("asc") && !dir.equals("desc")) {
            throw new BadRequestException("Direction de tri invalide : '" + direction + "'. Utilisez 'asc' ou 'desc'.");
        }
        return dir.equals("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
    }

    private Specification<Agence> buildSpecification(String ville,
                                                     String nom,
                                                     StatutAgence statut,
                                                     Boolean ouvert24h,
                                                     TypePointRetrait typePoint,
                                                     Boolean retraitAutorise,
                                                     Boolean retourAutorise,
                                                     Boolean autreVilleAutorise) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (ville != null && !ville.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("ville")), "%" + ville.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (nom != null && !nom.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nom")), "%" + nom.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (statut != null) {
                predicates.add(cb.equal(root.get("statut"), statut));
            }
            if (ouvert24h != null) {
                predicates.add(cb.equal(root.get("ouvert24h"), ouvert24h));
            }

            boolean filterByPoints = typePoint != null || retraitAutorise != null
                    || retourAutorise != null || autreVilleAutorise != null;
            if (filterByPoints) {
                query.distinct(true);
                Join<Agence, PointRetrait> pointsJoin = root.join("pointsRetrait", JoinType.INNER);
                predicates.add(cb.isTrue(pointsJoin.get("actif")));
                if (typePoint != null) {
                    predicates.add(cb.equal(pointsJoin.get("typePoint"), typePoint));
                }
                if (retraitAutorise != null) {
                    predicates.add(cb.equal(pointsJoin.get("retraitAutorise"), retraitAutorise));
                }
                if (retourAutorise != null) {
                    predicates.add(cb.equal(pointsJoin.get("retourAutorise"), retourAutorise));
                }
                if (autreVilleAutorise != null) {
                    predicates.add(cb.equal(pointsJoin.get("autreVilleAutorise"), autreVilleAutorise));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AgenceResponse toResponse(Agence agence, LocalDateTime referenceTime) {
        List<PointRetrait> points = pointRetraitRepository.findByAgenceIdOrderByIdAsc(agence.getId());
        boolean ouverte = evaluateOpening(agence, referenceTime).ouverte();
        return AgenceMapper.toAgenceResponse(agence, points, ouverte);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record OpeningEvaluation(boolean ouverte, String raison) {
    }
}
