package training.salonzied.IT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import training.salonzied.dao.repo.UserRepository;
import util.TestData;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserIT extends IT {

    @Autowired
    UserRepository userRepository;

    @Override
    public void doSetup() {
        userRepository.deleteAll();
        var user = TestData.getUserEntity();
        user.setId(null);
        userRepository.save(user);
    }

    @Test
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/user").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].firstName").value("Maria"))
                .andExpect(jsonPath("$[0].lastName").value("Popescu"))
                .andExpect(jsonPath("$[0].email").value("maria.popescu@example.com"))
                .andExpect(jsonPath("$[0].phone").value("+32 470 12 34 56"))
                .andExpect(jsonPath("$[0].gender").value("female"))
                .andExpect(jsonPath("$[0].publicId").value("4a2f1e8c-11aa-4d8c-bd88-b0c82a8ff21e"));
    }

    @SkipSetupIT
    @Test
    void getAllUsersEmpty() throws Exception {
        userRepository.deleteAll();
        
        mockMvc.perform(get("/user").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @SkipSetupIT
    @Test
    void createUser() throws Exception {
        userRepository.deleteAll();
        
        var createUserRequest = TestData.getCreateUserRequest();

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Maria"))
                .andExpect(jsonPath("$.lastName").value("Popescu"))
                .andExpect(jsonPath("$.email").value("maria.popescu@example.com"))
                .andExpect(jsonPath("$.phone").value("+32 470 12 34 56"))
                .andExpect(jsonPath("$.birthDate").value("1980-01-01"))
                .andExpect(jsonPath("$.gender").value("female"))
                .andExpect(jsonPath("$.publicId").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @SkipSetupIT
    @Test
    void createUserBadRequest() throws Exception {
        userRepository.deleteAll();
        
        var createUserRequest = TestData.getCreateUserRequest();
        createUserRequest.setEmail(null); // Missing required field
        createUserRequest.setFirstName(null); // Missing required field

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void getUserByEmail() throws Exception {
        String email = "maria.popescu@example.com";

        mockMvc.perform(get("/user/" + email).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Maria"))
                .andExpect(jsonPath("$.lastName").value("Popescu"))
                .andExpect(jsonPath("$.email").value("maria.popescu@example.com"))
                .andExpect(jsonPath("$.phone").value("+32 470 12 34 56"))
                .andExpect(jsonPath("$.gender").value("female"))
                .andExpect(jsonPath("$.publicId").value("4a2f1e8c-11aa-4d8c-bd88-b0c82a8ff21e"));
    }

    @Test
    void getUserByEmailNotFound() throws Exception {
        String email = "nonexistent@example.com";

        mockMvc.perform(get("/user/" + email).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @SkipSetupIT
    @Test
    void updateUser() throws Exception {
        // Setup user first
        userRepository.deleteAll();
        var user = TestData.getUserEntity();
        user.setId(null);
        userRepository.save(user);

        // Update it
        var updateRequest = TestData.getUpdateUserRequest();

        mockMvc.perform(put("/user/maria.popescu@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Marian"))
                .andExpect(jsonPath("$.lastName").value("Popescu"))
                .andExpect(jsonPath("$.phone").value("+40 723 45 67 89"))
                .andExpect(jsonPath("$.gender").value("male"))
                .andExpect(jsonPath("$.email").value("maria.popescu@example.com")) // Email shouldn't change
                .andExpect(jsonPath("$.publicId").exists());
    }

    @Test
    void updateUserNotFound() throws Exception {
        var updateRequest = TestData.getUpdateUserRequest();

        mockMvc.perform(put("/user/nonexistent@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void deleteUser() throws Exception {
        String email = "maria.popescu@example.com";

        mockMvc.perform(delete("/user/" + email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verify it's deleted
        mockMvc.perform(get("/user/" + email).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void deleteUserNotFound() throws Exception {
        String email = "nonexistent@example.com";

        mockMvc.perform(delete("/user/" + email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"));
    }

}

