package training.salonzied.IT;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IT {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void baseSetUp(TestInfo testInfo) {
        boolean shouldSkip = testInfo.getTestMethod()
                .map(m -> m.isAnnotationPresent(SkipSetupIT.class))
                .orElse(false);

        if (shouldSkip) {
            return;
        }

        // hook pt subclasses
        doSetup();
    }
    protected void doSetup() {
    }
}
