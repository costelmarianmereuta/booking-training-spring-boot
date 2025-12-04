package training.salonzied.service;

import com.salonized.dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import training.salonzied.dao.entities.UserEntity;
import training.salonzied.dao.repo.UserRepository;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.mapper.UserMapper;
import util.TestData;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;
    private User userDto;

    @BeforeEach
    void setUp() {
        userEntity = TestData.getUserEntity();
        userDto = new User();
        userDto.setPublicId(userEntity.getPublicId());
        userDto.setEmail(userEntity.getEmail());
        userDto.setFirstName(userEntity.getFirstName());
        userDto.setLastName(userEntity.getLastName());
    }

    @Test
    @DisplayName("Should get all users successfully")
    void getAllUsers_Success() {
        // Given
        List<UserEntity> entities = List.of(userEntity);
        when(userRepository.findAll()).thenReturn(entities);
        when(userMapper.entityToUserDto(userEntity)).thenReturn(userDto);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).entityToUserDto(userEntity);
    }

    @Test
    @DisplayName("Should add user successfully")
    void addUser_Success() {
        // Given
        var createRequest = TestData.getCreateUserRequest();
        when(userMapper.toUserEntity(createRequest)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.entityToUserDto(userEntity)).thenReturn(userDto);

        // When
        User result = userService.addUser(createRequest);

        // Then
        assertNotNull(result);
        verify(userMapper, times(1)).toUserEntity(createRequest);
        verify(userRepository, times(1)).save(userEntity);
        verify(userMapper, times(1)).entityToUserDto(userEntity);
    }

    @Test
    @DisplayName("Should update user successfully")
    void updateUser_Success() {
        // Given
        String email = userEntity.getEmail();
        var updateRequest = TestData.getUpdateUserRequest();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.entityToUserDto(userEntity)).thenReturn(userDto);

        // When
        User result = userService.updateUser(updateRequest, email);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userMapper, times(1)).toUserEntityFromUpdate(updateRequest, userEntity);
        verify(userRepository, times(1)).save(userEntity);
        verify(userMapper, times(1)).entityToUserDto(userEntity);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating non-existent user")
    void updateUser_NotFound() {
        // Given
        String email = "non-existent@example.com";
        var updateRequest = TestData.getUpdateUserRequest();
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(updateRequest, email)
        );

        assertTrue(exception.getMessage().contains(email) || exception.getMessage().contains("user"));
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void getUserByEmail_Success() {
        // Given
        String email = userEntity.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToUserDto(userEntity)).thenReturn(userDto);

        // When
        User result = userService.getUserByEmail(email);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userMapper, times(1)).entityToUserDto(userEntity);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found by email")
    void getUserByEmail_NotFound() {
        // Given
        String email = "non-existent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserByEmail(email)
        );

        assertTrue(exception.getMessage().contains(email) || exception.getMessage().contains("user"));
        verify(userRepository, times(1)).findByEmail(email);
        verify(userMapper, never()).entityToUserDto(any());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void deleteUser_Success() {
        // Given
        String email = userEntity.getEmail();
        when(userRepository.deleteByEmail(email)).thenReturn(1L);

        // When
        userService.deleteUser(email);

        // Then
        verify(userRepository, times(1)).deleteByEmail(email);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting non-existent user")
    void deleteUser_NotFound() {
        // Given
        String email = "non-existent@example.com";
        when(userRepository.deleteByEmail(email)).thenReturn(0L);

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUser(email)
        );

        assertTrue(exception.getMessage().contains(email) || exception.getMessage().contains("User"));
        verify(userRepository, times(1)).deleteByEmail(email);
    }
}

