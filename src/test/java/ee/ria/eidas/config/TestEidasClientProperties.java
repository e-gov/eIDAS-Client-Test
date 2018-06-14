package ee.ria.eidas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "test.client")
public class TestEidasClientProperties {

    private String targetUrl;
    private String targetSpUrl;
    private String spMetadataUrl;
    private String spStartUrl;
    private String spReturnUrl;
    private String spProviderName;
    private Integer acceptableTimeDiffMin;
    private String keystore;
    private String keystorePass;
    private String responseSigningKeyId;
    private String responseSigningKeyPass;
    private String idpUrl;
    private String idpMetadataUrl;
    private String idpStartUrl;
    private String advertizedSpReturnUrl;

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public void setTargetSpUrl(String targetSpUrl) {
        this.targetSpUrl = targetSpUrl;
    }

    public void setSpMetadataUrl(String spMetadataUrl) {
        this.spMetadataUrl = spMetadataUrl;
    }

    public String getAdvertizedSpReturnUrl() {
        return advertizedSpReturnUrl;
    }

    public void setAdvertizedSpReturnUrl(String advertizedSpReturnUrl) {
        this.advertizedSpReturnUrl = advertizedSpReturnUrl;
    }

    public void setSpStartUrl(String spStartUrl) {
        this.spStartUrl = spStartUrl;
    }

    public void setSpReturnUrl(String spReturnUrl) {
        this.spReturnUrl = spReturnUrl;
    }

    public void setSpProviderName(String spProviderName) {
        this.spProviderName = spProviderName;
    }

    public void setAcceptableTimeDiffMin(Integer acceptableTimeDiffMin) {
        this.acceptableTimeDiffMin = acceptableTimeDiffMin;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public void setResponseSigningKeyId(String responseSigningKeyId) {
        this.responseSigningKeyId = responseSigningKeyId;
    }

    public void setResponseSigningKeyPass(String responseSigningKeyPass) {
        this.responseSigningKeyPass = responseSigningKeyPass;
    }

    public void setIdpUrl(String idpUrl) {
        this.idpUrl = idpUrl;
    }

    public void setIdpMetadataUrl(String idpMetadataUrl) {
        this.idpMetadataUrl = idpMetadataUrl;
    }

    public void setIdpStartUrl(String idpStartUrl) {
        this.idpStartUrl = idpStartUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getTargetSpUrl() {
        return targetSpUrl;
    }

    public String getSpMetadataUrl() {
        return spMetadataUrl;
    }

    public String getFullSpMetadataUrl() {
        return targetSpUrl+spMetadataUrl;
    }

    public String getSpStartUrl() {
        return spStartUrl;
    }

    public String getSpReturnUrl() {
        return spReturnUrl;
    }

    public String getSpProviderName() {
        return spProviderName;
    }

    public Integer getAcceptableTimeDiffMin() {
        return acceptableTimeDiffMin;
    }

    public String getKeystore() {
        return keystore;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public String getResponseSigningKeyId() {
        return responseSigningKeyId;
    }

    public String getResponseSigningKeyPass() {
        return responseSigningKeyPass;
    }

    public String getIdpUrl() {
        return idpUrl;
    }

    public String getIdpMetadataUrl() {
        return idpMetadataUrl;
    }

    public String getIdpStartUrl() {
        return idpStartUrl;
    }

    public String getFullIdpMetadataUrl () {
        return idpUrl + idpMetadataUrl;
    }

    public String getFullIdpStartUrl () {
        return idpUrl + idpStartUrl;
    }

    public String getFullSpReturnUrl () {
        return advertizedSpReturnUrl;
    }
}
