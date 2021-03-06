package com.transferwise.common.incidents.victorops;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(value = "tw-incidents.victorops", ignoreUnknownFields = false)
@SuppressWarnings("checkstyle:magicnumber")
public class VictorOpsProperties {

  private Duration connectTimeout = Duration.ofSeconds(5);
  private Duration readTimeout = Duration.ofSeconds(30);
  private boolean enabled = true;
  private String incidentIdPrefix;
  private String notifyBaseUrl;
  private String apiToken;
  private String routingKey;
}
