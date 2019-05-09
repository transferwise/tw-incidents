package com.transferwise.common.incidents;

/**
 * Notifies other systems about Incidents. Those other systems can be logs, VictorOps, Slack, Email and others.
 */
public interface IncidentNotifier {
    void triggerIncident(Incident incident);

    /**
     * isShutdown indicates that the "recovery" is caused by the node shutting down.
     * Some notifier may not want to report recovery due to shutdowns.
     */
    void recoverIncident(Incident incident, boolean isShutdown);
}
