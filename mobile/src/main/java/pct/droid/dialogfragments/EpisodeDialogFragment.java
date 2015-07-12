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

package pct.droid.dialogfragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import android.support.annotation.Nullable;
import pct.droid.R;
import pct.droid.activities.MediaDetailActivity;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.providers.meta.MetaProvider;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.torrent.Magnet;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PixelUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.SortUtils;
import pct.droid.base.utils.StringUtils;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.base.utils.VersionUtils;
import pct.droid.widget.BottomSheetScrollView;
import pct.droid.widget.OptionSelector;

public class EpisodeDialogFragment extends DialogFragment {

    public static final String EXTRA_EPISODE = "episode";
    public static final String EXTRA_SHOW = "show";

    private static final int ANIM_SPEED = 200;

    private Integer mThreshold = 0, mBottom = 0;
    private Activity mActivity;
    private MetaProvider mMetaProvider;
    private SubsProvider mSubsProvider;
    private boolean mAttached = false, mTouching = false, mOpened = false;
    private String mSelectedSubtitleLanguage, mSelectedQuality;
    private Episode mEpisode;
    private Show mShow;
    private Magnet mMagnet;

    @Bind(R.id.scrollview)
    BottomSheetScrollView mScrollView;
    @Bind(R.id.placeholder)
    View mPlaceholder;
    @Bind(R.id.play_button)
    ImageButton mPlayButton;
    @Bind(R.id.header_image)
    ImageView mHeaderImage;
    @Bind(R.id.info)
    TextView mInfo;
    @Bind(R.id.title)
    TextView mTitle;
    @Bind(R.id.aired)
    TextView mAired;
    @Bind(R.id.synopsis)
    TextView mSynopsis;
    @Bind(R.id.subtitles)
    OptionSelector mSubtitles;
    @Bind(R.id.quality)
    OptionSelector mQuality;
    @Bind(R.id.magnet)
    @Nullable
    ImageButton mOpenMagnet;

