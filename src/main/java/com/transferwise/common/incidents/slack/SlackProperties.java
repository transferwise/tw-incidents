package com.transferwise.common.incidents.slack;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(value = "tw-incidents.slack", ignoreUnknownFields = false)
@SuppressWarnings("checkstyle:magicnumber")
public class SlackProperties {
    private boolean enabled = false;

    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(30);

    private String url;
    private String channel;  // Optional
    private String incidentIdPrefix = "";
}
