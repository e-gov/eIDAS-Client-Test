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

@SpringBootTest(classes = SupportedCountriesIntegrationTest.class)
@Category(IntegrationTest.class)
public class SupportedCountriesIntegrationTest extends TestsBase {

    @Test
    public void getSupportedCountriesList() {
        given()
                .when()
                .get(testEidasClientProperties.getSupportedCountriesUrl())
                .then()
                .statusCode(200)
                .header("Content-Type", equalTo("application/json"))
                .body(equalTo("[\"CA\"]"));
    }
}
