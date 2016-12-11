package butter.droid.base.providers.subs.ysubs.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import butter.droid.base.providers.subs.ysubs.response.models.Subs;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "success",
        "lastModified",
        "subtitles",
        "subs"
})
public class YSubsResponse {

    @JsonProperty("success")
    private boolean success;
    @JsonProperty("lastModified")
    private int lastModified;
    @JsonProperty("subtitles")
    private int subtitles;
    @JsonProperty("subs")
    private Subs subs;

    /**
     * @return The success
     */
    @JsonProperty("success")
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success The success
     */
    @JsonProperty("success")
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return The lastModified
     */
    @JsonProperty("lastModified")
    public int getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified The lastModified
     */
    @JsonProperty("lastModified")
    public void setLastModified(int lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return The subtitles
     */
    @JsonProperty("subtitles")
    public int getSubtitles() {
        return subtitles;
    }

    /**
     * @param subtitles The subtitles
     */
    @JsonProperty("subtitles")
    public void setSubtitles(int subtitles) {
        this.subtitles = subtitles;
    }

    /**
     * @return The subs
     */
    @JsonProperty("subs")
    public Subs getSubs() {
        return subs;
    }

    /**
     * @param subs The subs
     */
    @JsonProperty("subs")
    public void setSubs(Subs subs) {
        this.subs = subs;
    }

}
