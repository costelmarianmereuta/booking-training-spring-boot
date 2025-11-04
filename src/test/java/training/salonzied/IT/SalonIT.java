package training.salonzied.IT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salonized.dto.CreateSalonRequest;
import com.salonized.dto.Salon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import training.salonzied.api.SalonApi;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.service.SalonService;
import util.TestData;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SalonIT {

    @Autowired MockMvc mockMvc;
    @Autowired
    SalonRepository salonRepository;
    private boolean skipSetup= false;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        if (skipSetup) {
            return;
        }
        salonRepository.deleteAll();
        var salon = TestData.getSalonEntity();
        salon.setId(null);
        salonRepository.save(salon);
    }

    @Test
    void getAllSalons() throws Exception {

        mockMvc.perform(get("/salons").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Salon Prestige"))
                .andExpect(jsonPath("$[0].address.city").value("Bacau"))
                .andExpect(jsonPath("$[0].address.street").value("str proiectantului"))
                .andExpect(jsonPath("$[0].publicId").value("b7a6c6f4-9d5a-4f91-8e71-39ef99e8c9c3"));
    }

    @Test
    void addNewSalon() throws Exception {
        skipSetup=true;
        CreateSalonRequest createSalonRequest = TestData.getCreateSalonRequest();
        mockMvc.perform(post("/salons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSalonRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Salon"))
                .andExpect(jsonPath("$.address.city").value("Bacau"))
                .andExpect(jsonPath("$.address.houseNumber").value("12B"))
                .andExpect(jsonPath("$.address.postalBox").value("a12"))
                .andExpect(jsonPath("$.address.postcode").value("1080"))
                .andExpect(jsonPath("$.publicId").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").exists());
    }


}
