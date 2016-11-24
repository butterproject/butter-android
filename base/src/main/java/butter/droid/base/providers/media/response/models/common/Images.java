package butter.droid.base.providers.media.response.models.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class Images {

    @JsonProperty("poster")
    private String poster;
    @JsonProperty("fanart")
    private String fanart;
    @JsonProperty("banner")
    private String banner;

    /**
     * @return The poster
     */
    @JsonProperty("poster")
    public String getPoster() {
        return poster;
    }

    /**
     * @param poster The poster
     */
    @JsonProperty("poster")
    public void setPoster(String poster) {
        this.poster = poster;
    }

    /**
     * @return The fanart
     */
    @JsonProperty("fanart")
    public String getFanart() {
        return fanart;
    }

    /**
     * @param fanart The fanart
     */
    @JsonProperty("fanart")
    public void setFanart(String fanart) {
        this.fanart = fanart;
    }

    /**
     * @return The banner
     */
    @JsonProperty("banner")
    public String getBanner() {
        return banner;
    }

    /**
     * @param banner The banner
     */
    @JsonProperty("banner")
    public void setBanner(String banner) {
        this.banner = banner;
    }

}
