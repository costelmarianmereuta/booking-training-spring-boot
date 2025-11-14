package training.salonzied.IT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.dao.repo.TreatmentCategoryRepository;
import training.salonzied.dao.repo.TreatmentRepository;
import util.TestData;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TreatmentIT extends IT {

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private TreatmentCategoryRepository treatmentCategoryRepository;

    @Autowired
    private SalonRepository salonRepository;

    @Override
    public void doSetup() {
        treatmentRepository.deleteAll();
        treatmentCategoryRepository.deleteAll();
        salonRepository.deleteAll();
        
        // Setup salon first (required for category)
        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon = salonRepository.save(salon);
        
        // Setup category (required for treatment)
        var category = TestData.getCategoryEntity();
        category.setId(null);
        category.setSalon(salon);
        category = treatmentCategoryRepository.save(category);
        
        // Setup treatment
        var treatment = TestData.getTreatmentEntity();
        treatment.setId(null);
        treatment.setCategory(category);
        treatmentRepository.save(treatment);
    }

    @Test
    void getAllTreatments() throws Exception {

        mockMvc.perform(get("/treatments").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Hydrafacial"))
                .andExpect(jsonPath("$[0].price").value(75.00))
                .andExpect(jsonPath("$[0].duration").value(30))
                .andExpect(jsonPath("$[0].timeBetweenTreatments").value(15))
                .andExpect(jsonPath("$[0].categoryName").value("soin visage"))
                .andExpect(jsonPath("$[0].description").value("desc ..."));
    }

    @SkipSetupIT
    @Test
    void addTreatment() throws Exception {
        // Setup salon and category first (required for treatment)
        treatmentRepository.deleteAll();
        treatmentCategoryRepository.deleteAll();
        salonRepository.deleteAll();
        
        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon = salonRepository.save(salon);
        
        var category = TestData.getCategoryEntity();
        category.setId(null);
        category.setSalon(salon);
        treatmentCategoryRepository.save(category);

        mockMvc.perform(post("/treatments").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestData.getTreatmentRequest())))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Hydrafacial"))
                .andExpect(jsonPath("$.price").value(75.00))
                .andExpect(jsonPath("$.duration").value(30))
                .andExpect(jsonPath("$.timeBetweenTreatments").value(15))
                .andExpect(jsonPath("$.categoryName").value("soin visage"))
                .andExpect(jsonPath("$.description").value("desc ..."));

    }

    @Test
    void addTreatmentNameDuplicate() throws Exception {

        mockMvc.perform(post("/treatments").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestData.getTreatmentRequest())))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));

    }

    @Test
    void getTreatmentByName() throws Exception {
        String treatmentName = "Hydrafacial";

        mockMvc.perform(get("/treatments/" + treatmentName).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Hydrafacial"))
                .andExpect(jsonPath("$.price").value(75.00))
                .andExpect(jsonPath("$.duration").value(30))
                .andExpect(jsonPath("$.timeBetweenTreatments").value(15))
                .andExpect(jsonPath("$.categoryName").value("soin visage"))
                .andExpect(jsonPath("$.description").value("desc ..."));
    }

    @Test
    void getTreatmentByNameNotFound() throws Exception {
        String treatmentName = "NonExistentTreatment";

        mockMvc.perform(get("/treatments/" + treatmentName).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @SkipSetupIT
    @Test
    void updateTreatment() throws Exception {
        // Clean up first to avoid conflicts with other tests
        treatmentRepository.deleteAll();
        treatmentCategoryRepository.deleteAll();
        salonRepository.deleteAll();
        
        // First create salon and category
        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon = salonRepository.save(salon);
        
        var category = TestData.getCategoryEntity();
        category.setId(null);
        category.setSalon(salon);
        category = treatmentCategoryRepository.save(category);
        
        var treatment = TestData.getTreatmentEntity();
        treatment.setId(null);
        treatment.setCategory(category);
        treatmentRepository.save(treatment);

        // Now update it
        var updateRequest = TestData.getTreatmentRequest();
        updateRequest.setName("Updated Hydrafacial");
        updateRequest.setPrice(85.00);
        updateRequest.setDescription("Updated description");

        mockMvc.perform(put("/treatments/Hydrafacial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Hydrafacial"))
                .andExpect(jsonPath("$.price").value(85.00))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateTreatmentNotFound() throws Exception {
        var updateRequest = TestData.getTreatmentRequest();

        mockMvc.perform(put("/treatments/NonExistentTreatment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void deleteTreatment() throws Exception {
        String treatmentName = "Hydrafacial";

        mockMvc.perform(delete("/treatments/" + treatmentName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verify it's deleted
        mockMvc.perform(get("/treatments/" + treatmentName).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void deleteTreatmentNotFound() throws Exception {
        String treatmentName = "NonExistentTreatment";

        mockMvc.perform(delete("/treatments/" + treatmentName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

}
