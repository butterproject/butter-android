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
    import butter.droid.R;
    import butter.droid.base.content.preferences.PreferencesHandler;
    import butter.droid.base.manager.internal.media.MediaDisplayManager;
    import butter.droid.base.manager.internal.provider.ProviderManager;
    import butter.droid.base.providers.media.model.MediaMeta;
    import butter.droid.base.providers.media.model.MediaWrapper;
    import butter.droid.base.providers.media.model.StreamInfo;
    import butter.droid.base.providers.meta.MetaProvider;
    import butter.droid.base.torrent.Magnet;
    import butter.droid.base.utils.LocaleUtils;
    import butter.droid.base.utils.PixelUtils;
    import butter.droid.base.utils.StringUtils;
    import butter.droid.base.utils.ThreadUtils;
    import butter.droid.provider.base.model.Episode;
    import butter.droid.provider.base.model.Format;
    import butter.droid.provider.base.model.Torrent;
    import butter.droid.ui.media.detail.model.UiSubItem;
    import butter.droid.ui.media.detail.movie.dialog.SynopsisDialogFragment;
    import butter.droid.widget.BottomSheetScrollView;
    import butter.droid.widget.OptionPreview;
    import butter.droid.widget.OptionSelector;
    import butterknife.BindView;
    import butterknife.ButterKnife;
    import butterknife.OnClick;
    import com.squareup.picasso.Picasso;
    import dagger.android.support.DaggerAppCompatDialogFragment;
    import io.reactivex.Observable;
    import io.reactivex.Single;
    import io.reactivex.SingleObserver;
    import io.reactivex.android.schedulers.AndroidSchedulers;
    import io.reactivex.disposables.Disposable;
    import io.reactivex.schedulers.Schedulers;
    import java.util.Collections;
    import java.util.List;
    import java.util.Locale;
    import javax.inject.Inject;
    import org.parceler.Parcels;

public class EpisodeDialogFragment extends DaggerAppCompatDialogFragment {

    private static final String EXTRA_MEDIA_META = "butter.droid.ui.media.detail.dialog.EpisodeDialogFragment.mediaMeta";
    private static final String EXTRA_EPISODE = "butter.droid.ui.media.detail.dialog.EpisodeDialogFragment.episode";

    private static final int ANIM_SPEED = 200;

    @Inject ProviderManager providerManager;
    @Inject PreferencesHandler preferencesHandler;
    @Inject MediaDisplayManager mediaDisplayManager;

    private Integer threshold = 0;
    private Integer bottom = 0;
    private Activity activity;
    private MetaProvider metaProvider;
    private boolean touching;
    private boolean opened;
    private String selectedSubtitleLanguage;
    private Torrent selectedTorrent;
    private MediaMeta mediaMeta;
    private Episode episode;
    private Magnet magnet;

    @BindView(R.id.scrollview) BottomSheetScrollView scrollView;
    @BindView(R.id.placeholder) View placeholder;
    @BindView(R.id.play_button) ImageButton playButton;
    @BindView(R.id.header_image) ImageView headerImage;
    @BindView(R.id.info) TextView info;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.aired) TextView aired;
    @BindView(R.id.synopsis) TextView synopsis;
    @BindView(R.id.subtitles) OptionPreview subtitlesPreview;
    @BindView(R.id.quality) OptionSelector quality;
    @BindView(R.id.magnet) @Nullable ImageButton openMagnet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_Episode);
        setCancelable(false);

        activity = getActivity();
        threshold = PixelUtils.getPixelsFromDp(activity, 220);
        bottom = PixelUtils.getPixelsFromDp(activity, 33);
        Bundle args = getArguments();
        mediaMeta = args.getParcelable(EXTRA_MEDIA_META);
        episode = Parcels.unwrap(args.getParcelable(EXTRA_EPISODE));
        // TODO: 6/17/17
        //        metaProvider = episode.getMetaProvider();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.Theme_Butter))
                .inflate(R.layout.fragment_dialog_episode, container, false);
        ButterKnife.bind(this, view);

        if (mediaMeta.hasColor()) {
            playButton.setBackground(PixelUtils.changeDrawableColor(playButton.getContext(), R.drawable.play_button_circle,
                    mediaMeta.getColor()));
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) placeholder.getLayoutParams();
        layoutParams.height = PixelUtils.getScreenHeight(activity);
        placeholder.setLayoutParams(layoutParams);

        return view;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                smoothDismiss();
            }
            return true;
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
        scrollView.postDelayed(this::dismiss, ANIM_SPEED);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (null != metaProvider) {
            metaProvider.cancel();
        }

        // TODO: 11/4/17 Subs
