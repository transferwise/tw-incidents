package com.transferwise.common.incidents.slack;

import static java.util.Collections.singletonList;

import com.transferwise.common.incidents.Incident;
import com.transferwise.common.incidents.IncidentNotifier;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Order(Ordered.LOWEST_PRECEDENCE - 150)
@Slf4j
public class SlackIncidentNotifier implements IncidentNotifier {

  private static final String EMOJI_RED = ":sos:";
  private static final String EMOJI_GREEN = ":white_check_mark:";
  private static final String RED = "#f44336";
  private static final String GREEN = "#4CAF50";

  @Autowired
  private SlackProperties slackProperties;

  @Autowired
  @Qualifier("Slack")
  private RestTemplate restTemplate;

  @Override
  public void triggerIncident(Incident incident) {
    callSlack(incident, true);
  }

  @Override
  public void recoverIncident(Incident incident, boolean isShutdown) {
    if (!isShutdown) {
      callSlack(incident, false);
    }
  }

  protected void callSlack(Incident incident, boolean alert) {
    // TODO incident.getMessage() is not reported

    String summary = StringUtils.isNotEmpty(slackProperties.getIncidentIdPrefix())
        ? slackProperties.getIncidentIdPrefix() + ": " + incident.getSummary() : incident.getSummary();

    boolean response = post(incident.getId(), summary, incident.getMessage(), alert, incident.getStartTime());

    if (!response) {
      throw new IllegalStateException("Reporting incident '" + incident.getId() + "' as alert=" + alert + " to Slack failed");
    }
  }

  private boolean post(String id, String summary, String message, boolean alert, ZonedDateTime startTime) {
    if (StringUtils.isEmpty(slackProperties.getUrl())) {
      log.error("Slack url is not configured. Not sending a message.");
      return false;
    }

    // In Slack, if user is having the compact view, the emoji is not visible. We don't want that the only
    // thing differentiating an open from a resolved alert is the color, so in case of alert resolved, we add
    // "resolved" to the text.
    if (!alert) {
      summary = "[RESOLVED] " + summary;
    }

    List<SlackAttachment> attachments = new ArrayList<>();
    attachments.add(SlackAttachment.builder()
        .color(alert ? RED : GREEN)
        .markdownIn(singletonList("text"))
        .timeStamp(startTime != null ? startTime.toEpochSecond() : Instant.now().getEpochSecond())
        .text(summary)
        .build());

    // If message is different from summary, it probably contains details, so let's add it.
    if (alert && StringUtils.isNotEmpty(message) && !StringUtils.equals(summary, message)) {
      attachments.add(SlackAttachment.builder()
          .markdownIn(singletonList("text"))
          .text(message)
          .build());
    }

    SlackMessage slackMessage = SlackMessage.builder()
        .username(id)
        .emoji(alert ? EMOJI_RED : EMOJI_GREEN)
        .attachments(attachments)
        .channel(slackProperties.getChannel())
        .build();

    try {
      restTemplate.postForObject(slackProperties.getUrl(), slackMessage, String.class);
    } catch (RestClientException e) {
      log.error("Can't send Slack notification", e);
      return false;
    }
    return true;
  }
}
