package ee.ria.eidas;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import ee.ria.eidas.config.IntegrationTest;
import ee.ria.eidas.utils.OpenSAMLUtils;
import ee.ria.eidas.utils.ResponseBuilderUtils;
import ee.ria.eidas.utils.SystemPropertyActiveProfileResolver;
import ee.ria.eidas.utils.XmlUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles( profiles = {"dev"}, resolver = SystemPropertyActiveProfileResolver.class)
public abstract class TestsBase {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${test.client.url}")
    protected String testTargetUrl;

    @Value("${test.client.demoUrl}")
    protected String testTargetDemoUrl;

    @Value("${test.client.spMetadataUrl}")
    protected String spMetadataUrl;

    @Value("${test.client.spStartUrl}")
    protected String spStartUrl;

    @Value("${test.client.spReturnUrl}")
    protected String spReturnUrl;

    @Value("${test.client.spProviderName}")
    protected String spProviderName;

    @Value("${test.keystore}")
    protected String clientKeystore;

    @Value("${test.keystorePass}")
    protected String clienKeystorePass;

    @Value("${test.node.responseSigningKeyId}")
    protected String responseSigningKey;

    @Value("${test.node.responseSigningKeyPass}")
    protected String responseSigningPass;

    @Value("${test.node.idpStartUrl}")
    protected String idpStartUrl;

    protected  Credential signatureCredential;
    protected  Credential encryptionCredential;

    @Before
    public void setUp() throws MalformedURLException, InitializationException {
        URL url = new URL(testTargetUrl);
        port = url.getPort();
        baseURI = url.getProtocol() + "://" + url.getHost();

        Security.addProvider(new BouncyCastleProvider());
        InitializationService.initialize();

        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            Resource resource = resourceLoader.getResource(clientKeystore);
            keystore.load(resource.getInputStream(), clienKeystorePass.toCharArray());
            signatureCredential = getCredential(keystore, responseSigningKey, responseSigningPass );
            encryptionCredential = getEncryptionCredentialFromMetaData(getMetadataBody());
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
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(spMetadataUrl).then().log().ifError().statusCode(200).extract().body().asString();
    }

    protected XmlPath getMetadataBodyXML() {
        String metadataResponse = getMetadataBody();
        XmlPath metadataXml = new XmlPath(metadataResponse);
        return metadataXml;
    }

    protected Boolean validateMetadataSchema() {
        given()
        .config(config().xmlConfig(XmlConfig.xmlConfig().disableLoadingOfExternalDtd()))
                .when()
                .get(spMetadataUrl)
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
        return getAuthenticationReq("CA", "","");
    }

    protected String getAuthenticationReq(String country, String loa, String relayState) {
        return given()
                .queryParam("relayState",relayState)
                .queryParam("loa",loa)
                .queryParam("country",country)
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(spStartUrl).then().log().ifError().extract().body().asString();
    }

    protected String getAuthenticationReqForm(Map<String,String> values) {
        return given()
                .queryParams(values)
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(spStartUrl).then().log().ifError().extract().body().asString();
    }

    protected JsonPath sendSamlResponse(String relayState, String response) {
        return given()
                .queryParam("relayState",relayState)
                .queryParam("SAMLResponse", response)
                .config(config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(spReturnUrl).then().log().ifError().extract().body().jsonPath();
    }

    protected void validateMetadataSignature(String body) {
        XmlPath metadataXml = new XmlPath(body);
        try {
            java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("EntityDescriptor.Signature.KeyInfo.X509Data.X509Certificate"));
            validateSignature(body,x509);
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate parsing in validateSignature() failed:" + e.getMessage(), e);
        }
    }

