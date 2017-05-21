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

package butter.droid.ui.media.detail.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.providers.media.models.Episode;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Show;
import butter.droid.base.providers.meta.MetaProvider;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.torrent.Magnet;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.FragmentUtil;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.PixelUtils;
import butter.droid.base.utils.SortUtils;
import butter.droid.base.utils.StringUtils;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.ui.media.detail.movie.dialog.SynopsisDialogFragment;
import butter.droid.widget.BottomSheetScrollView;
import butter.droid.widget.OptionSelector;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;

public class EpisodeDialogFragment extends DialogFragment {

    private static final String EXTRA_EPISODE = "butter.droid.ui.media.detail.dialog.EpisodeDialogFragment.episode";
    private static final String EXTRA_SHOW = "butter.droid.ui.media.detail.dialog.EpisodeDialogFragment.show";

    private static final int ANIM_SPEED = 200;

    @Inject ProviderManager providerManager;
    @Inject PreferencesHandler preferencesHandler;
    @Inject PlayerManager playerManager;

    private Integer threshold = 0;
    private Integer bottom = 0;
    private Activity activity;
    private MetaProvider metaProvider;
    private boolean touching;
    private boolean opened;
    private String selectedSubtitleLanguage;
    private String selectedQuality;
    private Episode episode;
    private Show show;
    private Magnet magnet;

