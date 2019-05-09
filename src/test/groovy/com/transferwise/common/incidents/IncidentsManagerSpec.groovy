package com.transferwise.common.incidents

import com.transferwise.common.baseutils.clock.ClockHolder
import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class IncidentsManagerSpec extends Specification {
    def cleanup() {
        ClockHolder.setClock(Clock.systemDefaultZone())
    }

    def "incident generator is not called more often than its given interval"() {
        given:
            IncidentGenerator incidentGenerator = Mock(IncidentGenerator)
            incidentGenerator.pollingInterval >> Duration.ofSeconds(20)

            DefaultIncidentsManager incidentsManager = new DefaultIncidentsManager()
            incidentsManager.incidentGenerators = [incidentGenerator]
            incidentsManager.notifiers = []
            incidentsManager.incidentsProperties = new IncidentsProperties()

            incidentsManager.init();
            Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
            ClockHolder.setClock(clock)
        when:
            incidentsManager.gatherIncidents()
        then:
            1 * incidentGenerator.getActiveIncidents()
        when:
            incidentsManager.gatherIncidents()
        then:
            0 * incidentGenerator.getActiveIncidents()
        when:
            ClockHolder.setClock(Clock.offset(clock, Duration.ofSeconds(10)))
            incidentsManager.gatherIncidents()
        then:
            0 * incidentGenerator.getActiveIncidents()
        when:
            ClockHolder.setClock(Clock.offset(clock, Duration.ofSeconds(21)))
            incidentsManager.gatherIncidents()
        then:
            1 * incidentGenerator.getActiveIncidents()
    }
}
