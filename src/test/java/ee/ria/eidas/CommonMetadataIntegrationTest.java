package ee.ria.eidas;


import ee.ria.eidas.config.IntegrationTest;
import io.restassured.path.xml.XmlPath;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static io.restassured.path.xml.config.XmlPathConfig.xmlPathConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = CommonMetadataIntegrationTest.class)
@Category(IntegrationTest.class)
public class CommonMetadataIntegrationTest extends TestsBase {

    @Test
    public  void metap1_hasValidSignature() {
        try {
            validateMetadataSignature(getMetadataBody());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Metadata must have valid signature:  " + e.getMessage());
        }
    }

    @Test
    public void metap1_verifySamlMetadataSchema() {
        assertTrue("Metadata must be based on urn:oasis:names:tc:SAML:2.0:metadata schema", validateMetadataSchema());
    }

    @Test
    public void metap1_verifySamlMetadataIdentifier() {
        String response = getMetadataBody();
        XmlPath xmlPath = new XmlPath(response).using(xmlPathConfig().namespaceAware(false));
        assertEquals("The namespace should be expected", "urn:oasis:names:tc:SAML:2.0:metadata", xmlPath.getString("EntityDescriptor.@xmlns:md"));
    }

    @Test
    public void metap1_verifyUsedDigestAlgosInSignature() {
        XmlPath xmlPath = getMetadataBodyXML();

        List<String> digestMethods = xmlPath.getList("EntityDescriptor.Signature.SignedInfo.Reference.DigestMethod.@Algorithm");
        assertThat("One of the accepted digest algorithms must be present", digestMethods,
                anyOf(hasItem("http://www.w3.org/2001/04/xmlenc#sha512"), hasItem("http://www.w3.org/2001/04/xmlenc#sha256")));
    }

    @Test
    public void metap1_verifyUsedSignatureAlgosInSignature() {
        XmlPath xmlPath = getMetadataBodyXML();

        List<String> signingMethods = xmlPath.getList("EntityDescriptor.Signature.SignedInfo.SignatureMethod.@Algorithm");
        assertThat("One of the accepted signing algorithms must be present", signingMethods,
                anyOf(hasItem("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512"), hasItem("http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256"),
                        hasItem("http://www.w3.org/2007/05/xmldsig-more#sha512-rsa-MGF1"), hasItem("http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1")));
    }

    @Test
    public void metap2_mandatoryValuesArePresentInEntityDescriptor() {
        XmlPath xmlPath = getMetadataBodyXML();
        assertThat("The entityID must be the same as entpointUrl", xmlPath.getString("EntityDescriptor.@entityID"), endsWith(testEidasClientProperties.getSpMetadataUrl()));
    }

    @Test
    public void metap2_mandatoryValuesArePresentInSpssoDescriptor() {
        XmlPath xmlPath = getMetadataBodyXML();
        assertEquals("Authentication requests signing must be: true", "true", xmlPath.getString("EntityDescriptor.SPSSODescriptor.@AuthnRequestsSigned"));
        assertEquals("Authentication assertions signing must be: true", "true", xmlPath.getString("EntityDescriptor.SPSSODescriptor.@WantAssertionsSigned"));
        assertEquals("Enumeration must be: SAML 2.0", "urn:oasis:names:tc:SAML:2.0:protocol",
                xmlPath.getString("EntityDescriptor.SPSSODescriptor.@protocolSupportEnumeration"));
    }

    @Test
    public void metap2_certificatesArePresentInSpssoDescriptorBlock() {
        XmlPath xmlPath = getMetadataBodyXML();
        String signingCertificate = xmlPath.getString("**.findAll {it.@use == 'signing'}.KeyInfo.X509Data.X509Certificate");
        String encryptionCertificate = xmlPath.getString("**.findAll {it.@use == 'encryption'}.KeyInfo.X509Data.X509Certificate");
        assertThat("Signing certificate must be present", signingCertificate, startsWith("MII"));
        assertTrue("Signing certificate must be valid", isCertificateValid(signingCertificate));
        assertThat("Encryption certificate must be present", encryptionCertificate, startsWith("MII"));
        assertTrue("Encryption certificate must be valid", isCertificateValid(encryptionCertificate));
        assertThat("Signing and encryption certificates must be different", signingCertificate, not(equalTo(encryptionCertificate)));
    }

    @Test
    public void metap2_nameIdFormatIsCorrectInSpssoDescriptor() {
        XmlPath xmlPath = getMetadataBodyXML();
        assertEquals("Name ID format should be: unspecified", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
                xmlPath.getString("EntityDescriptor.SPSSODescriptor.NameIDFormat"));
    }

    @Test
    public void metap2_mandatoryValuesArePresentInAssertionConsumerService() {
        XmlPath xmlPath = getMetadataBodyXML();
        assertEquals("The binding must be: HTTP-POST", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
                xmlPath.getString("EntityDescriptor.SPSSODescriptor.AssertionConsumerService.@Binding"));
        assertThat("The Location should indicate correct return url",
                xmlPath.getString("EntityDescriptor.SPSSODescriptor.AssertionConsumerService.@Location"), endsWith( testEidasClientProperties.getFullSpReturnUrl()));
    }
}
