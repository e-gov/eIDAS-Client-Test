package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@SpringBootTest(classes = SupportedCountriesIntegrationTest.class)
@Category(IntegrationTest.class)
public class SupportedCountriesIntegrationTest extends TestsBase {

    @Test
    public void getSupportedCountriesList() {
        Response supportedCountries = given()
                .filter(new AllureRestAssured())
                .when()
                .get(testEidasClientProperties.getSupportedCountriesUrl())
                .then()
                .statusCode(200)
                .header("Content-Type", equalTo("application/json"))
                .extract().response();

        assertThat(supportedCountries.body().jsonPath().get("public").toString(), notNullValue());
        assertThat(supportedCountries.body().jsonPath().get("private").toString(), notNullValue());
    }
}
