package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import ee.ria.eidas.utils.OpenSAMLUtils;
import ee.ria.eidas.utils.ResponseBuilderUtils;
import io.restassured.RestAssured;
import io.restassured.path.xml.XmlPath;
import org.apache.commons.lang3.RandomStringUtils;
import org.cryptacular.codec.Base64Encoder;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.config;
import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = AuthenticationResponseIntegrationTest.class)
@Category(IntegrationTest.class)
public class AuthenticationResponseIntegrationTest extends TestsBase {

    @Value("${eidas.client.idpStartUrl}")
    private String idpStartUrl;

    @Value("${eidas.client.spProviderName}")
    private String spProviderName;

    @Value("${eidas.client.spStartUrl}")
    private String spStartUrl;

    @Value("${eidas.client.spReturnUrl}")
    private String spReturnUrl;

    //TODO: We do not receive a proper JSON response yet
    @Ignore
    @Test
    public void resp1_happyPath() {
        String base64Response = getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate");
        String loginResponse = sendSamlResponse("",base64Response );
        assertEquals("sasasa",loginResponse);
    }
    //TODO: We do not receive a proper JSON response yet
    @Ignore
    @Test
    public void resp1_authenticationFails() {
        String base64Response = getBase64SamlResponseWithErrors(getAuthenticationReqWithDefault(), "ConsentNotGiven");
        String loginResponse = sendSamlResponse("",base64Response );
        assertEquals("saas", loginResponse);
    }

    @Ignore //TODO: Inconsistency, this returns method not allowed in this endpoint (without body), others 200
    @Test
    public void resp1_headHttpMethodShouldNotReturnBody() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .head(spReturnUrl).then().log().ifValidationFails().statusCode(200).body(isEmptyOrNullString());
    }

    @Test
    public void resp1_notSupportedHttpPutMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .put(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Ignore //TODO: This return HTTP 400 in this endpoint
    @Test
    public void resp1_notSupportedHttpGetMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .get(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Test
    public void resp1_notSupportedHttpDeleteMethodShouldReturnError() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .delete(spReturnUrl).then().log().ifValidationFails().statusCode(405).body("error",Matchers.equalTo("Method Not Allowed"));
    }

    @Test //TODO: Need clarification what should be returned, currently there is inconsistency between endpoints
    public void resp1_optionsMethodShouldReturnAllowedMethods() {
        given()
                .formParam("relayState","")
                .formParam("SAMLResponse",getBase64SamlResponseMinimalAttributes(getAuthenticationReqWithDefault(), "TestFamily", "TestGiven", "TestPNO", "TestDate"))
                .contentType("application/x-www-form-urlencoded")
                .config(RestAssured.config().encoderConfig(encoderConfig().defaultContentCharset("UTF-8")))
                .when()
                .options(spReturnUrl).then().log().ifValidationFails().statusCode(200).header("Allow",Matchers.equalTo("POST, OPTIONS"));
    }
}
