package com.transferwise.common.incidents;

import java.time.Duration;
import java.util.List;

public interface IncidentGenerator {
    /**
     * For convenient code, the response list can contain nulls, which in turn will be ignored.
     * Can return null instead of empty list.
     */
    List<Incident> getActiveIncidents();

    /**
     * Returns interval for how often the getActiveIncidents method will be called.
     * <p>
     * Engineer may want to override this method when gathering of particular incidents is (very) expensive.
     * <p>
     * If return null, a generic default (usually 10s) will be used.
     */
    default Duration getPollingInterval() {
        return null;
    }
}
