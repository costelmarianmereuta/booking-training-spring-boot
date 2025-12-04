package training.salonzied.api;

import com.salonized.dto.Reservation;
import com.salonized.dto.ReservationRequest;
import com.salonized.dto.ReservationStatus;
import com.salonized.dto.ReservationUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import training.salonzied.service.ReservationService;
import training.salonzied.service.TreatmentService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationApi {

    private final ReservationService reservationService;
    private final TreatmentService treatmentService;


    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationRequest request) {
        Reservation reservation=reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getReservations(
            @RequestParam(required = false) String salonPublicId,
            @RequestParam(required = false) String userPublicId,
            @RequestParam(required = false) String treatmentName,
            @RequestParam(required = false)LocalDate localDate,
            @RequestParam(required = false)ReservationStatus reservationStatus) {

            List<Reservation> reservations = reservationService.getReservations(salonPublicId,userPublicId,treatmentName, localDate, reservationStatus);

           return ResponseEntity.status(HttpStatus.OK).body(reservations);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<Reservation>getReservation(@PathVariable String publicId) {
        Reservation reservation= reservationService.getReservationByPublicId(publicId);
        return ResponseEntity.status(HttpStatus.OK).body(reservation);
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<Reservation> updateReservation(@RequestBody ReservationUpdateRequest request, @PathVariable String publicId) {
        Reservation reservation = reservationService.updateReservation(request, publicId);
        return ResponseEntity.status(HttpStatus.OK).body(reservation);
    }
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Reservation> deleteReservation(@PathVariable String publicId) {
        reservationService.deleteReservation(publicId);
        return ResponseEntity.noContent().build();
    }



}
