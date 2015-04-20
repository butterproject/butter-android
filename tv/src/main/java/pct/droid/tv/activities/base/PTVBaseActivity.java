package pct.droid.tv.activities.base;

import android.support.v4.app.FragmentActivity;

import pct.droid.base.activities.TorrentBaseActivity;
import pct.droid.tv.activities.PTVSearchActivity;

public abstract class PTVBaseActivity extends PTVTorrentBaseActivity {

	@Override
	public boolean onSearchRequested() {
		PTVSearchActivity.startActivity(this);
		return true;
	}
}
