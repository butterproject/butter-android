package pct.droid.providers;

import com.google.gson.Gson;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

public abstract class BaseProvider {

    protected OkHttpClient mClient = new OkHttpClient();
    protected Gson mGson = new Gson();
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
    public static final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

    protected Call enqueue(Request request, com.squareup.okhttp.Callback requestCallback) {
        Call call = mClient.newCall(request);
        call.enqueue(requestCallback);
        return call;
    }

    protected String buildQuery(List<BasicNameValuePair> valuePairs) {
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < valuePairs.size(); i++) {
            NameValuePair pair = valuePairs.get(i);
            stringBuilder.append(pair.getName());
            stringBuilder.append("=");
            stringBuilder.append(pair.getValue());
            if(i + 1 != valuePairs.size()) stringBuilder.append("&");
        }

        return stringBuilder.toString();
    }

}
