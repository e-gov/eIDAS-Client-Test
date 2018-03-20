package ee.ria.eidas.utils;

import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
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

public class ResponseAssertionBuilderUtils extends ResponseBuilderBase {

    protected EncryptedAssertion buildEncrAssertion(Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = buildAssertionForSigning(inResponseId, recipient ,issueInstant, acceptableTimeMin, loa, givenName, familyName, personIdendifier, dateOfBirth, issuerValue, issuerFormat, audienceUri);
        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }

    protected EncryptedAssertion buildEncrAssertionWithMaxAttributes(Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String birthName, String birthNameFamily, String birthPlace, String address, String gender, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = buildMaximumAssertionForSigning(inResponseId, recipient ,issueInstant, acceptableTimeMin, loa, givenName, familyName, personIdendifier, dateOfBirth, birthName, birthNameFamily, birthPlace, address, gender, issuerValue, issuerFormat, audienceUri);
        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }

    protected Assertion buildAssertionForSigning(String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) {
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setIssueInstant(issueInstant);
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(buildIssuer(issuerValue, issuerFormat));
        assertion.setSubject(buildSubject(inResponseId, recipient, issueInstant, acceptableTimeMin, personIdendifier));
        assertion.setConditions(buildConditions(audienceUri, issueInstant, acceptableTimeMin));
        assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,loa));
        assertion.getAttributeStatements().add(buildMinimalAttributeStatement(givenName, familyName, personIdendifier, dateOfBirth));
        return assertion;
    }

    protected Assertion buildMaximumAssertionForSigning(String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdentifier, String dateOfBirth, String birthName, String birthNameFamily, String birthPlace, String address, String gender, String issuerValue, String issuerFormat, String audienceUri) {
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setIssueInstant(issueInstant);
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(buildIssuer(issuerValue, issuerFormat));
        assertion.setSubject(buildSubject(inResponseId, recipient, issueInstant, acceptableTimeMin, personIdentifier));
        assertion.setConditions(buildConditions(audienceUri, issueInstant, acceptableTimeMin));
        assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,loa));
        assertion.getAttributeStatements().add(buildMaximalAttributeStatement(givenName, familyName, personIdentifier, dateOfBirth, birthName, birthNameFamily, birthPlace, address, gender));
        return assertion;
    }

    protected Signature prepareSignature(Credential signCredential) throws SecurityException {
        Signature signature = (Signature) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
        signature.setSigningCredential(signCredential);
        signature.setSignatureAlgorithm(getSignatureAlgorithm(signCredential));
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        X509KeyInfoGeneratorFactory x509KeyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        x509KeyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfo keyInfo = x509KeyInfoGeneratorFactory.newInstance().generate(signCredential);
        signature.setKeyInfo(keyInfo);
        return signature;
    }

    protected EncryptedAssertion encryptAssertion (Assertion assertion,Credential encCredential) throws EncryptionException {
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
        return  encryptedAssertion;
    }

    protected EncryptedAssertion buildEncrAssertionWithoutAssertionSignature(Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws EncryptionException {
        Assertion assertion = buildAssertionForSigning(inResponseId, recipient ,issueInstant, acceptableTimeMin, loa, givenName, familyName, personIdendifier, dateOfBirth, issuerValue, issuerFormat, audienceUri);
        return encryptAssertion(assertion, encCredential);
    }

    protected Assertion buildAssertionWithoutEncryption(Credential signCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = buildAssertionForSigning(inResponseId, recipient ,issueInstant, acceptableTimeMin, loa, givenName, familyName, personIdendifier, dateOfBirth, issuerValue, issuerFormat, audienceUri);
        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return assertion;
    }

    protected EncryptedAssertion buildEncrAssertionWithoutNameId(Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = buildAssertionForSigning(inResponseId, recipient ,issueInstant, acceptableTimeMin, loa, givenName, familyName, personIdendifier, dateOfBirth, issuerValue, issuerFormat, audienceUri);
        assertion.getSubject().setNameID(null);
        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }

    protected EncryptedAssertion buildEncrAssertionWithWrongNameFormat(Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setIssueInstant(issueInstant);
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(buildIssuer(issuerValue, issuerFormat));
        assertion.setSubject(buildSubject(inResponseId, recipient, issueInstant, acceptableTimeMin, personIdendifier));
        assertion.setConditions(buildConditions(audienceUri, issueInstant, acceptableTimeMin));
        assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,loa));
        assertion.getAttributeStatements().add(buildMinimalAttributeStatementWithFaultyNameFormat(givenName, familyName, personIdendifier, dateOfBirth));
        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }

    protected EncryptedAssertion buildEncrAssertionWithAuthnStatementCnt(Integer cnt, Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setIssueInstant(issueInstant);
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(buildIssuer(issuerValue, issuerFormat));
        assertion.setSubject(buildSubject(inResponseId, recipient, issueInstant, acceptableTimeMin, personIdendifier));
        assertion.setConditions(buildConditions(audienceUri, issueInstant, acceptableTimeMin));
        if (cnt == 1) {
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,loa));
        }
        else if (cnt == 2) {
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,loa));
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,"http://eidas.europa.eu/LoA/low"));
        }
        else if (cnt == 3) {
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,loa));
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,"http://eidas.europa.eu/LoA/low"));
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,"http://eidas.europa.eu/LoA/high"));
        }
        assertion.getAttributeStatements().add(buildMinimalAttributeStatement(givenName, familyName, personIdendifier, dateOfBirth));
        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }

    protected EncryptedAssertion buildEncrAssertionForTimeManipulation(DateTime assertionTime, DateTime subjectTime, DateTime conditionsTime,  DateTime authnTime, Credential signCredential, Credential encCredential, String inResponseId, String recipient, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setIssueInstant(assertionTime);
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(buildIssuer(issuerValue, issuerFormat));
        assertion.setSubject(buildSubject(inResponseId, recipient, subjectTime, acceptableTimeMin, personIdendifier));
        assertion.setConditions(buildConditions(audienceUri, conditionsTime, acceptableTimeMin));
        assertion.getAuthnStatements().add(buildAuthnStatement(authnTime,loa));
        assertion.getAttributeStatements().add(buildMinimalAttributeStatement(givenName, familyName, personIdendifier, dateOfBirth));        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }

    protected EncryptedAssertion buildEncrAssertionWithAttributeCnt(Integer attributeCnt, Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setIssueInstant(issueInstant);
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(buildIssuer(issuerValue, issuerFormat));
        assertion.setSubject(buildSubject(inResponseId, recipient, issueInstant, acceptableTimeMin, personIdendifier));
        assertion.setConditions(buildConditions(audienceUri, issueInstant, acceptableTimeMin));
        assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant,loa));
        if (attributeCnt == 1) {
            assertion.getAttributeStatements().add(buildMinimalAttributeStatement(givenName, familyName, personIdendifier, dateOfBirth));
        } else if (attributeCnt == 2) {
            assertion.getAttributeStatements().add(buildMinimalAttributeStatement(givenName, familyName, personIdendifier, dateOfBirth));
            assertion.getAttributeStatements().add(buildMinimalAttributeStatement(givenName, familyName, personIdendifier, dateOfBirth));
        }

        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }

    protected EncryptedAssertion buildEncrAssertionWithoutSubject(Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = buildAssertionForSigning(inResponseId, recipient ,issueInstant, acceptableTimeMin, loa, givenName, familyName, personIdendifier, dateOfBirth, issuerValue, issuerFormat, audienceUri);
        assertion.setSubject(null);
        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }

    protected EncryptedAssertion buildEncrAssertionWithAuthnContextCnt(Integer cnt, Credential signCredential, Credential encCredential, String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String loa, String givenName, String familyName, String personIdendifier, String dateOfBirth, String issuerValue, String issuerFormat, String audienceUri) throws SecurityException, SignatureException, MarshallingException, EncryptionException {
        Signature signature = prepareSignature(signCredential);
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setIssueInstant(issueInstant);
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssuer(buildIssuer(issuerValue, issuerFormat));
        assertion.setSubject(buildSubject(inResponseId, recipient, issueInstant, acceptableTimeMin, personIdendifier));
        assertion.setConditions(buildConditions(audienceUri, issueInstant, acceptableTimeMin));
        if (cnt == 1) {
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant, loa));
        }
        else if (cnt == 2) {
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant, loa));
            assertion.getAuthnStatements().add(buildAuthnStatement(issueInstant, "http://eidas.europa.eu/LoA/low"));
        }
        assertion.getAttributeStatements().add(buildMinimalAttributeStatement(givenName, familyName, personIdendifier, dateOfBirth));
        assertion.setSignature(signature);
        XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        Signer.signObject(signature);

        return encryptAssertion(assertion, encCredential);
    }
}
