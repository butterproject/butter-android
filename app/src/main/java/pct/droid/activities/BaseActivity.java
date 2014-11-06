package pct.droid.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import org.nodejs.core.NodeJSService;
import android.content.Intent;
import butterknife.ButterKnife;
import pct.droid.PopcornApplication;
import pct.droid.R;

public class BaseActivity extends ActionBarActivity {

    protected Handler mHandler;

    public void onCreate(Bundle savedInstanceState, int layoutId) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);
        ButterKnife.inject(this);
        mHandler = new Handler(getMainLooper());
    }

    protected View getActionBarView() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        int resId = getResources().getIdentifier("toolbar", "id", getPackageName());
        return decorView.findViewById(resId);
    }

    protected PopcornApplication getApp() {
        return (PopcornApplication) getApplication();
    }

}