    public static EpisodeDialogFragment newInstance(Show show, Episode episode) {
        EpisodeDialogFragment frag = new EpisodeDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_SHOW, show);
        args.putParcelable(EXTRA_EPISODE, episode);
        frag.setArguments(args);
        return frag;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.Theme_PopcornTime)).inflate(R.layout.fragment_dialog_episode, container, false);
        ButterKnife.bind(this, v);

        if (!VersionUtils.isJellyBean()) {
            mPlayButton.setBackgroundDrawable(PixelUtils.changeDrawableColor(mPlayButton.getContext(), R.drawable.play_button_circle, mShow.color));
        } else {
            mPlayButton.setBackground(PixelUtils.changeDrawableColor(mPlayButton.getContext(), R.drawable.play_button_circle, mShow.color));
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mPlaceholder.getLayoutParams();
        layoutParams.height = PixelUtils.getScreenHeight(mActivity);
        mPlaceholder.setLayoutParams(layoutParams);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_Episode);
        setCancelable(false);

        mActivity = getActivity();
        mThreshold = PixelUtils.getPixelsFromDp(mActivity, 220);
        mBottom = PixelUtils.getPixelsFromDp(mActivity, 33);
        mShow = getArguments().getParcelable(EXTRA_SHOW);
        mEpisode = getArguments().getParcelable(EXTRA_EPISODE);
        mMetaProvider = mEpisode.getMetaProvider();
        mSubsProvider = mEpisode.getSubsProvider();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    smoothDismiss();
                }
                return true;
            }
        });
        return dialog;
    }

    public void smoothDismiss() {
        mOpened = false;

        if (mScrollView.getScrollY() <= mBottom) {
            dismiss();
            return;
        }

        mScrollView.animateScrollTo(0, ANIM_SPEED);
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    dismiss();
                } catch (Exception e) {
                }
            }
        }, ANIM_SPEED);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (null != mMetaProvider) mMetaProvider.cancel();
        if (null != mSubsProvider) mSubsProvider.cancel();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //mMaxHeight = PixelUtils.getScreenHeight(mActivity) - PixelUtils.getPixelsFromDp(mActivity, 50);
                int screenHeight = PixelUtils.getScreenHeight(mActivity);
                int scroll = (screenHeight / 3) * 2;
                mScrollView.animateScrollTo(scroll, ANIM_SPEED);
                mScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mOpened = true;
                    }
                }, ANIM_SPEED);
            }
        }, 250);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!TextUtils.isEmpty(mEpisode.title)) {
            mTitle.setText(mEpisode.title);
            mHeaderImage.setContentDescription(mEpisode.title);
        } else {
            mTitle.setText(R.string.no_title_available);
            mHeaderImage.setContentDescription(getString(R.string.no_title_available));
        }

        mAired.setVisibility(mEpisode.aired > 0 ? View.VISIBLE : View.GONE);
        Date airedDate = new Date((long) mEpisode.aired * 1000);
        mAired.setText(String.format(getString(R.string.aired), new SimpleDateFormat("MMMM dd, yyyy", LocaleUtils.getCurrent()).format(airedDate)));

        if (!TextUtils.isEmpty(mEpisode.overview))
            mSynopsis.setText(mEpisode.overview);
        else
            mSynopsis.setText(R.string.no_synopsis_available);


        String seasonStr = Integer.toString(mEpisode.season);
        if (seasonStr.length() < 2) seasonStr = "0" + seasonStr;
        String episodeStr = Integer.toString(mEpisode.episode);
        if (episodeStr.length() < 2) episodeStr = "0" + episodeStr;

        mInfo.setText("S" + seasonStr + "E" + episodeStr);

        mSubtitles.setFragmentManager(getFragmentManager());
        mQuality.setFragmentManager(getFragmentManager());
        mSubtitles.setTitle(R.string.subtitles);
        mQuality.setTitle(R.string.quality);

        final String[] qualities = mEpisode.torrents.keySet().toArray(new String[mEpisode.torrents.size()]);
        SortUtils.sortQualities(qualities);
        mQuality.setData(qualities);
        String defaultQuality = PrefUtils.get(mQuality.getContext(), Prefs.QUALITY_DEFAULT, "720p");
        int qualityIndex = Arrays.asList(qualities).contains(defaultQuality) ? Arrays.asList(qualities).indexOf(defaultQuality) : qualities.length - 1;
        mSelectedQuality = qualities[qualityIndex];
        mQuality.setText(mSelectedQuality);
        mQuality.setDefault(qualityIndex);

        updateMagnet();

        mQuality.setListener(new OptionSelector.SelectorListener() {
            @Override
            public void onSelectionChanged(int position, String value) {
                mSelectedQuality = value;
                updateMagnet();
            }
        });

        mSubtitles.setText(R.string.loading_subs);
        mSubtitles.setClickable(false);
        if (mSubsProvider != null) {
            mSubsProvider.getList(mEpisode, new SubsProvider.Callback() {
                @Override
                public void onSuccess(Map<String, String> subtitles) {
                    if (!mAttached) return;

                    mEpisode.subtitles = subtitles;

                    String[] languages = subtitles.keySet().toArray(new String[subtitles.size()]);
                    Arrays.sort(languages);
                    final String[] adapterLanguages = new String[languages.length + 1];
                    adapterLanguages[0] = "no-subs";
                    System.arraycopy(languages, 0, adapterLanguages, 1, languages.length);

                    String[] readableNames = new String[adapterLanguages.length];
                    for (int i = 0; i < readableNames.length; i++) {
                        String language = adapterLanguages[i];
                        if (language.equals("no-subs")) {
                            readableNames[i] = getString(R.string.no_subs);
                        } else {
                            Locale locale = LocaleUtils.toLocale(language);
                            readableNames[i] = locale.getDisplayName(locale);
                        }
                    }

                    mSubtitles.setListener(new OptionSelector.SelectorListener() {
                        @Override
                        public void onSelectionChanged(int position, String value) {
                            onSubtitleLanguageSelected(adapterLanguages[position]);
                        }
                    });
                    mSubtitles.setData(readableNames);
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSubtitles.setClickable(true);
                        }
                    });

                    String defaultSubtitle = PrefUtils.get(mSubtitles.getContext(), Prefs.SUBTITLE_DEFAULT, null);
                    if (subtitles.containsKey(defaultSubtitle)) {
                        onSubtitleLanguageSelected(defaultSubtitle);
                        mSubtitles.setDefault(Arrays.asList(adapterLanguages).indexOf(defaultSubtitle));
                    } else {
                        onSubtitleLanguageSelected("no-subs");
                        mSubtitles.setDefault(Arrays.asList(adapterLanguages).indexOf("no-subs"));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    mSubtitles.setData(new String[0]);
                    mSubtitles.setClickable(true);
                }
            });
        } else {
            mSubtitles.setText(R.string.no_subs_available);
        }

        mScrollView.setListener(new BottomSheetScrollView.Listener() {
            @Override
            public void onScroll(int scrollY, BottomSheetScrollView.Direction direction) {
            }

            @Override
            public void onTouch(boolean touching) {
                mTouching = touching;
                int scrollY = mScrollView.getScrollY();
                if (!mTouching && mOpened && scrollY <= mThreshold) {
                    smoothDismiss();
                }
            }

            @Override
            public void onScrollStart() {
            }

            @Override
            public void onScrollEnd() {
                if (!mTouching && mOpened && mScrollView.getScrollY() <= mThreshold) {
                    smoothDismiss();
                }
            }
        });

        if (mMetaProvider != null) {
            mMetaProvider.getEpisodeMeta(mEpisode.imdbId, mEpisode.season, mEpisode.episode, new MetaProvider.Callback() {
                @Override
                public void onResult(MetaProvider.MetaData metaData, Exception e) {
                    String imageUrl = mEpisode.headerImage;
                    if (e == null) {
                        imageUrl = metaData.images.poster;
                    }
                    Picasso.with(mHeaderImage.getContext()).load(imageUrl).into(mHeaderImage);
                }
            });
        } else {
            Picasso.with(mHeaderImage.getContext()).load(mEpisode.headerImage).into(mHeaderImage);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAttached = true;
    }

    private void updateMagnet() {
        if (mOpenMagnet == null) return;

        if (mMagnet == null) {
            mMagnet = new Magnet(mActivity, mEpisode.torrents.get(mSelectedQuality).url);
        }
        mMagnet.setUrl(mEpisode.torrents.get(mSelectedQuality).url);

        if (!mMagnet.canOpen()) {
            mOpenMagnet.setVisibility(View.GONE);
        } else {
            mOpenMagnet.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.synopsis)
    public void readMoreClick(View v) {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null)
            return;
        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle b = new Bundle();
        b.putString("text", mEpisode.overview);
        synopsisDialogFragment.setArguments(b);
        synopsisDialogFragment.show(getFragmentManager(), "overlay_fragment");
    }

    @OnClick(R.id.play_button)
    public void playClick() {
        smoothDismiss();
        Media.Torrent torrent = mEpisode.torrents.get(mSelectedQuality);
        StreamInfo streamInfo = new StreamInfo(mEpisode, mShow, torrent.url, mSelectedSubtitleLanguage, mSelectedQuality);
        ((MediaDetailActivity) getActivity()).playStream(streamInfo);
    }

    @Nullable
    @OnClick(R.id.magnet)
    public void openMagnet() {
        mMagnet.open(mActivity);
    }

    @OnClick(R.id.placeholder)
    public void outsideClick() {
        smoothDismiss();
    }

    private void onSubtitleLanguageSelected(String language) {
        mSelectedSubtitleLanguage = language;
        if (!language.equals("no-subs")) {
            final Locale locale = LocaleUtils.toLocale(language);
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSubtitles.setText(StringUtils.uppercaseFirst(locale.getDisplayName(locale)));
                }
            });
        } else {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSubtitles.setText(R.string.no_subs);
                }
            });
        }
    }

}
