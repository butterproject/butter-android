package pct.droid;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.nodejs.core.NodeJSService;

public class PopcornApplication extends Application {

    private Boolean mBound;
    private NodeJSService mService;
    private ServiceConnection mExternalConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent nodeServiceIntent = new Intent(this, NodeJSService.class);
        bindService(nodeServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void setExternalConnection(ServiceConnection connection) {
        mExternalConnection = connection;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            NodeJSService.ServiceBinder binder = (NodeJSService.ServiceBinder) service;
            mService = binder.getService();
            mBound = true;
            if(mExternalConnection != null) mExternalConnection.onServiceConnected(className, service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            if(mExternalConnection != null) mExternalConnection.onServiceDisconnected(componentName);
        }
    };



}
