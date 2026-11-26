package tn.esprit.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.user_service.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    List<User> findByNomContainingIgnoreCase(String nom);

    Optional<User> findByEmailIgnoreCase(String email);
}