    protected void validateSamlReqSignature(String body) {
        XmlPath metadataXml = new XmlPath(body);
        try {
            java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("AuthnRequest.Signature.KeyInfo.X509Data.X509Certificate"));
            validateSignature(body,x509);
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate parsing in validateSignature() failed:" + e.getMessage(), e);
        }
    }

    protected Boolean validateSamlResponseSignature(String body) {
        XmlPath metadataXml = new XmlPath(body);
        try {
            java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("Response.Signature.KeyInfo.X509Data.X509Certificate"));
            validateSignature(body,x509);
            return true;
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate parsing in validateSignature() failed:" + e.getMessage(), e);
        }
    }

    protected void validateSignature(String body, java.security.cert.X509Certificate x509) {
        try {
            x509.checkValidity();
            SignableSAMLObject signableObj = XmlUtils.unmarshallElement(body);
            X509Credential credential = CredentialSupport.getSimpleCredential(x509,null);
            SignatureValidator.validate(signableObj.getSignature(), credential);
        } catch (SignatureException e) {
            throw new RuntimeException("Signature validation in validateSignature() failed: " + e.getMessage(), e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("Certificate is not yet valid: " + e.getMessage(), e);
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("Certificate is expired: " + e.getMessage(), e);
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

    protected java.security.cert.X509Certificate getEncryptionCertificate(String body) throws CertificateException {
        XmlPath metadataXml = new XmlPath(body);
        java.security.cert.X509Certificate x509 = X509Support.decodeCertificate(metadataXml.getString("**.findAll {it.@use == 'encryption'}.KeyInfo.X509Data.X509Certificate"));
        return x509;
    }

    protected Credential getEncryptionCredentialFromMetaData (String body) throws CertificateException {
        java.security.cert.X509Certificate x509Certificate = getEncryptionCertificate(body);
        BasicX509Credential encryptionCredential = new BasicX509Credential(x509Certificate);
        return encryptionCredential;
    }

    protected String getBase64SamlResponseMinimalAttributes(String requestBody, String givenName, String familyName, String personIdentifier, String dateOfBirth, String loa) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        if(loa == null) {
            loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        }
        Response response = new ResponseBuilderUtils().buildAuthnResponse(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, givenName, familyName, personIdentifier, dateOfBirth);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMaximalAttributes(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMaxAttributes(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, DEFATTR_BIRTH_FIRST, DEFATTR_BIRTH_FAMILY, DEFATTR_BIRTH_PLACE, DEFATTR_ADDR, DEFATTR_GENDER);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributes(String requestBody) {
        return getBase64SamlResponseMinimalAttributes(requestBody, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, null );
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithoutAssertionSignature(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithUnsignedAssertions(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithoutEncryption(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithoutEncryption(signatureCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseDefaultMinimalAttributesWithMixedEncryption(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMixedEncryption(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithMultipleAssertions(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMultipleAssertions(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithoutStatus(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithoutStatus(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithStatusCodes(String requestBody, Integer statusCodeCnt) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");

        Response response = new ResponseBuilderUtils().buildAuthnResponseWithMultipleStatusCode(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE, statusCodeCnt);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithNotExistingLoa(String requestBody, String loa) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);

        Response response = new ResponseBuilderUtils().buildAuthnResponse(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithoutNameId(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithoutNameID(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseMinimalAttributesWithWrongNameFormat(String requestBody) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        String loa =  xmlPath.getString("AuthnRequest.RequestedAuthnContext.AuthnContextClassRef");
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithWrongNameFormat(signatureCredential, encryptionCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, loa, DEFATTR_FIRST, DEFATTR_FAMILY, DEFATTR_PNO, DEFATTR_DATE);
        String stringResponse = OpenSAMLUtils.getXmlString(response);
        validateSamlResponseSignature(stringResponse);
        return new String(Base64.getEncoder().encode(stringResponse.getBytes()));
    }

    protected String getBase64SamlResponseWithErrors(String requestBody, String error) {
        XmlPath xmlPath = getDecodedSamlRequestBodyXml(requestBody);
        Response response = new ResponseBuilderUtils().buildAuthnResponseWithError(signatureCredential, xmlPath.getString("AuthnRequest.@ID"),
                testTargetDemoUrl+spReturnUrl, error);
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
}
