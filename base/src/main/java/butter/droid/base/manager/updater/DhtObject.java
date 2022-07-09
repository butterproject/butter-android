package butter.droid.base.manager.updater;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DhtObject {
    @JsonProperty("server")
    private String server;

    public String[] getServers()
    {
        return server.split(",");
    }
}
