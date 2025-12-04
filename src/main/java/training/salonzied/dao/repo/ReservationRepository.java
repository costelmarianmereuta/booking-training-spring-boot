package training.salonzied.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import training.salonzied.dao.entities.ReservationEntity;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long>, JpaSpecificationExecutor<ReservationEntity> {
    Optional<ReservationEntity> findByPublicId(String publicId);

    long deleteByPublicId(String publicId);
}
