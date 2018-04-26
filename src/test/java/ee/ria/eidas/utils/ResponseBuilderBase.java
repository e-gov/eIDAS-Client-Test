package ee.ria.eidas.utils;

import org.joda.time.DateTime;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.*;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import javax.xml.namespace.QName;

import static org.opensaml.saml.common.SAMLVersion.VERSION_20;
import static org.opensaml.saml.saml2.core.StatusCode.*;

public class ResponseBuilderBase {


    protected Status buildErrorStatus(String error) {
        Status status = new StatusBuilder().buildObject();
        status.setStatusCode(buildStatusCode(error));
        status.setStatusMessage(buildStatusMessage(error));
        return status;
    }

    protected StatusCode buildStatusCode (String error) {
        StatusCode statusCode = new StatusCodeBuilder().buildObject();
        StatusCode statCode = new StatusCodeBuilder().buildObject();
        switch(error) {
            case "AuthFailed":
                statusCode.setValue(RESPONDER);
                statCode.setValue(AUTHN_FAILED);
                break;
            case "ConsentNotGiven":
                statusCode.setValue(REQUESTER);
                statCode.setValue(REQUEST_DENIED);
                break;
            default:
                statusCode.setValue(RESPONDER);
                statCode.setValue(INVALID_NAMEID_POLICY);
                break;
        }
        statusCode.setStatusCode(statCode);
        return statusCode;
    }

