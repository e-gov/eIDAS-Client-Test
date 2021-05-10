package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static ee.ria.eidas.config.EidasTestStrings.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = SamlResponseCorrectnessIntegrationTest.class)
@Category(IntegrationTest.class)
public class SamlResponseCorrectnessIntegrationTest extends TestsBase {

    @Test
    public void saml1_notEncryptedAssertionMustFail() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributesWithoutEncryption(getAuthenticationReqWithDefault());
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. Error handling message: Message is not schema-valid.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
     }

    @Test
    public void saml1_notEncryptedAndEncryptedAssertionsMixedMustFail() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributesWithMixedEncryption(getAuthenticationReqWithDefault());
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. Error handling message: Message is not schema-valid.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently internal server error is returned. Needs analysing.
    @Test
    public void saml1_multipleAssertionsInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithMultipleAssertions(getAuthenticationReqWithDefault());
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(500, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. Error handling message: Message is not schema-valid.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml2_missingStatusInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithoutStatus(getAuthenticationReqWithDefault());
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("The response is not schema valid", "Invalid SAMLResponse. Error handling message: Message is not schema-valid.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml2_missingStatusCodeInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithStatusCodes(getAuthenticationReqWithDefault(), 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("The response is not schema valid", "Invalid SAMLResponse. Error handling message: Message is not schema-valid.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Ignore //TODO: Currently internal server error is returned. Needs analysing.
    @Test
    public void saml2_multipleStatusCodesInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithStatusCodes(getAuthenticationReqWithDefault(), 2);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(500, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("The response is not schema valid", "Invalid SAMLResponse. Error handling message: Message is not schema-valid.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml2_notSupportedLoaInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithNotExistingLoa(getAuthenticationReqWithDefault(), "http://eidas.europa.eu/LoA/extreme");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Loa values are defined and other values should not be accepted", "Invalid SAMLResponse. AuthnContextClassRef is not greater or equal to the request level of assurance!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml2_emptyLoaInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithNotExistingLoa(getAuthenticationReqWithDefault(), "");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Loa can not be empty", "Invalid SAMLResponse. AuthnContextClassRef is not greater or equal to the request level of assurance!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml3_faultyAuthenticationResponse() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), null,"SomeName", "TestPNO", "TestDate", null);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid SAMLResponse. Missing mandatory attributes in the response assertion:"));
    }

    @Test
    public void saml8_userAuthFailsErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "AuthFailed");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(401, loginResponse.getStatusCode());
        assertEquals("User did not authenticate, error should be returned", "Unauthorized", getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("User did not authenticate, error should be returned", "Authentication failed.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml8_userConsentNotGiverErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "ConsentNotGiven");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(401, loginResponse.getStatusCode());
        assertEquals("User did not give consent, error should be returned", "Unauthorized", getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("User did not give consent, error should be returned", "No user consent received. User denied access.", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml8_someOtherErrorStatusErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "SomethingFailed");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(401, loginResponse.getStatusCode());
        assertEquals("User did not authenticate, error should be returned", "Unauthorized", getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("User did not authenticate, error should be returned", "Not existing keyword for error:SomethingFailed", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml9_samlResponseIssueInstantFarInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), -20, 0, 0, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. Error handling message: Message was rejected due to issue instant expiration", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml9_samlResponseIssueInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 20, 0, 0, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. Error handling message: Message was rejected because it was issued in the future", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml10_replayAttackShouldReturnError() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributes(getAuthenticationReqWithDefault());
        sendSamlResponse("", base64Response );
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. No corresponding SAML request session found for the given response!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml10_wrongInResponseToInResponse() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        String base64Response = getBase64SamlResponseInResponseTo("SomeWrongID", xmlPath.getString("AuthnRequest.@ID"));
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. No corresponding SAML request session found for the given response!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml10_wrongInResponseToInSubject() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        String base64Response = getBase64SamlResponseInResponseTo(xmlPath.getString("AuthnRequest.@ID"), "SomeWrongIDForSubject");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. No corresponding SAML request session found for the given response assertion!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml11_noAttributeStatementInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseWithAttributeCnt(0, getAuthenticationReqWithDefault());
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. Assertion must contain exactly 1 AttributeStatement!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml11_twoAttributeStatementInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseWithAttributeCnt(2, getAuthenticationReqWithDefault());
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. Assertion must contain exactly 1 AttributeStatement!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml11_noSubjectInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseWithoutSubject(getAuthenticationReqWithDefault());
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Correct error message is returned", "Invalid SAMLResponse. Assertion is missing subject!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml11_noAuthnContextInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnContextCnt(getAuthenticationReqWithDefault(), 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Message about AuthnStatement should be returned", "Invalid SAMLResponse. Assertion must contain exactly 1 AuthnStatement!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml11_twoAuthnContextInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnContextCnt(getAuthenticationReqWithDefault(), 2);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Message about AuthnStatement should be returned", "Invalid SAMLResponse. Assertion must contain exactly 1 AuthnStatement!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml13_samlAssertionIssueInstantInFarPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, -20, 0, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Message about incorrect issue time should be returned", "Invalid SAMLResponse. Assertion issue instant is expired!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml13_samlAssertionIssueInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 20, 0, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Message about incorrect issue time should be returned", "Invalid SAMLResponse. Assertion issue instant is in the future!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml15_wrongIssuerFormat() {
        String base64Response = getBase64SamlResponseIssuer(getAuthenticationReqWithDefault(), testEidasClientProperties.getFullIdpMetadataUrl(), "urn:oasis:names:tc:SAML:2.0:format:entity");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error must be returned on wrong format", "Invalid SAMLResponse. Assertion issuer's format must equal to: urn:oasis:names:tc:SAML:2.0:nameid-format:entity!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml15_wrongIssuerUrl() {
        String base64Response = getBase64SamlResponseIssuer(getAuthenticationReqWithDefault(), "http://192.32.221.22/metadata", ISSUER_FORMAT);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Issuer url must match with correct url", "Invalid SAMLResponse. Assertion issuer's value is not equal to the configured IDP metadata url!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml16_noNameIdErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 0, NAME_ID_FORMAT_UNSPECIFIED);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("NameID must be present", "Invalid SAMLResponse. Assertion subject is missing nameID!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml16_notSupportedNameIdMethodErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 1, NAME_ID_FORMAT_ENCRYPTED);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("NameID must have predefined acceptable value", "Invalid SAMLResponse. Assertion's subject name ID format is not equal to one of the following: [urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified, urn:oasis:names:tc:SAML:2.0:nameid-format:transient, urn:oasis:names:tc:SAML:2.0:nameid-format:persistent]", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml16_nameIdMethodUnspecifiedSuccessShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 1, NAME_ID_FORMAT_UNSPECIFIED);
        JsonPath loginResponse = sendSamlResponse("", base64Response);
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
    }

    @Test
    public void saml16_nameIdMethodTransientSuccessShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 1, NAME_ID_FORMAT_TRANSIENT);
        JsonPath loginResponse = sendSamlResponse("", base64Response);
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
    }

    @Test
    public void saml16_nameIdMethodPersistentSuccessShouldBeReturned() {
        String base64Response = getBase64SamlResponseNameIdCnt(getAuthenticationReqWithDefault(), 1, NAME_ID_FORMAT_PERSISTENT);
        JsonPath loginResponse = sendSamlResponse("", base64Response);
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
    }

    @Test
    public void saml17_noSubjectConfirmationErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseSubjectConfirmationCnt(getAuthenticationReqWithDefault(), 0, SUBJECT_CONFIRMATION_METHOD_BEARER);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Only one subject confirmation value is accepted", "Invalid SAMLResponse. Assertion subject must contain exactly 1 SubjectConfirmation!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml17_twoSubjectConfirmationsErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseSubjectConfirmationCnt(getAuthenticationReqWithDefault(), 2, SUBJECT_CONFIRMATION_METHOD_BEARER);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Only one subject confirmation value is accepted", "Invalid SAMLResponse. Assertion subject must contain exactly 1 SubjectConfirmation!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml17_notSupportedSubjectConfirmationMethodErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseSubjectConfirmationCnt(getAuthenticationReqWithDefault(), 1, SUBJECT_CONFIRMATION_METHOD_SENDER_VOUCHES);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("SubjectConfirmation must have predefined acceptable value", "Invalid SAMLResponse. Assertion SubjectConfirmation must equal to: urn:oasis:names:tc:SAML:2.0:cm:bearer!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml18_samlSubjectNotOnOrAfterInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, -20, 0, 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Current time must not be after SubjectConfirmationTime", "Invalid SAMLResponse. SubjectConfirmationData NotOnOrAfter is not valid!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml19_wrongRecipientUrlInSubjectConfirmationDataErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseRecipient(getAuthenticationReqWithDefault(), "randomRecipientUrl");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Recipient url must be correct return url", "Invalid SAMLResponse. Error handling message: SAML message failed received endpoint check", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml21_samlConditionsNotOnOrAfterInPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, 0, -20, 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Over time message should return error", "Invalid SAMLResponse. Assertion condition NotOnOrAfter is not valid!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml21_samlConditionsNotBeforeInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, 0, 20, 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Time in the future should return error", "Invalid SAMLResponse. Assertion condition NotBefore is not valid!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml22_noAudienceInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseAudienceCnt(getAuthenticationReqWithDefault(), 0, testEidasClientProperties.getFullSpMetadataUrl());
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("AudienceRestriction must be present", "Invalid SAMLResponse. Assertion condition's AudienceRestriction must contain at least 1 Audience!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml22_multipleAudienceInResponseShouldPass() {
        String base64Response = getBase64SamlResponseAudienceCnt(getAuthenticationReqWithDefault(), 2, testEidasClientProperties.getFullSpMetadataUrl());
        JsonPath loginResponse = sendSamlResponse("", base64Response);
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
    }

    @Test
    public void saml22_wrongAudienceUrlInResponseShouldReturnError() {
        String base64Response = getBase64SamlResponseAudienceCnt(getAuthenticationReqWithDefault(), 1, "someRandomUrl");
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Audience must match with configured url", "Invalid SAMLResponse. Audience does not match with configured SP entity ID!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml23_noAuthnStatementOnSuccessShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnStatement(getAuthenticationReqWithDefault(), 0);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error should be returned as only one AuthnStatement is allowed", "Invalid SAMLResponse. Assertion must contain exactly 1 AuthnStatement!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml23_multipleAuthnStatementOnSuccessShouldReturnError() {
        String base64Response = getBase64SamlResponseAuthnStatement(getAuthenticationReqWithDefault(), 2);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error should be returned as only one AuthnStatement is allowed", "Invalid SAMLResponse. Assertion must contain exactly 1 AuthnStatement!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml24_samlAuthnInstantInFarPastErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, 0, 0, -20);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error should be returned as time is past", "Invalid SAMLResponse. AuthnInstant is expired!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml24_samlAuthnInstantInFutureErrorShouldBeReturned() {
        String base64Response = getBase64SamlResponseTimeManipulation(getAuthenticationReqWithDefault(), 0, 0, 0, 0, 20);
        Response loginResponse = sendSamlResponseExtractResponse("", base64Response);
        assertEquals(400, loginResponse.getStatusCode());
        assertEquals("Generic error should be returned", BAD_REQUEST, getValueFromJsonResponse(loginResponse, STATUS_ERROR));
        assertEquals("Error should be returned as time is future", "Invalid SAMLResponse. AuthnInstant is in the future!", getValueFromJsonResponse(loginResponse, STATUS_ERROR_MESSAGE));
    }

    @Test
    public void saml26_mandatoryLegalAttributesAreAskedAndReturned() {

        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier LegalName");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");

        String base64Response = getBase64SamlResponseLegalMinimalAttributes(getAuthenticationReqForm(formParams).getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, DEFATTR_LEGAL_NAME, DEFATTR_LEGAL_PNO);
        JsonPath loginResponse = sendSamlResponse("", base64Response);
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
        assertEquals("Correct legal name is returned", DEFATTR_LEGAL_NAME, loginResponse.getString(STATUS_LEGAL_NAME));
        assertEquals("Correct legal pno is returned", DEFATTR_LEGAL_PNO, loginResponse.getString(STATUS_LEGAL_PNO));
    }

    @Test
    public void saml26_mandatoryLegalAttributesAreAskedButNotReturned() {

        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier LegalName");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");

        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqForm(formParams).getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_HIGH);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid SAMLResponse. Missing mandatory attributes in the response assertion: [LegalPersonIdentifier, LegalName]"));
    }

    @Test
    public void saml26_mandatoryLegalNameIsAskedButNotReturned() {

        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalName");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");

        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqForm(formParams).getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_HIGH);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid SAMLResponse. Missing mandatory attributes in the response assertion: [LegalName]"));
    }

    @Test
    public void saml26_mandatoryLegalPersonIdentifierIsAskedButNotReturned() {

        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");

        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqForm(formParams).getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, LOA_HIGH);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid SAMLResponse. Missing mandatory attributes in the response assertion: [LegalPersonIdentifier]"));
    }

    @Test
    public void saml26_mandatoryLegalAttributesAreAskedButOnlyLegalPersonIdentifierReturned() {

        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier LegalName");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");

        String base64Response = getBase64SamlResponseLegalMinimalAttributes(getAuthenticationReqForm(formParams).getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, null, DEFATTR_LEGAL_PNO);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid SAMLResponse. Missing mandatory attributes in the response assertion: [LegalName]"));
    }

    @Test
    public void saml26_mandatoryLegalAttributesAreAskedButOnlyLegalNameReturned() {

        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier LegalName");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");

        String base64Response = getBase64SamlResponseLegalMinimalAttributes(getAuthenticationReqForm(formParams).getBody().asString(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, DEFATTR_LEGAL_NAME, null);
        Response response = sendSamlResponseGetStatus("", base64Response );
        assertEquals("Status code should be: 400", 400, response.statusCode());
        assertEquals("Bad request error should be returned", BAD_REQUEST, getValueFromJsonResponse(response, STATUS_ERROR));
        assertThat("Correct error message", getValueFromJsonResponse(response, STATUS_ERROR_MESSAGE), startsWith("Invalid SAMLResponse. Missing mandatory attributes in the response assertion: [LegalPersonIdentifier]"));
    }

    @Test
    public void saml26_allLegalAttributesAreAskedAndReturned() {

        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier LegalName LegalAddress VATRegistration TaxReference LEI EORI SEED SIC D-2012-17-EUIdentifier");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");

        String req = getAuthenticationReqForm(formParams).getBody().asString();

        String base64Response = getBase64SamlResponseLegalMaximalAttributes(req);
        JsonPath loginResponse = sendSamlResponse("", base64Response);
        assertEquals("Correct loa is returned", LOA_HIGH, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
        assertEquals("Correct legal name is returned", DEFATTR_LEGAL_NAME, loginResponse.getString(STATUS_LEGAL_NAME));
        assertEquals("Correct legal pno is returned", DEFATTR_LEGAL_PNO, loginResponse.getString(STATUS_LEGAL_PNO));
        assertEquals("Correct legal address is returned", DEFATTR_LEGAL_ADDRESS, loginResponse.getString(STATUS_LEGAL_ADDRESS));
        assertEquals("Correct legal vat is returned", DEFATTR_LEGAL_VATREGISTRATION, loginResponse.getString(STATUS_LEGAL_VAT));
        assertEquals("Correct legal tax is returned", DEFATTR_LEGAL_TAXREFERENCE, loginResponse.getString(STATUS_LEGAL_TAX));
        assertEquals("Correct legal lei is returned", DEFATTR_LEGAL_LEI, loginResponse.getString(STATUS_LEGAL_LEI));
        assertEquals("Correct legal eori is returned", DEFATTR_LEGAL_EORI, loginResponse.getString(STATUS_LEGAL_EORI));
        assertEquals("Correct legal seed is returned", DEFATTR_LEGAL_SEED, loginResponse.getString(STATUS_LEGAL_SEED));
        assertEquals("Correct legal sic is returned", DEFATTR_LEGAL_SIC, loginResponse.getString(STATUS_LEGAL_SIC));
        assertEquals("Correct legal pno D-2012-EUIdendtifier returned", DEFATTR_LEGAL_D201217EUIDENTIFIER, loginResponse.getString(STATUS_LEGAL_D2012));
        //Custom attribute check
        assertEquals("Correct legal pno D-2012-EUIdendtifier returned", DEFATTR_LEGAL_BUSINESSCODES, loginResponse.getString("attributes.BusinessCodes"));
    }

    @Test
    public void saml26_noLegalAttributesAreAskedButAreReturned() {
        String base64Response = getBase64SamlResponseLegalMinimalAttributes(getAuthenticationReqWithDefault(), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, DEFATTR_LEGAL_NAME, DEFATTR_LEGAL_PNO);
        JsonPath loginResponse = sendSamlResponse("", base64Response);
        assertEquals("Correct loa is returned", LOA_SUBSTANTIAL, loginResponse.getString(STATUS_LOA));
        assertEquals("Correct person idendifier is returned", DEFATTR_PNO, loginResponse.getString(STATUS_PNO));
        assertEquals("Correct date is returned", DEFATTR_DATE, loginResponse.getString(STATUS_DATE));
        assertEquals("Correct family name is returned", DEFATTR_FAMILY, loginResponse.getString(STATUS_FAMILY));
        assertEquals("Correct first name is returned", DEFATTR_FIRST, loginResponse.getString(STATUS_FIRST));
        assertEquals("Correct legal name is returned", DEFATTR_LEGAL_NAME, loginResponse.getString(STATUS_LEGAL_NAME));
        assertEquals("Correct legal pno is returned", DEFATTR_LEGAL_PNO, loginResponse.getString(STATUS_LEGAL_PNO));
    }
}
