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
    public void metend1_caseSensitivityOnEndpoint() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get("/MeTaDaTa").then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Test
    public void metend1_optionsMethodShouldReturnAllowedMethods() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(200).header("Allow","GET,HEAD");
    }

    @Test
    public void metend1_notSupportedHttpPostMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(405).body("message",Matchers.equalTo("Request method 'POST' not supported"));
    }

    @Test
    public void metend1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(405).body("message",Matchers.equalTo("Request method 'PUT' not supported"));
    }

    @Test
    public void metend1_headHttpMethodShouldNotReturnBody() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(200).body(isEmptyOrNullString());
    }

    @Test
    public void metend1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(testEidasClientProperties.getSpMetadataUrl()).then().log().ifValidationFails().statusCode(405).body("message", Matchers.equalTo("Request method 'DELETE' not supported"));
    }

    @Test
    public void autend1_caseSensitivityOnGet() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get("/LoGiN").then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Ignore //TODO: Returns POST but logical would be GET
    @Test
    public void autend1_optionsMethodShouldReturnAllowedMethods() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(LOA,"LOW")
                .formParam(COUNTRY,"CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(200).header("Allow",Matchers.equalTo("POST,GET,HEAD"));
    }

    @Test
    public void autend1_headHttpMethodShouldNotReturnBody() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(LOA,"LOW")
                .formParam(COUNTRY,"CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(500).body(isEmptyOrNullString());
    }

    @Test
    public void autend1_notSupportedHttpPutMethodShouldReturnError() {
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
    public void autend1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(LOA,"LOW")
                .formParam(COUNTRY,"CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void resend1_caseSensitivityOnEndpoint() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post("/ReTuRnUrL").then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Ignore
    @Test //TODO: Need clarification what should be returned, currently there is inconsistency between endpoints
    public void resend1_optionsMethodShouldReturnAllowedMethods() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(200).header("Allow",Matchers.equalTo("POST, OPTIONS"));
    }

    @Ignore //TODO: Inconsistency, this returns method not allowed in this endpoint (without body), others 200
    @Test
    public void resend1_headHttpMethodShouldNotReturnBody() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(200).body(isEmptyOrNullString());
    }

    @Ignore
    @Test
    public void resend1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore //TODO: This return HTTP 400 in this endpoint
    @Test
    public void resend1_notSupportedHttpGetMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore
    @Test
    public void resend1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam(RELAY_STATE,"")
                .formParam(SAML_RESPONSE,getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(testEidasClientProperties.getSpReturnUrl()).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

}
