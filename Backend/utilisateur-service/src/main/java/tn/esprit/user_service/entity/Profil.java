package tn.esprit.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "profils")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProfil;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String numeroPermis;
    private LocalDate dateObtentionPermis;
    private LocalDate dateExpirationPermis;
    private String paysDelivrance;
}