package butter.droid.ui.trailer;

import butter.droid.base.ui.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module
public class TrailerModule {

  private final TrailerView view;

  public TrailerModule(TrailerView view) {
    this.view = view;
  }

  @Provides
  @ActivityScope
  TrailerView provideView() {
    return view;
  }

  @Provides
  @ActivityScope
  TrailerPresenter providePresenter(TrailerView trailerView) {
    return new TrailerPresenterImpl(trailerView);
  }

}
