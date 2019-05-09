package com.transferwise.common.incidents;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(value = "tw-incidents.core", ignoreUnknownFields = false)
@Data
@SuppressWarnings("checkstyle:magicnumber")
public class IncidentsProperties {
    private Duration incidentsCheckInterval = Duration.ofSeconds(10);
    private boolean enabled = true;
}
