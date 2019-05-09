package com.transferwise.common.incidents.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
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
