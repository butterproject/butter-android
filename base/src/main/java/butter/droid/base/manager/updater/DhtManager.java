package butter.droid.base.manager.updater;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.exceptions.NotInitializedException;

import org.json.JSONException;
import org.libtorrent4j.Hex;
import org.libtorrent4j.SessionManager;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

import butter.droid.base.R;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.utils.PrefUtils;

public class DhtManager implements Runnable {

    private Context mContext;
    private ObjectMapper mMapper;

    private static final int UPDATE_INTERVAL = 7 * 24 * 60 * 60;

    @Inject
    public DhtManager(Context context, ObjectMapper mapper)
    {
        this.mContext = context;
        this.mMapper = mapper;
    }

    public void updateSettings()
    {
        int updated = PrefUtils.get(mContext, Prefs.DHT_UPDATED, 0);
        if (updated + UPDATE_INTERVAL > System.currentTimeMillis() / 1000) {
            return;
        }

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
        String dhtPublicKey = mContext.getResources().getString(R.string.dht_public_key);
        SessionManager.MutableItem data = sm.dhtGetItem(Hex.decode(dhtPublicKey), Hex.decode(""), 20);
        int seq = PrefUtils.get(mContext, Prefs.DHT_SEQ, 0);
        if (data.seq <= seq) {
            return;
        }
        Log.d("DHT", "DHT updated " + data.seq);
        PrefUtils.save(mContext, Prefs.DHT_DATA, data.item.string());
        PrefUtils.save(mContext, Prefs.DHT_SEQ, data.seq);
        PrefUtils.save(mContext, Prefs.DHT_UPDATED, System.currentTimeMillis() / 1000);
    }

    public String[] servers()
    {
        String dht = PrefUtils.get(mContext, Prefs.DHT_DATA, "");
        if (dht.isEmpty()) {
            return new String[0];
        }
        try {
            DhtObject obj = mMapper.readValue(dht, DhtObject.class);
            return obj.getServers();
        } catch (JsonMappingException e) {
            return new String[0];
        } catch (JsonParseException e) {
            return new String[0];
        } catch (IOException e) {
            return new String[0];
        }
    }
}
