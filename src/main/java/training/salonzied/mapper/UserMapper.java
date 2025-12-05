package training.salonzied.mapper;

import com.salonized.dto.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.entities.UserEntity;
import training.salonzied.dao.entities.UserRole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "publicId", expression = "java(java.util.UUID.randomUUID().toString())")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "salon", ignore = true)
  @Mapping(target = "roles", expression = "java(mapRolesFromDto(request.getRoles()))")
  UserEntity toUserEntity(CreateUserRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "publicId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "salon", ignore = true)
  @Mapping(target = "roles", expression = "java(request.getRoles() != null ? mapRolesFromDto(request.getRoles()) : null)")
  void toUserEntityFromUpdate(UpdateUserRequest request, @MappingTarget UserEntity updatedUser);

  @Mapping(target = "salonPublicId", source = "salon.publicId")
  @Mapping(target = "roles", expression = "java(mapRolesToDto(entity.getRoles()))")
  User entityToUserDto(UserEntity entity);

  default Set<UserRole> mapRolesFromDto(List<com.salonized.dto.UserRole> dtoRoles) {
    // Return null if empty/null - service layer will set default to CLIENT
    if (dtoRoles == null || dtoRoles.isEmpty()) {
      return null;
    }
    return dtoRoles.stream()
            .map(dtoRole -> {
              try {
                return UserRole.valueOf(dtoRole.getValue());
              } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + dtoRole.getValue());
              }
            })
            .collect(Collectors.toSet());
  }

  default List<com.salonized.dto.UserRole> mapRolesToDto(Set<UserRole> entityRoles) {
    if (entityRoles == null || entityRoles.isEmpty()) {
      return List.of();
    }
    return entityRoles.stream()
            .map(entityRole -> {
              try {
                return com.salonized.dto.UserRole.fromValue(entityRole.name());
              } catch (Exception e) {
                throw new IllegalArgumentException("Invalid role: " + entityRole.name());
              }
            })
            .collect(Collectors.toList());
  }
}
