package ee.ria.eidas.config;

public class EidasTestStrings {
    //SAML response strings
    public static final String LOA_LOW = "http://eidas.europa.eu/LoA/low";
    public static final String LOA_SUBSTANTIAL = "http://eidas.europa.eu/LoA/substantial";
    public static final String LOA_HIGH = "http://eidas.europa.eu/LoA/high";
    public static final String STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
    public static final String ISSUER_FORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

    //JSON response strings
    public static final String STATUS_CODE = "statusCode";
    public static final String STATUS_LOA = "levelOfAssurance";
    public static final String STATUS_DATE = "attributes.DateOfBirth";
    public static final String STATUS_PNO = "attributes.PersonIdendifier";
    public static final String STATUS_FAMILY = "attributes.FamilyName";
    public static final String STATUS_FIRST = "attributes.FirstName";
    public static final String STATUS_ADDR = "attributes.CurrentAddress";
    public static final String STATUS_GENDER = "attributes.Gender";
    public static final String STATUS_BIRTH_NAME = "attributes.BirthName";
    public static final String STATUS_BIRTH_PLACE = "attributes.PlaceOfBirth";

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
}
