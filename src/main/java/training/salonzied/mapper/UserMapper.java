package training.salonzied.mapper;

import com.salonized.dto.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "publicId", expression = "java(java.util.UUID.randomUUID().toString())")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  UserEntity toUserEntity(CreateUserRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "publicId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void toUserEntityFromUpdate(UpdateUserRequest request, @MappingTarget UserEntity updatedUser);

  // Convertit Entity -> DTO (pour retour API)
  User entityToUserDto(UserEntity entity);


}
