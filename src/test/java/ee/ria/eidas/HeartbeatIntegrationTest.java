package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static java.lang.Math.toIntExact;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

@SpringBootTest(classes = HeartbeatIntegrationTest.class)
@Category(IntegrationTest.class)
public class HeartbeatIntegrationTest extends TestsBase {

    @Test
    public void heartbeatElementsPresence() {
        Response heartBeat = given()
                .filter(new AllureRestAssured())
                .when().get(testEidasClientProperties.getHealthcheckUrl())
                .then().log().ifValidationFails()
                .statusCode(200)
                .header("Content-Type", equalTo("application/json"))
                .extract().response();

        assertThat(heartBeat.body().jsonPath().get("status").toString(), Matchers.equalTo("UP"));
        assertThat(heartBeat.body().jsonPath().get("name"), Matchers.notNullValue());
        assertThat(heartBeat.body().jsonPath().get("version"), Matchers.notNullValue());
        assertThat(heartBeat.body().jsonPath().get("buildTime"), Matchers.notNullValue());
        assertThat(heartBeat.body().jsonPath().get("startTime"), Matchers.notNullValue());
        assertThat(heartBeat.body().jsonPath().get("currentTime"), Matchers.notNullValue());
        assertThat(heartBeat.body().jsonPath().get("dependencies.name"), Matchers.hasItem("credentials"));
        assertThat(heartBeat.body().jsonPath().get("dependencies.name"), Matchers.hasItem("hazelcast"));
        assertThat(heartBeat.body().jsonPath().get("dependencies.name"), Matchers.hasItem("eIDAS-Node"));
    }

    @Test
    public void defaultEndpointsDisabled() {
        given()
                .when().get(testEidasClientProperties.getHealthcheckUrl().replaceAll("heartbeat", "health"))
                .then().log().ifValidationFails().statusCode(404);
    }
}
