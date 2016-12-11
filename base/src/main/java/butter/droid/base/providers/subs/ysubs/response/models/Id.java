package butter.droid.base.providers.subs.ysubs.response.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Id {


    private Map<String, List<Language>> language = new HashMap<>();

    @JsonAnyGetter
    public Map<String, List<Language>> getLanguage() {
        return language;
    }


    @JsonAnySetter
    public void setLanguage(String name, List<Language> value) {
        language.put(name, value);
    }

}
