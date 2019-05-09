package com.transferwise.common.incidents.test;

import com.transferwise.common.incidents.Incident;
import com.transferwise.common.incidents.IncidentGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestIncidentGenerator implements IncidentGenerator {
    public List<Incident> activeIncidents = new ArrayList<>();

    @Override
    public List<Incident> getActiveIncidents() {
        return activeIncidents;
    }
}
