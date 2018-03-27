package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = EidasClientAuthApiIntegrationTest.class)
@Category(IntegrationTest.class)
public class EidasClientAuthApiIntegrationTest extends TestsBase {

    @Test
    public void authApi2_countryCodeNotInCorrectFormatShouldReturnError() {
        Response response =  getAuthenticationReqResponse("Est", "", "");

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid country! Valid countries:"));
    }

    @Test
    public void authApi2_countryCodeCaseSensitiveShouldReturnError() {
        Response response =  getAuthenticationReqResponse("ee", "", "");

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid country! Valid countries:"));
    }

    @Test
    public void authApi2_notSupportedCountryCodeShouldReturnError() {
        Response response =  getAuthenticationReqResponse("SZ", "", "");

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid country! Valid countries:"));
    }

    @Ignore //TODO: Internal Server Error is returned
    @Test
    public void authApi2_countryParameterMissingShouldReturnError() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("relayState", "1234abcd");

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertEquals("Bad request error should be returned", "Required String parameter 'country' is not present", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Internal Server Error is returned
    @Test
    public void authApi3_invalidLoaLevelsAreNotAccepted() {
        Response response =  getAuthenticationReqResponse("EE", "SUPER", "");

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid LoA! One of"));
    }

    @Test
    public void authApi3_loaMissingValueShouldReturnDefault() {
        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq("CA", "", ""));

        assertEquals("Correct LOA is returned", LOA_SUBSTANTIAL, samlRequest.getString(XML_LOA));
    }

    @Test
    public void authApi3_loaParameterMissingShouldReturnDefault() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("country", "EE");
        formParams.put("relayState", "1234abcd");

        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReqForm(formParams).getBody().asString());

        assertEquals("Correct LOA is returned",LOA_SUBSTANTIAL, samlRequest.getString(XML_LOA));
    }

    @Test
    public void authApi4_errorIsReturnedOnTooLongRelayState() {
        String relayState = RandomStringUtils.randomAlphanumeric(81);

        Response response = getAuthenticationReqResponse("EE", "", relayState);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid RelayState! Must match the following regexp:"));
    }

    @Test
    public void authApi4_errorIsReturnedOnWrongCharactersInRelayState() {
        String relayState = "<>$";

        Response response = getAuthenticationReqResponse("EE", "", relayState);

        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid RelayState! Must match the following regexp:"));
    }

    @Test
    public void authApi4_relayStateValueShouldBeReturned() {
        String relayState = RandomStringUtils.randomAlphanumeric(80);

        Response response = getAuthenticationReqResponse("CA", "", relayState);

        assertEquals("Status code should be: 200", 200, response.statusCode());
        assertEquals("Correct RelayState is returned", relayState, response.getBody().xmlPath(XmlPath.CompatibilityMode.HTML).getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

    @Test
    public void authApi4_relayStateMissingValueShouldReturnOkStatus() {
        Response response = getAuthenticationReqResponse("CA", "", "");

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi4_relayStateMissingShouldReturnOkStatus() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("country", "EE");

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status code should be: 200", 200, response.statusCode());
    }

    @Test
    public void authApi7_invalidParametersAreNotBlocking() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put("randomParam", "random");
        formParams.put("loa", "LOW");
        formParams.put("country", "EE");
        formParams.put("relayState", "1234abcd");

        Response response = getAuthenticationReqForm(formParams);

        assertEquals("Status is returned with correct relayState","1234abcd", response.getBody().xmlPath(XmlPath.CompatibilityMode.HTML).getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

}
