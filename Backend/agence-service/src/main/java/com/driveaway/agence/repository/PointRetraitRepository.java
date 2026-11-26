package com.driveaway.agence.repository;

import com.driveaway.agence.model.PointRetrait;
import com.driveaway.agence.model.TypePointRetrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PointRetraitRepository extends JpaRepository<PointRetrait, Long>, JpaSpecificationExecutor<PointRetrait> {

    List<PointRetrait> findByAgenceIdOrderByIdAsc(Long agenceId);

    Optional<PointRetrait> findByIdAndAgenceId(Long id, Long agenceId);

    boolean existsByAgenceId(Long agenceId);

    boolean existsByAgenceIdAndAdresseIgnoreCase(Long agenceId, String adresse);

    boolean existsByAgenceIdAndAdresseIgnoreCaseAndIdNot(Long agenceId, String adresse, Long id);

    long countByAgenceId(Long agenceId);

    long countByActifTrue();

    long countByActifTrueAndRetraitAutoriseTrue();

    long countByActifTrueAndRetourAutoriseTrue();

    long countByActifTrueAndAutreVilleAutoriseTrue();

    @Query("SELECT COALESCE(SUM(p.capaciteMax), 0) FROM PointRetrait p WHERE p.actif = true")
    long sumCapaciteActive();

    @Query("SELECT p.typePoint, COUNT(p) FROM PointRetrait p GROUP BY p.typePoint")
    List<Object[]> countGroupByTypePoint();

    long countByAgenceIdAndActifTrueAndRetourAutoriseTrue(Long agenceId);

    long countByAgenceIdAndActifTrueAndRetourAutoriseTrueAndAutreVilleAutoriseTrue(Long agenceId);

    long countByTypePoint(TypePointRetrait typePoint);
}