    protected StatusMessage buildStatusMessage(String error) {
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

    protected Status buildSuccessStatus() {
        Status status = new StatusBuilder().buildObject();
        StatusCode statusCode = new StatusCodeBuilder().buildObject();
        statusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
        status.setStatusCode(statusCode);
        StatusMessage statusMessage = new StatusMessageBuilder().buildObject();
        statusMessage.setMessage("urn:oasis:names:tc:SAML:2.0:status:Success");
        status.setStatusMessage(statusMessage);
        return status;
    }

    protected Status buildSuccessStatusWithStatusCode(Integer statusCodeCnt) {
        Status status = new StatusBuilder().buildObject();
        switch (statusCodeCnt) {
            case 0:
                break;
            case 1:
                StatusCode statusCode = new StatusCodeBuilder().buildObject();
                statusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
                status.setStatusCode(statusCode);
                break;
            case 2:
                StatusCode statusCode2 = new StatusCodeBuilder().buildObject();
                statusCode2.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
                status.setStatusCode(statusCode2);
                StatusCode statusCode3 = new StatusCodeBuilder().buildObject();
                statusCode3.setValue("urn:oasis:names:tc:SAML:2.0:status:FAIL");
                status.setStatusCode(statusCode3);
                break;
        }
        StatusMessage statusMessage = new StatusMessageBuilder().buildObject();
        statusMessage.setMessage("urn:oasis:names:tc:SAML:2.0:status:Success");
        status.setStatusMessage(statusMessage);
        return status;
    }

    protected Issuer buildIssuer(String issuerValue) {
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(issuerValue);
        issuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        return issuer;
    }

    protected Assertion buildAssertion(String inResponseId, String recipient, DateTime issuInstant, Integer acceptableTimeMin, String loa, String personIdentifier, String issuerValue, String audienceUri) {
        Assertion assertion = new AssertionBuilder().buildObject();
        assertion.setID(OpenSAMLUtils.generateSecureRandomId());
        assertion.setIssueInstant(issuInstant);
        assertion.setVersion(VERSION_20);
        assertion.setIssuer(buildIssuer(issuerValue));
        assertion.setSubject(buildSubject(inResponseId,recipient, issuInstant, acceptableTimeMin, personIdentifier));
        assertion.setConditions(buildConditions(audienceUri, issuInstant, acceptableTimeMin));
        assertion.getAuthnStatements().add(buildAuthnStatement(issuInstant, loa));
        return assertion;
    }

    protected Subject buildSubject(String inResponseId, String recipient, DateTime issueInstant, Integer acceptableTimeMin, String personIdentifier) {
        Subject subject = new SubjectBuilder().buildObject();
        NameID nameID = new NameIDBuilder().buildObject();
        nameID.setValue(personIdentifier);
        nameID.setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
        nameID.setNameQualifier("http://C-PEPS.gov.xx");
        subject.setNameID(nameID);
        SubjectConfirmation subjectConf = new SubjectConfirmationBuilder().buildObject();
        subjectConf.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        SubjectConfirmationData subConfData = new SubjectConfirmationDataBuilder().buildObject();
        subConfData.setAddress("172.24.0.1"); //TODO: this needs to be configurable probably
        subConfData.setInResponseTo(inResponseId);
        subConfData.setNotOnOrAfter(issueInstant.plusMinutes(acceptableTimeMin));
        subConfData.setRecipient(recipient);
        subjectConf.setSubjectConfirmationData(subConfData);
        subject.getSubjectConfirmations().add(subjectConf);
        return subject;
    }

    protected Conditions buildConditions(String audienceUri, DateTime issueInstant, Integer acceptableTimeMin) {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(issueInstant);
        conditions.setNotOnOrAfter(issueInstant.plusMinutes(acceptableTimeMin));
        AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(audienceUri);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    protected AuthnStatement buildAuthnStatement(DateTime issueInstant, String loa) {
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

    protected AttributeStatement buildMinimalAttributeStatement(String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        if(givenName != null) {
            attributeStatement.getAttributes().add(buildAttribute("FirstName", "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentGivenNameType", givenName));
        }
        attributeStatement.getAttributes().add(buildAttribute("FamilyName", "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentFamilyNameType", familyName));
        attributeStatement.getAttributes().add(buildAttribute("PersonIdentifier", "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:PersonIdentifierType", personIdentifier));
        attributeStatement.getAttributes().add(buildAttribute("DateOfBirth", "http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:DateOfBirthType", dateOfBirth));
        return attributeStatement;
    }

    protected AttributeStatement buildMinimalAttributeStatementWithLegalPerson(String givenName, String familyName, String personIdentifier, String dateOfBirth, String legalName, String legalPno) {
        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        attributeStatement.getAttributes().add(buildAttribute("FirstName", "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentGivenNameType", givenName));
        attributeStatement.getAttributes().add(buildAttribute("FamilyName", "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentFamilyNameType", familyName));
        attributeStatement.getAttributes().add(buildAttribute("PersonIdentifier", "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:PersonIdentifierType", personIdentifier));
        attributeStatement.getAttributes().add(buildAttribute("DateOfBirth", "http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:DateOfBirthType", dateOfBirth));
        if(legalName != null) {
            attributeStatement.getAttributes().add(buildAttribute("LegalName", "http://eidas.europa.eu/attributes/legalperson/LegalName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:LegalNameType", legalName));
        }
        if(legalPno != null) {
            attributeStatement.getAttributes().add(buildAttribute("LegalPersonIdentifier", "http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:LegalPersonIdentifierType", legalPno));
        }
        return attributeStatement;
    }

    protected AttributeStatement buildMaximalAttributeStatementWithLegalPerson(String givenName, String familyName, String personIdentifier, String dateOfBirth, String legalName, String legalPno, String legalAddress, String vatRegistration, String taxReference, String businessCodes, String lei, String eori, String seed, String sic, String d201217EuIdendifier) {
        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        attributeStatement.getAttributes().add(buildAttribute("FirstName", "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentGivenNameType", givenName));
        attributeStatement.getAttributes().add(buildAttribute("FamilyName", "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentFamilyNameType", familyName));
        attributeStatement.getAttributes().add(buildAttribute("PersonIdentifier", "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:PersonIdentifierType", personIdentifier));
        attributeStatement.getAttributes().add(buildAttribute("DateOfBirth", "http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:DateOfBirthType", dateOfBirth));
        if(legalName != null) {
            attributeStatement.getAttributes().add(buildAttribute("LegalName", "http://eidas.europa.eu/attributes/legalperson/LegalName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:LegalNameType", legalName));
        }
        if(legalPno != null) {
            attributeStatement.getAttributes().add(buildAttribute("LegalPersonIdentifier", "http://eidas.europa.eu/attributes/legalperson/LegalPersonIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:LegalPersonIdentifierType", legalPno));
        }
        attributeStatement.getAttributes().add(buildAttribute("LegalAddress", "http://eidas.europa.eu/attributes/legalperson/LegalAddress", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:LegalAddressType", legalAddress));
        attributeStatement.getAttributes().add(buildAttribute("VATRegistration", "http://eidas.europa.eu/attributes/legalperson/VATRegistration", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:VATRegistrationType", vatRegistration));
        attributeStatement.getAttributes().add(buildAttribute("TaxReference", "http://eidas.europa.eu/attributes/legalperson/TaxReference", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:TaxReferenceType", taxReference));
        attributeStatement.getAttributes().add(buildAttribute("BusinessCodes", "http://eidas.europa.eu/attributes/legalperson/BusinessCodes", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:BusinessCodesType", businessCodes));
        attributeStatement.getAttributes().add(buildAttribute("LEI", "http://eidas.europa.eu/attributes/legalperson/LEI", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:LEIType", lei));
        attributeStatement.getAttributes().add(buildAttribute("EORI", "http://eidas.europa.eu/attributes/legalperson/EORI", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:EORIType", eori));
        attributeStatement.getAttributes().add(buildAttribute("SEED", "http://eidas.europa.eu/attributes/legalperson/SEED", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:SEEDType", seed));
        attributeStatement.getAttributes().add(buildAttribute("SIC", "http://eidas.europa.eu/attributes/legalperson/SIC", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:SICType", sic));
        attributeStatement.getAttributes().add(buildAttribute("D-2012-17-EUIdentifier", "http://eidas.europa.eu/attributes/legalperson/D-2012-17-EUIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-legal:D-2012-17-EUIdentifierType", d201217EuIdendifier));

        return attributeStatement;
    }

    protected AttributeStatement buildMaximalAttributeStatement(String givenName, String familyName, String personIdentifier, String dateOfBirth, String birthName, String birthPlace, String address, String gender) {
        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        attributeStatement.getAttributes().add(buildAttribute("FirstName", "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentGivenNameType", givenName));
        attributeStatement.getAttributes().add(buildAttribute("FamilyName", "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CurrentFamilyNameType", familyName));
        attributeStatement.getAttributes().add(buildAttribute("PersonIdentifier", "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:PersonIdentifierType", personIdentifier));
        attributeStatement.getAttributes().add(buildAttribute("DateOfBirth", "http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:DateOfBirthType", dateOfBirth));
        attributeStatement.getAttributes().add(buildAttribute("BirthName", "http://eidas.europa.eu/attributes/naturalperson/BirthName", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:BirthNameType", birthName));
        attributeStatement.getAttributes().add(buildAttribute("PlaceOfBirth", "http://eidas.europa.eu/attributes/naturalperson/BirthPlaceCvlocation", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:BirthPlaceCvlocationType", birthPlace));
        attributeStatement.getAttributes().add(buildAttribute("CurrentAddress", "http://eidas.europa.eu/attributes/naturalperson/Cvaddress", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:CvaddressType", address));
        attributeStatement.getAttributes().add(buildAttribute("Gender", "http://eidas.europa.eu/attributes/naturalperson/GenderCode", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "eidas-natural:GenderCodeType", gender));
        return attributeStatement;
    }

    protected AttributeStatement buildMinimalAttributeStatementWithFaultyNameFormat(String givenName, String familyName, String personIdentifier, String dateOfBirth) {
        AttributeStatement attributeStatement = new AttributeStatementBuilder().buildObject();
        if(givenName != null) {
            attributeStatement.getAttributes().add(buildAttribute("FirstName", "http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName", "urn:oasis:names:tc:SAML:2.0:attrname:uri", "eidas-natural:CurrentGivenNameType", givenName));
        }
        attributeStatement.getAttributes().add(buildAttribute("FamilyName", "http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName", "urn:oasis:names:tc:SAML:2.0:format:uri", "eidas-natural:CurrentFamilyNameType", familyName));
        attributeStatement.getAttributes().add(buildAttribute("PersonIdentifier", "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier", "urn:oasis:names:tc:SAML:7.0:attrname-format:uri", "eidas-natural:PersonIdentifierType", personIdentifier));
        attributeStatement.getAttributes().add(buildAttribute("DateOfBirth", "http://eidas.europa.eu/attributes/naturalperson/DateOfBirth", "SAML:2.0:attrname-format:uri", "eidas-natural:DateOfBirthType", dateOfBirth));
        return attributeStatement;
    }

    protected Attribute buildAttribute(String friendlyName, String name, String nameFormat, String xsiType, String value) {
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

    protected String getSignatureAlgorithm(Credential credential) {
        if ("RSA".equals(credential.getPublicKey().getAlgorithm())) {
            return SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
        } else {
            return SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256;
        }
    }
}
