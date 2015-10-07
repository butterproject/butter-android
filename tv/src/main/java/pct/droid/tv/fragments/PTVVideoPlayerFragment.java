package pct.droid.tv.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sv244.torrentstream.StreamStatus;
import com.github.sv244.torrentstream.Torrent;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import pct.droid.base.content.preferences.Prefs;
import pct.droid.base.fragments.BaseVideoPlayerFragment;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.subs.Caption;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVMediaDetailActivity;
import pct.droid.tv.activities.PTVVideoPlayerActivity;
import pct.droid.tv.events.ConfigureSubtitleEvent;
import pct.droid.tv.events.PausePlaybackEvent;
import pct.droid.tv.events.PlaybackProgressChangedEvent;
import pct.droid.tv.events.ScaleVideoEvent;
import pct.droid.tv.events.SeekBackwardEvent;
import pct.droid.tv.events.SeekForwardEvent;
import pct.droid.tv.events.StartPlaybackEvent;
import pct.droid.tv.events.StreamProgressChangedEvent;
import pct.droid.tv.events.ToggleSubtitleEvent;
import pct.droid.tv.events.UpdatePlaybackStateEvent;

public class PTVVideoPlayerFragment extends BaseVideoPlayerFragment {
    @Bind(R.id.video_surface)
    SurfaceView videoSurface;

    @Bind(R.id.subtitle_text)
    TextView mSubtitleText;

