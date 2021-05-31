# Tw Incidents

![Apache 2](https://img.shields.io/hexpm/l/plug.svg)
![Java 11](https://img.shields.io/badge/Java-11-blue.svg)
![Maven Central](https://badgen.net/maven/v/maven-central/com.transferwise.common/tw-incidents)

Allows applications to raise incidents and recover from them.

Configuration options are in [IncidentsProperties](src/main/java/com/transferwise/common/incidents/IncidentsProperties.java)

Engineers have to provide implementations for `IncidentGenerator`s, which are responsible for gathering active incidents.

Optionally engineers can also provide implementations for `IncidentNotifier`s, which could broadcast incidents activations and recoveries to slack, email, logs etc.

An example for `IncidentGenerator`:
```
public class StuckTasksIncidentGenerator implements IncidentGenerator {
    @Autowired
    private ITaskDao taskDao;

    private Incident incident;

    @Override
    public List<Incident> getActiveIncidents() {
        int cnt = taskDao.getTasksCountInStatus(TaskStatus.ERROR);

        if (cnt > 0) {
            if (incident == null) { // If there already is active incident, we would use that. We would not want to spam VictorOps with new incident per polling interval.
                incident = new Incident()
                    .setMessage("" + cnt + " tasks in ERROR state.")
                    .setSummary("" + cnt + " tasks in ERROR state.");
            }
        } else {
            incident = null;
        }

        return Arrays.asList(incident);
    }

    @Override
    public Duration getPollingInterval(){
        // Getting count from database may be somewhat slow, so we provide larger getActiveIncidents call interval.
        return Duration.ofMinutes(5);
    }
}
```

## VictorOps

Configuration options are in [VictorOpsProperties](src/main/java/com/transferwise/common/incidents/victorops/VictorOpsProperties.java).

VictorOps example:
```yaml
tw-incidents:
  victorops:
    enabled: true
    notify-base-url: https://alert.victorops.com/integrations/generic/20131114/alert/
    api-token: '{cipher}MagicNumber123'
    routing-key: currencies
    incident-id-prefix: '${spring.application.name}/${partner-service.node-id}/'
```

Notes:
1. The `notify-base-url` is not just an example. This is the url that all the services should use.
2. You can get the `api-token` [here](https://portal.victorops.com/dash/transferwise/#/advanced/rest)

## Slack

Configuration options are in [SlackProperties](src/main/java/com/transferwise/common/incidents/slack/SlackProperties.java)

Slack example:
```yaml
tw-incidents:
  slack:
    enabled: true
    url: http://hooks.slack.com/...
    incidentIdPrefix: test-service
```

During testing, it might be useful to set the `channel` property to your Slack `@username`, so that messages come to you as Slackbot ones.

## License
Copyright 2021 TransferWise Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
