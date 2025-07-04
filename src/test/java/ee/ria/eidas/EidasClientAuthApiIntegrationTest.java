package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.qameta.allure.restassured.AllureRestAssured;
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = EidasClientAuthApiIntegrationTest.class)
@Category(IntegrationTest.class)
public class EidasClientAuthApiIntegrationTest extends TestsBase {

    @Test
    public void authApi1_countryCodeNotInCorrectFormatShouldReturnError() {
        Response response =  getAuthenticationReqResponse("Est", "", "", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid country for PUBLIC sector! Valid countries:"));
    }

    @Test
    public void authApi1_countryCodeCaseSensitiveShouldPass() {
        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq(DEF_COUNTRY.toLowerCase(), "", "", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC));
        assertEquals("Correct LOA is returned", LOA_SUBSTANTIAL, samlRequest.getString(XML_LOA));
    }

    @Test
    public void authApi1_notSupportedCountryCodeForPublicShouldReturnError() {
        Response response =  getAuthenticationReqResponse("SZ", "", "", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid country for PUBLIC sector! Valid countries:"));
    }

    @Test
    public void authApi1_notSupportedCountryCodeForPrivateShouldReturnError() {
        Response response =  getAuthenticationReqResponse("SZ", "", "", REQUESTER_ID_VALUE, SP_TYPE_PRIVATE);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid country for PRIVATE sector! Valid countries:"));
    }

    @Test
    public void authApi1_countryParameterMissingShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(RELAY_STATE, "1234abcd");

        Response response = getAuthenticationReqFormFail(formParams);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertEquals("Bad request error should be returned", "Required request parameter 'Country' for method parameter type String is not present", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void authApi2_invalidLoaLevelsAreNotAccepted() {
        Response response =  getAuthenticationReqResponse(DEF_COUNTRY, "SUPER", "", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid value for parameter LoA"));
    }

    @Test
    public void authApi2_loaMissingValueShouldReturnDefault() {
        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq(DEF_COUNTRY, "", "", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC));

        assertEquals("Correct LOA is returned", LOA_SUBSTANTIAL, samlRequest.getString(XML_LOA));
    }

    @Test
    public void authApi2_loaParameterMissingShouldReturnDefault() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReqForm(formParams).getBody().asString());

        assertEquals("Correct LOA is returned",LOA_SUBSTANTIAL, samlRequest.getString(XML_LOA));
    }

    @Test
    public void authApi3_errorIsReturnedOnTooLongRelayState() {
        String relayState = RandomStringUtils.randomAlphanumeric(81);

        Response response = getAuthenticationReqResponse(DEF_COUNTRY, "", relayState, REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid RelayState (" + relayState + ")! Must match the following regexp:"));
    }

    @Test
    public void authApi3_errorIsReturnedOnWrongCharactersInRelayState() {
        String relayState = "<>$";

        Response response = getAuthenticationReqResponse(DEF_COUNTRY, "", relayState, REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid RelayState (" + relayState + ")! Must match the following regexp:"));
    }

    @Test
    public void authApi3_relayStateValueShouldBeReturned() {
        String relayState = RandomStringUtils.randomAlphanumeric(80);

        Response response = getAuthenticationReqResponse(DEF_COUNTRY, "", relayState, REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 200", 200, response.statusCode());
        assertEquals("Correct RelayState is returned", relayState, response.getBody().xmlPath(XmlPath.CompatibilityMode.HTML).getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

    @Test
    public void authApi3_relayStateMissingValueShouldReturnOkStatus() {
        Response response = getAuthenticationReqResponse(DEF_COUNTRY, "", "", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi3_relayStateMissingShouldReturnOkStatus() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi4_requesterIdFormatOkShouldReturnOkStatus() {
        Response response = getAuthenticationReqResponse(DEF_COUNTRY, "", "", "prefix:suffix", SP_TYPE_PRIVATE);

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi4_requesterIdFormatNotOkShouldReturnOkStatus() {
        Response response = getAuthenticationReqResponse(DEF_COUNTRY, "", "", "urn:uri:@", SP_TYPE_PRIVATE);

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi4_requesterIdEmptyValueShouldReturnError() {
        Response response =  getAuthenticationReqResponse(DEF_COUNTRY, "", "", "", SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), is("Required request parameter 'RequesterID' for method parameter type String is present but converted to null"));
    }

    @Test
    public void authApi4_requesterIdIllegalCharactersShouldReturnError() {
        Response response =  getAuthenticationReqResponse(DEF_COUNTRY, "", "", "SPA CE", SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), is("Invalid RequesterID (SPA CE)! Must match the following regexp: ^((?!urn:uuid:)[a-zA-Z][a-zA-Z0-9+.-]*:.*|urn:uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})$"));
    }

    @Test
    public void authApi4_requesterIdMissingShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqFormFail(formParams);

        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), is("Required request parameter 'RequesterID' for method parameter type String is not present"));
    }

    @Test
    public void authApi5_spTypeEmptyValueShouldReturnError() {
        Response response =  getAuthenticationReqResponse(DEF_COUNTRY, "", "", REQUESTER_ID_VALUE, "");

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), is("Required request parameter 'SPType' for method parameter type SPType is present but converted to null"));
    }

    @Test
    public void authApi5_spTypeInvalidValueShouldReturnError() {
        Response response =  getAuthenticationReqResponse(DEF_COUNTRY, "", "", REQUESTER_ID_VALUE, "private&public");

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), is("Invalid value for parameter SPType"));
    }

