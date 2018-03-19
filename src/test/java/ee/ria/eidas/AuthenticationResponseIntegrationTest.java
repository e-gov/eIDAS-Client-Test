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

    @Test
    public void resp1_validMinimalInputAuthentication() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributes(getAuthenticationReqWithDefault());
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponseJson.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponseJson.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponseJson.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponseJson.getString(STATUS_FIRST));
    }

    @Test //TODO: There is confusion in spec about birth family and first name.
    public void resp1_validMaximalInputAuthentication() {
        String base64Response = getBase64SamlResponseDefaultMaximalAttributes(getAuthenticationReqWithDefault());
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponseJson.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponseJson.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponseJson.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponseJson.getString(STATUS_FIRST));
//        assertEquals("Correct birth first name is returned", DEFATTR_BIRTH_FIRST, loginResponseJson.getString(STATUS_BIRTH_FIRST));
//        assertEquals("Correct birth family name is returned", DEFATTR_BIRTH_FAMILY, loginResponseJson.getString(STATUS_BIRTH_FAMILY));
        assertEquals("Correct birth place is returned", DEFATTR_BIRTH_PLACE, loginResponseJson.getString(STATUS_BIRTH_PLACE));
        assertEquals("Correct gender is returned", DEFATTR_GENDER, loginResponseJson.getString(STATUS_GENDER));
        assertEquals("Correct address is returned", DEFATTR_ADDR, loginResponseJson.getString(STATUS_ADDR));
    }

    @Ignore //TODO: Need a specification for error statuses
    @Test
    public void resp3_responseWithLowLoaShouldNotBeAcceptedWhenSubstantialWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Error message is expected", "", loginResponseJson.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Need a specification for error statuses
    @Test
    public void resp3_responseWithLowLoaShouldNotBeAcceptedWhenHighWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Error message is expected", "", loginResponseJson.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Need a specification for error statuses
    @Test
    public void resp3_responseWithSubstantialLoaShouldNotBeAcceptedWhenHighWasRequired() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Error message is expected", "", loginResponseJson.getString(STATUS_ERROR_MESSAGE));
    }

    @Test
    public void resp3_responseWithSameLoaShouldBeAcceptedLow() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_LOW);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_LOW, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp3_responseWithSameLoaShouldBeAcceptedSubstantial() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp3_responseWithSameLoaShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","HIGH",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp3_responseWithHigherLoaLowShouldBeAcceptedSubstantial() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_SUBSTANTIAL);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp3_responseWithHigherLoaLowShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","LOW",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    @Test
    public void resp3_responseWithHigherLoaSubstantialShouldBeAcceptedHigh() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReq("CA","SUBSTANTIAL",""), "TestGiven","TestFamily","TestPNO", "TestDate", LOA_HIGH);
        JsonPath loginResponseJson = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponseJson.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponseJson.getString(STATUS_LOA));
    }

    //TODO: We do not receive a proper JSON error response yet
    @Ignore
    @Test
    public void resp9_authenticationFails() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "ConsentNotGiven");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("User did not give consent, error should be returned","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

}
