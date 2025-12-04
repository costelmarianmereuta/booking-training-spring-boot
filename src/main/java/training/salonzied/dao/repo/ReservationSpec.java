package training.salonzied.dao.repo;

import com.salonized.dto.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;
import training.salonzied.dao.entities.ReservationEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationSpec {


    //todo  facut exercitii cu asta
    public static Specification<ReservationEntity> getReservations(
            String salonPublicId,
            String userPublicId,
            String treatmentName,
            LocalDate date,
            ReservationStatus status
    ) {
        Specification<ReservationEntity> spec = Specification.where(null);

        if (salonPublicId != null && !salonPublicId.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("salon").get("publicId"), salonPublicId));
        }

        if (userPublicId != null && !userPublicId.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("user").get("publicId"), userPublicId));
        }

        if (treatmentName != null && !treatmentName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("treatment").get("name"), treatmentName));
        }

        if (date != null) {
            // startTime între [00:00, 23:59:59] în ziua respectivă
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("startTime"), start, end));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        return spec;
    }

}
