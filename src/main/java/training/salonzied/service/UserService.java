package training.salonzied.service;

import com.salonized.dto.CreateUserRequest;
import com.salonized.dto.UpdateUserRequest;
import com.salonized.dto.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import training.salonzied.dao.entities.UserEntity;
import training.salonzied.dao.repo.UserRepository;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<User> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        return userEntities.stream()
                .map(userMapper::entityToUserDto)
                .toList();
    }

    @Transactional
    public User addUser(CreateUserRequest request) {
        UserEntity userEntity = userMapper.toUserEntity(request);
        UserEntity createUser = userRepository.save(userEntity);
        log.debug("Created User: {}", userEntity);
        return userMapper.entityToUserDto(createUser);
    }

    @Transactional
    public User updateUser(UpdateUserRequest request, String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("user not found with this email: {}", email));
        // set the entity with the new data from the request
       userMapper.toUserEntityFromUpdate(request, userEntity);
        UserEntity savedEntity = userRepository.save(userEntity);
        log.debug("Updated User: {}", userEntity);
        return userMapper.entityToUserDto(savedEntity);
    }

    public User getUserByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("user not found with this email: {}", email));
        log.debug("User: {}", userEntity);
        return userMapper.entityToUserDto(userEntity);
    }

    @Transactional
    public void deleteUser(String email) {
        long deleted = userRepository.deleteByEmail(email);
        if (deleted == 0){
            throw new EntityNotFoundException("User", email);
        }
        log.debug("User deleted with email: {}", email);
    }
}
