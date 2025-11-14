package training.salonzied.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import training.salonzied.dao.entities.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);

    long deleteByEmail(String email);

}
