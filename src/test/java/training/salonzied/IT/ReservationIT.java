package training.salonzied.IT;

import com.salonized.dto.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import training.salonzied.dao.entities.*;
import training.salonzied.dao.repo.*;
import util.TestData;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationIT extends IT {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    SalonRepository salonRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TreatmentRepository treatmentRepository;

    @Autowired
    TreatmentCategoryRepository treatmentCategoryRepository;

    @Override
    public void doSetup() {
        // Delete in order to respect foreign key constraints
        reservationRepository.deleteAll();
        treatmentRepository.deleteAll();
        treatmentCategoryRepository.deleteAll();
        userRepository.deleteAll();
        salonRepository.deleteAll();

        // Setup salon with opening hours
        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon.setOpeningHours(TestData.getWorkingHours());
        salon = salonRepository.save(salon);

        // Setup user
        var user = TestData.getUserEntity();
        user.setId(null);
        user = userRepository.save(user);

        // Setup category
        var category = TestData.getCategoryEntity();
        category.setId(null);
        category.setSalon(salon);
        category = treatmentCategoryRepository.save(category);

        // Setup treatment
        var treatment = TestData.getTreatmentEntity();
        treatment.setId(null);
        treatment.setCategory(category);
        treatmentRepository.save(treatment);

        // Setup reservation
        var reservation = TestData.getReservationEntity();
        reservation.setId(null);
        reservation.setSalon(salon);
        reservation.setUser(user);
        reservation.setTreatment(treatment);
        reservationRepository.save(reservation);
    }

    @Test
    void getAllReservations() throws Exception {
        mockMvc.perform(get("/reservations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].publicId").value("reservation-123"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].notes").value("Client prefers soft massage"));
    }

    @Test
    void getAllReservationsWithFilters() throws Exception {
        String salonPublicId = "b7a6c6f4-9d5a-4f91-8e71-39ef99e8c9c3";
        String userPublicId = "4a2f1e8c-11aa-4d8c-bd88-b0c82a8ff21e";
        String treatmentName = "Hydrafacial";

        mockMvc.perform(get("/reservations")
                        .param("salonPublicId", salonPublicId)
                        .param("userPublicId", userPublicId)
                        .param("treatmentName", treatmentName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].publicId").value("reservation-123"));
    }

    @SkipSetupIT
    @Test
    void getAllReservationsEmpty() throws Exception {
        reservationRepository.deleteAll();
        treatmentRepository.deleteAll();
        treatmentCategoryRepository.deleteAll();
        userRepository.deleteAll();
        salonRepository.deleteAll();

        mockMvc.perform(get("/reservations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @SkipSetupIT
    @Test
    void createReservation() throws Exception {
        // Setup salon with opening hours
        reservationRepository.deleteAll();
        treatmentRepository.deleteAll();
        treatmentCategoryRepository.deleteAll();
        userRepository.deleteAll();
        salonRepository.deleteAll();

        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon.setOpeningHours(TestData.getWorkingHours());
        salon = salonRepository.save(salon);

        var user = TestData.getUserEntity();
        user.setId(null);
        user = userRepository.save(user);

        var category = TestData.getCategoryEntity();
        category.setId(null);
        category.setSalon(salon);
        category = treatmentCategoryRepository.save(category);

        var treatment = TestData.getTreatmentEntity();
        treatment.setId(null);
        treatment.setCategory(category);
        treatmentRepository.save(treatment);

        var reservationRequest = TestData.getReservationRequest();
        reservationRequest.setStartTime(LocalDateTime.of(2025, 12, 22, 10, 30)); // Luni, 10:30

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.notes").value("Client prefers soft massage"))
                .andExpect(jsonPath("$.publicId").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createReservationSalonNotFound() throws Exception {
        var reservationRequest = TestData.getReservationRequest();
        reservationRequest.setSalonPublicId("non-existent-salon-id");

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void createReservationUserNotFound() throws Exception {
        var reservationRequest = TestData.getReservationRequest();
        reservationRequest.setUserPublicId("non-existent-user-id");

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void createReservationTreatmentNotFound() throws Exception {
        var reservationRequest = TestData.getReservationRequest();
        reservationRequest.setTreatmentName("NonExistentTreatment");

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void createReservationSalonClosed() throws Exception {
        var reservationRequest = TestData.getReservationRequest();
        // Duminică - salonul este închis (nu avem working hours pentru duminică)
        reservationRequest.setStartTime(LocalDateTime.of(2025, 12, 21, 10, 30)); // Duminică

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void createReservationOutsideOpeningHours() throws Exception {
        var reservationRequest = TestData.getReservationRequest();
        // Rezervare la 20:00, dar salonul se închide la 18:00
        reservationRequest.setStartTime(LocalDateTime.of(2025, 12, 22, 20, 0)); // Luni, 20:00

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void createReservationConflict() throws Exception {
        var reservationRequest = TestData.getReservationRequest();
        // Rezervare la aceeași oră ca cea existentă (10:30)
        reservationRequest.setStartTime(LocalDateTime.of(2025, 12, 20, 10, 30));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
    }

    @Test
    void createReservationBadRequest() throws Exception {
        var reservationRequest = TestData.getReservationRequest();
        reservationRequest.setSalonPublicId(null); // Missing required field

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void getReservationByPublicId() throws Exception {
        String publicId = "reservation-123";

        mockMvc.perform(get("/reservations/" + publicId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.publicId").value("reservation-123"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.notes").value("Client prefers soft massage"));
    }

    @Test
    void getReservationByPublicIdNotFound() throws Exception {
        String publicId = "non-existent-reservation-id";

        mockMvc.perform(get("/reservations/" + publicId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @SkipSetupIT
    @Test
    void updateReservation() throws Exception {
        // Setup salon, user, treatment, and reservation
        reservationRepository.deleteAll();
        treatmentRepository.deleteAll();
        treatmentCategoryRepository.deleteAll();
        userRepository.deleteAll();
        salonRepository.deleteAll();

        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon.setOpeningHours(TestData.getWorkingHours());
        salon = salonRepository.save(salon);

        var user = TestData.getUserEntity();
        user.setId(null);
        user = userRepository.save(user);

        var category = TestData.getCategoryEntity();
        category.setId(null);
        category.setSalon(salon);
        category = treatmentCategoryRepository.save(category);

        var treatment = TestData.getTreatmentEntity();
        treatment.setId(null);
        treatment.setCategory(category);
        treatmentRepository.save(treatment);

        var reservation = TestData.getReservationEntity();
        reservation.setId(null);
        reservation.setSalon(salon);
        reservation.setUser(user);
        reservation.setTreatment(treatment);
        reservation = reservationRepository.save(reservation);

        var updateRequest = TestData.getReservationUpdateRequest();
        updateRequest.setStartTime(LocalDateTime.of(2025, 12, 22, 14, 0)); // Luni, 14:00

        mockMvc.perform(put("/reservations/" + reservation.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.notes").value("Updated notes"))
                .andExpect(jsonPath("$.publicId").value(reservation.getPublicId()));
    }

    @Test
    void updateReservationNotFound() throws Exception {
        var updateRequest = TestData.getReservationUpdateRequest();

        mockMvc.perform(put("/reservations/non-existent-reservation-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void deleteReservation() throws Exception {
        String publicId = "reservation-123";

        mockMvc.perform(delete("/reservations/" + publicId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verify it's deleted
        mockMvc.perform(get("/reservations/" + publicId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void deleteReservationNotFound() throws Exception {
        String publicId = "non-existent-reservation-id";

        mockMvc.perform(delete("/reservations/" + publicId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

}