//        if (providerManager.hasCurrentSubsProvider()) {
//            providerManager.getCurrentSubsProvider().cancel();
//        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView.postDelayed(() -> {
            //mMaxHeight = PixelUtils.getScreenHeight(activity) - PixelUtils.getPixelsFromDp(activity, 50);
            int screenHeight = PixelUtils.getScreenHeight(activity);
            int scroll = (screenHeight / 3) * 2;
            scrollView.animateScrollTo(scroll, ANIM_SPEED);
            scrollView.postDelayed(() -> opened = true, ANIM_SPEED);
        }, 250);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!TextUtils.isEmpty(episode.getTitle())) {
            title.setText(episode.getTitle());
            headerImage.setContentDescription(episode.getTitle());
        } else {
            title.setText(R.string.no_title_available);
            headerImage.setContentDescription(getString(R.string.no_title_available));
        }

        // TODO: 6/17/17 meta provider
//        aired.setVisibility(episode.aired > 0 ? View.VISIBLE : View.GONE);
//        Date airedDate = new Date((long) episode.aired * 1000);
//        aired.setText(String.format(getString(R.string.aired), new SimpleDateFormat("MMMM dd, yyyy", LocaleUtils.getCurrent())
//                .format(airedDate)));

//        if (!TextUtils.isEmpty(episode.getOverview())) {
//            synopsis.setText(episode.getOverview());
//        } else {
//            synopsis.setText(R.string.no_synopsis_available);
//        }

//        info.setText(String.format(Locale.US, "S%02dE%02d", episode.getSeasion(), episode.getEpisode()));

//        subtitlesPreview.setFragmentManager(getFragmentManager());

        final Format[] formats = mediaDisplayManager.getSortedTorrentFormats(episode.getTorrents());
        String[] formatDisplay = new String[formats.length];
        for (int i = 0; i < formats.length; i++) {
            formatDisplay[i] = mediaDisplayManager.getFormatDisplayName(formats[i]);
        }
        quality.setData(formatDisplay);

        int defaultFormatIndex = mediaDisplayManager.getDefaultFormatIndex(formats);
//        // TODO: 7/30/17 Handle sorting
//        selectedTorrent = episode.getTorrents()[defaultFormatIndex];
        this.quality.setText(formatDisplay[defaultFormatIndex]);
        this.quality.setDefault(defaultFormatIndex);
