package butter.droid.ui.media.detail.dialog.subs;

import java.util.List;

import butter.droid.ui.media.detail.model.UiSubItem;

public interface SubsPickerView {
    void showSubtitles(List<UiSubItem> subs);

    void selfClose();
}
