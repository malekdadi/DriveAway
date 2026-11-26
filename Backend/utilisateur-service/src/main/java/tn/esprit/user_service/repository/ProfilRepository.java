package tn.esprit.user_service.repository;

import tn.esprit.user_service.entity.Profil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilRepository extends JpaRepository<Profil, Long> {

    Optional<Profil> findByUserIdUser(Long userId);

    boolean existsByUserIdUser(Long userId);
}