package training.salonzied.dao.repo;

import training.salonzied.dao.entities.SalonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalonRepository extends JpaRepository<SalonEntity, Long> {
    Optional<SalonEntity> findByPublicId(String publicId);
}
