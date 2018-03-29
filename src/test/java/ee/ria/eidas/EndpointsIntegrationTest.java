package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;


import static ee.ria.eidas.config.EidasTestStrings.*;
import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.*;

@SpringBootTest(classes = EndpointsIntegrationTest.class)
@Category(IntegrationTest.class)
public class EndpointsIntegrationTest extends TestsBase {

    @Test
    public void metEnd1_caseSensitivityOnEndpoint() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpMetadataUrl().toUpperCase()).then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Ignore //TODO: TARAEI-102
    @Test
    public void metEnd1_notSupportedHttpOptionsMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void metEnd1_notSupportedHttpPostMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void metEnd1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore //TODO: TARAEI-102
    @Test
    public void metEnd1_notSupportedHttpHeadMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void metEnd1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void autEnd1_caseSensitivityOnGet() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl().toUpperCase()).then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Ignore //TODO: TARAEI-102
    @Test
    public void autEnd1_notSupportedHttpOptionsMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(LOA,"LOW")
                .formParam(COUNTRY,"CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore //TODO: TARAEI-102
    @Test
    public void autEnd1_notSupportedHttpHeadMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(LOA,"LOW")
                .formParam(COUNTRY,"CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void autEnd1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(LOA,"LOW")
                .formParam(COUNTRY,"CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void autEnd1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(LOA,"LOW")
                .formParam(COUNTRY,"CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void autEnd1_notSupportedHttpPostMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(LOA,"LOW")
                .formParam(COUNTRY,"CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void resEnd1_caseSensitivityOnEndpoint() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl().toUpperCase()).then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Ignore //TODO: TARAEI-102
    @Test
    public void resEnd1_notSupportedHttpOptionsMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE, getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore //TODO: TARAEI-102
    @Test
    public void resEnd1_notSupportedHttpHeadMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE, getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void resEnd1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void resEnd1_notSupportedHttpGetMethodShouldReturnError() {
        given()
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void resEnd1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(405).body("error", Matchers.equalTo("Method Not Allowed"));
    }

}
