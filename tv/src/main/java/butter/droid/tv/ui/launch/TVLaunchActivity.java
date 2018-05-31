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

package butter.droid.tv.ui.launch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.provider.base.filter.Genre;
import butter.droid.provider.base.model.Clip;
import butter.droid.provider.base.model.Media;
import butter.droid.tv.service.RecommendationService;
import butter.droid.tv.ui.loading.TVStreamLoadingActivity;
import butter.droid.tv.ui.main.TVMainActivity;
import butter.droid.tv.ui.terms.TVTermsActivity;
import dagger.android.DaggerActivity;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.inject.Inject;

public class TVLaunchActivity extends DaggerActivity implements TVLaunchView {

    private static final int REQUEST_CODE_TERMS = 1;
    private static final int PERMISSIONS_REQUEST = 1232;

    @Inject TVLaunchPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.onCreate();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_TERMS:
                if (resultCode == RESULT_CANCELED) {
                    presenter.termsCanceled();
                    finish();
                } else {
                    presenter.termsAccepted();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    presenter.permissionsGranted();
                } else {
                    presenter.permissionsDenied();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override public void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
    }

    @Override public void close() {
        finish();
    }

    @Override public void showTermsScreen() {
        startActivityForResult(TVTermsActivity.getIntent(this), REQUEST_CODE_TERMS);
    }

    @Override public void startRecommendationService() {
        Intent recommendationIntent = new Intent(this, RecommendationService.class);
        startService(recommendationIntent);
    }

    @Override public void navigateForward() {
        String action = getIntent().getAction();
        Uri data = getIntent().getData();

        if (action != null && action.equals(Intent.ACTION_VIEW) && data != null) {
            String streamUrl = data.toString();
            try {
                streamUrl = URLDecoder.decode(streamUrl, "utf-8");
                final Media media = new Clip("0", streamUrl, -1, new Genre[0], -1, null, "", "",
                        streamUrl);
                TVStreamLoadingActivity.startActivity(this, new StreamInfo(streamUrl, new MediaWrapper(media, -1), null));
                finish();
                return;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        TVMainActivity.startActivity(this);
        finish();
    }
}
