package ee.ria.eidas.config;

public class EidasTestStrings {
    //eIDAS client API strings
    public static final String COUNTRY = "Country";
    public static final String LOA = "LoA";
    public static final String RELAY_STATE = "RelayState";
    public static final String SP_TYPE = "SPType";
    public static final String REQUESTER_ID = "RequesterID";
    public static final String ATTRIBUTES = "Attributes";
    public static final String SAML_REQUEST = "SAMLRequest";
    public static final String SAML_RESPONSE = "SAMLResponse";

    //Default test params
    public static final String DEF_COUNTRY = "CA";
    public static final String SP_TYPE_PUBLIC = "public";
    public static final String SP_TYPE_PRIVATE = "private";
    public static final String REQUESTER_ID_VALUE = "urn:uuid:132a40a2-d4f2-11ec-a693-2f14f32004a4";

    //SAML response strings
    public static final String LOA_NON_NOTIFIED = "http://non.eidas.eu/NotNotified/LoA/1";
    public static final String LOA_LOW = "http://eidas.europa.eu/LoA/low";
    public static final String LOA_SUBSTANTIAL = "http://eidas.europa.eu/LoA/substantial";
    public static final String LOA_HIGH = "http://eidas.europa.eu/LoA/high";
    public static final String STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
    public static final String ISSUER_FORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
    public static final String SUBJECT_CONFIRMATION_METHOD_SENDER_VOUCHES = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";
    public static final String SUBJECT_CONFIRMATION_METHOD_BEARER = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
    public static final String NAME_ID_FORMAT_UNSPECIFIED = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    public static final String NAME_ID_FORMAT_PERSISTENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
    public static final String NAME_ID_FORMAT_TRANSIENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
    public static final String NAME_ID_FORMAT_ENCRYPTED = "urn:oasis:names:tc:SAML:2.0:nameid-format:encrypted";

    //JSON response strings
    public static final String STATUS_CODE = "statusCode";
    public static final String STATUS_LOA = "levelOfAssurance";
    public static final String STATUS_DATE = "attributes.DateOfBirth";
    public static final String STATUS_PNO = "attributes.PersonIdentifier";
    public static final String STATUS_FAMILY = "attributes.FamilyName";
    public static final String STATUS_FIRST = "attributes.FirstName";
    public static final String STATUS_ADDR = "attributes.CurrentAddress";
    public static final String STATUS_GENDER = "attributes.Gender";
    public static final String STATUS_BIRTH_NAME = "attributes.BirthName";
    public static final String STATUS_BIRTH_PLACE = "attributes.PlaceOfBirth";
    public static final String STATUS_LEGAL_NAME = "attributes.LegalName";
    public static final String STATUS_LEGAL_PNO = "attributes.LegalPersonIdentifier";
    public static final String STATUS_LEGAL_ADDRESS = "attributes.LegalAddress";
    public static final String STATUS_LEGAL_VAT = "attributes.VATRegistration";
    public static final String STATUS_LEGAL_TAX = "attributes.TaxReference";
    public static final String STATUS_LEGAL_LEI = "attributes.LEI";
    public static final String STATUS_LEGAL_EORI = "attributes.EORI";
    public static final String STATUS_LEGAL_SEED = "attributes.SEED";
    public static final String STATUS_LEGAL_SIC = "attributes.SIC";
    public static final String STATUS_LEGAL_D2012 = "attributes.D-2012-17-EUIdentifier";

    //JSON response error strings
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_ERROR_MESSAGE = "message";

    //JSON response error messages
    public static final String BAD_SAML = "Bad SAML message";
    public static final String BAD_REQUEST = "Bad Request";

    //XML response strings
    public static final String XML_LOA =  "AuthnRequest.RequestedAuthnContext.AuthnContextClassRef";

    //Test data strings
    public static final String DEFATTR_FIRST = "Test-FirstName";
    public static final String DEFATTR_FAMILY = "Test-FamilyName";
    public static final String DEFATTR_PNO = "EE/CA/30011092212";
    public static final String DEFATTR_DATE = "1900-11-09";
    public static final String DEFATTR_BIRTH_NAME = "Test-Birth-First-Last-Name";
    public static final String DEFATTR_BIRTH_PLACE = "Country";
    public static final String DEFATTR_ADDR = "PGVpZGFzOkxvY2F0b3JEZXNpZ25hdG9yPjEyNTwvZWlkYXM6TG9jYXRvckRlc2lnbmF0b3I+DQo8ZWlkYXM6VGhvcm91Z2hmYXJlPktpbmdzd2F5PC9laWRhczpUaG9yb3VnaGZhcmU+DQo8ZWlkYXM6UG9zdE5hbWU+TG9uZG9uPC9laWRhczpQb3N0TmFtZT4gDQo8ZWlkYXM6UG9zdENvZGU+V0MyQiA2Tkg8L2VpZGFzOlBvc3Rjb2RlPg==";
    public static final String DEFATTR_GENDER = "Male";
    public static final String DEFATTR_LEGAL_NAME = "Good Company a/s";
    public static final String DEFATTR_LEGAL_PNO = "292938483902";
    public static final String DEFATTR_LEGAL_ADDRESS = "PGVpZGFzOkxvY2F0b3JEZXNpZ25hdG9yPjEyNTwvZWlkYXM6TG9jYXRvckRlc2lnbmF0b3I+DQo8ZWlkYXM6VGhvcm91Z2hmYXJlPktpbmdzd2F5PC9laWRhczpUaG9yb3VnaGZhcmU+DQo8ZWlkYXM6UG9zdE5hbWU+TG9uZG9uPC9laWRhczpQb3N0TmFtZT4gDQo8ZWlkYXM6UG9zdENvZGU+V0MyQiA2Tkg8L2VpZGFzOlBvc3Rjb2RlPg==";
    public static final String DEFATTR_LEGAL_VATREGISTRATION = "GB 730 7577 27";
    public static final String DEFATTR_LEGAL_TAXREFERENCE = "ABZ1230789";
    public static final String DEFATTR_LEGAL_BUSINESSCODES = "ABZ1230789";
    public static final String DEFATTR_LEGAL_D201217EUIDENTIFIER = "GB 755 267 1243";
    public static final String DEFATTR_LEGAL_LEI = "ES123567983568437254K";
    public static final String DEFATTR_LEGAL_EORI = "GB123456789000";
    public static final String DEFATTR_LEGAL_SEED = "GB 00000987ABC";
    public static final String DEFATTR_LEGAL_SIC = "3730";
}
