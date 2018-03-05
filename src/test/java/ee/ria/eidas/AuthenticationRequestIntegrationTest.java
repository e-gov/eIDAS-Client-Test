package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.path.xml.XmlPath;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = AuthenticationRequestIntegrationTest.class)
@Category(IntegrationTest.class)
public class AuthenticationRequestIntegrationTest extends TestsBase {

    @Test
    public void auth4_allLoaLevelsAreAccepted() {
        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "LOW", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/low", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));

        samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "SUBSTANTIAL", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/substantial", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));

        samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "HIGH", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test
    public void auth6_invalidLoaLevelsAreNotAccepted() {
        given()
                .formParam("relayState","")
                .formParam("loa","SUPER")
                .formParam("country","EE")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifError().statusCode(400).body("exception",equalTo("org.springframework.web.method.annotation.MethodArgumentTypeMismatchException"));

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "HIGH", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test //TODO: needs a method to fetch the supported country codes
    public void auth6_invalidCountryIsNotAccepted() {
        given()
                .formParam("relayState","")
                .formParam("loa","LOW")
                .formParam("country","Est")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifValidationFails().statusCode(400).body("error", Matchers.equalTo("Invalid country! Valid countries:[EE, CA]"));

        given()
                .formParam("relayState","")
                .formParam("loa","LOW")
                .formParam("country","ee")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifValidationFails().statusCode(400).body("error", Matchers.equalTo("Invalid country! Valid countries:[EE, CA]"));

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "HIGH", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test
    public void auth6_notSupportedCountryIsNotAccepted() {
        given()
                .formParam("relayState","")
                .formParam("loa","LOW")
                .formParam("country","SZ")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifValidationFails().statusCode(400).body("error", Matchers.equalTo("Invalid country! Valid countries:[EE, CA]"));

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "HIGH", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test
    public void auth6_loaMissingValueShouldReturnDefault() {
        String body = given()
                .formParam("relayState","")
                .formParam("loa","")
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifValidationFails().statusCode(200).extract().body().asString();

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(body);
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/substantial", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test
    public void auth6_optionalParametersMissingShouldReturnDefault() {
        String body = given()
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifValidationFails().statusCode(200).extract().body().asString();

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(body);
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/substantial", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test
    public void auth6_invalidParametersAreNotBlocking() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("randomParam", "random");
        formParams.put("loa", "LOW");
        formParams.put("country", "EE");
        formParams.put("relayState", "1234abcd");

        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, getAuthenticationReqForm(formParams));
        assertEquals("Status is returned with correct relayState","1234abcd", html.getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

    @Test
    public void auth6_errorIsReturnedOnWrongRelayStatePattern() {
        String relayState = RandomStringUtils.randomAlphanumeric(81);
        given()
                .formParam("relayState",relayState)
                .formParam("loa","LOW")
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifValidationFails().statusCode(400).body("error", Matchers.equalTo("Invalid RelayState! Must match the following regexp: ^[a-zA-Z0-9-_]{0,80}$"));

        relayState = "<>$";
        given()
                .formParam("relayState",relayState)
                .formParam("loa","LOW")
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifValidationFails().statusCode(400).body("error", Matchers.equalTo("Invalid RelayState! Must match the following regexp: ^[a-zA-Z0-9-_]{0,80}$"));

    }

    @Ignore
    @Test
    public void auth6_loaLevelCaseSensitivity() {
        String response = given()
                .formParam("relayState","")
                .formParam("lOa","high")
                .formParam("country","EE")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifError().statusCode(200).extract().body().asString();

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(response);
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Ignore
    @Test
    public void auth6_relayStateCaseSensitivity() {
        String response = given()
                .formParam("ReLaYStAtE","RelayState123")
                .formParam("loa","HIGH")
                .formParam("country","EE")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifError().statusCode(200).extract().body().asString();

        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, response);
        assertEquals("Status is returned with correct relayState","RelayState123", html.getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

    @Ignore
    @Test
    public void auth6_countryCaseSensitivity() {
        String response = given()
                .formParam("cOuNtRy","EE")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(spStartUrl).then().log().ifError().statusCode(200).extract().body().asString();

        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, response);
        assertEquals("Status is returned with correct relayState","EE", html.getString("**.findAll { it.@name == 'country' }.@value"));
    }

    @Test
    public void auth6_caseSensitivityOnPost() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post("/LoGiN").then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Test
    public void auth6_caseSensitivityOnGet() {
        given()
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get("/LoGiN").then().log().ifValidationFails().statusCode(404).body("error",equalTo("Not Found"));
    }

    @Test
    public void auth6_headHttpMethodShouldNotReturnBody() {
        given()
                .formParam("relayState","")
                .formParam("loa","LOW")
                .formParam("country","CA")
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(spStartUrl).then().log().ifValidationFails().statusCode(200).body(isEmptyOrNullString());
    }

    @Test
    public void auth6_notSupportedHttpPutMethodShouldReturnError() {
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
    public void auth6_notSupportedHttpDeleteMethodShouldReturnError() {
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
    public void auth6_optionsMethodShouldReturnAllowedMethods() {
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
    public void auth9_loginPageIsDisplayed() {
        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, getLoginPage());
        assertEquals("Login page is loaded", "eIDAS Client Login", html.getString("html.body.div.div.h1"));
    }
}
