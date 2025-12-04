package training.salonzied.service;

import com.salonized.dto.Salon;
import com.salonized.dto.SpecialWorkingHours;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.error.InvalidSpecialOpeningHoursException;
import training.salonzied.mapper.SalonMapper;
import util.TestData;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalonService Unit Tests")
class SalonServiceTest {

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private SalonMapper salonMapper;

    @InjectMocks
    private SalonService salonService;

    private SalonEntity salonEntity;
    private Salon salonDto;

    @BeforeEach
    void setUp() {
        salonEntity = TestData.getSalonEntity();
        salonDto = TestData.getSalon();
    }

    @Test
    @DisplayName("Should create salon successfully")
    void createSalon_Success() {
        // Given
        var request = TestData.getCreateSalonRequest();
        when(salonMapper.toEntity(request)).thenReturn(salonEntity);
        when(salonRepository.save(salonEntity)).thenReturn(salonEntity);
        when(salonMapper.entityToSalonDto(salonEntity)).thenReturn(salonDto);

        // When
        Salon result = salonService.createSalon(request);

        // Then
        assertNotNull(result);
        verify(salonMapper, times(1)).toEntity(request);
        verify(salonRepository, times(1)).save(salonEntity);
        verify(salonMapper, times(1)).entityToSalonDto(salonEntity);
    }

    @Test
    @DisplayName("Should throw InvalidSpecialOpeningHoursException when closedAllDay is true but times are provided")
    void createSalon_InvalidSpecialHours_ClosedAllDayWithTimes() {
        // Given
        var request = TestData.getCreateSalonRequest();
        var specialHours = new SpecialWorkingHours();
        specialHours.setClosedAllDay(true);
        specialHours.setStartTime(LocalTime.of(9, 0));
        specialHours.setEndTime(LocalTime.of(18, 0));
        specialHours.setClosingDay(LocalDate.of(2025, 12, 25));
        request.setSpecialOpeningHours(List.of(specialHours));

        // When & Then
        assertThrows(
                InvalidSpecialOpeningHoursException.class,
                () -> salonService.createSalon(request)
        );

        verify(salonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidSpecialOpeningHoursException when closedAllDay is false but times are null")
    void createSalon_InvalidSpecialHours_NoTimesWhenNotClosed() {
        // Given
        var request = TestData.getCreateSalonRequest();
        var specialHours = new SpecialWorkingHours();
        specialHours.setClosedAllDay(false);
        specialHours.setStartTime(null);
        specialHours.setEndTime(null);
        specialHours.setClosingDay(LocalDate.of(2025, 12, 25));
        request.setSpecialOpeningHours(List.of(specialHours));

        // When & Then
        assertThrows(
                InvalidSpecialOpeningHoursException.class,
                () -> salonService.createSalon(request)
        );

        verify(salonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get all salons successfully")
    void getSalons_Success() {
        // Given
        List<SalonEntity> entities = List.of(salonEntity);
        when(salonRepository.findAll()).thenReturn(entities);
        when(salonMapper.entityToSalonDto(salonEntity)).thenReturn(salonDto);

        // When
        List<Salon> result = salonService.getSalons();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(salonRepository, times(1)).findAll();
        verify(salonMapper, times(1)).entityToSalonDto(salonEntity);
    }

    @Test
    @DisplayName("Should get salon by publicId successfully")
    void getSalonByPublicId_Success() {
        // Given
        String publicId = salonEntity.getPublicId();
        when(salonRepository.findByPublicId(publicId)).thenReturn(Optional.of(salonEntity));
        when(salonMapper.entityToSalonDto(salonEntity)).thenReturn(salonDto);

        // When
        Salon result = salonService.getSalonByPublicId(publicId);

        // Then
        assertNotNull(result);
        verify(salonRepository, times(1)).findByPublicId(publicId);
        verify(salonMapper, times(1)).entityToSalonDto(salonEntity);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when salon not found")
    void getSalonByPublicId_NotFound() {
        // Given
        String publicId = "non-existent-id";
        when(salonRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> salonService.getSalonByPublicId(publicId)
        );

        assertTrue(exception.getMessage().contains(publicId) || exception.getMessage().contains("Salon"));
        verify(salonRepository, times(1)).findByPublicId(publicId);
        verify(salonMapper, never()).entityToSalonDto(any());
    }

    @Test
    @DisplayName("Should update salon successfully")
    void updateSalon_Success() {
        // Given
        String publicId = salonEntity.getPublicId();
        var updateRequest = TestData.getUpdateSalonRequest();
        when(salonRepository.findByPublicId(publicId)).thenReturn(Optional.of(salonEntity));
        when(salonRepository.save(salonEntity)).thenReturn(salonEntity);
        when(salonMapper.entityToSalonDto(salonEntity)).thenReturn(salonDto);

        // When
        Salon result = salonService.updateSalon(publicId, updateRequest);

        // Then
        assertNotNull(result);
        verify(salonRepository, times(1)).findByPublicId(publicId);
        verify(salonRepository, times(1)).save(salonEntity);
        verify(salonMapper, times(1)).entityToSalonDto(salonEntity);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating non-existent salon")
    void updateSalon_NotFound() {
        // Given
        String publicId = "non-existent-id";
        var updateRequest = TestData.getUpdateSalonRequest();
        when(salonRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                EntityNotFoundException.class,
                () -> salonService.updateSalon(publicId, updateRequest)
        );

        verify(salonRepository, times(1)).findByPublicId(publicId);
        verify(salonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete salon successfully")
    void deleteSalon_Success() {
        // Given
        String publicId = salonEntity.getPublicId();
        when(salonRepository.deleteByPublicId(publicId)).thenReturn(1L);

        // When
        salonService.deleteSalon(publicId);

        // Then
        verify(salonRepository, times(1)).deleteByPublicId(publicId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting non-existent salon")
    void deleteSalon_NotFound() {
        // Given
        String publicId = "non-existent-id";
        when(salonRepository.deleteByPublicId(publicId)).thenReturn(0L);

        // When & Then
        assertThrows(
                EntityNotFoundException.class,
                () -> salonService.deleteSalon(publicId)
        );

        verify(salonRepository, times(1)).deleteByPublicId(publicId);
    }
}

