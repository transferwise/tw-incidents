package com.transferwise.common.incidents;

import com.transferwise.common.incidents.slack.SlackIncidentNotifier;
import com.transferwise.common.incidents.slack.SlackProperties;
import com.transferwise.common.incidents.victorops.VictorOpsIncidentNotifier;
import com.transferwise.common.incidents.victorops.VictorOpsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(value = "tw-incidents.enabled", matchIfMissing = true)
@EnableConfigurationProperties({IncidentsProperties.class, VictorOpsProperties.class, SlackProperties.class})
public class IncidentsAutoConfiguration {
    @Autowired
    private VictorOpsProperties victorOpsProperties;
    @Autowired
    private SlackProperties slackProperties;

    @Bean
    public DefaultIncidentsManager incidentsManager() {
        return new DefaultIncidentsManager();
    }

    @Bean
    @Qualifier("VictorOps")
    @ConditionalOnProperty(value = "tw-incidents.victorops.enabled")
    public RestTemplate victorOpsRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.setConnectTimeout(victorOpsProperties.getConnectTimeout())
                .setReadTimeout(victorOpsProperties.getReadTimeout()).build();
    }

    @Bean
    @ConditionalOnProperty(value = "tw-incidents.victorops.enabled")
    public VictorOpsIncidentNotifier victorOpsIncidentNotifier() {
        return new VictorOpsIncidentNotifier();
    }


    @Bean
    @Qualifier("Slack")
    @ConditionalOnProperty(value = "tw-incidents.slack.enabled")
    public RestTemplate slackRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.setConnectTimeout(slackProperties.getConnectTimeout())
                .setReadTimeout(slackProperties.getReadTimeout()).build();
    }

    @Bean
    @ConditionalOnProperty(value = "tw-incidents.slack.enabled")
    public SlackIncidentNotifier slackIncidentNotifier() {
        return new SlackIncidentNotifier();
    }


    @Bean
    public LoggingIncidentNotifier loggingIncidentNotifier() {
        return new LoggingIncidentNotifier();
    }

}
