package training.salonzied.dao.repo;

import com.salonized.dto.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import training.salonzied.dao.entities.TreatmentEntity;

import java.util.Optional;

public interface TreatmentRepository extends JpaRepository<TreatmentEntity,Integer> {
    boolean existsByName(String name);
    Optional<TreatmentEntity> findByName(String name);
    long deleteByName(String name);
}
