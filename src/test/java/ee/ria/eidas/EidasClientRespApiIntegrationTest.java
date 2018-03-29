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

        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        Response response = sendSamlResponseGetStatus(relayState, base64Response );

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid RelayState! Must match the following regexp:"));
    }

    @Ignore //TODO: Currently there is no relay state filter
    @Test
    public void respApi2_errorIsReturnedOnWrongCharactersInRelayState() {
        String relayState = "<>$";

        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        Response response = sendSamlResponseGetStatus(relayState, base64Response );

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid RelayState! Must match the following regexp:"));
    }



    @Test
    public void authApi3_relayStateMissingValueShouldReturnOkStatus() {
        Response response = getAuthenticationReqResponse("CA", "", "");

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi3_relayStateMissingShouldReturnOkStatus() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(COUNTRY, "EE");

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Ignore //TODO: Currently there is no additional attributes support
    @Test
    public void authApi4_notSupportedAdditionalAttributeShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ADDITIONAL_ATTRIBUTES, "NotExistingAttribute");
        formParams.put(COUNTRY, "EE");
        formParams.put(RELAY_STATE, "1234abcd");

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid AdditionalParameters! Unrecognized attibute(s) provided:"));
    }

    @Ignore //TODO: Currently there is no additional attributes support
    @Test
    public void authApi4_additionalAttributesNotSepparatedCorrectlyShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ADDITIONAL_ATTRIBUTES, "LegalPersonIdentifierCurrentAddressGender");
        formParams.put(COUNTRY, "EE");
        formParams.put(RELAY_STATE, "1234abcd");

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid AdditionalParameters! Unrecognized attibute(s) provided:"));
    }

    @Ignore //TODO: Currently there is no additional attributes support
    @Test
    public void authApi4_additionalAttributesSepparatedWithWrongCharacterShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ADDITIONAL_ATTRIBUTES, "LegalPersonIdentifier&CurrentAddress&Gender");
        formParams.put(COUNTRY, "EE");
        formParams.put(RELAY_STATE, "1234abcd");

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid AdditionalParameters! Unrecognized attibute(s) provided:"));
    }

    @Ignore //TODO: Currently there is no additional attributes support
    @Test
    public void authApi4_additionalAttributesSepparatedWithoutUrlEncodingShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ADDITIONAL_ATTRIBUTES, "LegalPersonIdentifier CurrentAddress Gender");
        formParams.put(COUNTRY, "EE");
        formParams.put(RELAY_STATE, "1234abcd");

        Response response = given()
                .queryParams(formParams)
   //             .contentType("application/x-www-form-urlencoded") //TODO: what is default behavior in restAssured? Needs to be rechecked when additional Attributes are implemented
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getSpStartUrl()).then().log().ifError().extract().response();

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid AdditionalParameters! Unrecognized attibute(s) provided:"));
    }

    @Test
    public void authApi5_checkHtmlValues() {
        String response = getAuthenticationReq("CA", "SUBSTANTIAL", "RelayState");

        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, response);

        assertEquals("Target url is present", testEidasClientProperties.getFullIdpStartUrl(), html.getString("html.body.form.@action"));
        assertEquals("Method is post", "post", html.getString("html.body.form.@method"));
        assertEquals("Relay state is present", "RelayState", html.getString("**.findAll { it.@name == 'RelayState' }.@value"));
        assertEquals("Country is present", "CA", html.getString("**.findAll { it.@name == 'country' }.@value")); //TODO: Should be Country?
        assertEquals("SAML request is present because LoA is accessible", LOA_SUBSTANTIAL, getDecodedSamlRequestBodyXml(response).getString(XML_LOA));
    }

    @Test
    public void authApi6_multipleParametersAreNotBlocking() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(LOA, "LOW");
        formParams.put(COUNTRY, "EE");
        formParams.put(RELAY_STATE, "1234abcd");

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReqForm(formParams).getBody().asString());

        assertEquals("Correct LOA is returned", LOA_LOW, samlRequest.getString(XML_LOA)); //TODO: Is it ok to take last?
        }

    @Test
    public void authApi6_invalidParametersAreNotBlocking() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("randomParam", "random");
        formParams.put(LOA, "LOW");
        formParams.put(COUNTRY, "EE");
        formParams.put(RELAY_STATE, "1234abcd");

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status is returned with correct relayState","1234abcd", response.getBody().xmlPath(XmlPath.CompatibilityMode.HTML).getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

}
