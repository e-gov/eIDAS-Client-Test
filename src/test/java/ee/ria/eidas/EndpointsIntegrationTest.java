package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.path.xml.XmlPath;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.path.xml.config.XmlPathConfig.xmlPathConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = EndpointsIntegrationTest.class)
@Category(IntegrationTest.class)
public class EndpointsIntegrationTest extends TestsBase {

    @Test
    public void metend1_caseSensitivityOnEndpoint() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get("/SP/MeTaDaTa").then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Test
    public void metend1_optionsMethodShouldReturnAllowedMethods() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(spMetadataUrl).then().log().ifValidationFails().statusCode(200).header("Allow","GET,HEAD");
    }

    @Test
    public void metend1_notSupportedHttpPostMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spMetadataUrl).then().log().ifValidationFails().statusCode(405).header("Allow","GET").body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void metend1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(spMetadataUrl).then().log().ifValidationFails().statusCode(405).header("Allow","GET").body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void metend1_headHttpMethodShouldNotReturnBody() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(spMetadataUrl).then().log().ifValidationFails().statusCode(200).body(isEmptyOrNullString());
    }

    @Test
    public void metend1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(spMetadataUrl).then().log().ifValidationFails().statusCode(405).header("Allow","GET").body("error", Matchers.equalTo("Method Not Allowed"));
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
                .formParam("relayState","")
                .formParam("loa","LOW")
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(spStartUrl).then().log().ifValidationFails().statusCode(200).header("Allow",Matchers.equalTo("POST,GET,HEAD"));
    }

    @Test
    public void autend1_headHttpMethodShouldNotReturnBody() {
        given()
                .formParam("relayState","")
                .formParam("loa","LOW")
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(spStartUrl).then().log().ifValidationFails().statusCode(400).body(isEmptyOrNullString());
    }

    @Test
    public void autend1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("loa","LOW")
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(spStartUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void autend1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("loa","LOW")
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(spStartUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void resend1_caseSensitivityOnEndpoint() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post("/ReTuRnUrL").then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Ignore
    @Test //TODO: Need clarification what should be returned, currently there is inconsistency between endpoints
    public void resend1_optionsMethodShouldReturnAllowedMethods() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(spReturnUrl).then().log().ifValidationFails().statusCode(200).header("Allow",Matchers.equalTo("POST, OPTIONS"));
    }

    @Ignore //TODO: Inconsistency, this returns method not allowed in this endpoint (without body), others 200
    @Test
    public void resend1_headHttpMethodShouldNotReturnBody() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(spReturnUrl).then().log().ifValidationFails().statusCode(200).body(isEmptyOrNullString());
    }

    @Ignore
    @Test
    public void resend1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore //TODO: This return HTTP 400 in this endpoint
    @Test
    public void resend1_notSupportedHttpGetMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore
    @Test
    public void resend1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

}
