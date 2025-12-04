package training.salonzied.mapper;

import com.salonized.dto.Salon;
import com.salonized.dto.WorkingHours;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.entities.WorkingHour;
import util.TestData;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalonMapper Unit Tests")
class SalonMapperTest {

    private final SalonMapper mapper = Mappers.getMapper(SalonMapper.class);

    @Test
    @DisplayName("Should map CreateSalonRequest to SalonEntity")
    void mapSalonRequestToSalonEntity() {
        // Given
        var request = TestData.getCreateSalonRequest();

        // When
        SalonEntity entity = mapper.toEntity(request);

        // Then
        assertNotNull(entity);
        assertEquals(request.getName(), entity.getName());
        assertEquals(request.getAddress().getCity(), entity.getAddress().getCity());
        assertEquals(request.getAddress().getHouseNumber(), entity.getAddress().getHouseNumber());
        assertEquals(request.getAddress().getPostalBox(), entity.getAddress().getPostalBox());
        assertEquals(request.getAddress().getPostcode(), entity.getAddress().getPostcode());
        assertNotNull(entity.getPublicId());
        assertNull(entity.getId()); // Ignored
        assertNull(entity.getCreatedAt()); // Ignored
        assertNull(entity.getUpdatedAt()); // Ignored
    }

    @Test
    @DisplayName("Should map CreateSalonRequest with openingHours to SalonEntity")
    void mapSalonRequestWithOpeningHours() {
        // Given
        var request = TestData.getCreateSalonRequest();
        var workingHours = List.of(
                WorkingHours.builder()
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .build()
        );
        request.setOpeningHours(workingHours);

        // When
        SalonEntity entity = mapper.toEntity(request);

        // Then
        assertNotNull(entity);
        assertNotNull(entity.getOpeningHours());
        assertEquals(1, entity.getOpeningHours().size());
        assertEquals(DayOfWeek.MONDAY, entity.getOpeningHours().get(0).getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), entity.getOpeningHours().get(0).getStartTime());
        assertEquals(LocalTime.of(18, 0), entity.getOpeningHours().get(0).getEndTime());
    }

    @Test
    @DisplayName("Should map CreateSalonRequest with specialOpeningHours to SalonEntity")
    void mapSalonRequestWithSpecialOpeningHours() {
        // Given
        var request = TestData.getCreateSalonRequest();
        var specialHours = List.of(
                com.salonized.dto.SpecialWorkingHours.builder()
                        .closingDay(java.time.LocalDate.of(2025, 12, 25))
                        .closedAllDay(true)
                        .build()
        );
        request.setSpecialOpeningHours(specialHours);

        // When
        SalonEntity entity = mapper.toEntity(request);

        // Then
        assertNotNull(entity);
        assertNotNull(entity.getSpecialOpeningHours());
        assertEquals(1, entity.getSpecialOpeningHours().size());
        assertTrue(entity.getSpecialOpeningHours().get(0).isClosedAllDay());
    }

    @Test
    @DisplayName("Should map SalonEntity to Salon DTO")
    void mapSalonEntityToSalonDto() {
        // Given
        SalonEntity entity = TestData.getSalonEntity();

        // When
        Salon salon = mapper.entityToSalonDto(entity);

        // Then
        assertNotNull(salon);
        assertEquals(entity.getPublicId(), salon.getPublicId());
        assertEquals(entity.getName(), salon.getName());
        assertEquals(entity.getAddress().getCity(), salon.getAddress().getCity());
        assertEquals(entity.getAddress().getStreet(), salon.getAddress().getStreet());
        assertEquals(entity.getAddress().getHouseNumber(), salon.getAddress().getHouseNumber());
        assertEquals(entity.getAddress().getPostalBox(), salon.getAddress().getPostalBox());
        assertEquals(entity.getAddress().getPostcode(), salon.getAddress().getPostcode());
    }

    @Test
    @DisplayName("Should map SalonEntity with openingHours to Salon DTO")
    void mapSalonEntityWithOpeningHours() {
        // Given
        SalonEntity entity = TestData.getSalonEntity();
        entity.setOpeningHours(List.of(
                WorkingHour.builder()
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(18, 0))
                        .build()
        ));

        // When
        Salon salon = mapper.entityToSalonDto(entity);

        // Then
        assertNotNull(salon);
        assertNotNull(salon.getOpeningHours());
        assertEquals(1, salon.getOpeningHours().size());
        assertEquals(DayOfWeek.MONDAY, salon.getOpeningHours().get(0).getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), salon.getOpeningHours().get(0).getStartTime());
        assertEquals(LocalTime.of(18, 0), salon.getOpeningHours().get(0).getEndTime());
    }

    @Test
    @DisplayName("Should map SalonEntity with specialOpeningHours to Salon DTO")
    void mapSalonEntityWithSpecialOpeningHours() {
        // Given
        SalonEntity entity = TestData.getSalonEntity();
        entity.setSpecialOpeningHours(List.of(
                training.salonzied.dao.entities.SpecialOpeningHours.builder()
                        .closingDay(java.time.LocalDate.of(2025, 12, 25))
                        .closedAllDay(true)
                        .build()
        ));

        // When
        Salon salon = mapper.entityToSalonDto(entity);

        // Then
        assertNotNull(salon);
        assertNotNull(salon.getSpecialOpeningHours());
        assertEquals(1, salon.getSpecialOpeningHours().size());
        assertTrue(salon.getSpecialOpeningHours().get(0).getClosedAllDay());
    }

    @Test
    @DisplayName("Should generate unique publicId for each mapping")
    void generateUniquePublicId() {
        // Given
        var request = TestData.getCreateSalonRequest();

        // When
        SalonEntity entity1 = mapper.toEntity(request);
        SalonEntity entity2 = mapper.toEntity(request);

        // Then
        assertNotNull(entity1.getPublicId());
        assertNotNull(entity2.getPublicId());
        assertNotEquals(entity1.getPublicId(), entity2.getPublicId(), 
                "Each mapping should generate a unique publicId");
    }

    @Test
    @DisplayName("Should handle null openingHours in CreateSalonRequest")
    void handleNullOpeningHours() {
        // Given
        var request = TestData.getCreateSalonRequest();
        request.setOpeningHours(null);

        // When
        SalonEntity entity = mapper.toEntity(request);

        // Then
        assertNotNull(entity);
        // openingHours poate fi null sau empty list, depinde de implementarea MapStruct
    }

    @Test
    @DisplayName("Should handle null specialOpeningHours in CreateSalonRequest")
    void handleNullSpecialOpeningHours() {
        // Given
        var request = TestData.getCreateSalonRequest();
        request.setSpecialOpeningHours(null);

        // When
        SalonEntity entity = mapper.toEntity(request);

        // Then
        assertNotNull(entity);
        // specialOpeningHours poate fi null sau empty list
    }
}
