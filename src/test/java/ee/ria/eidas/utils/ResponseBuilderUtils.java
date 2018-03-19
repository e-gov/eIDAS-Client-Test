package ee.ria.eidas.utils;

import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.*;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.Signer;

import static org.opensaml.saml.common.SAMLVersion.VERSION_20;

public class ResponseBuilderUtils extends ResponseAssertionBuilderUtils {

    public Response buildAuthnResponse(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithMaxAttributes(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String birthName, String birthNameFamily, String birthPlace, String address, String gender, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertionWithMaxAttributes(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, birthName, birthNameFamily, birthPlace, address, gender, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithUnsignedAssertions(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertionWithoutAssertionSignature(encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithoutEncryption(Credential signCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getAssertions().add(buildAssertionWithoutEncryption(signCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithMixedEncryption(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getAssertions().add(buildAssertionWithoutEncryption(signCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential,inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithMultipleAssertions(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName + "1", familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithoutStatus(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.setStatus(null);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithMultipleStatusCode(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, Integer statusCodeCnt, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.setStatus(null);
            authnResponse.setStatus(buildSuccessStatusWithStatusCode(statusCodeCnt));
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithoutNameID(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertionWithoutNameId(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            authnResponse.setSignature(signature);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithWrongNameFormat(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertionWithWrongNameFormat(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            authnResponse.setSignature(signature);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithError(Credential signCredential, String inResponseId, String recipient, String error, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setStatus(buildErrorStatus(error));
            authnResponse.setIssuer(buildIssuer(issuerValue, issuerFormat));
            authnResponse.getAssertions().add(buildAssertion(inResponseId,recipient, timeNow, null, "EE/EE/33232", issuerValue, issuerFormat));

            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);

            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    protected Response buildResponseForSigningWithoutAssertion (String inResponseId, String recipient, DateTime timeNow, String issuerValue, String issuerFormat) {
        Response authnResponse = new ResponseBuilder().buildObject();
        authnResponse.setIssueInstant(timeNow);
        authnResponse.setDestination(recipient);
        authnResponse.setInResponseTo(inResponseId);
        authnResponse.setVersion(VERSION_20);
        authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
        authnResponse.setStatus(buildSuccessStatus());
        authnResponse.setIssuer(buildIssuer(issuerValue, issuerFormat));
        return authnResponse;
    }

    public Response buildAuthnResponseWithInResponseTo(Credential signCredential, Credential encCredential, String inResponseIdResponse, String inResponseIdSubject, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String issuerValue, String issuerFormat) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseIdResponse, recipient, timeNow, issuerValue, issuerFormat);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseIdSubject, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth, issuerValue, issuerFormat));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }
}
