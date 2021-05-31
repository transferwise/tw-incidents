package com.transferwise.common.incidents.victorops;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VictorOpsRequest {

  @JsonProperty("entity_id")
  private String entityId;
  @JsonProperty("message_type")
  private MessageType messageType;
  @JsonProperty("entity_display_name")
  private String entityDisplayName;
  @JsonProperty("state_message")
  private String stateMessage;
  @JsonProperty("state_start_time")
  private long stateStartSecondsFromEpoch;

  public enum MessageType {
    CRITICAL,
    RECOVERY,
    AKNOWLEDGMENT,
    INFO
  }
}
