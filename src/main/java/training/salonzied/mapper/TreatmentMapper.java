package training.salonzied.mapper;

import com.salonized.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import training.salonzied.dao.entities.CategoryEntity;
import training.salonzied.dao.entities.TreatmentEntity;

@Mapper(componentModel = "spring")
public interface TreatmentMapper {

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  TreatmentEntity toTreatmentEntity(TreatmentRequest treatment);

  // Convertit Entity -> DTO (pour retour API)
  @Mapping(target = "categoryName", source = "category.name")
  Treatment treatmentToTreatmentDto(TreatmentEntity entity);

  @Mapping(target = "salonName", source = "salon.name")
  Category toTreatmentCategory(CategoryEntity categoryEntity);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  CategoryEntity toTreatmentCategoryEntity(CategoryRequest categoryRequest);

}
