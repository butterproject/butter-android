package pct.droid;

import android.app.Application;
import android.content.Intent;

import org.nodejs.core.NodeJSService;

public class PopcornApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, NodeJSService.class));
    }

}
