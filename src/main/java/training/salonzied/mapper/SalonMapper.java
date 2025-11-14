package training.salonzied.mapper;

import com.salonized.dto.*;
import org.mapstruct.*;
import training.salonzied.dao.entities.SalonEntity;

@Mapper(componentModel = "spring")
public interface SalonMapper {

  @Mapping(target = "publicId", expression = "java(java.util.UUID.randomUUID().toString())")
  @Mapping(target = "id", ignore = true) // jamais rempli depuis le client
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  SalonEntity toEntity(CreateSalonRequest request);

  // Convertit Entity -> DTO (pour retour API)
  Salon entityToSalonDto(SalonEntity entity);


}