    @Test
    public void authApi5_spTypePublicShouldReturnOkStatus() {
        Response response = getAuthenticationReqResponse(DEF_COUNTRY, "", "", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi5_spTypePrivateShouldReturnOkStatus() {
        Response response = getAuthenticationReqResponse(DEF_COUNTRY, "", "", REQUESTER_ID_VALUE, SP_TYPE_PRIVATE);

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi5_spTypeMissingShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);

        Response response = getAuthenticationReqFormFail(formParams);

        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), is("Required request parameter 'SPType' for method parameter type SPType is not present"));
    }

    @Test
    public void authApi6_notSupportedAdditionalAttributeShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "NotExistingAttribute");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqFormFail(formParams);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Found one or more invalid Attributes value(s). Valid values are:"));
    }

    @Test
    public void authApi6_additionalAttributesNotSeparatedCorrectlyShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifierCurrentAddressGender");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqFormFail(formParams);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Found one or more invalid Attributes value(s). Valid values are:"));
    }

    @Test
    public void authApi6_additionalAttributesSeparatedWithWrongCharacterShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier&CurrentAddress&Gender");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqFormFail(formParams);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Found one or more invalid Attributes value(s). Valid values are:"));
    }

    @Ignore //TODO: Need a way to force the attributes without URL encoding
    @Test
    public void authApi6_additionalAttributesSeparatedWithoutUrlEncodingShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier CurrentAddress Gender");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = given()
                .filter(new AllureRestAssured())
                .queryParams(formParams)
                .config(config())
//                .contentType("application/x-www-form-urlencoded") //TODO: what is default behavior in restAssured? Needs to be rechecked when additional Attributes are implemented
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .log().all()
                .when()
                .get(testEidasClientProperties.getSpStartUrl())
                .then()
                .log().ifError()
                .extract().response();

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid AdditionalParameters! Unrecognized attibute(s) provided:"));
    }

    @Test
    public void authApi7_checkHtmlValues() {
        String response = getAuthenticationReq(DEF_COUNTRY, "SUBSTANTIAL", "RelayState", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC);

        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, response);

        assertEquals("Target url is present", testEidasClientProperties.getFullIdpStartUrl(), html.getString("html.body.form.@action"));
        assertEquals("Method is post", "post", html.getString("html.body.form.@method"));
        assertEquals("Relay state is present", "RelayState", html.getString("**.findAll { it.@name == 'RelayState' }.@value"));
        assertEquals("Country is present", DEF_COUNTRY, html.getString("**.findAll { it.@name == 'country' }.@value"));
        assertEquals("SAML request is present because LoA is accessible", LOA_SUBSTANTIAL, getDecodedSamlRequestBodyXml(response).getString(XML_LOA));
    }

    @Test
    public void authApi8_multipleParametersAreNotBlocking() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(LOA, "LOW");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReqForm(formParams).getBody().asString());

        assertEquals("Correct LOA is returned", LOA_LOW, samlRequest.getString(XML_LOA));
        }

    @Test
    public void authApi8_invalidParametersAreNotBlocking() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("randomParam", "random");
        formParams.put(LOA, "LOW");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status is returned with correct relayState","1234abcd", response.getBody().xmlPath(XmlPath.CompatibilityMode.HTML).getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

}
