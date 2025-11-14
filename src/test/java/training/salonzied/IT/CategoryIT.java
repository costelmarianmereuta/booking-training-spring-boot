package training.salonzied.IT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.dao.repo.TreatmentCategoryRepository;
import training.salonzied.dao.repo.TreatmentRepository;
import util.TestData;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryIT extends IT {

    @Autowired
    SalonRepository salonRepository;
    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    TreatmentCategoryRepository treatmentCategoryRepository;

    @Override
    public void doSetup() {
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
    }

    @Test
    void getAllCategories() throws Exception {
        mockMvc.perform(get("/treatments/category").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("soin visage"))
                .andExpect(jsonPath("$[0].description").value("cat desc"))
                .andExpect(jsonPath("$[0].salonName").value("Salon Prestige"));
    }

    @SkipSetupIT
    @Test
    void getAllCategoriesEmpty() throws Exception {
        treatmentCategoryRepository.deleteAll();
        salonRepository.deleteAll();
        
        mockMvc.perform(get("/treatments/category").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @SkipSetupIT
    @Test
    void createCategory() throws Exception {
        // Setup salon first
        treatmentCategoryRepository.deleteAll();
        salonRepository.deleteAll();
        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon = salonRepository.save(salon);
        
        var categoryRequest = TestData.getCategoryRequest();
        categoryRequest.setName("Facial care");
        categoryRequest.setSalonPublicId(salon.getPublicId());

        mockMvc.perform(post("/treatments/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Facial care"))
                .andExpect(jsonPath("$.description").value("Treatments focused on facial skin care and relaxation"))
                .andExpect(jsonPath("$.salonName").value("Salon Prestige"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createCategoryNameDuplicate() throws Exception {
        var categoryRequest = TestData.getCategoryRequest();
        categoryRequest.setName("soin visage"); // Already exists from setup

        mockMvc.perform(post("/treatments/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"));
    }

    @SkipSetupIT
    @Test
    void createCategorySalonNotFound() throws Exception {
        treatmentCategoryRepository.deleteAll();
        salonRepository.deleteAll();
        
        var categoryRequest = TestData.getCategoryRequest();
        categoryRequest.setSalonPublicId("non-existent-salon-id");

        mockMvc.perform(post("/treatments/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @SkipSetupIT
    @Test
    void createCategoryBadRequest() throws Exception {
        treatmentCategoryRepository.deleteAll();
        salonRepository.deleteAll();
        
        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon = salonRepository.save(salon);
        
        var categoryRequest = TestData.getCategoryRequest();
        categoryRequest.setName(null); // Missing required field
        categoryRequest.setSalonPublicId(salon.getPublicId());

        mockMvc.perform(post("/treatments/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @SkipSetupIT
    @Test
    void updateCategory() throws Exception {
        // Setup salon and category first
        treatmentCategoryRepository.deleteAll();
        salonRepository.deleteAll();
        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salon = salonRepository.save(salon);
        
        var category = TestData.getCategoryEntity();
        category.setId(null);
        category.setSalon(salon);
        treatmentCategoryRepository.save(category);

        // Update it
        var updateRequest = TestData.getCategoryRequest();
        updateRequest.setName("Updated Category Name");
        updateRequest.setDescription("Updated description");

        mockMvc.perform(put("/treatments/category/soin visage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Category Name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.salonName").value("Salon Prestige"));
    }

    @Test
    void updateCategoryNotFound() throws Exception {
        var updateRequest = TestData.getCategoryRequest();

        mockMvc.perform(put("/treatments/category/NonExistentCategory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void deleteCategory() throws Exception {
        String categoryName = "soin visage";

        mockMvc.perform(delete("/treatments/category/" + categoryName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verify it's deleted
        mockMvc.perform(get("/treatments/category").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteCategoryNotFound() throws Exception {
        String categoryName = "NonExistentCategory";

        mockMvc.perform(delete("/treatments/category/" + categoryName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

}

