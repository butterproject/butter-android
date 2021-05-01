package com.connectsdk.service.airplay;

import android.annotation.SuppressLint;
import android.util.Log;

import com.connectsdk.core.Util;
import com.connectsdk.etc.helper.HttpMessage;
import com.connectsdk.service.DeviceService.PairingType;
import com.connectsdk.service.airplay.auth.AirPlayAuth;
import com.connectsdk.service.airplay.auth.AuthUtils;
import com.connectsdk.service.command.ServiceCommand;
import com.connectsdk.service.command.ServiceCommand.ServiceCommandProcessor;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;
import com.connectsdk.service.command.URLServiceSubscription;
import com.connectsdk.service.config.AirPlayServiceConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@SuppressLint("DefaultLocale")
public class AirPlayServiceSocketClient implements ServiceCommandProcessor {
    int PORT = 7000;
    PairingType mPairingType;

    public enum State {
        NONE,
        INITIAL,
        CONNECTING,
        REGISTERING,
        REGISTERED,
        DISCONNECTING
    };

    State state = State.INITIAL;

    AirPlayServiceSocketClientListener mListener;
    AirPlayAuth airPlayAuth;
    Socket socket;
    AirPlayServiceConfig mconfig;

    public String getAuthToken() {
        return mconfig.getAuthToken();
    }

    public void setListener(AirPlayServiceSocketClient.AirPlayServiceSocketClientListener mListener) {
        this.mListener = mListener;
    }

    public AirPlayServiceSocketClient(AirPlayServiceConfig config, PairingType pairingType , String ipAddress) {
        this.mPairingType = pairingType;
        this.mconfig = config;
        if (mconfig.getAuthToken() == "") {
            mconfig.setAuthToken(AirPlayAuth.generateNewAuthToken());
        }
        airPlayAuth = new AirPlayAuth(new InetSocketAddress(ipAddress, PORT), mconfig.getAuthToken());
        state = State.INITIAL;
    }

    public State getState() {
        return state;
    }

    public boolean isConnected() {
        return this.getState() == State.REGISTERED;
    }

    public void pair(final String pin) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    airPlayAuth.doPairing(pin);
                    socket = airPlayAuth.authenticate();
                    state = State.REGISTERED;
                    if (mListener != null) {
                        mListener.onConnect();
                    }
                } catch (Exception ex) {
                    state = State.INITIAL;
                    ex.printStackTrace();
                    mListener.onRegistrationFailed(new ServiceCommandError(ex.toString()));
                }
            }
        }).start();
    }

    public void connect() {
        synchronized (this) {
            if (state != State.INITIAL) {
                Log.w(Util.T, "already connecting; not trying to connect again: " + state);
                return; // don't try to connect again while connected
            }

            state = State.CONNECTING;
        }

        try {
            socket = airPlayAuth.authenticate();
            state = State.REGISTERED;
            if (mListener != null) {
                mListener.onConnect();
            }
        } catch (Exception e) {
            try {
                airPlayAuth.startPairing();
                if (mListener != null)
                    mListener.onBeforeRegister(mPairingType);
            } catch (Exception ex) {
                ex.printStackTrace();
                mListener.onRegistrationFailed(new ServiceCommandError(ex.toString()));
            }
        }

        int count = 0;
        while(state == State.CONNECTING){
            try {
                count++;
                Thread.sleep(100);
                if (count > 200) {
                    mListener.onRegistrationFailed(new ServiceCommandError("Pairing Timeout"));
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                mListener.onRegistrationFailed(new ServiceCommandError(e.toString()));
            }
        }
    }

    public void disconnect() {
        this.mPairingType = null;
        this.mconfig = null;
        airPlayAuth = null;
        state = State.INITIAL;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectWithError(ServiceCommandError error) {
        state = State.INITIAL;

        if (mListener != null)
            mListener.onCloseWithError(error);
    }

    public void sendCommand(final ServiceCommand<?> serviceCommand) {
        new Thread(new Runnable() {
            public void run() {
                byte[] payload = (byte[]) serviceCommand.getPayload();
                String url = serviceCommand.getTarget();

                try {
                    if (serviceCommand.getHttpMethod().equalsIgnoreCase(ServiceCommand.TYPE_GET)) {
                        Util.postSuccess(serviceCommand.getResponseListener(), AuthUtils.getData(socket, url));
                    } else if (serviceCommand.getHttpMethod().equalsIgnoreCase(ServiceCommand.TYPE_POST)) {
                        Util.postSuccess(serviceCommand.getResponseListener(),
                                AuthUtils.postData(socket, url, HttpMessage.CONTENT_TYPE_APPLICATION_PLIST, payload));
                    } else if (serviceCommand.getHttpMethod().equalsIgnoreCase(ServiceCommand.TYPE_PUT)) {
                        Util.postSuccess(serviceCommand.getResponseListener(),
                                AuthUtils.putData(socket, url, HttpMessage.CONTENT_TYPE_APPLICATION_PLIST, payload));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    String[] tmp = e.getMessage().split(" ");
                    int statusCode = Integer.parseInt(tmp[tmp.length - 1]);
                    Util.postError(serviceCommand.getResponseListener(), ServiceCommandError.getError(statusCode));
                }
            }
        }).start();
    }

    public void unsubscribe(URLServiceSubscription<?> subscription) { }

    public void unsubscribe(ServiceSubscription<?> subscription) { }

    public interface AirPlayServiceSocketClientListener {

        public void onConnect();
        public void onCloseWithError(ServiceCommandError error);
        public void onFailWithError(ServiceCommandError error);

        public void onBeforeRegister(PairingType pairingType);
        public void onRegistrationFailed(ServiceCommandError error);
        public Boolean onReceiveMessage(JSONObject message);
    }
}
