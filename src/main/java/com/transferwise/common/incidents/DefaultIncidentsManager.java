package com.transferwise.common.incidents;

import com.transferwise.common.baseutils.ExceptionUtils;
import com.transferwise.common.baseutils.clock.ClockHolder;
import com.transferwise.common.gracefulshutdown.GracefulShutdownStrategy;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class DefaultIncidentsManager implements IncidentsManager, GracefulShutdownStrategy {
    @Autowired
    private List<IncidentNotifier> notifiers;
    @Autowired
    private List<IncidentGenerator> incidentGenerators;
    @Autowired
    private IncidentsProperties incidentsProperties;

    private Map<IncidentGenerator, IncidentGeneratorState> incidentGeneratorStates;

    private volatile boolean healthy = true;

    private ScheduledExecutorService scheduledExecutorService;

    private Lock lock;

    private Thread onShutdownIncidentsReleasingThread;

    @PostConstruct
    public void init() {
        incidentGeneratorStates = new ConcurrentHashMap<>();
        for (IncidentGenerator incidentGenerator : incidentGenerators) {
            log.info("Registering incident generator '" + incidentGenerator + "'.");
            incidentGeneratorStates.put(incidentGenerator, new IncidentGeneratorState());
        }
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        lock = new ReentrantLock();
    }

    @Override
    public boolean isHealthy() {
        return healthy;
    }

    protected void gatherIncidents() {
        lock.lock();
        try {
            for (IncidentGenerator incidentGenerator : incidentGenerators) {
                IncidentGeneratorState incidentGeneratorState = incidentGeneratorStates.get(incidentGenerator);

                Duration pollingInterval = incidentGenerator.getPollingInterval();
                if (pollingInterval != null) {
                    ZonedDateTime lastQueryTime = incidentGeneratorState.getLastQueryTime();
                    if (lastQueryTime != null && lastQueryTime.plus(pollingInterval).isAfter(ZonedDateTime.now(ClockHolder.getClock()))) {
                        continue;
                    }
                }

                incidentGeneratorState.setLastQueryTime(ZonedDateTime.now(ClockHolder.getClock()));

                Map<String, Incident> newIncidents = new HashMap<>();
                Map<String, Incident> activeIncidents = incidentGeneratorState.getActiveIncidents();

                try {
                    getActiveIncidents(incidentGenerator).forEach((incident) -> newIncidents.put(incident.getId(), incident));
                    incidentGeneratorState.setHealthy(true);
                } catch (Throwable t) {
                    incidentGeneratorState.setHealthy(false);
                    log.error(t.getMessage(), t);
                    continue;
                }

                newIncidents.forEach((id, incident) -> {
                    if (!activeIncidents.containsKey(id)) {
                        triggerIncident(incident);
                        activeIncidents.put(id, incident);
                    }
                });

                activeIncidents.forEach((id, incident) -> {
                    if (!newIncidents.containsKey(id)) {
                        recoverIncident(incident, false);
                        activeIncidents.remove(id);
                    }
                });
            }
        } finally {
            lock.unlock();
        }
    }

    protected List<Incident> getActiveIncidents(IncidentGenerator incidentGenerator) {
        List<Incident> gIncidents = incidentGenerator.getActiveIncidents();

        return (gIncidents == null ? Stream.<Incident>empty() : gIncidents.stream()).filter(Objects::nonNull).peek((incident) -> {
            if (incident.getStartTime() == null) {
                incident.setStartTime(ZonedDateTime.now(ClockHolder.getClock()));
            }
            if (incident.getId() == null) {
                incident.setId(UUID.randomUUID().toString());
            }
        }).collect(Collectors.toList());
    }

    protected void releaseActiveIncidents() {
        for (IncidentGenerator incidentGenerator : incidentGenerators) {
            IncidentGeneratorState incidentGeneratorState = incidentGeneratorStates.get(incidentGenerator);

            Map<String, Incident> activeIncidents = incidentGeneratorState.getActiveIncidents();
            activeIncidents.forEach((id, incident) -> {
                recoverIncident(incident, true);
                activeIncidents.remove(id);
            });
        }
    }

    protected void triggerIncident(Incident incident) {
        notifiers.forEach((notifier) -> notifier.triggerIncident(incident));
    }

    protected void recoverIncident(Incident incident, boolean isShutdown) {
        notifiers.forEach((notifier) -> notifier.recoverIncident(incident, isShutdown));
    }

    @Override
    public void applicationStarted() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                gatherIncidents();
                lock.lock();
                try {
                    healthy = incidentGeneratorStates.values().stream().allMatch(IncidentGeneratorState::isHealthy);
                } finally {
                    lock.unlock();
                }

            } catch (Throwable t) {
                healthy = false;
                log.error(t.getMessage(), t);
            }
        }, incidentsProperties.getIncidentsCheckInterval().toMillis(), incidentsProperties.getIncidentsCheckInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void prepareForShutdown() {
        scheduledExecutorService.shutdown();

        onShutdownIncidentsReleasingThread = new Thread(() -> {
            ExceptionUtils.doUnchecked(() -> {
                scheduledExecutorService.awaitTermination(incidentsProperties.getIncidentsCheckInterval().toMillis(), TimeUnit.MILLISECONDS);
            });
            lock.lock();
            try {
                // We release active incidents on shutdown. If they are still relevant, they will reappear after restart, but already with new ids.
                releaseActiveIncidents();
            } finally {
                lock.unlock();
            }
        });
        onShutdownIncidentsReleasingThread.start();
    }

    @Override
    public boolean canShutdown() {
        return scheduledExecutorService.isTerminated() && (onShutdownIncidentsReleasingThread == null || !onShutdownIncidentsReleasingThread.isAlive());
    }

    @Data
    @Accessors(chain = true)
    private static class IncidentGeneratorState {
        private Map<String, Incident> activeIncidents = new ConcurrentHashMap<>();
        private ZonedDateTime lastQueryTime;
        private boolean healthy = true;
    }
}
