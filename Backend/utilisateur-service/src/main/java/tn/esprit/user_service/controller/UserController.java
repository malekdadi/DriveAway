package tn.esprit.user_service.controller;

import tn.esprit.user_service.entity.User;
import tn.esprit.user_service.service.UserService;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // CREATE
    @PostMapping
    public User ajouter(@RequestBody User user) {
        return userService.ajouterUser(user);
    }

    // READ ALL
    @GetMapping
    public List<User> afficherTous() {
        return userService.afficherUsers();
    }

    // READ ONE
    @GetMapping("/{id}")
    public User afficher(@PathVariable Long id) {
        return userService.afficherUser(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public User modifier(@PathVariable Long id,
                         @RequestBody User user) {
        return userService.modifierUser(id, user);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        userService.supprimerUser(id);
    }

    // MÉTIER : BLOCK
    @PutMapping("/{id}/bloquer")
    public User bloquer(@PathVariable Long id) {
        return userService.bloquerUser(id);
    }

    // MÉTIER : ACTIVATE
    @PutMapping("/{id}/activer")
    public User activer(@PathVariable Long id) {
        return userService.activerUser(id);
    }
    // SEARCH BY NOM OR EMAIL
    @GetMapping("/search")
    public List<User> rechercher(@RequestParam(required = false) String nom,
                                 @RequestParam(required = false) String email) {
        if (nom != null && !nom.isBlank()) {
            return userService.rechercherParNom(nom);
        }
        if (email != null && !email.isBlank()) {
            return userService.rechercherParEmail(email);
        }
        return userService.afficherUsers();
    }
}