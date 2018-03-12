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

@SpringBootTest(classes = SamlResponseCorrectnessIntegrationTest.class)
@Category(IntegrationTest.class)
public class SamlResponseCorrectnessIntegrationTest extends TestsBase {

    @Test
    public void saml1_unsignedAssertionMustFailOnPostBinding() {
        String base64Response = getBase64SamlResponseDefaultMinimalAttributesWithoutAssertionSignature(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("Assertion must be signed on POST binding, error should be returned","The SAML Assertion was not signed", loginResponse.getString("message"));
    }

    @Test
    public void saml1_notEncryptedAssertionMustFail() {
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
    public void saml1_missingStatusInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithoutStatus(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently NPE is returned
    @Test
    public void saml1_missingStatusCodeInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithStatusCodes(getAuthenticationReqWithDefault(), 0);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently this passes, it is unclear what it should do...
    @Test
    public void saml1_multipleStatusCodesInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithStatusCodes(getAuthenticationReqWithDefault(), 2);
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently nameID presence is not checked
    @Test
    public void saml1_missingNameIdInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithoutNameId(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently the loa format is not checked
    @Test
    public void saml1_notSupportedLoaInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithNotExistingLoa(getAuthenticationReqWithDefault(), "http://eidas.europa.eu/LoA/extreme");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently the loa format is not checked
    @Test
    public void saml1_emptyLoaInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithNotExistingLoa(getAuthenticationReqWithDefault(), "");
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

    @Ignore //TODO: Currently the format name is not checked
    @Test
    public void saml1_wrongNameFormatInResponseMustFail() {
        String base64Response = getBase64SamlResponseMinimalAttributesWithWrongNameFormat(getAuthenticationReqWithDefault());
        JsonPath loginResponse = sendSamlResponse("",base64Response );
        assertEquals("500", loginResponse.getString("status"));
        assertEquals("","", loginResponse.getString("message"));
    }

}
