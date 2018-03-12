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

    public Response buildAuthnResponse(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            authnResponse.setSignature(signature);
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithUnsignedAssertions(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertionWithoutAssertionSignature(encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithoutEncryption(Credential signCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.getAssertions().add(buildAssertionWithoutEncryption(signCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithMixedEncryption(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.getAssertions().add(buildAssertionWithoutEncryption(signCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential,inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithMultipleAssertions(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName + "1", familyName, personIdentifier, dateOfBirth));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithoutStatus(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.setStatus(null);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithMultipleStatusCode(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, Integer statusCodeCnt) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.setStatus(null);
            authnResponse.setStatus(buildSuccessStatusWithStatusCode(statusCodeCnt));
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithoutNameID(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertionWithoutNameId(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithWrongNameFormat(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = prepareSignature(signCredential);
            DateTime timeNow = new DateTime();
            Response authnResponse = buildResponseForSigningWithoutAssertion(inResponseId, recipient, timeNow);
            authnResponse.getEncryptedAssertions().add(buildEncrAssertionWithWrongNameFormat(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);
            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithError(Credential signCredential, String inResponseId, String recipient, String error) {
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
            authnResponse.setIssuer(buildIssuer());
            authnResponse.getAssertions().add(buildAssertion(inResponseId,recipient, timeNow, null, "EE/EE/33232"));

            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);

            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    protected Response buildResponseForSigningWithoutAssertion (String inResponseId, String recipient, DateTime timeNow) {
        Response authnResponse = new ResponseBuilder().buildObject();
        authnResponse.setIssueInstant(timeNow);
        authnResponse.setDestination(recipient);
        authnResponse.setInResponseTo(inResponseId);
        authnResponse.setVersion(VERSION_20);
        authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
        authnResponse.setStatus(buildSuccessStatus());
        authnResponse.setIssuer(buildIssuer());
        return authnResponse;
    }
}
