package training.salonzied.dao.repo;

import com.salonized.dto.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import training.salonzied.dao.entities.ReservationEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long>, JpaSpecificationExecutor<ReservationEntity> {
    Optional<ReservationEntity> findByPublicId(String publicId);

    long deleteByPublicId(String publicId);

    @Query("SELECT r FROM ReservationEntity r " +
           "WHERE r.employee.id = :employeeId " +
           "AND r.status != :cancelledStatus " +
           "AND r.startTime < :endTime " +
           "AND (:excludePublicId IS NULL OR r.publicId != :excludePublicId)")
    List<ReservationEntity> findPotentialConflictingReservationsForEmployee(
            @Param("employeeId") Long employeeId,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludePublicId") String excludePublicId,
            @Param("cancelledStatus") ReservationStatus cancelledStatus);
}
