package training.salonzied.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import training.salonzied.dao.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPublicId(String publicId);

    long deleteByEmail(String email);

    @Query("SELECT DISTINCT u FROM UserEntity u " +
           "LEFT JOIN FETCH u.workingHours " +
           "WHERE u.salon.id = :salonId")
    List<UserEntity> findEmployeesBySalonId(@Param("salonId") Long salonId);
}
