package training.salonzied.service;

import com.salonized.dto.Reservation;
import com.salonized.dto.ReservationRequest;
import com.salonized.dto.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import training.salonzied.dao.entities.*;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.dao.repo.TreatmentRepository;
import training.salonzied.dao.repo.UserRepository;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.error.ReservationOutsideOpeningHoursException;
import training.salonzied.error.SalonClosedException;
import training.salonzied.mapper.ReservationMapper;
import training.salonzied.dao.repo.ReservationRepository;
import util.TestData;
import org.springframework.data.jpa.domain.Specification;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Teste unitare pentru ReservationService folosind JUnit 5 și Mockito
 * 
 * Pattern folosit:
 * - @ExtendWith(MockitoExtension.class) pentru integrarea Mockito cu JUnit 5
 * - @InjectMocks pentru instanțierea automată a clasei de testat cu mock-urile injectate
 * - @Mock pentru crearea mock-urilor pentru dependențe
 * - Given-When-Then pattern pentru structurarea testelor
 * - Refolosirea obiectelor din util.TestData
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Unit Tests")
class ReservationServiceTest {

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private SalonRepository salonRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;

    private TreatmentEntity treatment;
    private SalonEntity salon;
    private UserEntity user;
    private UserEntity employee;
    private ReservationRequest reservationRequest;
    private ReservationEntity savedReservation;
    private Reservation reservationDto;

