package pct.droid.base.casting.server;

import java.util.Map;

public class InternalRewrite extends NanoHTTPD.Response {
    private final String uri;
    private final Map<String, String> headers;

    public InternalRewrite(final Map<String, String> headers, final String uri) {
        super(null);
        this.headers = headers;
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
