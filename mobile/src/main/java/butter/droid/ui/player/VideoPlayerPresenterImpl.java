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

package butter.droid.ui.player;

import android.content.ContentResolver;
import android.content.Intent;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.provider.base.filter.Genre;
import butter.droid.provider.base.model.Clip;
import butter.droid.provider.base.model.Media;
import butter.droid.utils.StreamInfoUtils;

public class VideoPlayerPresenterImpl implements VideoPlayerPresenter {

    private final VideoPlayerView view;
    private final ContentResolver contentResolver;

    private StreamInfo streamInfo;
    private long resumePosition;

    public VideoPlayerPresenterImpl(final VideoPlayerView view, final ContentResolver contentResolver) {
        this.view = view;
        this.contentResolver = contentResolver;
    }


    @Override public void onCreate(final StreamInfo streamInfo, final long resumePosition, final String action, final Intent intent) {

        if (Intent.ACTION_VIEW.equals(action)) {
            String videoLocation = StreamInfoUtils.getActionViewVideoLocation(contentResolver, intent);
            if (videoLocation != null) {
                final Media media = new Clip("0", videoLocation, -1, new Genre[0], null, "", "", "",
                        videoLocation);
                this.streamInfo = new StreamInfo(videoLocation, new MediaWrapper(media, -1), null);
                this.resumePosition = intent.getLongExtra("position", 0);
                view.showVideoFragment(this.streamInfo, this.resumePosition);
            } else {
                // TODO: 4/2/17 Show error
            }
        } else {
            this.streamInfo = streamInfo;
            this.resumePosition = resumePosition;

            if (streamInfo == null) {
                throw new IllegalStateException("StreamInfo has to be provided");
            }

            view.showVideoFragment(streamInfo, resumePosition);
        }

    }

    @Override public void close() {
        view.showExitDialog(streamInfo.getFullTitle());
    }
}
