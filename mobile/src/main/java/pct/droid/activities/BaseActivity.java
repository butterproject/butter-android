package pct.droid.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.bugsnag.android.Bugsnag;

import butterknife.ButterKnife;
import pct.droid.PopcornApplication;

public class BaseActivity extends ActionBarActivity {

    protected Handler mHandler;

    public void onCreate(Bundle savedInstanceState, int layoutId) {
        super.onCreate(savedInstanceState);
        Bugsnag.onActivityCreate(this);
        setContentView(layoutId);
        ButterKnife.inject(this);
        mHandler = new Handler(getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bugsnag.onActivityResume(this);
        getApp().startService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bugsnag.onActivityPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bugsnag.onActivityDestroy(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected PopcornApplication getApp() {
        return (PopcornApplication) getApplication();
    }

}