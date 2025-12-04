package training.salonzied.dao.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class SpecialOpeningHours {

    private LocalDate closingDay;      // ex: 2025-12-24

    private LocalTime startTime; // poate fi null dacă e închis toată ziua
    private LocalTime endTime;   // poate fi null dacă e închis toată ziua

    private boolean closedAllDay;
}

