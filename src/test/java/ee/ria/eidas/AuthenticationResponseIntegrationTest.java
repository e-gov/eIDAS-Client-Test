package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import static ee.ria.eidas.config.EidasTestStrings.*;
import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = AuthenticationResponseIntegrationTest.class)
@Category(IntegrationTest.class)
public class AuthenticationResponseIntegrationTest extends TestsBase {

    @Ignore
    @Test
    public void resp1_happyPath() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestGiven","TestFamily","TestPNO", "TestDate", null);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", "TestPNO", loginResponseJson.getString(STATUS_PNO));
        assertEquals("Correct date is returned", "TestDate", loginResponseJson.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", "TestFamily", loginResponseJson.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", "TestGiven", loginResponseJson.getString(STATUS_FIRST));
    }

    //TODO: We do not receive a proper JSON error response yet
    @Ignore
    @Test
    public void resp1_authenticationFails() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "ConsentNotGiven");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("saas", loginResponse);
    }

    @Ignore
    @Test
    public void resp1_faultyAuthentication() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), null,"TestFamily", "TestPNO", "TestDate", null);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("saas", loginResponse);
    }

    @Ignore //TODO: Need a specification for error statuses
    @Test
    public void resp3_responseWithLowLoaShouldNotBeAcceptedWhenSubstantialWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Error message is expected", "", loginResponseJson.getString(STATUS_CODE));
    }

    @Ignore //TODO: Need a specification for error statuses
    @Test
    public void resp3_responseWithLowLoaShouldNotBeAcceptedWhenHighWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Error message is expected", "", loginResponseJson.getString(STATUS_CODE));
    }

    @Ignore //TODO: Need a specification for error statuses
    @Test
    public void resp3_responseWithSubstantialLoaShouldNotBeAcceptedWhenHighWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Error message is expected", "", loginResponseJson.getString(STATUS_CODE));
    }

    @Ignore
    @Test
    public void resp3_responseWithSameLoaShouldBeAcceptedLow() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_LOW, loginResponseJson.getString(STATUS_LOA));
    }

    @Ignore
    @Test
    public void resp3_responseWithSameLoaShouldBeAcceptedSubstantial() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
    }

    @Ignore
    @Test
    public void resp3_responseWithSameLoaShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    @Ignore
    @Test
    public void resp3_responseWithHigherLoaLowShouldBeAcceptedSubstantial() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
    }

    @Ignore
    @Test
    public void resp3_responseWithHigherLoaLowShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    @Ignore
    @Test
    public void resp3_responseWithHigherLoaSubstantialShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    @Ignore //TODO: No relay state handling currently implemented
    @Test
    public void respX_relayStateChangeShouldReturnError() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL","relayState"), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("changedRelayState",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
    }

    @Ignore //TODO: Inconsistency, this returns method not allowed in this endpoint (without body), others 200
    @Test
    public void resp1_headHttpMethodShouldNotReturnBody() {
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
    public void resp1_notSupportedHttpPutMethodShouldReturnError() {
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
    public void resp1_notSupportedHttpGetMethodShouldReturnError() {
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
    public void resp1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
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
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate", null))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(spReturnUrl).then().log().ifValidationFails().statusCode(200).header("Allow",Matchers.equalTo("POST, OPTIONS"));
    }
}
