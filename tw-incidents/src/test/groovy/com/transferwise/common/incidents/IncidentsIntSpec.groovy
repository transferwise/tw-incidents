package com.transferwise.common.incidents

import com.transferwise.common.baseutils.clock.ClockHolder
import com.transferwise.common.incidents.test.TestIncidentGenerator
import com.transferwise.common.incidents.test.TestIncidentNotifier
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

import static org.awaitility.Awaitility.await
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*

@ActiveProfiles(["test"])
@SpringBootTest(classes = [TestApplication])
class IncidentsIntSpec extends Specification {
    @Autowired
    private TestIncidentGenerator testIncidentGenerator

    @Autowired
    @Qualifier("VictorOps")
    private RestTemplate victorOpsRestTemplate
    @Autowired
    @Qualifier("Slack")
    private RestTemplate slackRestTemplate

    @Autowired
    private TestIncidentNotifier testIncidentNotifier

    private ZonedDateTime now

    def setup() {
        now = ZonedDateTime.parse("2007-12-03T10:15:30+01:00")
        ClockHolder.setClock(Clock.fixed(now.toInstant(), ZoneId.systemDefault()))
    }

    def cleanup() {
        testIncidentGenerator.getActiveIncidents().clear()
        testIncidentNotifier.openIncidents.clear()
    }

    def "notifications work"() {
        given:
        Incident incident = new Incident()
                .setId("test/errors")
                .setSummary("Hello World!")
                .setMessage("First there was a World. Then came Hello.")

        MockRestServiceServer victorOpsMockServer = MockRestServiceServer.bindTo(victorOpsRestTemplate).build()

        victorOpsMockServer.expect(requestTo("http://victorops.non-existing.tw.ee/alert/myToken/myRoutingKey"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"entity_id":"test-service/test-node/test/errors","message_type":"CRITICAL","entity_display_name":"test-service/test-node/:Hello World!","state_message":"First there was a World. Then came Hello.","state_start_time":${
                    now.toEpochSecond()
                }}""")).andRespond(MockRestResponseCreators.withSuccess("""{
"result":"success",
"entity_id":"test-service/test-node/test/errors"
}""", MediaType.APPLICATION_JSON_UTF8))

        victorOpsMockServer.expect(requestTo("http://victorops.non-existing.tw.ee/alert/myToken/myRoutingKey"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"entity_id":"test-service/test-node/test/errors","message_type":"RECOVERY","entity_display_name":"test-service/test-node/:Hello World!","state_message":"First there was a World. Then came Hello.","state_start_time":${
                    now.toEpochSecond()
                }}""")).andRespond(MockRestResponseCreators.withSuccess("""{
"result":"success",
"entity_id":"test-service/test-node/test/errors"
}""", MediaType.APPLICATION_JSON_UTF8))

        mockSlack()

        when:
        testIncidentGenerator.activeIncidents.add(incident)
        await().until { testIncidentNotifier.openIncidents.containsKey(incident.getId()) }
        then:
        1 == 1
        when:
        testIncidentGenerator.activeIncidents.remove(incident)
        await().until { !testIncidentNotifier.openIncidents.containsKey(incident.getId()) }
        victorOpsMockServer.verify()
        then:
        1 == 1
    }

    def "specific victorops routing key is provided for incident with specified routing key"() {
        given:
        Incident incident = new Incident()
                .setId("test/errors")
                .setSummary("Hello World!")
                .setMessage("First there was a World. Then came Hello.")
                .setRoutingKey("MyFancyRoutingKey")

        MockRestServiceServer victorOpsMockServer = MockRestServiceServer.bindTo(victorOpsRestTemplate).build()

        victorOpsMockServer.expect(requestTo("http://victorops.non-existing.tw.ee/alert/myToken/MyFancyRoutingKey"))
                .andRespond(MockRestResponseCreators.withSuccess("""{
"result":"success",
"entity_id":"test-service/test-node/test/errors"
}""", MediaType.APPLICATION_JSON_UTF8))

        victorOpsMockServer.expect(requestTo("http://victorops.non-existing.tw.ee/alert/myToken/MyFancyRoutingKey"))
                .andRespond(MockRestResponseCreators.withSuccess("""{
"result":"success",
"entity_id":"test-service/test-node/test/errors"
}""", MediaType.APPLICATION_JSON_UTF8))

        mockSlack()

        when:
        testIncidentGenerator.activeIncidents.add(incident)
        await().until { testIncidentNotifier.openIncidents.containsKey(incident.getId()) }
        then:
        1 == 1
        when:
        testIncidentGenerator.activeIncidents.remove(incident)
        await().until { !testIncidentNotifier.openIncidents.containsKey(incident.getId()) }
        victorOpsMockServer.verify()
        then:
        1 == 1
    }

    private void mockSlack() {
        MockRestServiceServer slackMockServer = MockRestServiceServer.bindTo(slackRestTemplate).build()

        slackMockServer.expect(requestTo("http://slack.non-existing.tw.ee/alert/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"username":"test/errors","icon_emoji":":sos:","attachments":[{"color":"#f44336","text":"test-service: Hello World!","footer":null,"fields":null,"mrkdwn_in":["text"],"ts":${now.toEpochSecond()}},{"color":null,"text":"First there was a World. Then came Hello.","footer":null,"fields":null,"mrkdwn_in":["text"],"ts":0}],"channel":null}"""))
                .andRespond(MockRestResponseCreators.withSuccess())
        slackMockServer.expect(requestTo("http://slack.non-existing.tw.ee/alert/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("""{"username":"test/errors","icon_emoji":":white_check_mark:","attachments":[{"color":"#4CAF50","text":"[RESOLVED] test-service: Hello World!","footer":null,"fields":null,"mrkdwn_in":["text"],"ts":${now.toEpochSecond()}}],"channel":null}"""))
                .andRespond(MockRestResponseCreators.withSuccess())
    }
}
