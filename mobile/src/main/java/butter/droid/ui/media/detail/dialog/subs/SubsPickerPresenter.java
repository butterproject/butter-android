package butter.droid.ui.media.detail.dialog.subs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.ui.media.detail.model.UiSubItem;

public interface SubsPickerPresenter {
    void onViewCreated(@NonNull MediaWrapper mediaWrapper, @Nullable Subtitle selected);

    void onSubsItemSelected(UiSubItem item);
}
