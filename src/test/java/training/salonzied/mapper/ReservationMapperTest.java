package training.salonzied.mapper;

import com.salonized.dto.Reservation;
import com.salonized.dto.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import training.salonzied.dao.entities.*;
import util.TestData;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReservationMapper Unit Tests")
class ReservationMapperTest {

    private ReservationMapper mapper;
    private ReservationEntity reservationEntity;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ReservationMapper.class);
        
        TreatmentEntity treatment = TestData.getTreatmentEntity();
        SalonEntity salon = TestData.getSalonEntity();
        UserEntity user = TestData.getUserEntity();
        
        LocalDateTime startTime = LocalDateTime.of(2025, 12, 20, 10, 30);
        reservationEntity = ReservationEntity.builder()
                .id(1L)
                .publicId("reservation-123")
                .startTime(startTime)
                .durationOfBooking(treatment.getDuration())
                .status(ReservationStatus.SCHEDULED)
                .notes("Test notes")
                .treatment(treatment)
                .salon(salon)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("Should map ReservationEntity to Reservation DTO")
    void mapReservationEntityToReservationDto() {
        // When
        Reservation reservation = mapper.entityToReservationDto(reservationEntity);

        // Then
        assertNotNull(reservation);
        assertEquals(reservationEntity.getPublicId(), reservation.getPublicId());
        assertEquals(reservationEntity.getStartTime(), reservation.getStartTime());
        assertEquals(reservationEntity.getDurationOfBooking(), reservation.getDurationOfBooking());
        assertEquals(reservationEntity.getStatus(), reservation.getStatus());
        assertEquals(reservationEntity.getNotes(), reservation.getNotes());
        
        // Verifică mapping-urile custom
        assertEquals(reservationEntity.getSalon().getPublicId(), reservation.getSalonPublicId());
        assertEquals(reservationEntity.getUser().getPublicId(), reservation.getUserPublicId());
        assertEquals(reservationEntity.getTreatment().getName(), reservation.getTreatmentName());
        assertEquals(reservationEntity.getTreatment().getPrice(), reservation.getPriceOfBooking());
        
        // Verifică calculul endTime
        LocalDateTime expectedEndTime = reservationEntity.getStartTime()
                .plusMinutes(reservationEntity.getDurationOfBooking());
        assertEquals(expectedEndTime, reservation.getEndTime());
    }

    @Test
    @DisplayName("Should calculate endTime correctly for different durations")
    void calculateEndTimeCorrectly() {
        // Given - Rezervare de 60 minute
        LocalDateTime startTime = LocalDateTime.of(2025, 12, 20, 10, 30);
        reservationEntity.setStartTime(startTime);
        reservationEntity.setDurationOfBooking(60);

        // When
        Reservation reservation = mapper.entityToReservationDto(reservationEntity);

        // Then
        LocalDateTime expectedEndTime = startTime.plusMinutes(60);
        assertEquals(expectedEndTime, reservation.getEndTime());
    }

    @Test
    @DisplayName("Should map all reservation statuses correctly")
    void mapReservationStatuses() {
        // Given - Test pentru fiecare status
        ReservationStatus[] statuses = {
                ReservationStatus.SCHEDULED,
                ReservationStatus.CANCELLED,
                ReservationStatus.COMPLETED
        };

        for (ReservationStatus status : statuses) {
            reservationEntity.setStatus(status);

            // When
            Reservation reservation = mapper.entityToReservationDto(reservationEntity);

            // Then
            assertEquals(status, reservation.getStatus());
        }
    }

    @Test
    @DisplayName("Should handle null notes in reservation")
    void handleNullNotes() {
        // Given
        reservationEntity.setNotes(null);

        // When
        Reservation reservation = mapper.entityToReservationDto(reservationEntity);

        // Then
        assertNull(reservation.getNotes());
    }
}



