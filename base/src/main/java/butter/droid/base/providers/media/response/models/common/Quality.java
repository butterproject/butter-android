package butter.droid.base.providers.media.response.models.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Quality {

    @JsonProperty("provider")
    private String provider;
    private int peers;
    private int seeds;
    @JsonProperty("file")
    @JsonIgnore
    private String file;
    @JsonProperty("url")
    private String url;

    /**
     * @return The provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider The provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return The peers
     */
    @JsonProperty("peers")
    public int getPeers() {
        return peers;
    }

    /**
     * @param peers The peers
     */
    @JsonProperty("peers")
    public void setPeers(int peers) {
        this.peers = peers;
    }

    /**
     * @return The seeds
     */
    @JsonProperty("seeds")
    public int getSeeds() {
        return seeds;
    }

    /**
     * @param seeds The seeds
     */
    @JsonProperty("seeds")
    public void setSeeds(int seeds) {
        this.seeds = seeds;
    }

    /**
     * @return The file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file The file
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

}
