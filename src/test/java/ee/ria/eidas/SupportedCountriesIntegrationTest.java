package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


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
                .body(containsString("\"CA\""));
    }
}
