package com.transferwise.common.incidents;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Incident {
    /**
     * Globally unique id for this incident. When incident with particular id is not present anymore it will be automatically deregistered.
     * <p>
     * Can be null, in this case the core will populate it with GUID.
     */
    private String id;
    private String summary;
    private String message;
    /**
     * Can be null, in this case the core will set a value for it.
     */
    private ZonedDateTime startTime;
    /**
     * Some incidents may need special routing/notification. If this is left null, a common routing will be used.
     */
    private String routingKey;

    /**
     * Could be used for a special notifier in the future.
     */
    private Map<String, String> metaData;
}
