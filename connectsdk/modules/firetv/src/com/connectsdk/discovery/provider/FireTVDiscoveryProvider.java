/*
 * FireTVDiscoveryProvider
 * Connect SDK
 *
 * Copyright (c) 2015 LG Electronics.
 * Created by Oleksii Frolov on 08 Jul 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connectsdk.discovery.provider;

import android.content.Context;

import com.amazon.whisperplay.fling.media.controller.DiscoveryController;
import com.amazon.whisperplay.fling.media.controller.RemoteMediaPlayer;
import com.connectsdk.core.Util;
import com.connectsdk.discovery.DiscoveryFilter;
import com.connectsdk.discovery.DiscoveryProvider;
import com.connectsdk.discovery.DiscoveryProviderListener;
import com.connectsdk.service.FireTVService;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.config.ServiceDescription;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FireTVDiscoveryProvider provides discovery implementation for FireTV devices.
 * FireTVDiscoveryProvider acts as a layer on top of Fling SDK, and requires the Fling SDK library
 * to function. This implementation does not use discovery filters because only one type of service
 * can be discovered. Currently it can discover only FireTV device with default media player
 * application.
 *
 * Using Connect SDK for discovery/control of FireTV devices will result in your app complying with
 * the Fling SDK terms of service.
 */
public class FireTVDiscoveryProvider implements DiscoveryProvider {

    private DiscoveryController discoveryController;

    private boolean isRunning;

    DiscoveryController.IDiscoveryListener fireTVListener;

    ConcurrentHashMap<String, ServiceDescription> foundServices
            = new ConcurrentHashMap<>();

    CopyOnWriteArrayList<DiscoveryProviderListener> serviceListeners
            = new CopyOnWriteArrayList<>();

    public FireTVDiscoveryProvider(Context context) {
        this(new DiscoveryController(context));
    }

    public FireTVDiscoveryProvider(DiscoveryController discoveryController) {
        this.discoveryController = discoveryController;
        this.fireTVListener = new FireTVDiscoveryListener();
    }

    /**
     * Safely start discovery. Ignore if it's already started.
     */
    @Override
    public void start() {
        if (!isRunning) {
            discoveryController.start(fireTVListener);
            isRunning = true;
        }
    }

    /**
     * Safely stop discovery and remove all found FireTV services because they don't work when
     * discovery is stopped. Ignore if it's already stopped.
     */
    @Override
    public void stop() {
        if (isRunning) {
            discoveryController.stop();
            isRunning = false;
        }
        for (ServiceDescription serviceDescription : foundServices.values()) {
            notifyListenersThatServiceLost(serviceDescription);
        }
        foundServices.clear();
    }

    /**
     * Safely restart discovery
     */
    @Override
    public void restart() {
        stop();
        start();
    }

    /**
     * Invokes restart method since FlingSDK doesn't have analog of rescan
     */
    @Override
    public void rescan() {
        // discovery controller doesn't have rescan capability
        restart();
    }

    /**
     * Stop discovery and removes all cached services
     */
    @Override
    public void reset() {
        foundServices.clear();
        stop();
    }

    @Override
    public void addListener(DiscoveryProviderListener listener) {
        serviceListeners.add(listener);
    }

    @Override
    public void removeListener(DiscoveryProviderListener listener) {
        serviceListeners.remove(listener);
    }

    /**
     * DiscoveryFilter is not used in current implementation
     */
    @Override
    public void addDeviceFilter(DiscoveryFilter filter) {
        // intentionally left blank
    }

    /**
     * DiscoveryFilter is not used in current implementation
     */
    @Override
    public void removeDeviceFilter(DiscoveryFilter filter) {
        // intentionally left blank
    }

    /**
     * DiscoveryFilter is not used in current implementation
     */
    @Override
    public void setFilters(List<DiscoveryFilter> filters) {
        // intentionally left blank
    }

    @Override
    public boolean isEmpty() {
        return foundServices.isEmpty();
    }

    private void notifyListenersThatServiceAdded(final ServiceDescription serviceDescription) {
        Util.runOnUI(new Runnable() {
            @Override
            public void run() {
                for (DiscoveryProviderListener listener : serviceListeners) {
                    listener.onServiceAdded(FireTVDiscoveryProvider.this, serviceDescription);
                }
            }
        });
    }

    private void notifyListenersThatServiceLost(final ServiceDescription serviceDescription) {
        Util.runOnUI(new Runnable() {
            @Override
            public void run() {
                for (DiscoveryProviderListener listener : serviceListeners) {
                    listener.onServiceRemoved(FireTVDiscoveryProvider.this, serviceDescription);
                }
            }
        });
    }

    private void notifyListenersThatDiscoveryFailed(final ServiceCommandError error) {
        Util.runOnUI(new Runnable() {
            @Override
            public void run() {
                for (DiscoveryProviderListener listener : serviceListeners) {
                    listener.onServiceDiscoveryFailed(FireTVDiscoveryProvider.this, error);
                }
            }
        });
    }


    class FireTVDiscoveryListener implements DiscoveryController.IDiscoveryListener {

        @Override
        public void playerDiscovered(RemoteMediaPlayer remoteMediaPlayer) {
            if (remoteMediaPlayer == null) {
                return;
            }
            String uid = remoteMediaPlayer.getUniqueIdentifier();
            ServiceDescription serviceDescription = foundServices.get(uid);

            if (serviceDescription == null) {
                serviceDescription = new ServiceDescription();
                updateServiceDescription(serviceDescription, remoteMediaPlayer);
                foundServices.put(uid, serviceDescription);
                notifyListenersThatServiceAdded(serviceDescription);
            } else {
                updateServiceDescription(serviceDescription, remoteMediaPlayer);
            }
        }

        @Override
        public void playerLost(RemoteMediaPlayer remoteMediaPlayer) {
            if (remoteMediaPlayer == null) {
                return;
            }
            ServiceDescription serviceDescription
                    = foundServices.get(remoteMediaPlayer.getUniqueIdentifier());
            if (serviceDescription != null) {
                notifyListenersThatServiceLost(serviceDescription);
                foundServices.remove(remoteMediaPlayer.getUniqueIdentifier());
            }
        }

        @Override
        public void discoveryFailure() {
            final ServiceCommandError error = new ServiceCommandError("FireTV discovery failure");
            notifyListenersThatDiscoveryFailed(error);
        }

        private void updateServiceDescription(ServiceDescription serviceDescription,
                                              RemoteMediaPlayer remoteMediaPlayer) {
            String uid = remoteMediaPlayer.getUniqueIdentifier();
            serviceDescription.setDevice(remoteMediaPlayer);
            serviceDescription.setFriendlyName(remoteMediaPlayer.getName());
            serviceDescription.setIpAddress(uid);
            serviceDescription.setServiceID(FireTVService.ID);
            serviceDescription.setUUID(uid);
        }

    }

}
