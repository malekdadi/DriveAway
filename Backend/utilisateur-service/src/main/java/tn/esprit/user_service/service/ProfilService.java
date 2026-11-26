package tn.esprit.user_service.service;

import org.springframework.stereotype.Service;
import tn.esprit.user_service.entity.Profil;
import tn.esprit.user_service.entity.User;
import tn.esprit.user_service.repository.ProfilRepository;
import tn.esprit.user_service.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProfilService {

    private final ProfilRepository profilRepository;
    private final UserRepository userRepository;

    public ProfilService(ProfilRepository profilRepository, UserRepository userRepository) {
        this.profilRepository = profilRepository;
        this.userRepository = userRepository;
    }

    // CREATE
    public Profil ajouterProfil(Profil profil) {
        if (profil.getUser() == null || profil.getUser().getIdUser() == null) {
            throw new RuntimeException("user.idUser est obligatoire");
        }

        Long userId = profil.getUser().getIdUser();
        if (profilRepository.existsByUserIdUser(userId)) {
            throw new RuntimeException("Ce user possède déjà un profil");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User introuvable"));

        profil.setUser(user);
        return profilRepository.save(profil);
    }

    // READ ALL
    public List<Profil> afficherProfils() {
        return profilRepository.findAll();
    }

    // READ ONE
    public Profil afficherProfil(Long id) {
        return profilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
    }

    // READ BY USER
    public Profil afficherProfilParUser(Long userId) {
        return profilRepository.findByUserIdUser(userId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
    }

    // UPDATE
    public Profil modifierProfil(Long id, Profil nouveauProfil) {
        Profil profil = afficherProfil(id);

        profil.setNumeroPermis(nouveauProfil.getNumeroPermis());
        profil.setDateObtentionPermis(nouveauProfil.getDateObtentionPermis());
        profil.setDateExpirationPermis(nouveauProfil.getDateExpirationPermis());
        profil.setPaysDelivrance(nouveauProfil.getPaysDelivrance());

        return profilRepository.save(profil);
    }

    // DELETE
    public void supprimerProfil(Long id) {
        Profil profil = afficherProfil(id);
        profilRepository.delete(profil);
    }

    // MÉTIER : VALIDATION DU PERMIS
    public boolean verifierPermis(Long id) {
        Profil profil = afficherProfil(id);

        return profil.getDateExpirationPermis() != null
                && profil.getDateExpirationPermis().isAfter(LocalDate.now());
    }
}