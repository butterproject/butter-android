package pct.droid.tv.activities.base;

import com.github.sv244.torrentstream.Torrent;

import pct.droid.tv.activities.PTVSearchActivity;

public abstract class PTVBaseActivity extends PTVTorrentBaseActivity {

	@Override
	public boolean onSearchRequested() {
		PTVSearchActivity.startActivity(this);
		return true;
	}

	@Override
	public void onStreamPrepared(Torrent torrent) {
		super.onStreamPrepared(torrent);

		// todo?
	}

}