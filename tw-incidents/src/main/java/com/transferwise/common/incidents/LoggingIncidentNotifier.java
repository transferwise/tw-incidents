package com.transferwise.common.incidents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 200)
public class LoggingIncidentNotifier implements IncidentNotifier {

  public void triggerIncident(Incident incident) {
    log.error("Incident '" + incident.getId() + "' - '" + incident.getSummary() + "' triggered: " + incident.getMessage());
  }

  @Override
  public void recoverIncident(Incident incident, boolean isShutdown) {
    if (!isShutdown) {
      log.info("Incident '" + incident.getId() + "' - '" + incident.getSummary() + "' recovered.");
    }
  }
}
