package ee.ria.eidas.utils;

import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.*;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.Signer;

import static org.opensaml.saml.common.SAMLVersion.VERSION_20;

public class ResponseBuilderUtils extends ResponseAssertionBuilderUtils {

    public Response buildAuthnResponse(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(signCredential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
            signature.setKeyInfo(keyInfo);

            DateTime timeNow = new DateTime();

            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setStatus(buildSuccessStatus());
            authnResponse.setIssuer(buildIssuer());
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));

            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);

            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithUnsignedAssertions(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        try {
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(signCredential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
            signature.setKeyInfo(keyInfo);

            DateTime timeNow = new DateTime();

            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setStatus(buildSuccessStatus());
            authnResponse.setIssuer(buildIssuer());
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
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(signCredential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
            signature.setKeyInfo(keyInfo);

            DateTime timeNow = new DateTime();

            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setStatus(buildSuccessStatus());
            authnResponse.setIssuer(buildIssuer());
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
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(signCredential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
            signature.setKeyInfo(keyInfo);

            DateTime timeNow = new DateTime();

            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setStatus(buildSuccessStatus());
            authnResponse.setIssuer(buildIssuer());
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
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(signCredential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
            signature.setKeyInfo(keyInfo);

            DateTime timeNow = new DateTime();

            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setStatus(buildSuccessStatus());
            authnResponse.setIssuer(buildIssuer());
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
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(signCredential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
            signature.setKeyInfo(keyInfo);

            DateTime timeNow = new DateTime();

            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setIssuer(buildIssuer());
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
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(signCredential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
            signature.setKeyInfo(keyInfo);

            DateTime timeNow = new DateTime();

            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setStatus(buildSuccessStatusWithStatusCode(statusCodeCnt));
            authnResponse.setIssuer(buildIssuer());
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));

            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);

            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildFaultyAuthnResponse(Credential signCredential, Credential encCredential, String inResponseId, String recipient, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, DateTime timeNow) {
        try {
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(signCredential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
            signature.setKeyInfo(keyInfo);

            if (timeNow == null) {
                timeNow = new DateTime();
            }

            Response authnResponse = new ResponseBuilder().buildObject();
            authnResponse.setIssueInstant(timeNow);
            authnResponse.setDestination(recipient);
            authnResponse.setInResponseTo(inResponseId);
            authnResponse.setVersion(VERSION_20);
            authnResponse.setID(OpenSAMLUtils.generateSecureRandomId());
            authnResponse.setSignature(signature);
            authnResponse.setStatus(buildSuccessStatus());
            authnResponse.setIssuer(buildIssuer());
            authnResponse.getEncryptedAssertions().add(buildEncrAssertion(signCredential, encCredential, inResponseId, recipient, timeNow, loa, givenName, familyName, personIdentifier, dateOfBirth));

            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);

            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    public Response buildAuthnResponseWithError(Credential credential, String inResponseId, String recipient, String error) {
        try {
            Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                    .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
            signature.setSigningCredential(credential);
            signature.setSignatureAlgorithm(getSignatureAlgorithm(credential));
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

            X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(credential);
            signature.setKeyInfo(keyInfo);
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

}
