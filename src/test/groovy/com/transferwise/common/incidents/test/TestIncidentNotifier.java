package com.transferwise.common.incidents.test;

import com.transferwise.common.incidents.Incident;
import com.transferwise.common.incidents.IncidentNotifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TestIncidentNotifier implements IncidentNotifier {
    public Map<String, Incident> openIncidents = new ConcurrentHashMap<>();

    @Override
    public void triggerIncident(Incident incident) {
        openIncidents.put(incident.getId(), incident);
    }

    @Override
    public void recoverIncident(Incident incident, boolean isShutdown) {
        openIncidents.remove(incident.getId());
    }
}