    private boolean mIsSubtitleEnabled = false;
    private boolean mMediaSessionMetadataApplied = false;
    private Handler mDisplayHandler;
    private MediaSession mMediaSession;
    private StreamInfo mStreamInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mStreamInfo = intent.getParcelableExtra(PTVVideoPlayerActivity.INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_videoplayer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        videoSurface.setVisibility(View.VISIBLE);
        mSubtitleText.setVisibility(View.INVISIBLE);

        mSubtitleText.setTextColor(
            PrefUtils.get(
                getActivity(),
                Prefs.SUBTITLE_COLOR,
                ContextCompat.getColor(getActivity(), R.color.subtitle_default_text_color)));
        mSubtitleText.setText("");
        updateSubtitleSize(PrefUtils.get(getActivity(), Prefs.SUBTITLE_SIZE, SUBTITLE_MINIMUM_SIZE));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isMediaSessionActive()) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isMediaSessionActive()) {
            mMediaSession.release();
        }
    }

    @Override
    protected SurfaceView getVideoSurface() {
        return videoSurface;
    }

    @Override
    protected void onErrorEncountered() {
        /* Encountered Error, exit player with a message */
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                    }
                })
                .setTitle(R.string.encountered_error_title)
                .setMessage(R.string.encountered_error)
                .create();
        dialog.show();
    }

    @Override
    protected void showPlayerInfo(String text) { }

    @Override
    protected void showOverlay() { }

    @Override
    protected void loadMedia() {
        super.loadMedia();
        activateMediaSession();
    }

    @Override
    protected void updatePlayPauseState() {
        EventBus.getDefault().post(new ToggleSubtitleEvent(mIsSubtitleEnabled));
        EventBus.getDefault().post(new UpdatePlaybackStateEvent(isPlaying()));

        if (mMediaSession != null) {
            PlaybackState.Builder builder = new PlaybackState.Builder();
            builder.setActions(isPlaying()
                ? PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PAUSE
                : PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PLAY);
            builder.setState(
                isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                getCurrentTime(),
                1.0f);
            mMediaSession.setPlaybackState(builder.build());
        }
    }

    @Override
    public void onHardwareAccelerationError() {
        showErrorDialog(
            getString(R.string.hardware_acceleration_error_title),
            getString(R.string.hardware_acceleration_error_message));
    }

    @Override
    protected void showTimedCaptionText(final Caption text) {
        if (mDisplayHandler == null) mDisplayHandler = new Handler(Looper.getMainLooper());
        mDisplayHandler.post(new Runnable() {
            @Override
            public void run() {
                if (text == null) {
                    if (mSubtitleText.getText().length() > 0) {
                        mSubtitleText.setText("");
                        mSubtitleText.setVisibility(View.INVISIBLE);
                    }
                    return;
                }

                SpannableStringBuilder styledString = (SpannableStringBuilder) Html.fromHtml(text.content);
                ForegroundColorSpan[] toRemoveSpans = styledString.getSpans(0, styledString.length(), ForegroundColorSpan.class);

                for (ForegroundColorSpan remove : toRemoveSpans) {
                    styledString.removeSpan(remove);
                }

                if (!mSubtitleText.getText().toString().equals(styledString.toString())) {
                    mSubtitleText.setText(styledString);
                    mSubtitleText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void setProgressVisible(boolean visible) { }

    @Override
    protected void onProgressChanged(long currentTime, long duration) {
        EventBus.getDefault().post(new PlaybackProgressChangedEvent(currentTime, duration));

        if (mMediaSession != null && duration > 0) {
            PlaybackState.Builder builder = new PlaybackState.Builder();
            builder.setActions(isPlaying()
                ? PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PAUSE
                : PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PLAY);
            builder.setState(
                isPlaying()
                    ? PlaybackState.STATE_PLAYING
                    : PlaybackState.STATE_PAUSED,
                getCurrentTime(),
                1.0f);
            mMediaSession.setPlaybackState(builder.build());

            if (!mMediaSessionMetadataApplied) {
                setupMediaMetadata();
            }
        }
    }

    @Override
    public void onPlaybackEndReached() {
        EventBus.getDefault().post(new UpdatePlaybackStateEvent(false));
        mMediaSession.setActive(false);
    }

    @Override
    public void onStreamReady(Torrent torrent) { }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus streamStatus) {
        super.onStreamProgress(torrent, streamStatus);
        EventBus.getDefault().post(new StreamProgressChangedEvent(getStreamerProgress()));
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
        showErrorDialog(getString(R.string.torrent_failed), e.getMessage());
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        disableHardwareAcceleration();
                        loadMedia();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                    }
                })
                .setTitle(title)
                .setMessage(message)
                .create();

        if (!getActivity().isFinishing())
            dialog.show();
    }

    @Override
    protected void updateSubtitleSize(int size) {
        mSubtitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10 + size);
    }

    @Override
    protected void onSubtitleEnabledStateChanged(boolean enabled) {
        super.onSubtitleEnabledStateChanged(enabled);
        mIsSubtitleEnabled = enabled;
        mSubtitleText.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        EventBus.getDefault().post(new ToggleSubtitleEvent(mIsSubtitleEnabled));
    }

    @Override
    public void startBeamPlayerActivity() { }

    @SuppressWarnings("unused")
    public void onEventMainThread(StartPlaybackEvent event) {
        togglePlayPause();

        if (mMediaSession != null) {
            PlaybackState.Builder builder = new PlaybackState.Builder();
            builder.setActions(PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE);
            builder.setState(PlaybackState.STATE_PLAYING, getCurrentTime(), 1.0f);
            mMediaSession.setPlaybackState(builder.build());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PausePlaybackEvent event) {
        togglePlayPause();

        if (mMediaSession != null) {
            PlaybackState.Builder builder = new PlaybackState.Builder();
            builder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE);
            builder.setState(PlaybackState.STATE_PAUSED, getCurrentTime(), 1.0f);
            mMediaSession.setPlaybackState(builder.build());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SeekBackwardEvent event) {
        if (!isSeeking()) {
            setSeeking(true);
            seek(event.getSeek());
            setSeeking(false);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SeekForwardEvent event) {
        if (!isSeeking()) {
            setSeeking(true);
            seek(event.getSeek());
            setSeeking(false);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ScaleVideoEvent event) {
        scaleClick();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ConfigureSubtitleEvent event) {
        subsClick();
    }

    private void activateMediaSession() {
        if (null != mMediaSession) return;
        Activity activity = getActivity();
        mMediaSession = new MediaSession(activity, "PopcornTimeMediaSession");
        mMediaSession.setCallback(new PopcornTimeMediaSessionCallback(activity));
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setActive(true);
        activity.setMediaController(new MediaController(activity, mMediaSession.getSessionToken()));
    }

    private void setupMediaMetadata() {
        mMediaSessionMetadataApplied = false;
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mStreamInfo.getMedia().videoId);
        metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, getDuration());

        if (mStreamInfo.isShow()) {
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, mStreamInfo.getShowTitle());
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, mStreamInfo.getShowEpisodeTitle());
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, mStreamInfo.getShowTitle());
        }
        else {
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, mStreamInfo.getTitle());
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, mStreamInfo.getTitle());
        }

        String imageUrl = mStreamInfo.getMedia().image;
        if (imageUrl != null && !imageUrl.equals("")) {
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, imageUrl);
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ART_URI, imageUrl);
            Picasso.with(getActivity()).load(imageUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                    mMediaSession.setMetadata(metadataBuilder.build());
                    mMediaSessionMetadataApplied = true;
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    mMediaSession.setMetadata(metadataBuilder.build());
                    mMediaSessionMetadataApplied = true;
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) { }
            });
        }
        else {
            mMediaSession.setMetadata(metadataBuilder.build());
            mMediaSessionMetadataApplied = true;
        }
    }

    public boolean isMediaSessionActive() {
        return mMediaSession.isActive();
    }

    public void deactivateMediaSession() {
        if (mMediaSession != null && mMediaSession.isActive()) mMediaSession.setActive(false);
    }

    private class PopcornTimeMediaSessionCallback extends MediaSession.Callback {
        private final WeakReference<Context> contextReference;

        public PopcornTimeMediaSessionCallback(Context context) {
            contextReference = new WeakReference<>(context);
        }

        @Override
        public void onPlay() {
            EventBus.getDefault().post(new StartPlaybackEvent());
        }

        @Override
        public void onPause() {
            EventBus.getDefault().post(new PausePlaybackEvent());
        }

        @Override
        public void onFastForward() {
            EventBus.getDefault().post(new SeekForwardEvent());
        }

        @Override
        public void onRewind() {
            EventBus.getDefault().post(new SeekForwardEvent());
        }

        @Override
        public void onPlayFromMediaId(@NonNull String mediaId, @NonNull Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            if (contextReference.get() == null) return;
            Context context = contextReference.get();
            Media media = extras.getParcelable(PTVMediaDetailActivity.EXTRA_ITEM);
            if (media == null) return;
            Intent detailIntent = PTVMediaDetailActivity.buildIntent(
                    context,
                    media,
                    media.headerImage == null ? "" : media.headerImage,
                    media.image == null ? "" : media.image);
            detailIntent.setAction(media.videoId);
            context.startActivity(detailIntent);
        }

        @Override
        public boolean onMediaButtonEvent(@NonNull Intent intent) {
            if (!intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) return false;
            KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) return false;

            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                EventBus.getDefault().post(new PausePlaybackEvent());
                return true;
            }
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (isPlaying()) {
                    EventBus.getDefault().post(new PausePlaybackEvent());
                }
                return true;
            }

            return false;
        }
    }
}
