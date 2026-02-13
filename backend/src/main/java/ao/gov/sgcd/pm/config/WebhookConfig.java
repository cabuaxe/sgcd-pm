package ao.gov.sgcd.pm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sgcd-pm.webhook")
@Getter
@Setter
public class WebhookConfig {

    private String secret = "";
}
