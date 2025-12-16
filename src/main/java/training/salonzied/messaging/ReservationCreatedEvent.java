package training.salonzied.messaging;

import com.salonized.dto.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ReservationCreatedEvent {
    private String reservationId;
    private String employeeName;
    private String reservationName;
    private String serviceName;
    private LocalDateTime reservationDate;
    private String salonName;
    private String salonAddress;
    private String userEmail;
    private String userName;

}
