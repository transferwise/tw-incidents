package com.transferwise.common.incidents.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.Builder;

@Builder
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class SlackAttachment {

  @JsonProperty("color")
  public String color;

  @JsonProperty("text")
  public String text;

  @JsonProperty("footer")
  public String footer;

  @JsonProperty("fields")
  public List<SlackField> fields;

  @JsonProperty("mrkdwn_in")
  public List<String> markdownIn;

  @JsonProperty("ts")
  public long timeStamp;
}
