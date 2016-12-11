package butter.droid.base.providers.media.response.models.movies;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Torrents {

    private Map<String, Language> languages = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Language> getLanguages() {
        return languages;
    }

    @JsonAnySetter
    public void setLanguages(String name, Language value) {
        languages.put(name, value);
    }
}