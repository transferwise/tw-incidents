package com.transferwise.common.incidents.test;

import com.transferwise.common.incidents.Incident;
import com.transferwise.common.incidents.IncidentNotifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order
public class TestIncidentNotifier implements IncidentNotifier {

  public final Map<String, Incident> openIncidents = new ConcurrentHashMap<>();

  @Override
  public void triggerIncident(Incident incident) {
    openIncidents.put(incident.getId(), incident);
  }

  @Override
  public void recoverIncident(Incident incident, boolean isShutdown) {
    openIncidents.remove(incident.getId());
  }
}
