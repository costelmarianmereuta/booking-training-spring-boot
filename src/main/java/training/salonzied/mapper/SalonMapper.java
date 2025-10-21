package training.salonzied.mapper;

import com.salonized.dto.*;
import training.salonzied.dao.entities.SalonEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SalonMapper {



    @Mapping(target = "publicId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "id", ignore = true)       // jamais rempli depuis le client
    @Mapping(target = "createdAt", ignore = true)
    SalonEntity toEntity(CreateSalonRequest request);

    // Convertit Entity -> DTO (pour retour API)
    Salon entityToSalonDto(SalonEntity entity);
}
