package butter.droid.base.providers.subs.ysubs.response.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subs {

    private Map<String, Id> ids = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Id> getIds() {
        return ids;
    }

    @JsonAnySetter
    public void setIds(String name, Id value) {
        ids.put(name, value);
    }
}
