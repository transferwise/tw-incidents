# Tw Incidents

Configuration options are in `IncidentsProperties`.

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

Configuration options are in `VictorOpsProperties`.

VictorOps example:
```
tw-boot:
  incidents:
    victorops:
      enabled: true
      notify-base-url: https://alert.victorops.com/integrations/generic/20131114/alert/
      api-token: '{cipher}MagicNumber123'
      routing-key: currencies
      incident-id-prefix: '${spring.application.name}/${lhv-service.node-id}/'
```

Notes:
1. The `notify-base-url` is not just an example. This is the url that all the services should use.
2. You can get the `api-token` [here](https://portal.victorops.com/dash/transferwise/#/advanced/rest) and encrypt it using
[config-server](https://github.com/transferwise/config-server#encrypting-sensitive-data).

Slack example:
```
tw-boot:
  incidents:
    slack:
      enabled: true
      url=http://hooks.slack.com/...
      incidentIdPrefix=test-service
```

During testing, it might be useful to set the `channel` property to your Slack `@username`, so that messages come to you as Slackbot ones.
