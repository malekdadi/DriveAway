package tn.esprit.user_service.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;

    private String nom;
    private String prenom;

    @Column(unique = true)
    private String email;

    private String telephone;
    private String adresse;

    private LocalDateTime dateInscription;

    private String statut;
}
