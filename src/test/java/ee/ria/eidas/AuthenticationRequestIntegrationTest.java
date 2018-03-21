package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.path.xml.XmlPath;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.equalTo;
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
                .queryParam("relayState","")
                .queryParam("loa","SUPER")
                .queryParam("country","EE")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifError().statusCode(400).body("exception",equalTo("org.springframework.web.method.annotation.MethodArgumentTypeMismatchException"));

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "HIGH", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test //TODO: needs a method to fetch the supported country codes
    public void auth6_invalidCountryIsNotAccepted() {
        given()
                .queryParam("relayState","")
                .queryParam("loa","LOW")
                .queryParam("country","Est")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(400).body("error", Matchers.startsWith("Invalid country! Valid countries:["));

        given()
                .queryParam("relayState","")
                .queryParam("loa","LOW")
                .queryParam("country","ee")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(400).body("error", Matchers.startsWith("Invalid country! Valid countries:["));

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "HIGH", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test
    public void auth6_notSupportedCountryIsNotAccepted() {
        given()
                .queryParam("relayState","")
                .queryParam("loa","LOW")
                .queryParam("country","SZ")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(400).body("error", Matchers.startsWith("Invalid country! Valid countries:["));

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("EE", "HIGH", "relayState"));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test
    public void auth6_loaMissingValueShouldReturnDefault() {
        String body = given()
                .queryParam("relayState","")
                .queryParam("loa","")
                .queryParam("country","CA")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(200).extract().body().asString();

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(body);
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/substantial", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Test
    public void auth6_optionalParametersMissingShouldReturnDefault() {
        String body = given()
                .queryParam("country","CA")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(200).extract().body().asString();

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
                .queryParam("relayState",relayState)
                .queryParam("loa","LOW")
                .queryParam("country","CA")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(400).body("error", Matchers.equalTo("Invalid RelayState! Must match the following regexp: ^[a-zA-Z0-9-_]{0,80}$"));

        relayState = "<>$";
        given()
                .queryParam("relayState",relayState)
                .queryParam("loa","LOW")
                .queryParam("country","CA")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifValidationFails().statusCode(400).body("error", Matchers.equalTo("Invalid RelayState! Must match the following regexp: ^[a-zA-Z0-9-_]{0,80}$"));

    }

    @Ignore
    @Test
    public void auth6_loaLevelCaseSensitivity() {
        String response = given()
                .queryParam("relayState","")
                .queryParam("lOa","high")
                .queryParam("country","EE")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifError().statusCode(200).extract().body().asString();

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(response);
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }

    @Ignore
    @Test
    public void auth6_relayStateCaseSensitivity() {
        String response = given()
                .queryParam("ReLaYStAtE","RelayState123")
                .queryParam("loa","HIGH")
                .queryParam("country","EE")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifError().statusCode(200).extract().body().asString();

        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, response);
        assertEquals("Status is returned with correct relayState","RelayState123", html.getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

    @Ignore
    @Test
    public void auth6_countryCaseSensitivity() {
        String response = given()
                .queryParam("cOuNtRy","EE")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifError().statusCode(200).extract().body().asString();

        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, response);
        assertEquals("Status is returned with correct relayState","EE", html.getString("**.findAll { it.@name == 'country' }.@value"));
    }

}
