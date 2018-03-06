package ee.ria.eidas.config;

public class EidasTestStrings {
    //SAML response strings
    public static final String LOA_LOW = "http://eidas.europa.eu/LoA/low";
    public static final String LOA_SUBSTANTIAL = "http://eidas.europa.eu/LoA/substantial";
    public static final String LOA_HIGH = "http://eidas.europa.eu/LoA/high";
    public static final String STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

    //JSON response strings
    public static final String STATUS_CODE = "statusCode";
    public static final String STATUS_LOA = "levelOfAssurance";
    public static final String STATUS_DATE = "attributes.DateOfBirth";
    public static final String STATUS_PNO = "attributes.PersonIdendifier";
    public static final String STATUS_FAMILY = "attributes.FamilyName";
    public static final String STATUS_FIRST = "attributes.FirstName";

}
