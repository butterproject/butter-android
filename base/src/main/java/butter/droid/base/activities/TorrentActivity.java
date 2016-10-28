package butter.droid.base.activities;

import butter.droid.base.torrent.TorrentService;

public interface TorrentActivity {

	TorrentService getTorrentService();

	void onTorrentServiceConnected();

	void onTorrentServiceDisconnected();
}