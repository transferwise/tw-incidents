package com.transferwise.common.incidents;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class IncidentsHealthIndicator implements HealthIndicator {

  @Autowired
  private DefaultIncidentsManager incidentsManager;

  @Override
  public final Health health() {
    Health.Builder builder = new Health.Builder();
    try {
      doHealthCheck(builder);
    } catch (Exception ex) {
      builder.down(ex);
    }
    return builder.build();
  }

  protected void doHealthCheck(Health.Builder builder) {
    if (incidentsManager.isHealthy()) {
      builder.up();
    } else {
      builder.down().withDetail("component", "IncidentsManager");
    }
  }
}
