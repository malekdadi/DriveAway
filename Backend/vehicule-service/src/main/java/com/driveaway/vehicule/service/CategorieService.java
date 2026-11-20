package com.driveaway.vehicule.service;

import com.driveaway.vehicule.dto.CategorieRequest;
import com.driveaway.vehicule.dto.CategorieResponse;
import com.driveaway.vehicule.exception.ConflictException;
import com.driveaway.vehicule.exception.ResourceNotFoundException;
import com.driveaway.vehicule.mapper.VehiculeMapper;
import com.driveaway.vehicule.model.Categorie;
import com.driveaway.vehicule.repository.CategorieRepository;
import com.driveaway.vehicule.repository.VehiculeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final VehiculeRepository vehiculeRepository;

    public CategorieService(CategorieRepository categorieRepository, VehiculeRepository vehiculeRepository) {
        this.categorieRepository = categorieRepository;
        this.vehiculeRepository = vehiculeRepository;
    }

    // ==================== CRUD ====================

    @Transactional(readOnly = true)
    public List<CategorieResponse> findAll() {
        return categorieRepository.findAll().stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CategorieResponse> findPage(Pageable pageable) {
        return categorieRepository.findAll(pageable)
                .map(VehiculeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CategorieResponse findById(Long id) {
        return VehiculeMapper.toResponse(getCategorie(id));
    }

    public CategorieResponse create(CategorieRequest request) {
        if (categorieRepository.existsByNomIgnoreCase(request.nom().trim())) {
            throw new ConflictException("Une catégorie nommée '" + request.nom() + "' existe déjà");
        }
        Categorie categorie = VehiculeMapper.toEntity(request);
        return VehiculeMapper.toResponse(categorieRepository.save(categorie));
    }

    public CategorieResponse update(Long id, CategorieRequest request) {
        Categorie categorie = getCategorie(id);
        if (categorieRepository.existsByNomIgnoreCaseAndIdCategorieNot(request.nom().trim(), id)) {
            throw new ConflictException("Une catégorie nommée '" + request.nom() + "' existe déjà");
        }
        VehiculeMapper.updateEntity(categorie, request);
        return VehiculeMapper.toResponse(categorie);
    }

    /** A category that still has vehicles cannot be deleted. */
    public void delete(Long id) {
        Categorie categorie = getCategorie(id);
        if (vehiculeRepository.existsByCategorieIdCategorie(id)) {
            throw new ConflictException("La catégorie " + id + " contient encore des véhicules ; supprimez-les d'abord");
        }
        categorieRepository.delete(categorie);
    }

    // ==================== Search / Filter ====================

    @Transactional(readOnly = true)
    public List<CategorieResponse> findByStatut(Boolean statut) {
        return categorieRepository.findByStatut(statut).stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategorieResponse> search(String nom) {
        return categorieRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(VehiculeMapper::toResponse)
                .toList();
    }

    // ==================== Image ====================

    public CategorieResponse updateImageUrl(Long id, String imageUrl) {
        Categorie categorie = getCategorie(id);
        categorie.setImageUrl(imageUrl);
        return VehiculeMapper.toResponse(categorie);
    }

    // ==================== Helper ====================

    Categorie getCategorie(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie", id));
    }
}
