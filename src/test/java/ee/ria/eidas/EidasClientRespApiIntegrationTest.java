package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static ee.ria.eidas.config.EidasTestStrings.*;
import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = EidasClientRespApiIntegrationTest.class)
@Category(IntegrationTest.class)
public class EidasClientRespApiIntegrationTest extends TestsBase {

    @Ignore //TODO: Currently there is no relay state filter
    @Test
    public void respApi2_errorIsReturnedOnTooLongRelayState() {
        String relayState = RandomStringUtils.randomAlphanumeric(81);

        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq(DEF_COUNTRY,"SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        Response response = sendSamlResponseGetStatus(relayState, base64Response );

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid RelayState! Must match the following regexp:"));
    }

    @Ignore //TODO: Currently there is no relay state filter
    @Test
    public void respApi2_errorIsReturnedOnWrongCharactersInRelayState() {
        String relayState = "<>$";

        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq(DEF_COUNTRY,"SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        Response response = sendSamlResponseGetStatus(relayState, base64Response );

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid RelayState! Must match the following regexp:"));
    }

    @Test
    public void respApi2_relayStateMissingValueShouldReturnOkStatus() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq(DEF_COUNTRY,"SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);

        Response response =  given()
                .formParam(RELAY_STATE, "")
                .formParam(SAML_RESPONSE, base64Response)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .extract().response();

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void respApi2_relayStateMissingShouldReturnOkStatus() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(COUNTRY, DEF_COUNTRY);

        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqForm(formParams).getBody().asString(), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);

        Response response =  given()
                .formParam(SAML_RESPONSE, base64Response)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .extract().response();

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void respApi3_samlResponseMissingShouldReturnErrorStatus() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);

        Response response =  given()
                .formParam(RELAY_STATE, "")
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .extract().response();

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Required String parameter 'SAMLResponse' is not present"));
    }

    @Test
    public void respApi3_samlResponseNotBase64ShouldReturnErrorStatus() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);

        Response response =  given()
                .formParam(SAML_RESPONSE, base64Response+"&/!")
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .extract().response();

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid SAMLResponse. Failed to read SAMLResponse."));
    }

    @Test
    public void respApi4_additionalParametersShouldReturnOk() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);

        Response response =  given()
                .formParam("randomParam", "randomValue")
                .formParam(SAML_RESPONSE, base64Response)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .extract().response();

        assertEquals("Status code should be: 200", 200, response.statusCode());
        assertEquals("Name is returned", "TestGiven", getValueFromJsonResponse(response, STATUS_FIRST));
    }

    @Test
    public void respApi4_multipleParametersAreNotBlocking() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        String base64Response2 = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestGiven2","TestFamily2","TestPNO", "TestDate", LOA_SUBSTANTIAL);

        Response response =  given()
                .formParam(SAML_RESPONSE, base64Response)
                .formParam(SAML_RESPONSE, base64Response2)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .extract().response();

        assertEquals("Status code should be: 200", 200, response.statusCode());
        assertEquals("Last used parameter is returned", "TestGiven", getValueFromJsonResponse(response, STATUS_FIRST));
     }
}
