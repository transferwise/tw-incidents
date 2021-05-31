package com.transferwise.common.incidents.victorops;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transferwise.common.baseutils.ExceptionUtils;
import com.transferwise.common.incidents.Incident;
import com.transferwise.common.incidents.IncidentNotifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

@Order(Ordered.LOWEST_PRECEDENCE - 100)
@Slf4j
public class VictorOpsIncidentNotifier implements IncidentNotifier {

  @Autowired
  private VictorOpsProperties victorOpsProperties;

  @Autowired
  @Qualifier("VictorOps")
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void triggerIncident(Incident incident) {
    callVictorOps(VictorOpsRequest.MessageType.CRITICAL, incident);
  }

  @Override
  public void recoverIncident(Incident incident, boolean isShutdown) {
    callVictorOps(VictorOpsRequest.MessageType.RECOVERY, incident);
  }

  protected void callVictorOps(VictorOpsRequest.MessageType messageType, Incident incident) {
    VictorOpsRequest request = new VictorOpsRequest()
        .setEntityId(StringUtils.trimToEmpty(victorOpsProperties.getIncidentIdPrefix()) + incident.getId())
        .setEntityDisplayName(victorOpsProperties.getIncidentIdPrefix() + ":" + incident.getSummary())
        .setMessageType(messageType)
        .setStateMessage(incident.getMessage())
        .setStateStartSecondsFromEpoch(incident.getStartTime().toEpochSecond());

    String responseSt = restTemplate.postForObject(getUrl(incident), request, String.class);
    VictorOpsResponse response = ExceptionUtils.doUnchecked(() -> objectMapper.readValue(responseSt, VictorOpsResponse.class));

    if (!VictorOpsResponse.Result.SUCCESS.name().equalsIgnoreCase(response.getResult())) {
      throw new IllegalStateException(
          "Registering incident '" + incident.getId() + "' as " + messageType + " in VictorOps failed: " + response.getResult());
    }
  }

  protected String getUrl(Incident incident) {
    String routingKey = incident.getRoutingKey() == null ? victorOpsProperties.getRoutingKey() : incident.getRoutingKey();

    return victorOpsProperties.getNotifyBaseUrl() + victorOpsProperties.getApiToken() + "/" + routingKey;
  }
}
