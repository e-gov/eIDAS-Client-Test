package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
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

    @Ignore //TODO: we are getting internal server error
    @Test
    public void saml1_notEncryptedAssertionMustFail() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributesWithoutEncryption(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
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

    @Ignore //TODO: There is no minimal attributes check!
    @Test
    public void saml3_faultyAuthenticationResponse() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), null,"TestFamily", "TestPNO", "TestDate", null);
        JsonPath loginResponse = sendSamlResponse("",base64Response);
        assertEquals("400", loginResponse.getString("status"));
        assertEquals("Missing required attribute, error should be returned","Error of some sort", loginResponse.getString(STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml8_userAuthFailsErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "AuthFailed");
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(401, loginResponse.getStatusCode());
        assertEquals("User did not give consent, error should be returned", "Unauthorized", getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("User did not give consent, error should be returned", "Authentication failed.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml8_userConsentNotGiverErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "ConsentNotGiven");
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(401, loginResponse.getStatusCode());
        assertEquals("User did not give consent, error should be returned", "Unauthorized", getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("User did not give consent, error should be returned", "No user consent received. User denied access.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml8_someOtherErrorStatusErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "SomethingFailed");
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(500, loginResponse.getStatusCode());
        assertEquals("Some other reason for failure in authentication, Internal Server Error should be returned", "Internal Server Error", getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Some other reason for failure in authentication, Internal Server Error should be returned", "Something went wrong internally. Please consult server logs for further details.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml9_samlResponseIssueInstantFarInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), -20, 0, 0, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Error handling message: Message was rejected due to issue instant expiration", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml9_samlResponseIssueInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 20, 0, 0, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Error handling message: Message was rejected because it was issued in the future", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml10_replayAttackShouldReturnError() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributes(getAuthenticationReqWithDefault());
        sendSamlResponse("", base64Response );
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "No corresponding SAML request session found for the given response assertion!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Ignore
    @Test
    public void saml10_wrongInResponseToInResponse() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        String base64Response = getBase64SamlResponseInResponseTo("SomeWrongID", xmlPath.getString("AuthnRequest.@ID"));
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "No corresponding SAML request session found for the given response assertion!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml10_wrongInResponseToInSubject() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        String base64Response = getBase64SamlResponseInResponseTo(xmlPath.getString("AuthnRequest.@ID"), "SomeWrongIDForSubject");
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "No corresponding SAML request session found for the given response assertion!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
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

    @Ignore //TODO: There might be issue with issueInstant handling
    @Test
    public void saml13_samlAssertionIssueInstantInFarPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, -20, 0, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Message about incorrect issue time should be returned", "Assertion issue instant is too old or in the future!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml13_samlAssertionIssueInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 20, 0, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Message about incorrect issue time should be returned", "Assertion issue instant is expired!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml15_wrongIssuerFormat() {
        String base64Response = getBase64SamlResponseIssuer(getAuthenticationReqWithDefault(), testEidasClientProperties.getFullIdpMetadataUrl(), "urn:oasis:names:tc:SAML:2.0:format:entity");
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error must be returned on wrong format", "Assertion issuer's format must equal to: urn:oasis:names:tc:SAML:2.0:nameid-format:entity!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml15_wrongIssuerUrl() {
        String base64Response = getBase64SamlResponseIssuer(getAuthenticationReqWithDefault(), "http://192.32.221.22/metadata", ISSUER_FORMAT);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Issuer url must match with correct url", "Assertion issuer's value is not equal to the configured IDP metadata url!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml16_noNameIdErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 0, NAME_ID_FORMAT_UNSPECIFIED);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("NameID must be present", "Assertion subject is missing nameID!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently nameID presence is not checked
    @Test //TODO: two nameId-s is currently not supported by base class
    public void saml16_twoNameIdErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 2, NAME_ID_FORMAT_UNSPECIFIED);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Generic error should be returned", "Assertion subject has...", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml16_notSupportedNameIdMethodErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 1, NAME_ID_FORMAT_ENCRYPTED);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("NameID must have predefined acceptable value", "Assertion's subject name ID format is not equal to one of the following: [urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified, urn:oasis:names:tc:SAML:2.0:nameid-format:transient, urn:oasis:names:tc:SAML:2.0:nameid-format:persistent]", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml16_nameIdMethodUnspecifiedSuccessShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 1, NAME_ID_FORMAT_UNSPECIFIED);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
    }

    @Test
    public void saml16_nameIdMethodTransientSuccessShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 1, NAME_ID_FORMAT_TRANSIENT);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
    }

    @Test
    public void saml16_nameIdMethodPersistentSuccessShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 1, NAME_ID_FORMAT_PERSISTENT);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml17_noSubjectConfirmationErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseSubjectConfirmationCnt(getAuthenticationReqWithDefault(), 0, SUBJECT_CONFIRMATION_METHOD_BEARER);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Only one subject confirmation value is accepted", "", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml17_twoSubjectConfirmationsErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseSubjectConfirmationCnt(getAuthenticationReqWithDefault(), 2, SUBJECT_CONFIRMATION_METHOD_BEARER);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Only one subject confirmation value is accepted", "Assertion subject must contain exactly 1 SubjectConfirmation!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));

    }

    @Test
    public void saml17_notSupportedSubjectConfirmationMethodErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseSubjectConfirmationCnt(getAuthenticationReqWithDefault(), 1, SUBJECT_CONFIRMATION_METHOD_SENDER_VOUCHES);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("SubjectConfirmation must have predefined acceptable value", "Assertion SubjectConfirmation must equal to: urn:oasis:names:tc:SAML:2.0:cm:bearer!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml18_samlSubjectNotOnOrAfterInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, -20, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Current time must not be after SubjectConfirmationTime", "SubjectConfirmationData NotOnOrAfter is not valid!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml19_wrongRecipientUrlInSubjectConfirmationDataErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseRecipient(getAuthenticationReqWithDefault(), "randomRecipientUrl");
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Recipient url must be correct return url", "Error handling message: SAML message failed received endpoint check", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Wrong message is returned
    @Test
    public void saml21_samlConditionsNotOnOrAfterInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, 0, -20, 0);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Over time message should return error", "SubjectConfirmationData NotOnOrAfter is not valid!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml21_samlConditionsNotBeforeInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, 0, 20, 0);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Time in the future should return error", "Assertion condition NotBefore is not valid!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml22_noAudienceInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseAudienceCnt(getAuthenticationReqWithDefault(), 0, testEidasClientProperties.getFullSpMetadataUrl());
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("AudienceRestriction must be present", "Assertion condition's AudienceRestriction must contain at least 1 Audience!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml22_multipleAudienceInResponseShouldPass() {
        String base64Response = getBase64SamlResponseAudienceCnt(getAuthenticationReqWithDefault(), 2, testEidasClientProperties.getFullSpMetadataUrl());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
    }

    @Test
    public void saml22_wrongAudienceUrlInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseAudienceCnt(getAuthenticationReqWithDefault(), 1, "someRandomUrl");
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Audience must match with configured url", "Audience does not match with configured SP entity ID!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml23_noAuthnStatementOnSuccessShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnStatement(getAuthenticationReqWithDefault(), 0);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error should be returned as only one AuthnStatement is allowed", "Assertion must contain exactly 1 AuthnStatement!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml23_multipleAuthnStatementOnSuccessShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnStatement(getAuthenticationReqWithDefault(), 2);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error should be returned as only one AuthnStatement is allowed", "Assertion must contain exactly 1 AuthnStatement!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml24_samlAuthnInstantInFarPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, 0, 0, -20);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error should be returned as time is past", "AuthnInstant is expired!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml24_samlAuthnInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, 0, 0, 20);
        Response loginResponse = sendSamlResponseExtractResponse("",base64Response );
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error should be returned as time is future", "AuthnInstant is in the future!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: We do not receive a proper JSON error response yet
    @Test
    public void saml26_noLegalAttributesAreAskedButAreReturned() {
        String base64Response = getBase64SamlResponseLegalMinimalAttributes(getAuthenticationReqWithDefault(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, DEFATTR_LEGAL_NAME, DEFATTR_LEGAL_PNO);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("Expected statusCode: Success", STATUS_SUCCESS, loginResponse.getString(STATUS_CODE));
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
        assertEquals("Correct legal name is returned", DEFATTR_LEGAL_NAME, loginResponse.getString(STATUS_LEGAL_NAME));
        assertEquals("Correct legal pno is returned", DEFATTR_LEGAL_PNO, loginResponse.getString(STATUS_LEGAL_PNO));
    }
}
