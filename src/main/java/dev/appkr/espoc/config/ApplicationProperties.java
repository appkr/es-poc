package dev.appkr.espoc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

@Getter
@Setter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

  private String version = "0.0.1.SNAPSHOT";

  private CorsConfiguration cors = new CorsConfiguration();

  private Scheduler scheduler = new Scheduler();

  @Getter
  @Setter
  class Scheduler {
    private Boolean enabled;
  }
}
