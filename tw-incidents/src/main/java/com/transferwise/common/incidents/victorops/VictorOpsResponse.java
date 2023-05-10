package com.transferwise.common.incidents.victorops;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VictorOpsResponse {

  @JsonProperty("entity_id")
  private String entityId;
  @JsonProperty("result")
  private String result;

  public enum Result {
    SUCCESS, FAILURE
  }
}
