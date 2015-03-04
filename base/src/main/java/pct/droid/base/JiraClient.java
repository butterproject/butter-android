/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class JiraClient {

    public static void getVersionId(final VersionCallback callback) {
        OkHttpClient okHttpClient = PopcornApplication.getHttpClient();
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
                if (response.isSuccessful()) {
                    JiraVersionResponse[] result = new Gson().fromJson(response.body().string(), JiraVersionResponse[].class);
                    String versionId = "";

                    String versionName = "";
                    try {
                        PackageInfo packageInfo = PopcornApplication.getAppContext().getPackageManager().getPackageInfo(PopcornApplication.getAppContext().getPackageName(), 0);
                        versionName = packageInfo.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (!versionName.isEmpty()) {
                        for (JiraVersionResponse version : result) {
                            if (version.name.equals(versionName)) {
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
