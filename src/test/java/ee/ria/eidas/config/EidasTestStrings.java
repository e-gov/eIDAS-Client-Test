package ee.ria.eidas.config;

public class EidasTestStrings {
    //SAML response strings
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

    //JSON response error strings
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_ERROR_MESSAGE = "message";

    //JSON response error messages
    public static final String BAD_SAML = "Bad SAML message";

    //Test data strings
    public static final String DEFATTR_FIRST = "Test-FirstName";
    public static final String DEFATTR_FAMILY = "Test-FamilyName";
    public static final String DEFATTR_PNO = "EE/CA/30011092212";
    public static final String DEFATTR_DATE = "1900-11-09";
    public static final String DEFATTR_BIRTH_NAME = "Test-Birth-First-Last-Name";
    public static final String DEFATTR_BIRTH_PLACE = "Country";
    public static final String DEFATTR_ADDR = "Street 1, Flat 3, Village 2, Country7";
    public static final String DEFATTR_GENDER = "Male";
    public static final String DEFATTR_LEGAL_NAME = "Good Company a/s";
    public static final String DEFATTR_LEGAL_PNO = "292938483902";
}