//
//        updateMagnet();
//
        this.quality.setListener((position, value) -> {
            selectedTorrent = episode.getTorrents()[position];
            updateMagnet();
        });

        if (providerManager.hasCurrentSubsProvider()) {
            subtitlesPreview.setText(R.string.loading_subs);
            subtitlesPreview.setClickable(false);

            providerManager.getCurrentSubsProvider().list(episode)
                    .flatMap(subs -> {
                        if (subs.isEmpty()) {
                            return Single.<List<UiSubItem>>just(Collections.EMPTY_LIST);
                        } else {
                            final String defaultSubtitle = preferencesHandler.getSubtitleDefaultLanguage();
                            return Observable.fromIterable(subs)
                                    .map(sub -> new UiSubItem(sub, defaultSubtitle.equals(sub.getLanguage())))
                                    .startWith(new UiSubItem(null, defaultSubtitle == null))
                                    .toList();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<List<UiSubItem>>() {
                        @Override public void onSubscribe(final Disposable d) {
                            // TODO dispose
                        }

                        @Override public void onSuccess(final List<UiSubItem> subs) {
                            if (subs.isEmpty()) {
                                subtitlesPreview.setText(R.string.no_subs_available);
//                                subtitleList = null;
                            } else {
                                subtitlesPreview.setEnabled(true);
//                                subtitleList = subs;

                                UiSubItem selectedItem = null;
                                for (final UiSubItem sub : subs) {
                                    if (sub.isSelected()) {
                                        selectedItem = sub;
                                        String name = sub.getName();
                                        if (TextUtils.isEmpty(name)) {
                                            subtitlesPreview.setText(R.string.no_subs);
                                        } else {
                                            subtitlesPreview.setText(name);
                                        }
                                        break;
                                    }
                                }
                                if (selectedItem == null) {
                                    selectedItem = subs.get(0);
                                }

//                                selectedSub = selectedItem;
                            }
                        }

                        @Override public void onError(final Throwable e) {
//                            subtitleList = null;
                            subtitlesPreview.setText(R.string.no_subs_available);
                            subtitlesPreview.setEnabled(false);
                        }
                    });
        } else {
            subtitlesPreview.setText(R.string.no_subs_available);
            subtitlesPreview.setClickable(false);
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
            // TODO: 6/17/17 meta
            //            metaProvider.getEpisodeMeta(episode.imdbId, episode.getSeasion(), episode.getEpisode(),
//                    (Callback) (metaData, ex) -> {
//                        String imageUrl = episode.getBackdrop();
//                        if (ex == null) {
//                            imageUrl = metaData.images.poster;
//                        }
//                        Picasso.with(headerImage.getContext()).load(imageUrl).into(headerImage);
//                    });
        } else {
            Picasso.with(headerImage.getContext()).load(episode.getBackdrop()).into(headerImage);
        }
    }

    private void updateMagnet() {
        if (openMagnet == null) {
            return;
        }
        if (magnet == null) {
            magnet = new Magnet(activity, selectedTorrent.getUrl());
        } else {
            magnet.setUrl(selectedTorrent.getUrl());
        }

        if (!magnet.canOpen()) {
            openMagnet.setVisibility(View.GONE);
        } else {
            openMagnet.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.synopsis)
    public void readMoreClick() {
        if (getFragmentManager().findFragmentByTag("overlay_fragment") != null) {
            return;
        }
        SynopsisDialogFragment synopsisDialogFragment = new SynopsisDialogFragment();
        Bundle args = new Bundle();
//        args.putString("text", ((Episode) episodeWrapper.getMedia()).getOverview());
        synopsisDialogFragment.setArguments(args);
        synopsisDialogFragment.show(getFragmentManager(), "overlay_fragment");
    }

    @OnClick(R.id.play_button)
    public void playClick() {
        smoothDismiss();
        // TODO Subs & parent
//        StreamInfo streamInfo = new StreamInfo(selectedTorrent, episodeWrapper, showWrapper, null);
        StreamInfo streamInfo = new StreamInfo(selectedTorrent, new MediaWrapper(episode, mediaMeta), null, null);
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
            ThreadUtils.runOnUiThread(() -> subtitlesPreview.setText(StringUtils.uppercaseFirst(locale.getDisplayName(locale))));
        } else {
            ThreadUtils.runOnUiThread(() -> subtitlesPreview.setText(R.string.no_subs));
        }
    }

    public static EpisodeDialogFragment newInstance(MediaMeta mediaMeta, Episode episode) {
        EpisodeDialogFragment frag = new EpisodeDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_MEDIA_META, mediaMeta);
        args.putParcelable(EXTRA_EPISODE, Parcels.wrap(episode));
        frag.setArguments(args);
        return frag;
    }

    public interface FragmentListener {

        void playStream(StreamInfo streamInfo);
    }

}
