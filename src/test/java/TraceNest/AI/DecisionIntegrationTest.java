
package TraceNest.AI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:tracenesttest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class DecisionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void springApplicationContextShouldLoad() {
        assertNotNull(mockMvc);
    }

    @Test
    void createDecisionShouldReturn201() throws Exception {
        mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Integration test decision",
                        "reason": "Testing the POST endpoint",
                        "status": "PENDING"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title")
                        .value("Integration test decision"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createDecisionWithBlankTitleShouldReturn400WithDetails()
            throws Exception {
        mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "",
                        "reason": "Testing validation",
                        "status": "PENDING"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.title")
                        .value("Title is required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createDecisionWithInvalidStatusShouldReturn400WithDetails()
            throws Exception {
        mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Invalid status test",
                        "reason": "Testing status validation",
                        "status": "APPROVED"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.status")
                        .value("Status must be PENDING, IN_PROGRESS, or COMPLETED"));
    }

    @Test
    void createDecisionWithBlankReasonShouldReturn400()
            throws Exception {
        mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Missing reason",
                        "reason": " ",
                        "status": "PENDING"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.reason")
                        .value("Reason is required"));
    }

    @Test
    void createDecisionWithMalformedJsonShouldReturn400()
            throws Exception {
        mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title": "Broken JSON", "reason":
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid request body. Check your JSON syntax and field values."));
    }

    @Test
    void getAllDecisionsShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/decisions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON));
    }

    @Test
    void getDecisionByIdShouldReturn200() throws Exception {
        long id = createDecisionAndGetId(
                "Find this decision",
                "Testing retrieval by ID",
                "PENDING");

        mockMvc.perform(get("/api/decisions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title")
                        .value("Find this decision"));
    }

    @Test
    void getDecisionByUnknownIdShouldReturn404WithErrorBody()
            throws Exception {
        mockMvc.perform(get("/api/decisions/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Decision not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateDecisionShouldReturn200() throws Exception {
        long id = createDecisionAndGetId(
                "Original decision",
                "Before update",
                "PENDING");

        mockMvc.perform(put("/api/decisions/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Updated decision",
                        "reason": "After update",
                        "status": "COMPLETED"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Updated decision"))
                .andExpect(jsonPath("$.reason")
                        .value("After update"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateUnknownDecisionShouldReturn404() throws Exception {
        mockMvc.perform(put("/api/decisions/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Updated decision",
                        "reason": "Testing missing ID",
                        "status": "COMPLETED"
                    }
                    """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Decision not found"));
    }

    @Test
    void deleteDecisionShouldReturn204() throws Exception {
        long id = createDecisionAndGetId(
                "Decision to delete",
                "Testing DELETE endpoint",
                "PENDING");

        mockMvc.perform(delete("/api/decisions/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/decisions/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownDecisionShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/decisions/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private long createDecisionAndGetId(
            String title,
            String reason,
            String statusValue) throws Exception {

        MvcResult result = mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "%s",
                        "reason": "%s",
                        "status": "%s"
                    }
                    """.formatted(title, reason, statusValue)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();

        Matcher matcher = Pattern
                .compile("\"id\"\\s*:\\s*(\\d+)")
                .matcher(response);

        assertTrue(matcher.find(),
                "POST response should contain a decision ID");

        return Long.parseLong(matcher.group(1));
    }
@Test
void createDecisionWithReasonExceeding2000CharactersShouldReturn400()
        throws Exception {

    String longReason = "a".repeat(2001);

    String requestBody = """
        {
            "title": "Long reason test",
            "reason": "%s",
            "status": "PENDING"
        }
        """.formatted(longReason);

    mockMvc.perform(post("/api/decisions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.details.reason")
                    .value("Reason cannot exceed 2000 characters"));
}
}