    @BeforeEach
    void setUp() {
        // Given - Setup date de test folosind TestData
        treatment = TestData.getTreatmentEntity();
        salon = TestData.getSalonEntity();
        salon.setOpeningHours(TestData.getWorkingHours());
        user = TestData.getUserEntity();
        employee = TestData.getEmployeeEntity();
        employee.setSalon(salon);

        LocalDateTime startTime = LocalDateTime.of(2025, 12, 20, 10, 30);
        reservationRequest = new ReservationRequest();
        reservationRequest.setSalonPublicId(salon.getPublicId());
        reservationRequest.setUserPublicId(user.getPublicId());
        reservationRequest.setTreatmentName(treatment.getName());
        reservationRequest.setStartTime(startTime);
        reservationRequest.setNotes("Client prefers soft massage");

        savedReservation = ReservationEntity.builder()
                .id(1L)
                .publicId("reservation-123")
                .startTime(startTime)
                .durationOfBooking(treatment.getDuration())
                .status(ReservationStatus.SCHEDULED)
                .notes("Client prefers soft massage")
                .treatment(treatment)
                .salon(salon)
                .user(user)
                .employee(employee)
                .build();

        reservationDto = new Reservation();
        reservationDto.setPublicId("reservation-123");
        reservationDto.setStatus(ReservationStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Should create reservation successfully when all validations pass")
    void createReservation_Success() {
        // Given - Configurare mock-uri
        when(treatmentRepository.findByName(treatment.getName()))
                .thenReturn(Optional.of(treatment));
        when(salonRepository.findByPublicId(salon.getPublicId()))
                .thenReturn(Optional.of(salon));
        when(userRepository.findByPublicId(user.getPublicId()))
                .thenReturn(Optional.of(user));
        when(userRepository.findEmployeesBySalonId(salon.getId()))
                .thenReturn(List.of(employee)); // Employee disponibil
        when(reservationRepository.findPotentialConflictingReservationsForEmployee(
                any(), any(), any(), any()))
                .thenReturn(List.of()); // Nu există rezervări conflictuale pentru employee
        when(reservationRepository.save(any(ReservationEntity.class)))
                .thenReturn(savedReservation);
        when(reservationMapper.entityToReservationDto(savedReservation))
                .thenReturn(reservationDto);

        // When - Executare metoda de testat
        Reservation result = reservationService.createReservation(reservationRequest);

        // Then - Verificări
        assertNotNull(result);
        assertEquals("reservation-123", result.getPublicId());
        assertEquals(ReservationStatus.SCHEDULED, result.getStatus());

        // Verifică că metodele au fost apelate
        verify(treatmentRepository, times(1)).findByName(treatment.getName());
        verify(salonRepository, times(1)).findByPublicId(salon.getPublicId());
        verify(userRepository, times(1)).findByPublicId(user.getPublicId());
        verify(userRepository, times(1)).findEmployeesBySalonId(salon.getId());
        verify(reservationRepository, times(1)).save(any(ReservationEntity.class));
        verify(reservationMapper, times(1)).entityToReservationDto(savedReservation);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when treatment not found")
    void createReservation_TreatmentNotFound() {
        // Given
        when(treatmentRepository.findByName(treatment.getName()))
                .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reservationService.createReservation(reservationRequest)
        );

        assertTrue(exception.getMessage().contains("treatment") || exception.getMessage().contains(treatment.getName()));
        verify(treatmentRepository, times(1)).findByName(treatment.getName());
        verify(salonRepository, never()).findByPublicId(anyString());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when salon not found")
    void createReservation_SalonNotFound() {
        // Given
        when(treatmentRepository.findByName(treatment.getName()))
                .thenReturn(Optional.of(treatment));
        when(salonRepository.findByPublicId(salon.getPublicId()))
                .thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reservationService.createReservation(reservationRequest)
        );

        // Mesajul este "salon not found: {publicId}"
        assertTrue(exception.getMessage().contains("salon"));
        assertTrue(exception.getMessage().contains("not found"));
        assertEquals("salon", exception.getEntity());
        assertEquals(salon.getPublicId(), exception.getId());
        verify(treatmentRepository, times(1)).findByName(treatment.getName());
        verify(salonRepository, times(1)).findByPublicId(salon.getPublicId());
        verify(userRepository, never()).findByPublicId(anyString());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw SalonClosedException when salon is closed on reservation day")
    void createReservation_SalonClosed() {
        // Given - Salon fără program pentru ziua respectivă (SUNDAY)
        LocalDateTime sundayTime = LocalDateTime.of(2025, 12, 21, 10, 30); // Duminică
        reservationRequest.setStartTime(sundayTime);

        when(treatmentRepository.findByName(treatment.getName()))
                .thenReturn(Optional.of(treatment));
        when(salonRepository.findByPublicId(salon.getPublicId()))
                .thenReturn(Optional.of(salon));
        when(userRepository.findByPublicId(user.getPublicId()))
                .thenReturn(Optional.of(user));
        when(userRepository.findEmployeesBySalonId(salon.getId()))
                .thenReturn(List.of(employee));

        // When & Then
        SalonClosedException exception = assertThrows(
                SalonClosedException.class,
                () -> reservationService.createReservation(reservationRequest)
        );

        assertTrue(exception.getMessage().contains("închis"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when reservation time is outside opening hours")
    void createReservation_OutsideOpeningHours() {
        // Given - Rezervare la 8:00, dar salonul se deschide la 9:00
        LocalDateTime earlyTime = LocalDateTime.of(2025, 12, 20, 8, 0);
        reservationRequest.setStartTime(earlyTime);

        when(treatmentRepository.findByName(treatment.getName()))
                .thenReturn(Optional.of(treatment));
        when(salonRepository.findByPublicId(salon.getPublicId()))
                .thenReturn(Optional.of(salon));
        when(userRepository.findByPublicId(user.getPublicId()))
                .thenReturn(Optional.of(user));
        when(userRepository.findEmployeesBySalonId(salon.getId()))
                .thenReturn(List.of(employee));

        // When & Then - Când rezervarea depășește programul, se aruncă ReservationOutsideOpeningHoursException
        ReservationOutsideOpeningHoursException exception = assertThrows(
                ReservationOutsideOpeningHoursException.class,
                () -> reservationService.createReservation(reservationRequest)
        );

        assertTrue(exception.getMessage().contains("depășește") || exception.getMessage().contains("program"));
        verify(reservationRepository, never()).save(any());
    }
}

