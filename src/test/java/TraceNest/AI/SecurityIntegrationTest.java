
package TraceNest.AI;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:tracenestsecuritytest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "app.jwt.secret=VHJhY2VOZXN0LVRlc3QtT25seS1TZWNyZXQtTm90LUZvci1Qcm9kdWN0aW9uLTIwMjY=",
    "app.jwt.expiration-ms=900000"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requestWithoutTokenShouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/decisions/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithInvalidTokenShouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/decisions/dashboard")
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer invalid-test-token"))
                .andExpect(status().isUnauthorized());
    }
}
