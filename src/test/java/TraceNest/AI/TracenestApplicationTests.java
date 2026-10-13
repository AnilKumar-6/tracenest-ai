
package TraceNest.AI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:tracenestcontexttest;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "app.jwt.secret=VHJhY2VOZXN0LVRlc3QtT25seS1TZWNyZXQtTm90LUZvci1Qcm9kdWN0aW9uLTIwMjY=",
    "app.jwt.expiration-ms=900000"
})
class TracenestApplicationTests {

    @Test
    void contextLoads() {
    }
}
