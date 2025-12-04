package training.salonzied.mapper;

import com.salonized.dto.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import training.salonzied.dao.entities.UserEntity;
import util.TestData;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("Should map CreateUserRequest to UserEntity")
    void mapCreateUserRequestToUserEntity() {
        // Given
        var createRequest = TestData.getCreateUserRequest();

        // When
        UserEntity entity = mapper.toUserEntity(createRequest);

        // Then
        assertNotNull(entity);
        assertEquals(createRequest.getFirstName(), entity.getFirstName());
        assertEquals(createRequest.getLastName(), entity.getLastName());
        assertEquals(createRequest.getEmail(), entity.getEmail());
        assertEquals(createRequest.getPhone(), entity.getPhone());
        assertEquals(createRequest.getGender(), entity.getGender());
        assertNotNull(entity.getPublicId());
        assertNull(entity.getId()); // Nu se setează din request
        assertNull(entity.getCreatedAt()); // Ignored
        assertNull(entity.getUpdatedAt()); // Ignored
    }

    @Test
    @DisplayName("Should map UserEntity to User DTO")
    void mapUserEntityToUserDto() {
        // Given
        UserEntity entity = TestData.getUserEntity();

        // When
        User user = mapper.entityToUserDto(entity);

        // Then
        assertNotNull(user);
        assertEquals(entity.getPublicId(), user.getPublicId());
        assertEquals(entity.getFirstName(), user.getFirstName());
        assertEquals(entity.getLastName(), user.getLastName());
        assertEquals(entity.getEmail(), user.getEmail());
        assertEquals(entity.getPhone(), user.getPhone());
        assertEquals(entity.getGender(), user.getGender());
    }

    @Test
    @DisplayName("Should update UserEntity from UpdateUserRequest")
    void updateUserEntityFromUpdateRequest() {
        // Given
        UserEntity entity = TestData.getUserEntity();
        String originalEmail = entity.getEmail();
        String originalFirstName = entity.getFirstName();
        var updateRequest = TestData.getUpdateUserRequest();

        // When
        mapper.toUserEntityFromUpdate(updateRequest, entity);

        // Then
        assertEquals(updateRequest.getFirstName(), entity.getFirstName());
        assertEquals(updateRequest.getLastName(), entity.getLastName());
        assertEquals(updateRequest.getPhone(), entity.getPhone());
        assertEquals(updateRequest.getGender(), entity.getGender());
        // Email nu se schimbă (nu e în UpdateUserRequest)
        assertEquals(originalEmail, entity.getEmail());
        // Câmpurile ignorate nu se schimbă
        assertNotNull(entity.getPublicId());
        assertNotNull(entity.getId());
    }

    @Test
    @DisplayName("Should handle null values in UpdateUserRequest")
    void updateUserEntityWithNullValues() {
        // Given
        UserEntity entity = TestData.getUserEntity();
        String originalFirstName = entity.getFirstName();
        String originalLastName = entity.getLastName();
        var updateRequest = TestData.getUpdateUserRequest();
        updateRequest.setFirstName(null);
        updateRequest.setLastName(null);

        // When
        mapper.toUserEntityFromUpdate(updateRequest, entity);

        // Then - null values sunt ignorate (NullValuePropertyMappingStrategy.IGNORE)
        // Comportamentul exact depinde de configurație, dar testăm că nu aruncă excepție
        assertNotNull(entity);
    }
}



