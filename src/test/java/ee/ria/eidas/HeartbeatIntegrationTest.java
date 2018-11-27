package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static java.lang.Math.toIntExact;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

@SpringBootTest(classes = HeartbeatIntegrationTest.class)
@Category(IntegrationTest.class)
public class HeartbeatIntegrationTest extends TestsBase {

    @Test
    public void heartbeatIsUp() {
        given()
                .when().get(testEidasClientProperties.getHealthcheckUrl())
                .then().log().ifValidationFails().statusCode(200)
                .header("Content-Type", equalTo("application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"))
                .body("status", equalTo("UP"),
                        "name", equalTo("eidas-client-webapp"),
                        "version", matchesPattern("^[0-9].[0-9].[0-9](-[a-zA-Z0-9]*)?$"),
                        "buildTime", lessThan(toIntExact(new Date().getTime() / 1000)),
                        "startTime", lessThanOrEqualTo(toIntExact(new Date().getTime() / 1000)),
                        //Hope clocks are in sync
                        "currentTime", lessThanOrEqualTo(toIntExact(new Date().getTime() / 1000)),
                        "dependencies.size()", equalTo(2),
                        "dependencies[0].name", equalTo("eIDAS-Node"),
                        "dependencies[0].status", equalTo("UP"),
                        "dependencies[1].name", equalTo("hazelcast"),
                        "dependencies[0].status", equalTo("UP")
                );
    }

    @Test
    public void heartbeatAsJsonUrl() {
        given()
                .when().get(testEidasClientProperties.getHealthcheckUrl())
                .then().log().ifValidationFails().statusCode(200)
                .header("Content-Type", equalTo("application/vnd.spring-boot.actuator.v1+json;charset=UTF-8"))
                .body("status", equalTo("UP"),
                        "name", equalTo("eidas-client-webapp"),
                        "version", matchesPattern("^[0-9].[0-9].[0-9](-[a-zA-Z0-9]*)?$"),
                        "buildTime", lessThan(toIntExact(new Date().getTime() / 1000)),
                        "startTime", lessThanOrEqualTo(toIntExact(new Date().getTime() / 1000)),
                        "currentTime", lessThanOrEqualTo(toIntExact(new Date().getTime() / 1000)),
                        "dependencies.size()", equalTo(2),
                        "dependencies[0].name", equalTo("eIDAS-Node"),
                        "dependencies[0].status", equalTo("UP"),
                        "dependencies[1].name", equalTo("hazelcast"),
                        "dependencies[0].status", equalTo("UP")
                );
    }

    @Test
    public void defaultEndpointsDisabled() {
        given()
                .when().get(testEidasClientProperties.getHealthcheckUrl().replaceAll("heartbeat", "health"))
                .then().log().ifValidationFails().statusCode(404);
    }
}
