package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
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

@SpringBootTest(classes = SamlResponseCorrectnessIntegrationTest.class)
@Category(IntegrationTest.class)
public class SamlResponseCorrectnessIntegrationTest extends TestsBase {

    @Test
    public void resp7_unsignedAssertionMustFailOnPostBinding() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributesWithoutAssertionSignature(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("Assertion must be signed on POST binding, error should be returned","The SAML Assertion was not signed", loginResponse.getString("message"));
    }

    @Test
    public void resp4_notEncryptedAssertionMustFail() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributesWithoutEncryption(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("Assertion must be encrypted, error should be returned","Saml Response does not contain any encrypted assertions", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently it is only checked that encrypted assertion is present.
    @Test
    public void saml1_notEncryptedAndEncryptedAssertionsMixedMustFail() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributesWithMixedEncryption(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("Assertion must be encrypted, error should be returned","Saml Response does not contain any encrypted assertions", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently it is only checked that encrypted assertion is present.
    @Test
    public void saml1_multipleAssertionsInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithMultipleAssertions(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","Saml Response does not contain any encrypted assertions", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently NPE is returned
    @Test
    public void saml2_missingStatusInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithoutStatus(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently NPE is returned
    @Test
    public void saml2_missingStatusCodeInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithStatusCodes(getAuthenticationReqWithDefault(), 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently this passes, it is unclear what it should do...
    @Test
    public void saml2_multipleStatusCodesInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithStatusCodes(getAuthenticationReqWithDefault(), 2);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently nameID presence is not checked
    @Test
    public void saml2_missingNameIdInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithoutNameId(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently the loa format is not checked
    @Test
    public void saml2_notSupportedLoaInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithNotExistingLoa(getAuthenticationReqWithDefault(), "http://eidas.europa.eu/LoA/extreme");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently the loa format is not checked
    @Test
    public void saml2_emptyLoaInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithNotExistingLoa(getAuthenticationReqWithDefault(), "");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently the format name is not checked
    @Test
    public void saml2_wrongNameFormatInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithWrongNameFormat(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("", base64Response);
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: There is no minimal attributes check!
    @Test
    public void saml3_faultyAuthenticationResponse() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), null,"TestFamily", "TestPNO", "TestDate", null);
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Missing required attribute, error should be returned","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml8_userAuthFailsErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "AuthFailed");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("401", loginResponse.getString("status"));
        assertEquals("User did not authenticate, error should be returned","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml8_userConsentNotGiverErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "ConsentNotGiven");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("401", loginResponse.getString("status"));
        assertEquals("User did not give consent, error should be returned","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml8_someOtherErrorStatusErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "SomethingFailed");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("Generic error should be returned","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml9_samlResponseIssueInstantFarInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), -20, 0,0, 0, 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml9_samlResponseIssueInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 20, 0,0, 0, 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no replay check!
    @Test
    public void saml10_replayAttackShouldReturnError() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributes(getAuthenticationReqWithDefault());
        sendSamlResponse("", base64Response );
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Repeated SAML response should not be accepted","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no ID check!
    @Test
    public void saml10_wrongInResponseToInResponse() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        String base64Response = getBase64SamlResponseInResponseTo("SomeWrongID", xmlPath.getString("AuthnRequest.@ID"));
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("SAML response with unregistered ID should not be accepted","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no ID check!
    @Test
    public void saml10_wrongInResponseToInSubject() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        String base64Response = getBase64SamlResponseInResponseTo(xmlPath.getString("AuthnRequest.@ID"), "SomeWrongIDForSubject");
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("SAML response with unregistered ID should not be accepted","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no check!
    @Test
    public void saml11_noAttributeStatementInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseWithAttributeCnt(0, getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no check!
    @Test
    public void saml11_twoAttributeStatementInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseWithAttributeCnt(2, getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no check!
    @Test
    public void saml11_noSubjectInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseWithoutSubject(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no check!
    @Test
    public void saml11_noAuthnContextInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnContextCnt(getAuthenticationReqWithDefault(), 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no check!
    @Test
    public void saml11_twoAuthnContextInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnContextCnt(getAuthenticationReqWithDefault(), 2);
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml13_samlAssertionIssueInstantInFarPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, -20,0, 0, 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml13_samlAssertionIssueInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 20,0, 0, 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no issuer check!
    @Test
    public void saml15_wrongIssuerFormat() {
        String base64Response = getBase64SamlResponseIssuer(getAuthenticationReqWithDefault(), idpUrl+idpMetadataUrl, "urn:oasis:names:tc:SAML:2.0:format:entity");
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("SAML response with wrong issuer name format should not be accepted","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no issuer check!
    @Test
    public void saml15_wrongIssuerUrl() {
        String base64Response = getBase64SamlResponseIssuer(getAuthenticationReqWithDefault(), "http://192.32.221.22/metadata", ISSUER_FORMAT);
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("SAML response with wrong metadata url should not be accepted","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml18_samlSubjectNotOnOrAfterInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0,-20, 0, 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml21_samlConditionsNotOnOrAfterInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0,0, -20, 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml21_samlConditionsNotBeforeInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0,0, 20, 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no check!
    @Test
    public void saml23_noAuthnStatementOnSuccessShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnStatement(getAuthenticationReqWithDefault(), 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("SAML response without AuthnStatement should not be accepted","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently there is no check!
    @Test
    public void saml23_multipleAuthnStatementOnSuccessShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnStatement(getAuthenticationReqWithDefault(), 2);
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("SAML response with multiple AuthnStatement should not be accepted","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml24_samlAuthnInstantInFarPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0,0, 0, -20);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml24_samlAuthnInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0,0, 0, 20);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Generic error should be returned", BAD_SAML, loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

}
