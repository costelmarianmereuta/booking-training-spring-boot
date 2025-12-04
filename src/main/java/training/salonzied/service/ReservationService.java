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
import training.salonzied.dao.entities.WorkingHour;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.dao.repo.TreatmentRepository;
import training.salonzied.dao.repo.UserRepository;
import training.salonzied.error.ConflictException;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.error.NoOpeningHoursConfiguredException;
import training.salonzied.error.ReservationOutsideOpeningHoursException;
import training.salonzied.error.SalonClosedException;
import training.salonzied.mapper.ReservationMapper;
import training.salonzied.dao.repo.ReservationRepository;
import training.salonzied.dao.repo.ReservationSpec;

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

    public Reservation createReservation(ReservationRequest request) {
        TreatmentEntity treatment = treatmentRepository.findByName(request.getTreatmentName()).orElseThrow(
                () -> new EntityNotFoundException("treatment", request.getTreatmentName()));

        SalonEntity salon = salonRepository.findByPublicId(request.getSalonPublicId()).orElseThrow(
                () -> new EntityNotFoundException("salon", request.getSalonPublicId()));

        UserEntity user = userRepository.findByPublicId(request.getUserPublicId()).orElseThrow(
                () -> new EntityNotFoundException("user", request.getUserPublicId()));

        LocalDateTime startTime = request.getStartTime();
        int duration = treatment.getDuration();

        // Verifică dacă salonul este deschis la ora rezervării
        validateSalonIsOpen(salon, startTime, duration);

        // Verifică dacă există alte rezervări la aceeași oră
        validateNoConflictingReservations(salon, startTime, duration, null);

        ReservationEntity reservationEntity = ReservationEntity.builder()
                .notes(request.getNotes())
                .startTime(startTime)
                .durationOfBooking(duration)
                .status(ReservationStatus.SCHEDULED)
                .publicId(UUID.randomUUID().toString())
                .treatment(treatment)
                .salon(salon)
                .user(user)
                .build();

        ReservationEntity reservationSaved = reservationRepository.save(reservationEntity);
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
            // Verifică dacă salonul este deschis la noua oră
            validateSalonIsOpen(reservation.getSalon(), newStartTime, newDuration);

            // Verifică dacă există alte rezervări la aceeași oră (excluzând rezervarea curentă)
            validateNoConflictingReservations(reservation.getSalon(), newStartTime, newDuration, reservation.getPublicId());
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

}
