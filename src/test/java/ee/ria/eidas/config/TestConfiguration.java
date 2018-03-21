package ee.ria.eidas.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        TestEidasClientProperties.class
})
public class TestConfiguration {
}
