package butter.droid.base.providers.subs.ysubs.response.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "hi",
        "rating",
        "url"
})
public class Language {

    @JsonProperty("id")
    private int id;
    @JsonProperty("hi")
    private int hi;
    @JsonProperty("rating")
    private int rating;
    @JsonProperty("url")
    private String url;

    /**
     * @return The id
     */
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    /**
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return The hi
     */
    @JsonProperty("hi")
    public int getHi() {
        return hi;
    }

    /**
     * @param hi The hi
     */
    @JsonProperty("hi")
    public void setHi(int hi) {
        this.hi = hi;
    }

    /**
     * @return The rating
     */
    @JsonProperty("rating")
    public int getRating() {
        return rating;
    }

    /**
     * @param rating The rating
     */
    @JsonProperty("rating")
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * @return The url
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

}
