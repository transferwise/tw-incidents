package com.transferwise.common.incidents.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.Builder;

@Builder
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class SlackMessage {

  @JsonProperty("username")
  public String username;

  @JsonProperty("icon_emoji")
  public String emoji;

  @JsonProperty("attachments")
  public List<SlackAttachment> attachments;

  @JsonProperty("channel")
  public String channel;
}
