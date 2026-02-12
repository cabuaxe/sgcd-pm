package ao.gov.sgcd.pm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sgcd-pm")
public class UserProperties {

    private List<UserCredential> users;

    @Data
    public static class UserCredential {
        private String username;
        private String passwordHash;
        private String role;
    }
}
