package training.salonzied.service;

import com.salonized.dto.Reservation;
import com.salonized.dto.ReservationRequest;
import com.salonized.dto.ReservationStatus;
import com.salonized.dto.ReservationUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import training.salonzied.dao.entities.ReservationEntity;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.entities.SpecialOpeningHours;
import training.salonzied.dao.entities.TreatmentEntity;
import training.salonzied.dao.entities.UserEntity;
import training.salonzied.dao.entities.UserRole;
import training.salonzied.dao.entities.WorkingHour;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.dao.repo.TreatmentRepository;
import training.salonzied.dao.repo.UserRepository;
import training.salonzied.error.ConflictException;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.error.NoEmployeeAvailableException;
import training.salonzied.error.NoOpeningHoursConfiguredException;
import training.salonzied.error.ReservationOutsideOpeningHoursException;
import training.salonzied.error.SalonClosedException;
import training.salonzied.mapper.ReservationMapper;
import training.salonzied.dao.repo.ReservationRepository;
import training.salonzied.dao.repo.ReservationSpec;
import training.salonzied.messaging.ReservationCreatedEvent;
import training.salonzied.messaging.ReservationEventPublisher;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationService {
    private final TreatmentRepository treatmentRepository;
    private final SalonRepository salonRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationEventPublisher reservationEventPublisher;

    public Reservation createReservation(ReservationRequest request) {
        TreatmentEntity treatment = treatmentRepository.findByName(request.getTreatmentName()).orElseThrow(
                () -> new EntityNotFoundException("treatment", request.getTreatmentName()));

        SalonEntity salon = salonRepository.findByPublicId(request.getSalonPublicId()).orElseThrow(
                () -> new EntityNotFoundException("salon", request.getSalonPublicId()));

        UserEntity user = userRepository.findByPublicId(request.getUserPublicId()).orElseThrow(
                () -> new EntityNotFoundException("user", request.getUserPublicId()));

        LocalDateTime startTime = request.getStartTime();
        int duration = treatment.getDuration();

        // Găsește un employee disponibil la ora rezervării
        UserEntity employee = findAvailableEmployee(salon, startTime, duration);
        if (employee == null) {
            throw new NoEmployeeAvailableException("Nu există niciun employee disponibil la ora " + startTime.toLocalTime() + " pentru salonul " + salon.getName());
        }

        // Verifică working hours employee (prioritate) și program salon
        validateEmployeeAndSalonAvailability(employee, salon, startTime, duration);

        // Verifică dacă există alte rezervări pentru acest employee la aceeași oră
        validateNoConflictingReservationsForEmployee(employee, startTime, duration, null);

        ReservationEntity reservationEntity = ReservationEntity.builder()
                .notes(request.getNotes())
                .startTime(startTime)
                .durationOfBooking(duration)
                .status(ReservationStatus.SCHEDULED)
                .publicId(UUID.randomUUID().toString())
                .treatment(treatment)
                .salon(salon)
                .user(user)
                .employee(employee)
                .build();

        ReservationEntity reservationSaved = reservationRepository.save(reservationEntity);
        ReservationCreatedEvent event = ReservationCreatedEvent.builder()
                .reservationId(reservationSaved.getPublicId())
                .serviceName(reservationSaved.getTreatment().getName())
                .reservationName(reservationSaved.getUser().getFirstName())
                .salonName(reservationSaved.getSalon().getName())
                .employeeName(reservationSaved.getEmployee().getFirstName())
                .reservationDate(reservationSaved.getStartTime())
                .userEmail(reservationSaved.getUser().getEmail())
                .userName(reservationSaved.getUser().getFirstName() + " " + reservationSaved.getUser().getLastName())
                .build();
        reservationEventPublisher.publishReservationCreated(event);

        return reservationMapper.entityToReservationDto(reservationSaved);
    }

    public List<Reservation> getReservations(String salonPublicId, String userPublicId, String treatmentName, LocalDate localDate, ReservationStatus reservationStatus) {


        Specification<ReservationEntity> specs = ReservationSpec.getReservations(salonPublicId, userPublicId, treatmentName, localDate, reservationStatus);
        List<ReservationEntity> reservationEntities = reservationRepository.findAll(specs);

        return reservationEntities.stream().map(reservationMapper::entityToReservationDto)
                .toList();
    }

    public Reservation getReservationByPublicId(String publicId) {
        ReservationEntity reservation = reservationRepository.findByPublicId(publicId).orElseThrow(
                () -> new EntityNotFoundException("reservation", publicId)
        );
        return reservationMapper.entityToReservationDto(reservation);
    }

@Transactional
    public Reservation updateReservation(ReservationUpdateRequest request, String publicId) {
        ReservationEntity reservation = reservationRepository.findByPublicId(publicId).orElseThrow(() -> new EntityNotFoundException("reservation", publicId));

        boolean startTimeChanged = request.getStartTime() != null && !request.getStartTime().equals(reservation.getStartTime());
        boolean treatmentChanged = false;
        int newDuration = reservation.getDurationOfBooking();

        // treatment (singurul caz unde ai nevoie de alt repository)
        if (request.getTreatmentName() != null) {
            if (!request.getTreatmentName().equals(reservation.getTreatment().getName())) {
                TreatmentEntity treatment = treatmentRepository.findByName(request.getTreatmentName())
                        .orElseThrow(() -> new EntityNotFoundException("treatment", request.getTreatmentName()));

                reservation.setTreatment(treatment);
                newDuration = treatment.getDuration();
                reservation.setDurationOfBooking(newDuration);
                treatmentChanged = true;
            }
        }

        LocalDateTime newStartTime = startTimeChanged ? request.getStartTime() : reservation.getStartTime();

        // Dacă startTime sau treatment s-a schimbat, validăm din nou
        if (startTimeChanged || treatmentChanged) {
            // Găsește un employee disponibil (sau folosește cel existent dacă e disponibil)
            UserEntity employee = reservation.getEmployee();
            if (employee == null || !isEmployeeAvailable(employee, newStartTime, newDuration)) {
                employee = findAvailableEmployee(reservation.getSalon(), newStartTime, newDuration);
                if (employee == null) {
                    throw new NoEmployeeAvailableException("Nu există niciun employee disponibil la ora " + newStartTime.toLocalTime());
                }
                reservation.setEmployee(employee);
            }

            // Verifică working hours employee (prioritate) și program salon
            validateEmployeeAndSalonAvailability(employee, reservation.getSalon(), newStartTime, newDuration);

            // Verifică dacă există alte rezervări pentru acest employee (excluzând rezervarea curentă)
            validateNoConflictingReservationsForEmployee(employee, newStartTime, newDuration, reservation.getPublicId());
        }

        if (startTimeChanged) {
            reservation.setStartTime(request.getStartTime());
        }

        // notes
        if (request.getNotes() != null) {
            reservation.setNotes(request.getNotes());
        }

        // status
        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }

        ReservationEntity saved = reservationRepository.save(reservation);
        return reservationMapper.entityToReservationDto(saved);
    }

    @Transactional
    public void deleteReservation(String publicId) {
        long deleted = reservationRepository.deleteByPublicId(publicId);
        if (deleted == 0){
            throw new EntityNotFoundException("Reservation", publicId);
        }
        log.info("Reservation deleted with id: " + publicId);
    }

    /**
     * Validează dacă salonul este deschis la ora și durata specificată
     */
    private void validateSalonIsOpen(SalonEntity salon, LocalDateTime startTime, int durationMinutes) {
        LocalDate reservationDate = startTime.toLocalDate();
        LocalTime reservationStartTime = startTime.toLocalTime();
        LocalTime reservationEndTime = reservationStartTime.plusMinutes(durationMinutes);

        // Verifică mai întâi special opening hours (au prioritate)
        if (salon.getSpecialOpeningHours() != null) {
            Optional<SpecialOpeningHours> specialHours = salon.getSpecialOpeningHours().stream()
                    .filter(sh -> sh.getClosingDay().equals(reservationDate))
                    .findFirst();

            if (specialHours.isPresent()) {
                SpecialOpeningHours special = specialHours.get();
                if (special.isClosedAllDay()) {
                    throw new SalonClosedException("Salonul este închis în data " + reservationDate);
                }
                if (special.getStartTime() != null && special.getEndTime() != null) {
                    if (reservationStartTime.isBefore(special.getStartTime()) || 
                        reservationEndTime.isAfter(special.getEndTime())) {
                        throw new ReservationOutsideOpeningHoursException("Rezervarea depășește programul special al salonului în data " + reservationDate);
                    }
                }
                return; // Special hours găsite, nu mai verificăm regular hours
            }
        }

        // Verifică regular opening hours
        if (salon.getOpeningHours() == null || salon.getOpeningHours().isEmpty()) {
            throw new NoOpeningHoursConfiguredException("Salonul nu are program de lucru configurat");
        }

        DayOfWeek dayOfWeek = startTime.getDayOfWeek();

        Optional<WorkingHour> workingHour = salon.getOpeningHours().stream()
                .filter(wh -> wh.getDayOfWeek() == dayOfWeek)
                .findFirst();

        if (workingHour.isEmpty()) {
            throw new SalonClosedException("Salonul este închis în ziua " + dayOfWeek);
        }

        WorkingHour wh = workingHour.get();
        if (reservationStartTime.isBefore(wh.getStartTime()) || 
            reservationEndTime.isAfter(wh.getEndTime())) {
            throw new ReservationOutsideOpeningHoursException("Rezervarea depășește programul de lucru al salonului");
        }
    }

    /**
     * Validează că nu există alte rezervări care se suprapun cu intervalul specificat
     */
    private void validateNoConflictingReservations(SalonEntity salon, LocalDateTime startTime, int durationMinutes, String excludePublicId) {
        LocalDateTime reservationEndTime = startTime.plusMinutes(durationMinutes);

        // Optimizare: query-ul filtrează rezervările care ar putea să se suprapună
        // Rezervările care se suprapun trebuie să îndeplinească:
        // - existingStartTime < newEndTime (rezervarea existentă începe înainte ca noua să se termine)
        // - existingEndTime > newStartTime (rezervarea existentă se termină după ce noua începe)
        // Pentru eficiență, filtrăm rezervările care încep înainte ca noua să se termine
        // și apoi verificăm suprapunerea completă în Java
        Specification<ReservationEntity> spec = Specification.where(null);
        
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("salon").get("id"), salon.getId()));
        
        spec = spec.and((root, query, cb) ->
                // Rezervări care încep înainte ca rezervarea nouă să se termine
                // (acestea sunt singurele care ar putea să se suprapună)
                cb.lessThan(root.get("startTime"), reservationEndTime));
        
        spec = spec.and((root, query, cb) ->
                // Exclude rezervările anulate
                cb.notEqual(root.get("status"), ReservationStatus.CANCELLED));

        // Exclude rezervarea curentă dacă este update
        if (excludePublicId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.notEqual(root.get("publicId"), excludePublicId));
        }

        List<ReservationEntity> potentialConflicts = reservationRepository.findAll(spec);

        // Verifică suprapunerea completă folosind LocalDateTime pentru a gestiona corect cazurile care trec peste miezul nopții
        for (ReservationEntity existing : potentialConflicts) {
            LocalDateTime existingStartTime = existing.getStartTime();
            LocalDateTime existingEndTime = existingStartTime.plusMinutes(existing.getDurationOfBooking());

            // Verifică suprapunere: rezervarea nouă începe înainte ca cea existentă să se termine
            // și rezervarea nouă se termină după ce cea existentă începe
            boolean overlaps = startTime.isBefore(existingEndTime) && 
                              reservationEndTime.isAfter(existingStartTime);

            if (overlaps) {
                throw new ConflictException("Există deja o rezervare la ora " + existingStartTime.toLocalTime() + 
                        " care se suprapune cu rezervarea dorită");
            }
        }
    }

    /**
     * Găsește un employee disponibil la ora și durata specificată
     */
    private UserEntity findAvailableEmployee(SalonEntity salon, LocalDateTime startTime, int durationMinutes) {
        // Folosim query pentru a încărca employees cu roles și working hours
        List<UserEntity> employees = userRepository.findEmployeesBySalonId(salon.getId());
        if (employees == null || employees.isEmpty()) {
            return null;
        }

        for (UserEntity employee : employees) {
            // Verifică dacă employee-ul este disponibil
            if (isEmployeeAvailable(employee, startTime, durationMinutes)) {
                return employee;
            }
        }

        return null;
    }

    /**
     * Verifică dacă un employee este disponibil la ora și durata specificată
     */
    private boolean isEmployeeAvailable(UserEntity employee, LocalDateTime startTime, int durationMinutes) {
        // Verifică working hours employee
        if (employee.getWorkingHours() == null || employee.getWorkingHours().isEmpty()) {
            return false; // Employee fără working hours nu poate face rezervări
        }

        DayOfWeek dayOfWeek = startTime.getDayOfWeek();
        LocalTime reservationStartTime = startTime.toLocalTime();
        LocalTime reservationEndTime = reservationStartTime.plusMinutes(durationMinutes);

        Optional<WorkingHour> employeeWorkingHour = employee.getWorkingHours().stream()
                .filter(wh -> wh.getDayOfWeek() == dayOfWeek)
                .findFirst();

        if (employeeWorkingHour.isEmpty()) {
            return false; // Employee nu lucrează în acea zi
        }

        WorkingHour wh = employeeWorkingHour.get();
        // Verifică dacă rezervarea se încadrează în working hours employee
        if (reservationStartTime.isBefore(wh.getStartTime()) || reservationEndTime.isAfter(wh.getEndTime())) {
            return false;
        }

        // Verifică dacă employee-ul are alte rezervări la aceeași oră
        return !hasConflictingReservations(employee, startTime, durationMinutes, null);
    }

    /**
     * Verifică dacă employee-ul are rezervări conflictuale
     */
    private boolean hasConflictingReservations(UserEntity employee, LocalDateTime startTime, int durationMinutes, String excludePublicId) {
        LocalDateTime reservationEndTime = startTime.plusMinutes(durationMinutes);

        // Folosim query direct pentru a evita lazy loading
        // Query-ul găsește rezervări care încep înainte ca noua să se termine (potențiale conflicte)
        List<ReservationEntity> potentialConflicts = reservationRepository.findPotentialConflictingReservationsForEmployee(
                employee.getId(),
                reservationEndTime,
                excludePublicId,
                ReservationStatus.CANCELLED
        );

        // Verificăm suprapunerea completă în Java
        for (ReservationEntity existing : potentialConflicts) {
            LocalDateTime existingStartTime = existing.getStartTime();
            LocalDateTime existingEndTime = existingStartTime.plusMinutes(existing.getDurationOfBooking());

            // Verifică suprapunere: noua începe înainte ca existentă să se termine
            // și noua se termină după ce existentă începe
            boolean overlaps = startTime.isBefore(existingEndTime) && reservationEndTime.isAfter(existingStartTime);
            if (overlaps) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validează working hours employee (prioritate) și program salon
     * Dacă employee lucrează peste programul salonului, se permite (prioritate employee)
     */
    private void validateEmployeeAndSalonAvailability(UserEntity employee, SalonEntity salon, LocalDateTime startTime, int durationMinutes) {
        LocalDate reservationDate = startTime.toLocalDate();
        LocalTime reservationStartTime = startTime.toLocalTime();
        LocalTime reservationEndTime = reservationStartTime.plusMinutes(durationMinutes);

        DayOfWeek dayOfWeek = startTime.getDayOfWeek();

        // 1. Verifică working hours employee (PRIORITATE)
        if (employee.getWorkingHours() == null || employee.getWorkingHours().isEmpty()) {
            throw new NoEmployeeAvailableException("Employee " + employee.getFirstName() + " " + employee.getLastName() + " nu are working hours configurate");
        }

        Optional<WorkingHour> employeeWorkingHour = employee.getWorkingHours().stream()
                .filter(wh -> wh.getDayOfWeek() == dayOfWeek)
                .findFirst();

        if (employeeWorkingHour.isEmpty()) {
            throw new NoEmployeeAvailableException("Employee " + employee.getFirstName() + " " + employee.getLastName() + " nu lucrează în ziua " + dayOfWeek);
        }

        WorkingHour employeeWh = employeeWorkingHour.get();
        if (reservationStartTime.isBefore(employeeWh.getStartTime()) || reservationEndTime.isAfter(employeeWh.getEndTime())) {
            throw new ReservationOutsideOpeningHoursException("Rezervarea depășește programul de lucru al employee-ului " + employee.getFirstName() + " " + employee.getLastName());
        }

        // 2. Verifică program salon (doar dacă employee nu lucrează peste programul salonului)
        LocalTime salonStartTime = null;
        LocalTime salonEndTime = null;

        // Verifică special opening hours salon
        if (salon.getSpecialOpeningHours() != null) {
            Optional<SpecialOpeningHours> specialHours = salon.getSpecialOpeningHours().stream()
                    .filter(sh -> sh.getClosingDay().equals(reservationDate))
                    .findFirst();

            if (specialHours.isPresent()) {
                SpecialOpeningHours special = specialHours.get();
                if (special.isClosedAllDay()) {
                    // Dacă salonul este închis, dar employee lucrează, verifică dacă employee lucrează peste program
                    if (employeeWh.getStartTime().isBefore(special.getStartTime()) || employeeWh.getEndTime().isAfter(special.getEndTime())) {
                        // Employee lucrează peste program salon - PERMIS (prioritate employee)
                        return;
                    }
                    throw new SalonClosedException("Salonul este închis în data " + reservationDate + " și employee-ul nu lucrează peste program");
                }
                salonStartTime = special.getStartTime();
                salonEndTime = special.getEndTime();
            }
        }

        // Dacă nu e special day, verifică regular opening hours
        if (salonStartTime == null) {
            if (salon.getOpeningHours() == null || salon.getOpeningHours().isEmpty()) {
                throw new NoOpeningHoursConfiguredException("Salonul nu are program de lucru configurat");
            }

            Optional<WorkingHour> salonWorkingHour = salon.getOpeningHours().stream()
                    .filter(wh -> wh.getDayOfWeek() == dayOfWeek)
                    .findFirst();

            if (salonWorkingHour.isEmpty()) {
                // Salon închis în acea zi - verifică dacă employee lucrează peste program
                // Dacă employee are working hours, se permite (prioritate employee)
                return;
            }

            WorkingHour salonWh = salonWorkingHour.get();
            salonStartTime = salonWh.getStartTime();
            salonEndTime = salonWh.getEndTime();
        }

        // Verifică dacă rezervarea se încadrează în programul salonului
        // DAR dacă employee lucrează peste program salon, se permite
        boolean employeeWorksBeyondSalon = employeeWh.getStartTime().isBefore(salonStartTime) || 
                                           employeeWh.getEndTime().isAfter(salonEndTime);

        if (!employeeWorksBeyondSalon) {
            // Employee lucrează în programul salonului - verifică normal
            if (reservationStartTime.isBefore(salonStartTime) || reservationEndTime.isAfter(salonEndTime)) {
                throw new ReservationOutsideOpeningHoursException("Rezervarea depășește programul de lucru al salonului");
            }
        }
        // Altfel, employee lucrează peste program salon - PERMIS (prioritate employee)
    }

    /**
     * Validează că nu există alte rezervări pentru acest employee la aceeași oră
     */
    private void validateNoConflictingReservationsForEmployee(UserEntity employee, LocalDateTime startTime, int durationMinutes, String excludePublicId) {
        if (hasConflictingReservations(employee, startTime, durationMinutes, excludePublicId)) {
            throw new ConflictException("Employee " + employee.getFirstName() + " " + employee.getLastName() + 
                    " are deja o rezervare la ora " + startTime.toLocalTime());
        }
    }

}
