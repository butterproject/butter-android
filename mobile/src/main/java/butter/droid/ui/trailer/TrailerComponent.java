package butter.droid.ui.trailer;

import butter.droid.base.ui.ActivityScope;
import dagger.Subcomponent;

@Subcomponent(
    modules = TrailerModule.class
)
@ActivityScope
public interface TrailerComponent {

  void inject(TrailerPlayerActivity activity);

  @Subcomponent.Builder
  interface Builder {

    Builder trailerModule(TrailerModule module);

    TrailerComponent build();
  }

}
