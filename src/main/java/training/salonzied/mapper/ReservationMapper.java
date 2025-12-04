package training.salonzied.mapper;

import com.salonized.dto.CreateUserRequest;
import com.salonized.dto.Reservation;
import com.salonized.dto.UpdateUserRequest;
import com.salonized.dto.User;
import org.mapstruct.*;
import training.salonzied.dao.entities.ReservationEntity;
import training.salonzied.dao.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

  @Mapping(target = "publicId", expression = "java(java.util.UUID.randomUUID().toString())")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  UserEntity toReservationEntity(CreateUserRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "publicId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void toUserEntityFromUpdate(UpdateUserRequest request, @MappingTarget UserEntity updatedUser);



    @Mapping(target = "salonPublicId", source = "salon.publicId")
    @Mapping(target = "userPublicId", source = "user.publicId")
    @Mapping(target = "treatmentName", source = "treatment.name")
    @Mapping(target = "priceOfBooking", source = "treatment.price")
    @Mapping(
            target = "endTime",
            expression = "java(entity.getStartTime().plusMinutes(entity.getDurationOfBooking()))"
    )
  // Convertit Entity -> DTO (pour retour API)
  Reservation entityToReservationDto(ReservationEntity entity);


}
