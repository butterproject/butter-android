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

package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import pct.droid.base.content.preferences.Prefs;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.service.RecommendationService;

public class PTVLaunchActivity extends Activity {


	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent recommendationIntent = new Intent(this, RecommendationService.class);
		startService(recommendationIntent);

		Boolean firstRun = PrefUtils.get(this, Prefs.FIRST_RUN, true);

		if (firstRun) {
			//run the welcome wizard
			PTVWelcomeActivity.startActivity(this);
		} else {
			String action = getIntent().getAction();
			Uri data = getIntent().getData();
			if (action != null && action.equals(Intent.ACTION_VIEW) && data != null) {
				String streamUrl = data.toString();
				try {
					streamUrl = URLDecoder.decode(streamUrl, "utf-8");
					PTVStreamLoadingActivity.startActivity(this, new StreamInfo(streamUrl));
					finish();
					return;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			PTVMainActivity.startActivity(this);
		}
	}
}
