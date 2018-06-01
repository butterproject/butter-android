package butter.droid.ui.media.detail.dialog.subs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.ui.SubFragmentScope;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.ui.media.detail.model.UiSubItem;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@SubFragmentScope
public class SubsPickerPresenterImpl implements SubsPickerPresenter {

    private final SubsPickerView view;
    private final SubsPickerParent parent;
    private final ProviderManager providerManager;

    @Inject
    public SubsPickerPresenterImpl(final SubsPickerView view, final SubsPickerParent parent,
            final ProviderManager providerManager) {
        this.view = view;
        this.parent = parent;
        this.providerManager = providerManager;
    }


    @Override public void onViewCreated(@NonNull MediaWrapper mediaWrapper, @Nullable Subtitle selected) {
        String selectedLang = selected != null ? selected.getLanguage() : null;

        providerManager.getSubsProvider(mediaWrapper.getProviderId()).list(mediaWrapper.getMedia())
                .flatMap(subs -> {
//                    if (subs.isEmpty()) {
//                        return Single.<List<UiSubItem>>just(Collections.EMPTY_LIST);
//                    } else {
                    return Observable.fromIterable(subs)
                            .map(sub -> new UiSubItem(sub, sub.getLanguage().equals(selectedLang)))
                            .startWith(new UiSubItem(null, selectedLang == null))
                            .toList();
//                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<UiSubItem>>() {
                    @Override public void onSubscribe(final Disposable d) {
//                        subtitlesRequest = d;
                    }

                    @Override public void onSuccess(final List<UiSubItem> subs) {
                        if (subs.isEmpty()) {
                            // TODO display text there is no subtitles
//                            view.setSubtitleText(R.string.no_subs_available);
//                            subtitleList = null;
                            view.showSubtitles(subs);
                        } else {
                            view.showSubtitles(subs);
                        }

                    }

                    @Override public void onError(final Throwable e) {
                        // TODO
//                        view.setSubtitleText(R.string.no_subs_available);
                    }
                });
    }

    @Override public void onSubsItemSelected(UiSubItem item) {
        parent.subtitleSelected(item.getSubtitle());
        view.selfClose();
    }
}
