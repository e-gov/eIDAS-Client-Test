package ee.ria.eidas.utils;

import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.*;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import javax.xml.namespace.QName;

import static org.opensaml.saml.common.SAMLVersion.VERSION_20;

public class ResponseBuilderUtils {

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
            authnResponse.getAssertions().add(buildAssertion(inResponseId,recipient, timeNow, null));

            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnResponse).marshall(authnResponse);
            Signer.signObject(signature);

            return authnResponse;
        } catch (Exception e) {
            throw new RuntimeException("SAML error:" + e.getMessage(), e);
        }
    }

    private Status buildErrorStatus(String error) {
        Status status = new StatusBuilder().buildObject();
        status.setStatusCode(buildStatusCode(error));
        status.setStatusMessage(buildStatusMessage(error));
        return status;
    }

    private StatusCode buildStatusCode (String error) {
        StatusCode statusCode = new StatusCodeBuilder().buildObject();
        StatusCode statCode = new StatusCodeBuilder().buildObject();
        String valueUrn = "urn:oasis:names:tc:SAML:2.0:status:";
        switch(error) {
            case "AuthFailed":
                statusCode.setValue(valueUrn + "Responder");
                statCode.setValue(valueUrn + "AuthnFailed");
                break;
            case "ConsentNotGiven":
                statusCode.setValue(valueUrn + "Requester");
                statCode.setValue(valueUrn + "RequestDenied");
                break;
            default:
                statusCode.setValue("Not existing keyword for error:" + error);
                statCode.setValue("Not existing keyword for error:");
                break;
        }
        statusCode.setStatusCode(statCode);
        return statusCode;
    }

    private StatusMessage buildStatusMessage(String error) {
        StatusMessage statusMessage = new StatusMessageBuilder().buildObject();
        switch(error) {
            case "AuthFailed":
                statusMessage.setMessage("003002 - Authentication Failed.");
                break;
            case "ConsentNotGiven":
                statusMessage.setMessage("202007 - Consent not given for a mandatory attribute.");
                break;
            default:
                statusMessage.setMessage("Not existing keyword for error:" + error);
                break;
        }
        return statusMessage;
    }

    private Status buildSuccessStatus() {
        Status status = new StatusBuilder().buildObject();
        StatusCode statusCode = new StatusCodeBuilder().buildObject();
        statusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
        status.setStatusCode(statusCode);
        StatusMessage statusMessage = new StatusMessageBuilder().buildObject();
        statusMessage.setMessage("urn:oasis:names:tc:SAML:2.0:status:Success");
        status.setStatusMessage(statusMessage);
        return status;
    }

    private Issuer buildIssuer() {
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue("http://localhost:8080/EidasNode/ConnectorResponderMetadata");
        issuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        return issuer;
    }

    private Assertion buildAssertion(String inResponseId, String recipient, DateTime issuInstant, String loa) {
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setIssueInstant(new DateTime());
        assertion.setVersion(VERSION_20);
        assertion.setIssuer(buildIssuer());
        assertion.setSubject(buildSubject(inResponseId,recipient,issuInstant));
        assertion.setConditions(buildConditions(issuInstant));
        assertion.getAuthnStatements().add(buildAuthnStatement(issuInstant, loa));

        return assertion;
    }

    private Subject buildSubject(String inResponseId, String recipient, DateTime issueInstant) {
        Subject subject = new SubjectBuilder().buildObject();
        NameID nameID = new NameIDBuilder().buildObject();
        nameID.setValue("NotAvailable");
        nameID.setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
        nameID.setNameQualifier("http://C-PEPS.gov.xx");
        subject.setNameID(nameID);
        SubjectConfirmation subjectConf = new SubjectConfirmationBuilder().buildObject();
        subjectConf.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        SubjectConfirmationData subConfData = new SubjectConfirmationDataBuilder().buildObject();
        subConfData.setAddress("172.24.0.1");
        subConfData.setInResponseTo(inResponseId);
        subConfData.setNotOnOrAfter(issueInstant.plusMinutes(5));
        subConfData.setRecipient(recipient);
        subjectConf.setSubjectConfirmationData(subConfData);
        subject.getSubjectConfirmations().add(subjectConf);
        return subject;
    }

    private Conditions buildConditions(DateTime issueInstant) {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(issueInstant);
        conditions.setNotOnOrAfter(issueInstant.plusMinutes(5));
        AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI("http://localhost:8080/SP/metadata");
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    private AuthnStatement buildAuthnStatement(DateTime issueInstant, String loa) {
        AuthnStatement authnStatement = new AuthnStatementBuilder().buildObject();
        authnStatement.setAuthnInstant(issueInstant);
        AuthnContext authnCont = new AuthnContextBuilder().buildObject();
        AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder().buildObject();
        authnContextClassRef.setAuthnContextClassRef(loa);
        authnCont.setAuthnContextClassRef(authnContextClassRef);
        authnCont.setAuthnContextDecl(null);
        authnStatement.setAuthnContext(authnCont);
        return  authnStatement;
    }

    private EncryptedAssertion buildEncrAssertion(Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(signCredential);
        signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
        signature.setKeyInfo(keyInfo);

        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setIssueInstant(issueInstant);
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(buildIssuer());
        assertion.setSubject(buildSubject(inResponseId,recipient, issueInstant));
        assertion.setConditions(buildConditions(issueInstant));
        assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,loa));
        assertion.getAttributeStatements().add(buildMinimalAttributeStatement(givenName, familyName, personIdendifier, dateOfBirth));
        assertion.setSignature(signature);

        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);


        KeyEncryptionParameters keyParams = new KeyEncryptionParameters();
        keyParams.setEncryptionCredential(encCredential);
        keyParams.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
        keyParams.setKeyInfoGenerator(keyInfoGenerator);

        DataEncryptionParameters encryptParams = new DataEncryptionParameters();
        encryptParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);

        Encrypter samlEncrypter = new Encrypter(encryptParams, keyParams);
        samlEncrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
        EncryptedAssertion encryptedAssertion = samlEncrypter.encrypt(assertion);
        return encryptedAssertion;
    }

    private AttributeStatement buildMinimalAttributeStatement(String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        if(givenName != null) {
            attributeStatement.getAttributes().add(buildAttribute("FirstName", "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentGivenNameType", givenName));
        }
        attributeStatement.getAttributes().add(buildAttribute("FamilyName", "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentFamilyNameType", familyName));
        attributeStatement.getAttributes().add(buildAttribute("PersonIdendifier", "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:PersonIdentifierType", personIdentifier));
        attributeStatement.getAttributes().add(buildAttribute("DateOfBirth", "http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:DateOfBirthType", dateOfBirth));
        return attributeStatement;
    }

    private Attribute buildAttribute(String friendlyName, String name, String nameFormat, String xsiType, String value) {
        Attribute attribute = new AttributeBuilder().buildObject();
        attribute.setFriendlyName(friendlyName);
        attribute.setName(name);
        attribute.setNameFormat(nameFormat);
        attribute.getAttributeValues().add(buildAttributeValue(xsiType, value));
        return attribute;
    }

    private XSAny buildAttributeValue(String xsiType, String value) {
        XSAny attributevalue = new XSAnyBuilder().buildObject(AttributeValue.DEFAULT_ELEMENT_NAME);
        attributevalue.getUnknownAttributes().put(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi"), xsiType);
        attributevalue.setTextContent(value);
        return attributevalue;
    }

    private String getSignatureAlgorithm(Credential credential) {
        if ("RSA".equals(credential.getPublicKey().getAlgorithm())) {
            return SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        } else {
            return SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256;
        }
    }
}
