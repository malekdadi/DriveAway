package com.driveaway.vehicule.controller;

import com.driveaway.vehicule.dto.*;
import com.driveaway.vehicule.service.CategorieService;
import com.driveaway.vehicule.service.FileStorageService;
import com.driveaway.vehicule.service.VehiculeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backoffice")
public class BackofficeController {

    private final CategorieService categorieService;
    private final VehiculeService vehiculeService;
    private final FileStorageService fileStorageService;

    public BackofficeController(CategorieService categorieService,
                                VehiculeService vehiculeService,
                                FileStorageService fileStorageService) {
        this.categorieService = categorieService;
        this.vehiculeService = vehiculeService;
        this.fileStorageService = fileStorageService;
    }

    // ==================== Dashboard ====================

    @GetMapping
    public String dashboard(Model model) {
        Map<String, Long> stats = vehiculeService.countByStatut();
        List<VehiculeResponse> all = vehiculeService.findAll();

        model.addAttribute("totalVehicules", all.size());
        model.addAttribute("disponibles", stats.getOrDefault("DISPONIBLE", 0L));
        model.addAttribute("loues", stats.getOrDefault("LOUE", 0L));
        model.addAttribute("totalCategories", categorieService.findAll().size());
        model.addAttribute("recentVehicules", all);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Dashboard - DriveAway");
        model.addAttribute("contentTemplate", "dashboard");
        return "layout";
    }

    // ==================== Categories ====================

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categorieService.findAll());
        model.addAttribute("activePage", "categories");
        model.addAttribute("pageTitle", "Catégories - DriveAway");
        model.addAttribute("contentTemplate", "categories");
        return "layout";
    }

    @GetMapping("/categories/new")
    public String newCategorie(Model model) {
        model.addAttribute("categorie", new CategorieFormDTO());
        model.addAttribute("activePage", "categories");
        model.addAttribute("pageTitle", "Nouvelle catégorie - DriveAway");
        model.addAttribute("contentTemplate", "categorie-form");
        return "layout";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategorie(@PathVariable Long id, Model model) {
        CategorieResponse cat = categorieService.findById(id);
        CategorieFormDTO form = new CategorieFormDTO();
        form.setIdCategorie(cat.idCategorie());
        form.setNom(cat.nom());
        form.setDescription(cat.description());
        form.setStatut(cat.statut());
        form.setImageUrl(cat.imageUrl());

        model.addAttribute("categorie", form);
        model.addAttribute("activePage", "categories");
        model.addAttribute("pageTitle", "Modifier catégorie - DriveAway");
        model.addAttribute("contentTemplate", "categorie-form");
        return "layout";
    }

    @PostMapping("/categories/save")
    public String saveCategorie(@ModelAttribute CategorieFormDTO form, RedirectAttributes ra) {
        try {
            CategorieRequest request = new CategorieRequest(form.getNom(), form.getDescription(), form.getStatut());
            String imageUrl = form.getImageUrl();

            // Handle uploaded file if present
            if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
                imageUrl = fileStorageService.saveFile(form.getImageFile());
            }

            if (form.getIdCategorie() != null) {
                categorieService.update(form.getIdCategorie(), request);
                if (imageUrl != null) {
                    categorieService.updateImageUrl(form.getIdCategorie(), imageUrl);
                }
                ra.addFlashAttribute("success", "Catégorie modifiée avec succès");
            } else {
                CategorieResponse created = categorieService.create(request);
                if (imageUrl != null && !imageUrl.isBlank()) {
                    categorieService.updateImageUrl(created.idCategorie(), imageUrl);
                }
                ra.addFlashAttribute("success", "Catégorie créée avec succès");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/backoffice/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategorie(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categorieService.delete(id);
            ra.addFlashAttribute("success", "Catégorie supprimée");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/backoffice/categories";
    }

    // ==================== Vehicules ====================

    @GetMapping("/vehicules")
    public String listVehicules(Model model) {
        model.addAttribute("vehicules", vehiculeService.findAll());
        model.addAttribute("allCategories", categorieService.findAll());
        model.addAttribute("activePage", "vehicules");
        model.addAttribute("pageTitle", "Véhicules - DriveAway");
        model.addAttribute("contentTemplate", "vehicules");
        return "layout";
    }

    @GetMapping("/vehicules/new")
    public String newVehicule(Model model) {
        VehiculeFormDTO form = new VehiculeFormDTO();
        form.setStatut("DISPONIBLE");

        model.addAttribute("vehicule", form);
        model.addAttribute("allCategories", categorieService.findAll());
        model.addAttribute("activePage", "vehicules");
        model.addAttribute("pageTitle", "Nouveau véhicule - DriveAway");
        model.addAttribute("contentTemplate", "vehicule-form");
        return "layout";
    }

    @GetMapping("/vehicules/edit/{id}")
    public String editVehicule(@PathVariable Long id, Model model) {
        VehiculeResponse v = vehiculeService.findById(id);
        VehiculeFormDTO form = new VehiculeFormDTO();
        form.setIdVehicule(v.idVehicule());
        form.setImmatriculation(v.immatriculation());
        form.setMarque(v.marque());
        form.setModele(v.modele());
        form.setAnnee(v.annee());
        form.setKilometrage(v.kilometrage());
        form.setStatut(v.statut());
        form.setIdCategorie(v.categorie() != null ? v.categorie().idCategorie() : null);
        form.setImages(String.join(", ", v.images()));

        model.addAttribute("vehicule", form);
        model.addAttribute("allCategories", categorieService.findAll());
        model.addAttribute("activePage", "vehicules");
        model.addAttribute("pageTitle", "Modifier véhicule - DriveAway");
        model.addAttribute("contentTemplate", "vehicule-form");
        return "layout";
    }

    @PostMapping("/vehicules/save")
    public String saveVehicule(@ModelAttribute VehiculeFormDTO form, RedirectAttributes ra) {
        try {
            VehiculeRequest request = new VehiculeRequest(
                    form.getImmatriculation(), form.getMarque(), form.getModele(),
                    form.getAnnee(), form.getKilometrage(), form.getStatut(), form.getIdCategorie()
            );

            // Handle uploaded files if any
            List<String> newUploadedImages = fileStorageService.saveFiles(form.getImageFiles());
            String finalImages = form.getImages();

            if (!newUploadedImages.isEmpty()) {
                String uploadedStr = String.join(", ", newUploadedImages);
                if (finalImages != null && !finalImages.isBlank()) {
                    finalImages = finalImages + ", " + uploadedStr;
                } else {
                    finalImages = uploadedStr;
                }
            }

            if (form.getIdVehicule() != null) {
                vehiculeService.update(form.getIdVehicule(), request);
                if (finalImages != null) {
                    vehiculeService.updateImages(form.getIdVehicule(), finalImages);
                }
                ra.addFlashAttribute("success", "Véhicule modifié avec succès");
            } else {
                VehiculeResponse created = vehiculeService.create(request);
                if (finalImages != null && !finalImages.isBlank()) {
                    vehiculeService.updateImages(created.idVehicule(), finalImages);
                }
                ra.addFlashAttribute("success", "Véhicule ajouté avec succès");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/backoffice/vehicules";
    }

    @PostMapping("/vehicules/delete/{id}")
    public String deleteVehicule(@PathVariable Long id, RedirectAttributes ra) {
        try {
            vehiculeService.delete(id);
            ra.addFlashAttribute("success", "Véhicule supprimé");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/backoffice/vehicules";
    }
}
