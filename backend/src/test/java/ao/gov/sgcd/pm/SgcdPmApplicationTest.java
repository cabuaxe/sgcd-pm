package ao.gov.sgcd.pm;

import ao.gov.sgcd.pm.seed.DataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SgcdPmApplicationTest {

    @MockitoBean
    private DataSeeder dataSeeder; // Prevent CommandLineRunner from executing

    @Test
    void contextLoads() {
        // Verifies the Spring application context loads successfully
        // with the test profile (H2 in-memory, Flyway disabled)
    }

    @Test
    void main_shouldNotThrow() {
        // Verify the main class exists and has the expected structure.
        // We do not call main() directly since @SpringBootTest already
        // starts the application context.
        assertDoesNotThrow(() -> {
            Class<?> clazz = Class.forName("ao.gov.sgcd.pm.SgcdPmApplication");
            assertNotNull(clazz);
            assertNotNull(clazz.getDeclaredMethod("main", String[].class));
        });
    }

    @Test
    void applicationClass_shouldHaveSpringBootApplicationAnnotation() {
        Class<?> clazz = SgcdPmApplication.class;
        assertTrue(clazz.isAnnotationPresent(
                        org.springframework.boot.autoconfigure.SpringBootApplication.class),
                "SgcdPmApplication should be annotated with @SpringBootApplication");
    }
}
