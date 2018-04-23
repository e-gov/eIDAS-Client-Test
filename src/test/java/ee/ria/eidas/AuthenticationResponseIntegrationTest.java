package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import static ee.ria.eidas.config.EidasTestStrings.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = AuthenticationResponseIntegrationTest.class)
@Category(IntegrationTest.class)
public class AuthenticationResponseIntegrationTest extends TestsBase {

    @Test
    public void resp1_validMinimalInputAuthentication() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributes(getAuthenticationReqWithDefault());
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
        assertEquals("Correct person identifier is returned", DEFATTR_PNO, loginResponseJson.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponseJson.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponseJson.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponseJson.getString(STATUS_FIRST));
    }

    @Test
    public void resp1_validMaximalInputAuthentication() {
        String base64Response = getBase64SamlResponseDefaultMaximalAttributes(getAuthenticationReqWithDefault());
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
        assertEquals("Correct person identifier is returned", DEFATTR_PNO, loginResponseJson.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponseJson.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponseJson.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponseJson.getString(STATUS_FIRST));
        assertEquals("Correct birth name is returned", DEFATTR_BIRTH_NAME, loginResponseJson.getString(STATUS_BIRTH_NAME));
        assertEquals("Correct birth place is returned", DEFATTR_BIRTH_PLACE, loginResponseJson.getString(STATUS_BIRTH_PLACE));
        assertEquals("Correct gender is returned", DEFATTR_GENDER, loginResponseJson.getString(STATUS_GENDER));
        assertEquals("Correct address is returned", DEFATTR_ADDR, loginResponseJson.getString(STATUS_ADDR));
    }

    @Test
    public void resp4_responseWithLowLoaShouldNotBeAcceptedWhenSubstantialWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("AuthnContextClassRef is not greater or equal to the request level of assurance!"));
    }

    @Test
    public void resp4_responseWithLowLoaShouldNotBeAcceptedWhenHighWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("AuthnContextClassRef is not greater or equal to the request level of assurance!"));
    }

    @Test
    public void resp4_responseWithSubstantialLoaShouldNotBeAcceptedWhenHighWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("AuthnContextClassRef is not greater or equal to the request level of assurance!"));
    }

    @Test
    public void resp4_responseWithSameLoaShouldBeAcceptedLow() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_LOW, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp4_responseWithSameLoaShouldBeAcceptedSubstantial() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp4_responseWithSameLoaShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp4_responseWithHigherLoaLowShouldBeAcceptedSubstantial() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp4_responseWithHigherLoaLowShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp4_responseWithHigherLoaSubstantialShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp8_unsignedAssertionMustFailOnPostBinding() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributesWithoutAssertionSignature(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("Assertion must be signed on POST binding, error should be returned","The SAML Assertion was not signed", loginResponse.getString("message"));
    }
}
