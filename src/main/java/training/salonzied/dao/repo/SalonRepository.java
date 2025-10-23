package training.salonzied.dao.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import training.salonzied.dao.entities.SalonEntity;

@Repository
public interface SalonRepository extends JpaRepository<SalonEntity, Long> {
  Optional<SalonEntity> findByPublicId(String publicId);
}
