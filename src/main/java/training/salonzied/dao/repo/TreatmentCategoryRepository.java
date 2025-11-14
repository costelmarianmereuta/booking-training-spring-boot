package training.salonzied.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import training.salonzied.dao.entities.CategoryEntity;

import java.util.Optional;

public interface TreatmentCategoryRepository extends JpaRepository<CategoryEntity,Long> {
    boolean existsByName(String name);
    Optional<CategoryEntity> findByName(String name);
    long  deleteByName(String name);
}
