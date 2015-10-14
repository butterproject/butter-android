package pct.droid.tv.activities.base;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.github.sv244.torrentstream.Torrent;

import pct.droid.base.updater.PopcornUpdater;
import pct.droid.base.utils.VersionUtils;
import pct.droid.tv.activities.PTVSearchActivity;

public abstract class PTVBaseActivity extends PTVTorrentBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState, int layoutId) {
		super.onCreate(savedInstanceState, layoutId);

		if(!VersionUtils.isUsingCorrectBuild()) {
			new AlertDialog.Builder(this)
					.setMessage(pct.droid.base.R.string.wrong_abi)
					.setCancelable(false)
					.show();

			PopcornUpdater.getInstance(this, new PopcornUpdater.Listener() {
				@Override
				public void updateAvailable(String updateFile) {
					Intent installIntent = new Intent(Intent.ACTION_VIEW);
					installIntent.setDataAndType(Uri.parse("file://" + updateFile), PopcornUpdater.ANDROID_PACKAGE);
					installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(installIntent);
				}
			}).checkUpdatesManually();
		}
	}

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