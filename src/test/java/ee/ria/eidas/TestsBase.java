package ee.ria.eidas;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import ee.ria.eidas.config.IntegrationTest;
import ee.ria.eidas.config.TestConfiguration;
import ee.ria.eidas.config.TestEidasClientProperties;
import ee.ria.eidas.utils.OpenSAMLUtils;
import ee.ria.eidas.utils.ResponseBuilderUtils;
import ee.ria.eidas.utils.SystemPropertyActiveProfileResolver;
import ee.ria.eidas.utils.XmlUtils;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.config.XmlConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialSupport;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.security.x509.X509Support;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static ee.ria.eidas.config.EidasTestStrings.*;
import static io.restassured.RestAssured.*;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.internal.matcher.xml.XmlXsdMatcher.matchesXsdInClasspath;

@RunWith(SpringRunner.class)
@Category(IntegrationTest.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles(profiles = {"dev"}, resolver = SystemPropertyActiveProfileResolver.class)
public abstract class TestsBase {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    protected TestEidasClientProperties testEidasClientProperties;

    protected Credential signatureCredential;
    protected Credential signatureCredentialUntrusted;
    protected Credential encryptionCredential;
    protected SSLConfig sslConfig;

    @Before
    public void setUp() throws MalformedURLException, InitializationException {
        URL url = new URL(testEidasClientProperties.getTargetUrl());
        port = url.getPort();
        baseURI = url.getProtocol() + "://" + url.getHost();

        Security.addProvider(new BouncyCastleProvider());
        InitializationService.initialize();

        XmlPath metadata = getMetadataBodyXML();

        testEidasClientProperties.setAdvertizedSpReturnUrl(metadata.getString("EntityDescriptor.SPSSODescriptor.AssertionConsumerService.@Location"));

        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            Resource resource = resourceLoader.getResource(testEidasClientProperties.getKeystore());
            keystore.load(resource.getInputStream(), testEidasClientProperties.getKeystorePass().toCharArray());
            signatureCredential = getCredential(keystore, testEidasClientProperties.getResponseSigningKeyId(), testEidasClientProperties.getResponseSigningKeyPass());
            KeyStore keystoreDefault = KeyStore.getInstance(KeyStore.getDefaultType());
            Resource resourceDefault = resourceLoader.getResource("classpath:samlKeystore.jks");
            keystoreDefault.load(resourceDefault.getInputStream(), "changeit".toCharArray());
            signatureCredentialUntrusted = getCredential(keystoreDefault, "test_cert", "changeit");
            encryptionCredential = getEncryptionCredentialFromMetaData(metadata);
            if (testEidasClientProperties.getTargetUrl().startsWith("https")) {
                //If client authentication is required then keystore should have the correct private key
                //If client authentication is not required then keystore can be the same as truststore without any private keys
                sslConfig = new SSLConfig().
                        keyStore(testEidasClientProperties.getHttpsKeystore(), testEidasClientProperties.getHttpsKeystorePassword()).
                        trustStore(testEidasClientProperties.getHttpsTruststore(), testEidasClientProperties.getHttpsTruststorePassword());
            }
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong initializing credentials:", e);
        }
    }

    private Credential getCredential(KeyStore keystore, String keyPairId, String privateKeyPass) {
        try {
            Map<String, String> passwordMap = new HashMap<>();
            passwordMap.put(keyPairId, privateKeyPass);
            KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(keystore, passwordMap);

            Criterion criterion = new EntityIdCriterion(keyPairId);
            CriteriaSet criteriaSet = new CriteriaSet();
            criteriaSet.add(criterion);

            return resolver.resolveSingle(criteriaSet);
        } catch (ResolverException e) {
            throw new RuntimeException("Something went wrong reading credentials", e);
        }
    }

    protected String getMetadataBody() {
        return given()
                .filter(new AllureRestAssured())
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(testEidasClientProperties.getFullSpMetadataUrl()).then().log().ifError().statusCode(200).extract().body().asString();
    }

    protected XmlPath getMetadataBodyXML() {
        String metadataResponse = getMetadataBody();
        XmlPath metadataXml = new XmlPath(metadataResponse);
        return metadataXml;
    }

    protected Boolean validateMetadataSchema() {
        given()
                .filter(new AllureRestAssured())
                .config(config().xmlConfig(XmlConfig.xmlConfig().disableLoadingOfExternalDtd()).sslConfig(sslConfig))
                .when()
                .get(testEidasClientProperties.getSpMetadataUrl())
                .then().log().ifError()
                .statusCode(200)
                .body(matchesXsdInClasspath("SPschema.xsd").using(new ClasspathResourceResolver()));
        return true;
    }

    protected XmlPath getDecodedSamlRequestBodyXml(String body) {
        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, body);
        String SAMLRequestString = html.getString("**.findAll { it.@name == 'SAMLRequest' }.@value");
        String decodedRequest = new String(Base64.getDecoder().decode(SAMLRequestString), StandardCharsets.UTF_8);
        XmlPath decodedSAMLrequest = new XmlPath(decodedRequest);
        return decodedSAMLrequest;
    }

    protected String getDecodedSamlRequestBody(String body) {
        XmlPath html = new XmlPath(XmlPath.CompatibilityMode.HTML, body);
        String SAMLRequestString = html.getString("**.findAll { it.@name == 'SAMLRequest' }.@value");
        String decodedSAMLrequest = new String(Base64.getDecoder().decode(SAMLRequestString), StandardCharsets.UTF_8);
        return decodedSAMLrequest;
    }

    protected String getAuthenticationReqWithDefault() {
        return getAuthenticationReq(DEF_COUNTRY, "", "");
    }

    protected String getAuthenticationReq(String country, String loa, String relayState) {
        return given()
                .filter(new AllureRestAssured())
                .queryParam(RELAY_STATE, relayState)
                .queryParam(LOA, loa)
                .queryParam(COUNTRY, country)
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")).sslConfig(sslConfig))
                .when()
                .get(testEidasClientProperties.getSpStartUrl())
                .then()
                .extract().body().asString();
    }

    protected io.restassured.response.Response getAuthenticationReqResponse(String country, String loa, String relayState) {
        return given()
                .filter(new AllureRestAssured())
                .queryParam(RELAY_STATE, relayState)
                .queryParam(LOA, loa)
                .queryParam(COUNTRY, country)
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")).sslConfig(sslConfig))
                .when()
                .get(testEidasClientProperties.getSpStartUrl())
                .then()
                .extract().response();
    }

    protected io.restassured.response.Response getAuthenticationReqForm(Map<String, String> values) {
        return given()
                .filter(new AllureRestAssured())
                .queryParams(values)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")).sslConfig(sslConfig))
                //.log().all()
                .when()
                .get(testEidasClientProperties.getSpStartUrl())
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().response();
    }

    protected io.restassured.response.Response getAuthenticationReqFormFail(Map<String, String> values) {
        return given()
                .filter(new AllureRestAssured())
                .queryParams(values)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")).sslConfig(sslConfig))
                //.log().all()
                .when()
                .get(testEidasClientProperties.getSpStartUrl())
                .then()
                .extract().response();
    }

    protected JsonPath sendSamlResponse(String relayState, String response) {
        return given()
                .filter(new AllureRestAssured())
                .formParam(RELAY_STATE, relayState)
                .formParam(SAML_RESPONSE, response)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")).sslConfig(sslConfig))
                .log().all()
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .log().all()
                .extract().body().jsonPath();
    }

    protected io.restassured.response.Response sendSamlResponseGetStatus(String relayState, String response) {
        return given()
                .filter(new AllureRestAssured())
                .formParam(RELAY_STATE, relayState)
                .formParam(SAML_RESPONSE, response)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")).sslConfig(sslConfig))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .extract().response();
    }

    protected io.restassured.response.Response sendSamlResponseExtractResponse(String relayState, String response) {
        return given()
                .filter(new AllureRestAssured())
                .formParam(RELAY_STATE, relayState)
                .formParam(SAML_RESPONSE, response)
                .contentType("application/x-www-form-urlencoded")
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")).sslConfig(sslConfig))
                .when()
                .post(testEidasClientProperties.getSpReturnUrl())
                .then()
                .extract().response();
    }

    protected void validateMetadataSignature(String body) {
        XmlPath metadataXml = new XmlPath(body);
        try {
            java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("EntityDescriptor.Signature.KeyInfo.X509Data.X509Certificate"));
            validateSignature(body, x509);
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate parsing in validateSignature() failed:" + e.getMessage(), e);
        }
    }

    protected void validateSamlReqSignature(String body) {
        XmlPath metadataXml = new XmlPath(body);
        try {
            java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("AuthnRequest.Signature.KeyInfo.X509Data.X509Certificate"));
            validateSignature(body, x509);
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate parsing in validateSignature() failed:" + e.getMessage(), e);
        }
    }

    protected Boolean validateSamlResponseSignature(String body) {
        XmlPath metadataXml = new XmlPath(body);
        try {
            java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("Response.Signature.KeyInfo.X509Data.X509Certificate"));
            validateSignature(body, x509);
            return true;
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate parsing in validateSignature() failed:" + e.getMessage(), e);
        }
    }

    protected void validateSignature(String body, java.security.cert.X509Certificate x509) {
        try {
            x509.checkValidity();
            SignableSAMLObject signableObj = XmlUtils.unmarshallElement(body);
            X509Credential credential = CredentialSupport.getSimpleCredential(x509, null);
            SignatureValidator.validate(signableObj.getSignature(), credential);
        } catch (SignatureException e) {
            throw new RuntimeException("Signature validation in validateSignature() failed: " + e.getMessage(), e);
        } catch (CertificateNotYetValidException e) {
            //Expired certificates are used in test environment
            return;
        } catch (CertificateExpiredException e) {
            //Expired certificates are used in test environment
            return;
        }
    }

    protected Boolean isCertificateValid(String certString) {
        try {
            java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(certString);
            isCertificateValidX509(x509);
            x509.checkValidity();
            return true;
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("Certificate is expired: " + e.getMessage(), e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("Certificate is not yet valid: " + e.getMessage(), e);
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate parsing in isCertificateValid() failed: " + e.getMessage(), e);
        }
    }

    protected void isCertificateValidX509(java.security.cert.X509Certificate x509) {
        try {
            x509.checkValidity();
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("Certificate is expired: " + e.getMessage(), e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("Certificate is not yet valid: " + e.getMessage(), e);
        }
    }

    protected java.security.cert.X509Certificate getEncryptionCertificate(XmlPath body) throws CertificateException {
        java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(body.getString("**.findAll {it.@use == 'encryption'}.KeyInfo.X509Data.X509Certificate"));
        return x509;
    }

    protected Credential getEncryptionCredentialFromMetaData(XmlPath body) throws CertificateException {
        java.security.cert.X509Certificate x509Certificate = getEncryptionCertificate(body);
        BasicX509Credential encryptionCredential = new BasicX509Credential(x509Certificate);
        return encryptionCredential;
    }

    protected String getValueFromJsonResponse(io.restassured.response.Response response, String key) {
        return response.getBody().jsonPath().getString(key);
    }

    protected String getBase64SamlResponseMinimalAttributes(String requestBody, String givenName, String familyName, String personIdentifier, String dateOfBirth, String loa) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        if (loa == null) {
            loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        }
        Response response = new ResponseBuilderUtils().buildAuthnResponse(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, givenName, familyName, personIdentifier, dateOfBirth, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseLegalMinimalAttributes(String requestBody, String givenName, String familyName, String personIdentifier, String dateOfBirth, String legalName, String legalPno) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        Response response = new ResponseBuilderUtils().buildLegalAuthnResponse(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"), givenName, familyName, personIdentifier, dateOfBirth, legalName, legalPno, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseLegalMaximalAttributes(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMaxLegalAttributes(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO,
                DEFATTR_DATE, DEFATTR_LEGAL_NAME, DEFATTR_LEGAL_PNO, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl(),
                DEFATTR_LEGAL_ADDRESS, DEFATTR_LEGAL_VATREGISTRATION, DEFATTR_LEGAL_TAXREFERENCE, DEFATTR_LEGAL_BUSINESSCODES, DEFATTR_LEGAL_LEI, DEFATTR_LEGAL_EORI, DEFATTR_LEGAL_SEED, DEFATTR_LEGAL_SIC, DEFATTR_LEGAL_D201217EUIDENTIFIER);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMaximalAttributes(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMaxAttributes(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, DEFATTR_BIRTH_NAME, DEFATTR_BIRTH_PLACE, DEFATTR_ADDR, DEFATTR_GENDER, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributes(String requestBody) {
        return getBase64SamlResponseMinimalAttributes(requestBody, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, null);
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithoutAssertionSignature(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithUnsignedAssertions(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithoutSignature(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithoutSignature(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithUntrustedSignature(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponse(signatureCredentialUntrusted, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithFaultySignature(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponse(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        response.setConsent("TestConsent");
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithoutEncryption(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithoutEncryption(signatureCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithMixedEncryption(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMixedEncryption(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithMultipleAssertions(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMultipleAssertions(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithoutStatus(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithoutStatus(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithStatusCodes(String requestBody, Integer statusCodeCnt) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMultipleStatusCode(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, statusCodeCnt, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithNotExistingLoa(String requestBody, String loa) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);

        Response response = new ResponseBuilderUtils().buildAuthnResponse(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseNameIdCnt(String requestBody, Integer nameIdCnt, String nameIdFormat) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        Response response = new ResponseBuilderUtils().buildAuthnResponseNameIdCnt(nameIdCnt, nameIdFormat, signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithWrongNameFormat(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithWrongNameFormat(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseWithErrors(String requestBody, String error) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithError(signatureCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), error, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    public class ClasspathResourceResolver implements LSResourceResolver {
        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
            InputStream resource = ClassLoader.getSystemResourceAsStream(systemId);
            return new DOMInputImpl(publicId, systemId, baseURI, resource, null);
        }
    }

    protected String getBase64SamlResponseInResponseTo(String inResponseToResponse, String inResponseToSubject) {
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithInResponseTo(signatureCredential, encryptionCredential, inResponseToResponse, inResponseToSubject,
                testEidasClientProperties.getFullSpReturnUrl(), LOA_SUBSTANTIAL, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseIssuer(String requestBody, String issuerValue, String issuerFormat) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithIssuer(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, issuerValue, issuerFormat, testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseAuthnStatement(String requestBody, Integer cnt) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithAuthnStatement(cnt, signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseTimeManipulation(String requestBody, Integer addResponseMin, Integer addAssertionMin, Integer addSubjectMin, Integer addConditionsMin, Integer addAuthnMin) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithTimeManipulation(addResponseMin, addAssertionMin, addSubjectMin, addConditionsMin, addAuthnMin, signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseWithAttributeCnt(Integer attributeCnt, String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithAttributeStatementCnt(attributeCnt, signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseWithoutSubject(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithoutSubject(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseAuthnContextCnt(String requestBody, Integer authnContextCnt) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithAuthnContextCnt(authnContextCnt, signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseAudienceCnt(String requestBody, Integer audienceCnt, String audienceUrl) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithAudienceCnt(audienceCnt, signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), audienceUrl);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseRecipient(String requestBody, String recipient) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        Response response = new ResponseBuilderUtils().buildAuthnResponse(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                recipient, xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef"), DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseSubjectConfirmationCnt(String requestBody, Integer subjectConfirmationCnt, String subjectConfirmationMethod) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa = xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseSubjectConfirmationCnt(subjectConfirmationCnt, subjectConfirmationMethod, signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testEidasClientProperties.getFullSpReturnUrl(), loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, testEidasClientProperties.getFullIdpMetadataUrl(), testEidasClientProperties.getAcceptableTimeDiffMin(), testEidasClientProperties.getFullSpMetadataUrl());
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }
}
