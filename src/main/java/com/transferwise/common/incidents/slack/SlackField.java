package com.transferwise.common.incidents.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class SlackField {

    @JsonProperty("title")
    public String title;
}
