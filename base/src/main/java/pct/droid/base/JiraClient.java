package pct.droid.base;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class JiraClient {

    public static void getVersionId(final VersionCallback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.JIRA_API + "project/" + Constants.JIRA_PROJECT + "/versions")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onResult("");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()) {
                    JiraVersionResponse[] result = new Gson().fromJson(response.body().string(), JiraVersionResponse[].class);
                    String versionId = "";

                    String versionName = "";
                    try {
                        PackageInfo packageInfo = PopcornApplication.getAppContext().getPackageManager().getPackageInfo(PopcornApplication.getAppContext().getPackageName(), 0);
                        versionName = packageInfo.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if(!versionName.isEmpty()) {
                        for(JiraVersionResponse version : result) {
                            if(version.name.equals(versionName)) {
                                callback.onResult(version.id);
                                return;
                            }
                        }
                    }

                    callback.onResult(versionId);
                }
            }
        });
    }

    public interface VersionCallback {
        public void onResult(String result);
    }

    class JiraVersionResponse {
        public String id;
        public String name;
    }

}
