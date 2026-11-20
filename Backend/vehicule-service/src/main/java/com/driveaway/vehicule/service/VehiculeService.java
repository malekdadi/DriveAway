package com.driveaway.vehicule.service;

import com.driveaway.vehicule.dto.VehiculeRequest;
import com.driveaway.vehicule.dto.VehiculeResponse;
import com.driveaway.vehicule.exception.ConflictException;
import com.driveaway.vehicule.exception.ResourceNotFoundException;
import com.driveaway.vehicule.mapper.VehiculeMapper;
import com.driveaway.vehicule.model.Categorie;
import com.driveaway.vehicule.model.Vehicule;
import com.driveaway.vehicule.repository.CategorieRepository;
import com.driveaway.vehicule.repository.VehiculeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final CategorieRepository categorieRepository;

    public VehiculeService(VehiculeRepository vehiculeRepository, CategorieRepository categorieRepository) {
        this.vehiculeRepository = vehiculeRepository;
        this.categorieRepository = categorieRepository;
    }

    // ==================== CRUD ====================

    @Transactional(readOnly = true)
    public List<VehiculeResponse> findAll() {
        return vehiculeRepository.findAll().stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehiculeResponse findById(Long id) {
        return VehiculeMapper.toResponse(getVehicule(id));
    }

    public VehiculeResponse create(VehiculeRequest request) {
        if (vehiculeRepository.existsByImmatriculation(request.immatriculation().trim())) {
            throw new ConflictException("Un véhicule avec l'immatriculation '" + request.immatriculation() + "' existe déjà");
        }
        Categorie categorie = getRequiredCategorie(request.idCategorie());
        Vehicule vehicule = new Vehicule();
        VehiculeMapper.updateEntity(vehicule, request, categorie);
        return VehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    public VehiculeResponse update(Long id, VehiculeRequest request) {
        Vehicule vehicule = getVehicule(id);
        if (vehiculeRepository.existsByImmatriculationAndIdVehiculeNot(request.immatriculation().trim(), id)) {
            throw new ConflictException("Un véhicule avec l'immatriculation '" + request.immatriculation() + "' existe déjà");
        }
        Categorie categorie = getRequiredCategorie(request.idCategorie());
        VehiculeMapper.updateEntity(vehicule, request, categorie);
        return VehiculeMapper.toResponse(vehicule);
    }

    public void delete(Long id) {
        Vehicule vehicule = getVehicule(id);
        vehiculeRepository.delete(vehicule);
    }

    // ==================== Search / Filter ====================

    @Transactional(readOnly = true)
    public List<VehiculeResponse> findByStatut(String statut) {
        return vehiculeRepository.findByStatut(statut).stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehiculeResponse> findByCategorie(Long idCategorie) {
        // Also validates category exists
        getRequiredCategorie(idCategorie);
        return vehiculeRepository.findByCategorieIdCategorie(idCategorie).stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehiculeResponse> searchByMarque(String marque) {
        return vehiculeRepository.findByMarqueContainingIgnoreCase(marque).stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehiculeResponse findByImmatriculation(String immatriculation) {
        Vehicule vehicule = vehiculeRepository.findByImmatriculation(immatriculation)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule with immatriculation " + immatriculation, 0L));
        return VehiculeMapper.toResponse(vehicule);
    }

    @Transactional(readOnly = true)
    public List<VehiculeResponse> findDisponibles() {
        return vehiculeRepository.findByStatut("DISPONIBLE").stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    // ==================== Status & Kilometrage (inter-service) ====================

    public VehiculeResponse updateStatut(Long id, String newStatut) {
        Vehicule vehicule = getVehicule(id);
        vehicule.setStatut(newStatut);
        return VehiculeMapper.toResponse(vehicule);
    }

    public VehiculeResponse updateKilometrage(Long id, Integer newKilometrage) {
        Vehicule vehicule = getVehicule(id);
        vehicule.setKilometrage(newKilometrage);
        return VehiculeMapper.toResponse(vehicule);
    }

    // ==================== Statistics ====================

    @Transactional(readOnly = true)
    public Map<String, Long> countByStatut() {
        return vehiculeRepository.findAll().stream()
                .collect(Collectors.groupingBy(Vehicule::getStatut, Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public long countByCategorie(Long idCategorie) {
        return vehiculeRepository.countByCategorieIdCategorie(idCategorie);
    }

    // ==================== Helpers ====================

    Vehicule getVehicule(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    private Categorie getRequiredCategorie(Long idCategorie) {
        return categorieRepository.findById(idCategorie)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie", idCategorie));
    }
}