    @BindView(R.id.scrollview) BottomSheetScrollView scrollView;
    @BindView(R.id.placeholder) View placeholder;
    @BindView(R.id.play_button) ImageButton playButton;
    @BindView(R.id.header_image) ImageView headerImage;
    @BindView(R.id.info) TextView info;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.aired) TextView aired;
    @BindView(R.id.synopsis) TextView synopsis;
    @BindView(R.id.subtitles) OptionSelector subtitles;
    @BindView(R.id.quality) OptionSelector quality;
    @BindView(R.id.magnet) @Nullable ImageButton openMagnet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.Theme_Butter))
                .inflate(R.layout.fragment_dialog_episode, container, false);
        ButterKnife.bind(this, view);

        playButton.setBackground(PixelUtils.changeDrawableColor(playButton.getContext(), R.drawable.play_button_circle, show.color));

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) placeholder.getLayoutParams();
        layoutParams.height = PixelUtils.getScreenHeight(activity);
        placeholder.setLayoutParams(layoutParams);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileButterApplication.getAppContext()
                .getComponent()
                .inject(this);

        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_Episode);
        setCancelable(false);

        activity = getActivity();
        threshold = PixelUtils.getPixelsFromDp(activity, 220);
        bottom = PixelUtils.getPixelsFromDp(activity, 33);
        show = getArguments().getParcelable(EXTRA_SHOW);
        episode = getArguments().getParcelable(EXTRA_EPISODE);
        metaProvider = episode.getMetaProvider();
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
        opened = false;

        if (scrollView.getScrollY() <= bottom) {
            dismiss();
            return;
        }

        scrollView.animateScrollTo(0, ANIM_SPEED);
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, ANIM_SPEED);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (null != metaProvider) {
            metaProvider.cancel();
        }
        if (providerManager.hasCurrentSubsProvider()) {
            providerManager.getCurrentSubsProvider().cancel();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //mMaxHeight = PixelUtils.getScreenHeight(activity) - PixelUtils.getPixelsFromDp(activity, 50);
                int screenHeight = PixelUtils.getScreenHeight(activity);
                int scroll = (screenHeight / 3) * 2;
                scrollView.animateScrollTo(scroll, ANIM_SPEED);
                scrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        opened = true;
                    }
                }, ANIM_SPEED);
            }
        }, 250);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!TextUtils.isEmpty(episode.title)) {
            title.setText(episode.title);
            headerImage.setContentDescription(episode.title);
        } else {
            title.setText(R.string.no_title_available);
            headerImage.setContentDescription(getString(R.string.no_title_available));
        }

        aired.setVisibility(episode.aired > 0 ? View.VISIBLE : View.GONE);
        Date airedDate = new Date((long) episode.aired * 1000);
        aired.setText(String.format(getString(R.string.aired),
                new SimpleDateFormat("MMMM dd, yyyy", LocaleUtils.getCurrent()).format(airedDate)));

        if (!TextUtils.isEmpty(episode.overview)) {
            synopsis.setText(episode.overview);
        } else {
            synopsis.setText(R.string.no_synopsis_available);
        }

        String seasonStr = Integer.toString(episode.season);
        if (seasonStr.length() < 2) {
            seasonStr = "0" + seasonStr;
        }
        String episodeStr = Integer.toString(episode.episode);
        if (episodeStr.length() < 2) {
            episodeStr = "0" + episodeStr;
        }

        info.setText("S" + seasonStr + "E" + episodeStr);

        subtitles.setFragmentManager(getFragmentManager());
        quality.setFragmentManager(getFragmentManager());
        subtitles.setTitle(R.string.subtitles);
        quality.setTitle(R.string.quality);

        final String[] qualities = episode.torrents.keySet().toArray(new String[episode.torrents.size()]);
        SortUtils.sortQualities(qualities);
        quality.setData(qualities);

        String quality = playerManager.getDefaultQuality(Arrays.asList(qualities));
        int qualityIndex = Arrays.asList(qualities).indexOf(quality);
        selectedQuality = quality;
        this.quality.setText(selectedQuality);
        this.quality.setDefault(qualityIndex);

        updateMagnet();

        this.quality.setListener(new OptionSelector.SelectorListener() {
            @Override
            public void onSelectionChanged(int position, String value) {
                selectedQuality = value;
                updateMagnet();
            }
        });

        subtitles.setText(R.string.loading_subs);
        subtitles.setClickable(false);
        if (providerManager.hasCurrentSubsProvider()) {
            providerManager.getCurrentSubsProvider().getList(episode, new SubsProvider.Callback() {
                @Override
                public void onSuccess(Map<String, String> subtitles) {
                    if (!FragmentUtil.isAdded(EpisodeDialogFragment.this)) {
                        return;
                    }

                    episode.subtitles = subtitles;

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

                    EpisodeDialogFragment.this.subtitles.setListener(new OptionSelector.SelectorListener() {
                        @Override
                        public void onSelectionChanged(int position, String value) {
                            onSubtitleLanguageSelected(adapterLanguages[position]);
                        }
                    });
                    EpisodeDialogFragment.this.subtitles.setData(readableNames);
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            EpisodeDialogFragment.this.subtitles.setClickable(true);
                        }
                    });

                    String defaultSubtitle = preferencesHandler.getSubtitleDefaultLanguage();
                    if (subtitles.containsKey(defaultSubtitle)) {
                        onSubtitleLanguageSelected(defaultSubtitle);
                        EpisodeDialogFragment.this.subtitles.setDefault(Arrays.asList(adapterLanguages).indexOf(defaultSubtitle));
                    } else {
                        onSubtitleLanguageSelected(SubsProvider.SUBTITLE_LANGUAGE_NONE);
                        EpisodeDialogFragment.this.subtitles.setDefault(
                                Arrays.asList(adapterLanguages).indexOf(SubsProvider.SUBTITLE_LANGUAGE_NONE));
                    }
                }

                @Override
                public void onFailure(Exception ex) {
                    subtitles.setData(new String[0]);
                    subtitles.setClickable(true);
                }
            });
        } else {
            subtitles.setText(R.string.no_subs_available);
        }

        scrollView.setListener(new BottomSheetScrollView.Listener() {
            @Override
            public void onScroll(int scrollY, BottomSheetScrollView.Direction direction) {
            }

            @Override
            public void onTouch(boolean touching) {
                EpisodeDialogFragment.this.touching = touching;
                int scrollY = scrollView.getScrollY();
                if (!EpisodeDialogFragment.this.touching && opened && scrollY <= threshold) {
                    smoothDismiss();
                }
            }

            @Override
            public void onScrollStart() {
            }

            @Override
            public void onScrollEnd() {
                if (!touching && opened && scrollView.getScrollY() <= threshold) {
                    smoothDismiss();
                }
            }
        });

        if (metaProvider != null) {
            metaProvider.getEpisodeMeta(episode.imdbId, episode.season, episode.episode,
                    new MetaProvider.Callback() {
                        @Override
                        public void onResult(MetaProvider.MetaData metaData, Exception ex) {
                            String imageUrl = episode.headerImage;
                            if (ex == null) {
                                imageUrl = metaData.images.poster;
                            }
                            Picasso.with(headerImage.getContext()).load(imageUrl).into(headerImage);
                        }
                    });
        } else {
            Picasso.with(headerImage.getContext()).load(episode.headerImage).into(headerImage);
        }
    }

    private void updateMagnet() {
        if (openMagnet == null) {
            return;
        }

        if (magnet == null) {
            magnet = new Magnet(activity, episode.torrents.get(selectedQuality).url);
        }
        magnet.setUrl(episode.torrents.get(selectedQuality).url);

        if (!magnet.canOpen()) {
            openMagnet.setVisibility(View.GONE);
        } else {
            openMagnet.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.synopsis)
    public void readMoreClick(View view) {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null) {
            return;
        }
        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle args = new Bundle();
        args.putString("text", episode.overview);
        synopsisDialogFragment.setArguments(args);
        synopsisDialogFragment.show(getFragmentManager(), "overlay_fragment");
    }

    @OnClick(R.id.play_button)
    public void playClick() {
        smoothDismiss();
        Media.Torrent torrent = episode.torrents.get(selectedQuality);
        StreamInfo streamInfo = new StreamInfo(episode, show, torrent.url, selectedSubtitleLanguage,
                selectedQuality);
        ((FragmentListener) getActivity()).playStream(streamInfo);
    }

    @Nullable
    @OnClick(R.id.magnet)
    public void openMagnet() {
        magnet.open(activity);
    }

    @OnClick(R.id.placeholder)
    public void outsideClick() {
        smoothDismiss();
    }

    private void onSubtitleLanguageSelected(String language) {
        selectedSubtitleLanguage = language;
        if (!language.equals("no-subs")) {
            final Locale locale = LocaleUtils.toLocale(language);
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    subtitles.setText(StringUtils.uppercaseFirst(locale.getDisplayName(locale)));
                }
            });
        } else {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    subtitles.setText(R.string.no_subs);
                }
            });
        }
    }

    public static EpisodeDialogFragment newInstance(Show show, Episode episode) {
        EpisodeDialogFragment frag = new EpisodeDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_SHOW, show);
        args.putParcelable(EXTRA_EPISODE, episode);
        frag.setArguments(args);
        return frag;
    }

    public interface FragmentListener {

        void playStream(StreamInfo streamInfo);
    }

}
