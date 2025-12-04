package training.salonzied.mapper;

import com.salonized.dto.Category;
import com.salonized.dto.Treatment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import training.salonzied.dao.entities.CategoryEntity;
import training.salonzied.dao.entities.TreatmentEntity;
import util.TestData;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TreatmentMapper Unit Tests")
class TreatmentMapperTest {

    private final TreatmentMapper mapper = Mappers.getMapper(TreatmentMapper.class);

    @Test
    @DisplayName("Should map TreatmentRequest to TreatmentEntity")
    void mapTreatmentRequestToTreatmentEntity() {
        // Given
        var treatmentRequest = TestData.getTreatmentRequest();

        // When
        TreatmentEntity entity = mapper.toTreatmentEntity(treatmentRequest);

        // Then
        assertNotNull(entity);
        assertEquals(treatmentRequest.getName(), entity.getName());
        assertEquals(treatmentRequest.getDescription(), entity.getDescription());
        assertEquals(treatmentRequest.getPrice(), entity.getPrice());
        assertEquals(treatmentRequest.getDuration(), entity.getDuration());
        assertEquals(treatmentRequest.getTimeBetweenTreatments(), entity.getTimeBetweenTreatments());
        assertNull(entity.getCreatedAt()); // Ignored
        assertNull(entity.getUpdatedAt()); // Ignored
    }

    @Test
    @DisplayName("Should map TreatmentEntity to Treatment DTO")
    void mapTreatmentEntityToTreatmentDto() {
        // Given
        TreatmentEntity entity = TestData.getTreatmentEntity();

        // When
        Treatment treatment = mapper.treatmentToTreatmentDto(entity);

        // Then
        assertNotNull(treatment);
        assertEquals(entity.getName(), treatment.getName());
        assertEquals(entity.getDescription(), treatment.getDescription());
        assertEquals(entity.getPrice(), treatment.getPrice());
        assertEquals(entity.getDuration(), treatment.getDuration());
        assertEquals(entity.getTimeBetweenTreatments(), treatment.getTimeBetweenTreatments());
        // Verifică mapping-ul pentru categoryName
        assertEquals(entity.getCategory().getName(), treatment.getCategoryName());
    }

    @Test
    @DisplayName("Should map CategoryEntity to Category DTO")
    void mapCategoryEntityToCategoryDto() {
        // Given
        CategoryEntity categoryEntity = TestData.getCategoryEntity();

        // When
        Category category = mapper.toTreatmentCategory(categoryEntity);

        // Then
        assertNotNull(category);
        assertEquals(categoryEntity.getName(), category.getName());
        assertEquals(categoryEntity.getDescription(), category.getDescription());
        // Verifică mapping-ul pentru salonName
        assertEquals(categoryEntity.getSalon().getName(), category.getSalonName());
    }

    @Test
    @DisplayName("Should map CategoryRequest to CategoryEntity")
    void mapCategoryRequestToCategoryEntity() {
        // Given
        var categoryRequest = TestData.getCategoryRequest();

        // When
        CategoryEntity entity = mapper.toTreatmentCategoryEntity(categoryRequest);

        // Then
        assertNotNull(entity);
        assertEquals(categoryRequest.getName(), entity.getName());
        assertEquals(categoryRequest.getDescription(), entity.getDescription());
        assertNull(entity.getCreatedAt()); // Ignored
        assertNull(entity.getUpdatedAt()); // Ignored
        // Salon se setează separat în service
    }
}



