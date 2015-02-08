package pct.droid.base.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import hugo.weaving.DebugLog;
import pct.droid.base.PopcornApplication;
import pct.droid.base.R;
import pct.droid.base.casting.CastingManager;
import pct.droid.base.casting.server.CastingServer;
import pct.droid.base.preferences.DefaultPlayer;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.providers.subs.OpenSubsProvider;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.providers.subs.YSubsProvider;
import pct.droid.base.torrent.DownloadStatus;
import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.ThreadUtils;


/**
 * This fragment handles starting a stream of a torrent.
 * <p/>
 * <p/>
 * It does multiple things:
 * <p/>
 * <pre>
 * 1. Downloads torrent file
 * 2. Downloads subtitles file
 * (1 and 2 happen asynchronously. Generally the torrent will take much longer than downloading subs)
 *
 * 3. Starts downloading (buffering) the torrent
 * 4. Starts the Video activity
 * </pre>
 * <p/>
 * <p/>
 * <p/>
 * //todo: most of this logic should probably be factored out into its own service at some point
 */
public abstract class BaseStreamLoadingFragment extends Fragment implements TorrentService.Listener {

    protected FragmentListener mCallback;
    private SubsProvider mSubsProvider;
    private Boolean mPlayerStarted = false, mHasSubs = false;
    private TorrentService mService;

    private StreamInfo mStreamInfo;

    private enum SubsStatus {SUCCESS, FAILURE, DOWNLOADING}

    private SubsStatus mSubsStatus = SubsStatus.DOWNLOADING;
    private String mSubtitleLanguage = null, mVideoLocation = "";

    public enum State {
        UNINITIALISED, WAITING_TORRENT, WAITING_SUBTITLES, BUFFERING, STREAMING, ERROR;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mStreamInfo = mCallback.getStreamInformation();
        loadSubs();
    }


