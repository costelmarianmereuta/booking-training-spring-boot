package training.salonzied.service;

import com.salonized.dto.CreateUserRequest;
import com.salonized.dto.UpdateUserRequest;
import com.salonized.dto.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.entities.UserEntity;
import training.salonzied.dao.entities.UserRole;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.dao.repo.UserRepository;
import training.salonzied.error.BusinessRuleException;
import training.salonzied.error.EmployeeMustHaveSalonException;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.error.NoEmployeeAvailableException;
import training.salonzied.mapper.UserMapper;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SalonRepository salonRepository;

    public List<User> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        return userEntities.stream()
                .map(userMapper::entityToUserDto)
                .toList();
    }

    @Transactional
    public User addUser(CreateUserRequest request) {
        UserEntity userEntity = userMapper.toUserEntity(request);
        
        // Set default role to CLIENT if no roles provided
        if (userEntity.getRoles() == null || userEntity.getRoles().isEmpty()) {
            userEntity.setRoles(Set.of(UserRole.CLIENT));
            log.debug("No roles provided, setting default role to CLIENT");
        }
        
        // Set salon if provided
        if (request.getSalonPublicId() != null) {
            SalonEntity salon = salonRepository.findByPublicId(request.getSalonPublicId())
                    .orElseThrow(() -> new EntityNotFoundException("salon not found with publicId: {}", request.getSalonPublicId()));
            userEntity.setSalon(salon);
        }
        
        // Validate that EMPLOYEE/MANAGER roles have a salon
        validateEmployeeHasSalon(userEntity);
        
        // Validate that CLIENT cannot have working hours
        validateClientCannotHaveWorkingHours(userEntity);
        
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
        
        // Update salon if provided
        if (request.getSalonPublicId() != null) {
            SalonEntity salon = salonRepository.findByPublicId(request.getSalonPublicId())
                    .orElseThrow(() -> new EntityNotFoundException("salon not found with publicId: {}", request.getSalonPublicId()));
            userEntity.setSalon(salon);
        } else if (request.getSalonPublicId() == null && request.getRoles() != null) {
            // If roles are being updated and no salon is provided, check if we need to clear salon
            Set<UserRole> newRoles = userEntity.getRoles();
            if (newRoles != null && !newRoles.contains(UserRole.EMPLOYEE) && !newRoles.contains(UserRole.MANAGER)) {
                userEntity.setSalon(null);
            }
        }
        
        // Validate that EMPLOYEE/MANAGER roles have a salon
        validateEmployeeHasSalon(userEntity);
        
        // Validate that CLIENT cannot have working hours
        validateClientCannotHaveWorkingHours(userEntity);
        
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

    private void validateEmployeeHasSalon(UserEntity userEntity) {
        Set<UserRole> roles = userEntity.getRoles();
        if (roles != null && (roles.contains(UserRole.EMPLOYEE) || roles.contains(UserRole.MANAGER))) {
            if (userEntity.getSalon() == null) {
                throw new EmployeeMustHaveSalonException("User with EMPLOYEE or MANAGER role must have a salon assigned");
            }
        }
    }

    private void validateClientCannotHaveWorkingHours(UserEntity userEntity) {
        Set<UserRole> roles = userEntity.getRoles();
        if (roles != null && roles.contains(UserRole.CLIENT)) {
            if (userEntity.getWorkingHours() != null && !userEntity.getWorkingHours().isEmpty()) {
                throw new NoEmployeeAvailableException("User with CLIENT role cannot have working hours");
            }
        }
    }
}
