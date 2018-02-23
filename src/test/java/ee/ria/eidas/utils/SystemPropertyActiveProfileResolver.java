package ee.ria.eidas.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

public class SystemPropertyActiveProfileResolver implements ActiveProfilesResolver {
    public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";
    private final DefaultActiveProfilesResolver defaultActiveProfilesResolver = new DefaultActiveProfilesResolver();

    public String[] resolve(Class<?> testClass) {
        if(System.getProperties().containsKey(SPRING_PROFILES_ACTIVE)) {
            final String profiles = System.getProperty(SPRING_PROFILES_ACTIVE);
            return profiles.split("\\s*,\\s*");
        } else {
            return defaultActiveProfilesResolver.resolve(testClass);
        }
    }
}