    @Override
    public void onStart() {
        super.onStart();
        TorrentService.bindHere(getActivity(), mServiceConnection);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mService != null)
            getActivity().unbindService(mServiceConnection);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentListener) mCallback = (FragmentListener) activity;
    }


    /**
     * Update the view based on a state.
     *
     * @param state
     * @param extra - an optional extra piece of data relating to the state, such as an error message, or status data
     */
    protected abstract void updateView(State state, Object extra);

    /**
     * Start the internal player for a streaming torrent
     *
     * @param activity
     * @param location
     * @param media
     * @param quality
     * @param subtitleLanguage
     * @param resumePosition
     */
    protected abstract void startPlayerActivity(FragmentActivity activity, String location, Media media, String quality,
                                                String subtitleLanguage,
                                                int resumePosition);

    @DebugLog
    private void setState(final State state) {
        setState(state, null);
    }

    @DebugLog
    private void setState(final State state, final Object extra) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView(state, extra);
            }
        });
    }

    /**
     * Starts the player for a torrent stream.
     * <p/>
     * Will either start an external player, or the internal one
     */
    @DebugLog
    private void startPlayer(String location) {
        if (mHasSubs && mSubsStatus == SubsStatus.DOWNLOADING) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setState(State.WAITING_SUBTITLES);
                }
            });
            return;
        }

        if (!mPlayerStarted) {
            mPlayerStarted = true;

            //play with default 'external' player
            //todo: remove torrents listeners when closing activity and move service closing to detail/overview activities

            boolean playingExternal = false;
            if(CastingManager.getInstance(getActivity()).isConnected()) {
                CastingServer.setCurrentVideo(location);
                playingExternal = !CastingManager.getInstance(getActivity()).loadMedia(mStreamInfo.getMedia(), CastingServer.getVideoURL(), false);
            } else {
                playingExternal = DefaultPlayer.start(getActivity(), mStreamInfo.getMedia(), mSubtitleLanguage, location);
            }

            if (!playingExternal) {
                //play internally
                mService.removeListener();
                startPlayerActivity(getActivity(), "file://" + location, mStreamInfo.getMedia(), mStreamInfo.getQuality(),
                        mStreamInfo.getSubtitleLanguage(), 0);
                getActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayerStarted) {
            getActivity().onBackPressed();
        }

        setState(State.WAITING_TORRENT);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((TorrentService.ServiceBinder) service).getService();
            mService.setListener(BaseStreamLoadingFragment.this);

            //kicks off the torrent stream
            startStream();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };


    /**
     * Starts the torrent service streaming a torrent url
     */
    private void startStream() {
        if (null == mService) throw new IllegalStateException("Torrent service must be bound");
        String torrentUrl = mStreamInfo.getTorrentUrl();

        //if the torrent service is currently streaming another file, stop it.
        if (mService.isStreaming() && !mService.getCurrentTorrentUrl().equals(torrentUrl)) {
            mService.stopStreaming();
        }
        //start streaming the new file
        mService.streamTorrent(torrentUrl);
    }

    /**
     * Stops the torrent service streaming
     */
    @DebugLog
    public void cancelStream() {
        CastingManager.getInstance(getActivity()).stop();
        if (mService != null) {
            mService.stopStreaming();
        }
    }

    @Override
    @DebugLog
    public void onStreamStarted() {
        setState(State.BUFFERING);
    }

    @Override
    @DebugLog
    public void onStreamError(Exception e) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                State state = State.ERROR;
                setState(State.ERROR, getString(R.string.error_files));
            }
        });
    }

    /**
     * Called when torrent buffering has reached 100%
     *
     * @param videoLocation
     */
    @Override
    @DebugLog
    public void onStreamReady(File videoLocation) {
        mVideoLocation = videoLocation.toString();
        startPlayer(mVideoLocation);
    }


    /**
     * Called when the torrent buffering status has been updated
     *
     * @param status
     */
    @Override
    @DebugLog
    public void onStreamProgress(DownloadStatus status) {
        if (mVideoLocation.isEmpty()) {
            setState(State.STREAMING, status);
        } else {
            startPlayer(mVideoLocation);
        }
    }


    /**
     * Downloads the subs file
     */
    private void loadSubs() {
        final Media data = mStreamInfo.getMedia();
        if (null != data) {

            //if there are no subtitles specified, try to use the default subs
            if (mStreamInfo.getSubtitleLanguage() == null && data.subtitles != null && data.subtitles.size() > 0) {
                if (data.subtitles.containsKey(PrefUtils.get(getActivity(), Prefs.SUBTITLE_DEFAULT, "no-subs"))) {
                    mStreamInfo.setSubtitleLanguage(PrefUtils.get(getActivity(), Prefs.SUBTITLE_DEFAULT, "no-subs"));
                }
            }

            //todo: tidy up
            mSubsStatus = SubsStatus.SUCCESS;

            //load subtitles
            if (data.subtitles != null && data.subtitles.size() > 0 && mStreamInfo.mSubtitleLanguage != null) {
                mHasSubs = true;
                mSubtitleLanguage = mStreamInfo.mSubtitleLanguage;
                if (!mSubtitleLanguage.equals("no-subs")) {
                    SubsProvider.download(getActivity(), data, mSubtitleLanguage, new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            mSubsStatus = SubsStatus.FAILURE;
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            mSubsStatus = SubsStatus.SUCCESS;
                        }
                    });
                } else {
                    mSubsStatus = SubsStatus.SUCCESS;
                }
            } else {
			    mSubsProvider = data.getSubsProvider();
				if (null != mSubsProvider) {
					SubsProvider.Callback subsCallback = new SubsProvider.Callback() {
						@Override
						public void onSuccess(Map<String, String> items) {
							data.subtitles = items;
							mSubsStatus = SubsStatus.SUCCESS;
						}

						@Override
						public void onFailure(Exception e) {
							mSubsStatus = SubsStatus.FAILURE;
						}
					};

					if (mStreamInfo.isShow()) {
						mSubsProvider.getList(mStreamInfo.getShow(), (Show.Episode) data, subsCallback);
					} else {
						mSubsProvider.getList((Movie) data, subsCallback);
					}
				}
            }
        }
    }

    public interface FragmentListener {
        StreamInfo getStreamInformation();
    }


    /**
     * Container for all information needed to start a stream
     */
    public static class StreamInfo implements Parcelable {
        private Media mMedia;
        private String mSubtitleLanguage;
        private String mQuality;
        private String mTorrentUrl;
        private Show mShow;

        public StreamInfo(String torrentUrl) {
            this(null, null, torrentUrl, null, null);
        }

        public StreamInfo(Media media, String torrentUrl, String subtitleLanguage, String quality) {
            this(media, null, torrentUrl, subtitleLanguage, quality);
        }

        public StreamInfo(Media media, Show show, String torrentUrl, String subtitleLanguage, String quality) {
            mMedia = media;
            mShow = show;
            mTorrentUrl = torrentUrl;
            mSubtitleLanguage = subtitleLanguage;
            mQuality = quality;
        }

        public boolean isShow() {
            return null != mShow;
        }

        public Show getShow() {
            return mShow;
        }

        public Media getMedia() {
            return mMedia;
        }

        public String getSubtitleLanguage() {
            return mSubtitleLanguage;
        }

        public String getQuality() {
            return mQuality;
        }

        public String getTorrentUrl() {
            return mTorrentUrl;
        }

        public void setSubtitleLanguage(String subtitleLanguage) {
            mSubtitleLanguage = subtitleLanguage;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mMedia, 0);
            dest.writeString(this.mSubtitleLanguage);
            dest.writeString(this.mQuality);
            dest.writeString(this.mTorrentUrl);
            dest.writeParcelable(this.mShow, 0);
        }

        private StreamInfo(Parcel in) {
            this.mMedia = in.readParcelable(Media.class.getClassLoader());
            this.mSubtitleLanguage = in.readString();
            this.mQuality = in.readString();
            this.mTorrentUrl = in.readString();
            this.mShow = in.readParcelable(Show.class.getClassLoader());
        }

        public static final Creator<StreamInfo> CREATOR = new Creator<StreamInfo>() {
            public StreamInfo createFromParcel(Parcel source) {
                return new StreamInfo(source);
            }

            public StreamInfo[] newArray(int size) {
                return new StreamInfo[size];
            }
        };
    }


}
