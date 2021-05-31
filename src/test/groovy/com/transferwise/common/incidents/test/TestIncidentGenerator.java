package com.transferwise.common.incidents.test;

import com.transferwise.common.incidents.Incident;
import com.transferwise.common.incidents.IncidentGenerator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TestIncidentGenerator implements IncidentGenerator {

  public final List<Incident> activeIncidents = new ArrayList<>();

  @Override
  public List<Incident> getActiveIncidents() {
    return activeIncidents;
  }
}
