package pct.droid.tv.interfaces.main;

import java.util.ArrayList;

import pct.droid.base.providers.media.types.Media;

public interface MainDataManagerCallback {
	public void onSuccess(ArrayList<Media> items);

	public void onFailure(Exception e);
}
