
// package TraceNest.AI;

// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest(properties = {
//     "spring.datasource.url=jdbc:h2:mem:tracenesttest;DB_CLOSE_DELAY=-1",
//     "spring.datasource.driver-class-name=org.h2.Driver",
//     "spring.datasource.username=sa",
//     "spring.datasource.password=",
//     "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
//     "spring.jpa.hibernate.ddl-auto=create-drop"
    
    
// })
// class DecisionIntegrationTest {

//     @Test
//     void springApplicationContextShouldLoad() {
//         assertNotNull(this);
//     }
// }

package TraceNest.AI;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
        assertNotNull(this);
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
    void createDecisionWithBlankTitleShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "",
                        "reason": "Testing validation",
                        "status": "PENDING"
                    }
                    """))
                .andExpect(status().isBadRequest());
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
        String response = mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Find this decision",
                        "reason": "Testing retrieval by ID",
                        "status": "PENDING"
                    }
                    """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        java.util.regex.Matcher matcher =
                java.util.regex.Pattern
                        .compile("\"id\"\\s*:\\s*(\\d+)")
                        .matcher(response);

        assertTrue(matcher.find(),
                "POST response should contain a decision ID");

        long id = Long.parseLong(matcher.group(1));

        mockMvc.perform(get("/api/decisions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Find this decision"));
    }

    @Test
    void updateDecisionShouldReturn200() throws Exception {
        String response = mockMvc.perform(post("/api/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Original decision",
                        "reason": "Before update",
                        "status": "PENDING"
                    }
                    """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        java.util.regex.Matcher matcher =
                java.util.regex.Pattern
                        .compile("\"id\"\\s*:\\s*(\\d+)")
                        .matcher(response);

        assertTrue(matcher.find(),
                "POST response should contain a decision ID");

        long id = Long.parseLong(matcher.group(1));

        mockMvc.perform(put("/api/decisions/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Updated decision",
                        "reason": "After update",
                        "status": "APPROVED"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Updated decision"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
    @Test
void deleteDecisionShouldReturn204() throws Exception {
    String response = mockMvc.perform(post("/api/decisions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "title": "Decision to delete",
                    "reason": "Testing DELETE endpoint",
                    "status": "PENDING"
                }
                """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    java.util.regex.Matcher matcher =
            java.util.regex.Pattern
                    .compile("\"id\"\\s*:\\s*(\\d+)")
                    .matcher(response);

    assertTrue(matcher.find(),
            "POST response should contain a decision ID");

    long id = Long.parseLong(matcher.group(1));

    mockMvc.perform(delete("/api/decisions/" + id))
            .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/decisions/" + id))
            .andExpect(status().isNotFound());
}
}
