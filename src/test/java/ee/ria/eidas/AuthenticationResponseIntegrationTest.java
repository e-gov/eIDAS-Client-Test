package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = AuthenticationResponseIntegrationTest.class)
@Category(IntegrationTest.class)
public class AuthenticationResponseIntegrationTest extends TestsBase {

    //TODO: We do not receive a proper JSON response yet
    @Ignore
    @Test
    public void resp1_happyPath() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate");
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );

        assertEquals("Expected statusCode: Success", "urn:oasis:names:tc:SAML:2.0:status:Success", loginResponseJson.getString("statusCode"));
        assertEquals("Expected statusCode: Success", "http://eidas.europa.eu/LoA/substantial", loginResponseJson.getString("levelOfAssurance"));
        assertEquals("Expected statusCode: Success", "TestPNO", loginResponseJson.getString("attributes.PersonIdendifier"));
        assertEquals("Expected statusCode: Success", "TestDate", loginResponseJson.getString("attributes.DateOfBirth"));
        assertEquals("Expected statusCode: Success", "TestGiven", loginResponseJson.getString("attributes.FamilyName"));
        assertEquals("Expected statusCode: Success", "TestFamily", loginResponseJson.getString("attributes.FirstName"));
    }
    //TODO: We do not receive a proper JSON response yet
    @Ignore
    @Test
    public void resp1_authenticationFails() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "ConsentNotGiven");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("saas", loginResponse);
    }

    @Ignore //TODO: Inconsistency, this returns method not allowed in this endpoint (without body), others 200
    @Test
    public void resp1_headHttpMethodShouldNotReturnBody() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(spReturnUrl).then().log().ifValidationFails().statusCode(200).body(isEmptyOrNullString());
    }

    @Ignore
    @Test
    public void resp1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore //TODO: This return HTTP 400 in this endpoint
    @Test
    public void resp1_notSupportedHttpGetMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore
    @Test
    public void resp1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore
    @Test //TODO: Need clarification what should be returned, currently there is inconsistency between endpoints
    public void resp1_optionsMethodShouldReturnAllowedMethods() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(spReturnUrl).then().log().ifValidationFails().statusCode(200).header("Allow",Matchers.equalTo("POST, OPTIONS"));
    }
}
