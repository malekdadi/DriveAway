package tn.esprit.user_service.controller;

import tn.esprit.user_service.entity.Profil;
import tn.esprit.user_service.service.ProfilService;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profils")
@CrossOrigin("*")
public class ProfilController {

    private final ProfilService profilService;

    public ProfilController(ProfilService profilService) {
        this.profilService = profilService;
    }

    // CREATE
    @PostMapping
    public Profil ajouter(@RequestBody Profil profil) {
        return profilService.ajouterProfil(profil);
    }

    // READ ALL
    @GetMapping
    public List<Profil> afficherTous() {
        return profilService.afficherProfils();
    }

    // READ ONE
    @GetMapping("/{id}")
    public Profil afficher(@PathVariable Long id) {
        return profilService.afficherProfil(id);
    }

    // READ BY USER
    @GetMapping("/user/{userId}")
    public Profil afficherParUser(@PathVariable Long userId) {
        return profilService.afficherProfilParUser(userId);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Profil modifier(@PathVariable Long id,
                           @RequestBody Profil profil) {
        return profilService.modifierProfil(id, profil);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        profilService.supprimerProfil(id);
    }

    // MÉTIER : VALIDATION DU PERMIS
    @GetMapping("/{id}/permis-valide")
    public boolean verifierPermis(@PathVariable Long id) {
        return profilService.verifierPermis(id);
    }
}