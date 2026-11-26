package tn.esprit.user_service.service;

import tn.esprit.user_service.entity.User;
import tn.esprit.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // CREATE
    public User ajouterUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        user.setDateInscription(LocalDateTime.now());
        user.setStatut("ACTIVE");

        return userRepository.save(user);
    }

    // READ ALL
    public List<User> afficherUsers() {
        return userRepository.findAll();
    }

    // READ ONE
    public User afficherUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User introuvable"));
    }

    // UPDATE
    public User modifierUser(Long id, User nouveauUser) {
        User user = afficherUser(id);

        user.setNom(nouveauUser.getNom());
        user.setPrenom(nouveauUser.getPrenom());
        user.setEmail(nouveauUser.getEmail());
        user.setTelephone(nouveauUser.getTelephone());
        user.setAdresse(nouveauUser.getAdresse());

        return userRepository.save(user);
    }

    // DELETE
    public void supprimerUser(Long id) {
        User user = afficherUser(id);
        userRepository.delete(user);
    }

    // MÉTIER SIMPLE : BLOCK USER
    public User bloquerUser(Long id) {
        User user = afficherUser(id);
        user.setStatut("BLOCKED");

        return userRepository.save(user);
    }

    // MÉTIER SIMPLE : ACTIVATE USER
    public User activerUser(Long id) {
        User user = afficherUser(id);
        user.setStatut("ACTIVE");

        return userRepository.save(user);
    }

    // SEARCH BY NOM
    public List<User> rechercherParNom(String nom) {
        return userRepository.findByNomContainingIgnoreCase(nom);
    }

    // SEARCH BY EMAIL
    public List<User> rechercherParEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(List::of)
                .orElseGet(List::of);
    }
}