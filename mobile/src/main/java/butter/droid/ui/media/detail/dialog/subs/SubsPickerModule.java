package butter.droid.ui.media.detail.dialog.subs;

import dagger.Binds;
import dagger.Module;

@Module
public interface SubsPickerModule {

    @Binds SubsPickerView bindView(SubsPickerDialog dialog);
    
    @Binds SubsPickerPresenter bindPresenter(SubsPickerPresenterImpl presenter);

}
