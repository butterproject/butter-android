package pct.droid.fragments;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import pct.droid.R;
import pct.droid.activities.StreamLoadingActivity;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.utils.NetworkUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.VersionUtils;
import pct.droid.dialogfragments.MessageDialogFragment;
import pct.droid.dialogfragments.StringArraySelectorDialogFragment;
import pct.droid.dialogfragments.SynopsisDialogFragment;
import pct.droid.widget.OptionSelector;

public class ShowDetailFragment extends BaseDetailFragment {

    private Show mShow;

    @InjectView(R.id.play_button)
    ImageButton mPlayButton;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.meta)
    TextView mMeta;
    @InjectView(R.id.synopsis)
    TextView mSynopsis;
    @InjectView(R.id.read_more)
    TextView mReadMore;
    @InjectView(R.id.rating)
    RatingBar mRating;
    @Optional
    @InjectView(R.id.cover_image)
    ImageView mCoverImage;

    public static ShowDetailFragment newInstance(Show show, int color) {
        Bundle b = new Bundle();
        b.putParcelable(DATA, show);
        b.putInt(COLOR, color);
        ShowDetailFragment showDetailFragment = new ShowDetailFragment();
        showDetailFragment.setArguments(b);
        return showDetailFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShow = getArguments().getParcelable(DATA);
        mPaletteColor = getArguments().getInt(COLOR, getResources().getColor(R.color.primary));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_showdetail, container, false);
        ButterKnife.inject(this, mRoot);

        if(VersionUtils.isJellyBean()) {
            mPlayButton.setBackgroundDrawable(PixelUtils.changeDrawableColor(mPlayButton.getContext(), R.drawable.play_button_circle, mPaletteColor));
        } else {
            mPlayButton.setBackground(PixelUtils.changeDrawableColor(mPlayButton.getContext(), R.drawable.play_button_circle, mPaletteColor));
        }

        Double rating = Double.parseDouble(mShow.rating);
        mTitle.setText(mShow.title);
        mRating.setProgress(rating.intValue());

        String metaDataStr = mShow.year;

        if (mShow.status != null) {
            metaDataStr += " • ";
            if(mShow.status == Show.Status.CONTINUING) {
                metaDataStr += getString(R.string.continuing);
            } else {
                metaDataStr += getString(R.string.ended);
            }
        }

        if (!TextUtils.isEmpty(mShow.genre)) {
            metaDataStr += " • ";
            metaDataStr += mShow.genre;
        }

        mMeta.setText(metaDataStr);

        if (!TextUtils.isEmpty(mShow.synopsis)) {
            mSynopsis.setText(mShow.synopsis);
            mSynopsis.post(new Runnable() {
                @Override
                public void run() {
                    boolean ellipsized = false;
                    Layout layout = mSynopsis.getLayout();
                    if(layout == null) return;
                    int lines = layout.getLineCount();
                    if(lines > 0) {
                        int ellipsisCount = layout.getEllipsisCount(lines-1);
                        if (ellipsisCount > 0) {
                            ellipsized = true;
                        }
                    }
                    mReadMore.setVisibility(ellipsized ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mSynopsis.setClickable(false);
            mReadMore.setVisibility(View.GONE);
        }

        if(mCoverImage != null) {
            Picasso.with(mCoverImage.getContext()).load(mShow.image).into(mCoverImage);
        }

        return mRoot;
    }

    @OnClick(R.id.read_more)
    public void openReadMore(View v) {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
            return;
        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle b = new Bundle();
        b.putString("text", mShow.synopsis);
        synopsisDialogFragment.setArguments(b);
        synopsisDialogFragment.show(mActivity.getFragmentManager(), "overlay_fragment");
    }

    @OnClick(R.id.play_button)
    public void play() {
        // Ready for the future
        // Start next not-watched episode. For when we are going to keep the watched statuses of episodes.
        
        // Temporary: show dialog for selection
        List<String> availableSeasonsStringList = new ArrayList<>();
        final List<Integer> availableSeasons = new ArrayList<>();
        for (String key : mShow.episodes.keySet()) {
            if (!availableSeasons.contains(mShow.episodes.get(key).season)) {
                availableSeasons.add(mShow.episodes.get(key).season);
                availableSeasonsStringList
                        .add(getString(R.string.season) + " " + ((Integer) mShow.episodes.get(key).season).toString());
            }
        }
        Collections.sort(availableSeasonsStringList);
        Collections.sort(availableSeasons);

        openDialog(getString(R.string.season),
                availableSeasonsStringList.toArray(new String[availableSeasonsStringList.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        final int selectedSeason = availableSeasons.get(position);
                        final List<String> availableChapters = new ArrayList<>();
                        List<String> availableChaptersStringList = new ArrayList<>();
                        for (String key : mShow.episodes.keySet()) {
                            if (mShow.episodes.get(key).season == selectedSeason) {
                                availableChapters.add(key);
                                availableChaptersStringList.add(((Integer) mShow.episodes.get(key).episode).toString());
                            }
                        }

                        // sorting hack
                        Collections.sort(availableChapters, new Comparator<String>() {
                            @Override
                            public int compare(String lhs, String rhs) {
                                Show.Episode lEpisode = mShow.episodes.get(lhs);
                                Show.Episode rEpisode = mShow.episodes.get(rhs);

                                return lEpisode.episode > rEpisode.episode ? 1 : -1;
                            }
                        });
                        Collections.sort(availableChaptersStringList, new Comparator<String>() {
                            @Override
                            public int compare(String lhs, String rhs) {
                                int a = Integer.parseInt(lhs);
                                int b = Integer.parseInt(rhs);
                                if (a > b) {
                                    return 1;
                                } else if (a < b) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            }
                        });

                        for (final ListIterator<String> iter = availableChaptersStringList.listIterator(); iter.hasNext(); ) {
                            final String element = iter.next();
                            iter.set(getString(R.string.episode) + " " + element);
                        }

                        dialog.dismiss();

                        openDialog(getString(R.string.episode),
                                availableChaptersStringList.toArray(new String[availableChaptersStringList.size()]),
                                new DialogInterface.OnClickListener() {
                                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {
                                        String key = availableChapters.get(position);
                                        Show.Episode episode = mShow.episodes.get(key);
                                        Media.Torrent torrent =
                                                episode.torrents.get(episode.torrents.keySet().toArray(new String[1])[0]);

                                        if (PrefUtils.get(mActivity, Prefs.WIFI_ONLY, true) &&
                                                !NetworkUtils.isWifiConnected(mActivity) &&
                                                NetworkUtils .isNetworkConnected(mActivity)) {
                                            MessageDialogFragment.show(mActivity.getFragmentManager(), R.string.wifi_only, R.string.wifi_only_message);
                                        } else {
                                            StreamLoadingFragment.StreamInfo streamInfo = new StreamLoadingFragment.StreamInfo(episode, mShow, torrent.url, null, key);

                                            if (VersionUtils.isLollipop()) {
                                                StreamLoadingActivity.startActivity(mActivity, streamInfo, Pair.create((View) mCoverImage, mCoverImage.getTransitionName()));
                                            } else {
                                                StreamLoadingActivity.startActivity(mActivity, streamInfo);
                                            }
                                        }
                                    }
                                }
                        );
                    }
                });
    }

    public void openDialog(String title, String[] items, DialogInterface.OnClickListener onClickListener) {
        StringArraySelectorDialogFragment.show(mActivity.getSupportFragmentManager(), title, items, -1, onClickListener);
    }

}
