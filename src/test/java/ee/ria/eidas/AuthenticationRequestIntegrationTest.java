package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.hamcrest.text.MatchesPattern;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ee.ria.eidas.config.EidasTestStrings.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = AuthenticationRequestIntegrationTest.class)
@Category(IntegrationTest.class)
public class AuthenticationRequestIntegrationTest extends TestsBase {

    @Test
    public  void auth1_hasValidSignature() {
        try {
            validateSamlReqSignature(getDecodedSamlRequestBody(getAuthenticationReqWithDefault()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Authentication request must have valid signature:  " + e.getMessage());
        }
    }

    @Test
    public void auth1_parametersArePresent() {
        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, getAuthenticationReq(DEF_COUNTRY, "LOW", "relayState", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC));
        assertEquals("Country code is present",DEF_COUNTRY, html.getString("**.findAll { it.@name == 'country' }.@value"));
        assertEquals("RelayState is present","relayState", html.getString("**.findAll { it.@name == 'RelayState' }.@value"));
    }

    @Test
    public void auth1_verifyUsedDigestAlgosInSignature() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());

        List<String> digestMethods = xmlPath.getList("EntityDescriptor.Signature.SignedInfo.Reference.DigestMethod.@Algorithm");
        assertThat("One of the accepted digest algorithms must be present", digestMethods,
                anyOf(hasItem("http://www.w3.org/2001/04/xmlenc#sha512"), hasItem("http://www.w3.org/2001/04/xmlenc#sha256")));
    }

    @Test
    public void auth1_verifyUsedSignatureAlgosInSignature() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());

        List<String> signingMethods = xmlPath.getList("EntityDescriptor.Signature.SignedInfo.SignatureMethod.@Algorithm");
        assertThat("One of the accepted signing algorithms must be present", signingMethods,
                anyOf(hasItem("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512"), hasItem("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256"),
                        hasItem("http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1"), hasItem("http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1")));
    }

    @Test
    public void auth2_defaultAttributesArePresentAndSetTrueForNaturalPersons() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        assertEquals("Family name must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName' }.@isRequired"));
        assertEquals("First name must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName' }.@isRequired"));
        assertEquals("Date of birth must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/DateOfBirth' }.@isRequired"));
        assertEquals("Person identifier must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier' }.@isRequired"));
    }
    @Test
    public void auth2_identifierOnlyForNaturalPersons() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "PersonIdentifier");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqForm(formParams);
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(response.body().asString());

        assertEquals("Person identifier must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier' }.@isRequired"));
    }

    @Test
    public void auth2_optionalAttributesArePresentAndSetTrueForNaturalPersons() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "FamilyName FirstName DateOfBirth BirthName PersonIdentifier PlaceOfBirth CurrentAddress Gender");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqForm(formParams);
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(response.body().asString());

        assertEquals("Family name must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName' }.@isRequired"));
        assertEquals("First name must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName' }.@isRequired"));
        assertEquals("Date of birth must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/DateOfBirth' }.@isRequired"));
        assertEquals("Person identifier must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier' }.@isRequired"));

        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/BirthName' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/PlaceOfBirth' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/CurrentAddress' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/Gender' }.@isRequired"));
    }

    @Test
    public void auth2_mandatoryAttributesArePresentAndSetTrueForLegalPersons() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalPersonIdentifier LegalName");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqForm(formParams);
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(response.body().asString());

        assertEquals("Identifier must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/LegalName' }.@isRequired"));
        assertEquals("RequesterID must be present and have correct value", REQUESTER_ID_VALUE,
                xmlPath.getString("AuthnRequest.Extensions.RequesterID"));
        assertEquals("SPType must be present and have correct value", SP_TYPE_PUBLIC,
                xmlPath.getString("AuthnRequest.Extensions.SPType"));
    }

    @Test
    public void auth2_optionalAttributesArePresentAndSetFalseForLegalPersons() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "LegalAddress VATRegistration TaxReference LEI EORI SEED SIC D-2012-17-EUIdentifier LegalPersonIdentifier LegalName");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqForm(formParams);
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(response.body().asString());
        System.out.println(response.body().asString());

        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/LegalPersonAddress' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/VATRegistrationNumber' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/TaxReference' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/LEI' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/EORI' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/SEED' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/SIC' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/D-2012-17-EUIdentifier' }.@isRequired"));
    }

    @Test
    public void auth2_allAttributesArePresentAndSetCorrectlyForNaturalAndLegalPersons() {
        Map<String,String> formParams = new HashMap<String,String>();
        formParams.put(LOA, "HIGH");
        formParams.put(ATTRIBUTES, "FamilyName FirstName DateOfBirth PersonIdentifier LegalAddress VATRegistration TaxReference LEI EORI SEED SIC D-2012-17-EUIdentifier LegalPersonIdentifier LegalName BirthName PlaceOfBirth CurrentAddress Gender");
        formParams.put(COUNTRY, DEF_COUNTRY);
        formParams.put(RELAY_STATE, "1234abcd");
        formParams.put(REQUESTER_ID, REQUESTER_ID_VALUE);
        formParams.put(SP_TYPE, SP_TYPE_PUBLIC);

        Response response = getAuthenticationReqForm(formParams);
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(response.body().asString());

        assertEquals("Family name must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName' }.@isRequired"));
        assertEquals("First name must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName' }.@isRequired"));
        assertEquals("Date of birth must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/DateOfBirth' }.@isRequired"));
        assertEquals("Person identifier must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier' }.@isRequired"));

        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/BirthName' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/PlaceOfBirth' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/CurrentAddress' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/naturalperson/Gender' }.@isRequired"));

        assertEquals("Identifier must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: true", "true",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/LegalName' }.@isRequired"));

        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/LegalPersonAddress' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/VATRegistrationNumber' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/TaxReference' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/LEI' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/EORI' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/SEED' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/SIC' }.@isRequired"));
        assertEquals("Identifier must be present and required set to: false", "false",
                xmlPath.getString("**.findAll { it.@Name == 'http://eidas.europa.eu/attributes/legalperson/D-2012-17-EUIdentifier' }.@isRequired"));
    }

    @Test
    public void auth2_mandatoryValuesArePresentSpTypePublic() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        assertEquals("SPType must be: public", SP_TYPE_PUBLIC, xmlPath.getString("AuthnRequest.Extensions.SPType"));
        assertEquals("RequesterID must be: TEST-REQUESTER-ID", REQUESTER_ID_VALUE, xmlPath.getString("AuthnRequest.Extensions.RequesterID"));
        assertEquals("The NameID policy must be: unspecified", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", xmlPath.getString("AuthnRequest.NameIDPolicy.@Format"));
        assertThat("Issuer must point to Metadata url", xmlPath.getString("AuthnRequest.Issuer"), endsWith(testEidasClientProperties.getSpMetadataUrl()));
    }

    @Test
    public void auth2_mandatoryValuesArePresentSpTypePrivate() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReq(DEF_COUNTRY, "", "", REQUESTER_ID_VALUE, SP_TYPE_PRIVATE));
        assertEquals("SPType must be: private", SP_TYPE_PRIVATE, xmlPath.getString("AuthnRequest.Extensions.SPType"));
        assertEquals("RequesterID must be: TEST-REQUESTER-ID", REQUESTER_ID_VALUE, xmlPath.getString("AuthnRequest.Extensions.RequesterID"));
        assertEquals("The NameID policy must be: unspecified", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", xmlPath.getString("AuthnRequest.NameIDPolicy.@Format"));
        assertThat("Issuer must point to Metadata url", xmlPath.getString("AuthnRequest.Issuer"), endsWith(testEidasClientProperties.getSpMetadataUrl()));
    }

    @Test
    public void auth2_authenticationLevelIsPresent() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        List<String> loa = xmlPath.getList("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        assertThat("One of the accepted authentication levels must be present", loa,
                anyOf(hasItem("http://eidas.europa.eu/LoA/low"), hasItem("http://eidas.europa.eu/LoA/substantial"), hasItem("http://eidas.europa.eu/LoA/high")));
    }

    @Test
    public void auth3_mandatoryValuesArePresentInEntityDescriptor() {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(getAuthenticationReqWithDefault());
        assertThat("The Destination must be the connected eIDAS node URL", xmlPath.getString("AuthnRequest.@Destination"), endsWith(testEidasClientProperties.getIdpStartUrl()));
        assertThat("ID must be in NCName format" ,  xmlPath.getString("AuthnRequest.@ID"), MatchesPattern.matchesPattern("^[a-zA-Z0-9_.]*$"));
        assertEquals("The ForceAuthn must be: true", "true", xmlPath.getString("AuthnRequest.@ForceAuthn"));
        assertEquals("The IsPassive must be: false", "false", xmlPath.getString("AuthnRequest.@IsPassive"));
        assertEquals("The Version must be: 2.0", "2.0", xmlPath.getString("AuthnRequest.@Version"));
        assertEquals("ProviderName must be correct", testEidasClientProperties.getSpProviderName(), xmlPath.getString("AuthnRequest.@ProviderName"));
        Instant currentTime = Instant.now();
        Instant issuingTime = Instant.parse(xmlPath.getString("AuthnRequest.@IssueInstant"));
        // This assertion may cause flakyness if the client server clock is different
        assertThat("The issuing time should be within 5 seconds of current time",issuingTime, allOf(lessThan(currentTime), greaterThan(currentTime.minus(Duration.ofMillis(5000)))));
    }

    @Test
    public void auth4_allLoaLevelsAreAccepted() {
        XmlPath samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq(DEF_COUNTRY, "LOW", "relayState", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/low", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));

        samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq(DEF_COUNTRY, "SUBSTANTIAL", "relayState", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/substantial", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));

        samlRequest = getDecodedSamlRequestBodyXml(getAuthenticationReq(DEF_COUNTRY, "HIGH", "relayState", REQUESTER_ID_VALUE, SP_TYPE_PUBLIC));
        assertEquals("Correct LOA is returned","http://eidas.europa.eu/LoA/high", samlRequest.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"));
    }
}
