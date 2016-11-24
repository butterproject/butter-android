package butter.droid.base.providers.media.response.models.movies;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Language {

    public Map<String, Quality> qualities = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Quality> getQualities() {
        return qualities;
    }

    @JsonAnySetter
    public void setQualities(String name, Quality value) {
        qualities.put(name, value);

    }
}
