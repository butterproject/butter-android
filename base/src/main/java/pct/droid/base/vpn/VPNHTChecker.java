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

package pct.droid.base.vpn;

import android.content.Context;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import pct.droid.base.PopcornApplication;
import pct.droid.base.utils.PackageUtils;
import pct.droid.base.utils.PrefUtils;

public class VPNHTChecker {

    public static final String VPN_AVAILABLE = "vpn_available";
    private static Request sCheckingRequest;

    public static boolean isDownloadAvailable(final Context context) {
        if(PackageUtils.isInstalled(context, VPNManager.PACKAGE_VPNHT) || PrefUtils.get(context, VPN_AVAILABLE, false)) {
            return true;
        }

        if(sCheckingRequest == null) {
            sCheckingRequest = new Request.Builder().head().url("https://play.google.com/store/apps/details?id=ht.vpn.android").build();
            PopcornApplication.getHttpClient().newCall(sCheckingRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    sCheckingRequest = null;
                    PrefUtils.save(context, VPN_AVAILABLE, false);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    sCheckingRequest = null;
                    if (response.isSuccessful()) {
                        PrefUtils.save(context, VPN_AVAILABLE, true);
                    } else {
                        PrefUtils.save(context, VPN_AVAILABLE, false);
                    }
                }
            });
        }

        return false;
    }

}
