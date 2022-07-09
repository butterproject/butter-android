package butter.droid.base.manager.updater;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.exceptions.NotInitializedException;

import org.libtorrent4j.AlertListener;
import org.libtorrent4j.Entry;
import org.libtorrent4j.Hex;
import org.libtorrent4j.SessionManager;
import org.libtorrent4j.Sha1Hash;
import org.libtorrent4j.TcpEndpoint;
import org.libtorrent4j.alerts.Alert;

import java.util.ArrayList;

import javax.inject.Inject;

import butter.droid.base.R;

public class DhtManager implements Runnable {

    private Context mContext;

    @Inject
    public DhtManager(Context context)
    {
        this.mContext = context;
    }

    public void updateSettings()
    {
        try {
            SessionManager sm = TorrentStream.getInstance().getSessionManager();
            this.updateFromDHT(sm);
            return;
        } catch (NotInitializedException e) {
            Log.d("DHT", "NotInitializedException");
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this, 5000);
    }

    @Override
    public void run() {
        this.updateSettings();
    }

    protected void updateFromDHT(SessionManager sm) {
        Log.d("DHT", "DHT contains " + sm.stats().dhtNodes() + " nodes");
        //String dht = mContext.getResources().getString(R.string.dht);
        String dht = "a4a9ad29e303e137ecb995c50a4e104b3e8f72e5";
        Log.d("DHT", "HERE");
        byte[] publicKey = Hex.decode("50b1e3e01d68ff20664b22881b3ae1a7b4c0b2892a7bd68e40dbadea3ab45790");
        byte[] salt = Hex.decode("");
        SessionManager.MutableItem data = sm.dhtGetItem(publicKey, salt, 20);
        Log.d("DHT", data.toString());
        Log.d("DHT", data.item.toString());
        Log.d("DHT", String.valueOf(data.seq));
        Entry e = sm.dhtGetItem(Sha1Hash.parseHex(dht), 100);
        Log.d("DHT", e.toString());
    }
}
