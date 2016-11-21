/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.providers;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * BaseProvider.java
 * <p/>
 * Base class for providers, has code to enqueue network requests to the OkHttpClient
 */
public abstract class BaseProvider {

    private final OkHttpClient client;
    protected Gson mGson;

    public BaseProvider(OkHttpClient client, Gson gson) {
        this.client = client;
        this.mGson = gson;
    }

    protected OkHttpClient getClient() {
        return client;
    }

    /**
     * Enqueue request without callback
     *
     * @param request Request
     * @return Call
     */
    protected Call enqueue(Request request) {
        return enqueue(request, null);
    }

    /**
     * Enqueue request with callback
     *
     * @param request         Request
     * @param requestCallback Callback
     * @return Call
     */
    protected Call enqueue(Request request, Callback requestCallback) {
        Call call = getClient().newCall(request);
        if (requestCallback != null) {
            call.enqueue(requestCallback);
        }
        return call;
    }

    /**
     * This method will be called when user is done with data that he required. Provider should at this point
     * clean after itself. For example cancel all ongoing network request.
     */
    public abstract void cancel();

    /**
     * Build URL encoded query
     *
     * @param valuePairs List with key-value items
     * @return Query string
     */
    protected String buildQuery(List<NameValuePair> valuePairs) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            for (int i = 0; i < valuePairs.size(); i++) {
                NameValuePair pair = valuePairs.get(i);
                stringBuilder.append(URLEncoder.encode(pair.getName(), "utf-8"));
                stringBuilder.append("=");
                stringBuilder.append(URLEncoder.encode(pair.getValue(), "utf-8"));
                if (i + 1 != valuePairs.size()) stringBuilder.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return stringBuilder.toString();
    }

    public class NameValuePair {
        private String mName;
        private String mValue;

        public NameValuePair(String name, String value) {
            mName = name;
            mValue = value;
        }

        public String getName() {
            return mName;
        }

        public String getValue() {
            return mValue;
        }
    }

}
