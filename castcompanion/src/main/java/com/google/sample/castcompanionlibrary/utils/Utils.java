/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.castcompanionlibrary.utils;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A collection of utility methods, all static.
 */
public class Utils {

    private static final String TAG = LogUtils.makeLogTag(Utils.class);
    private static final String KEY_IMAGES = "images";
    private static final String KEY_URL = "movie-urls";
    private static final String KEY_CONTENT_TYPE = "content-type";
    private static final String KEY_STREAM_TYPE = "stream-type";
    private static final String KEY_STREAM_DURATION = "stream-duration";
    private static final String KEY_CUSTOM_DATA = "custom-data";
    private static final String KEY_TRACK_ID = "track-id";
    private static final String KEY_TRACK_CONTENT_ID = "track-custom-id";
    private static final String KEY_TRACK_NAME = "track-name";
    private static final String KEY_TRACK_TYPE = "track-type";
    private static final String KEY_TRACK_SUBTYPE = "track-subtype";
    private static final String KEY_TRACK_LANGUAGE = "track-language";
    private static final String KEY_TRACK_CUSTOM_DATA = "track-custom-data";
    private static final String KEY_TRACKS_DATA = "track-data";
    public static final boolean IS_KITKAT_OR_ABOVE = Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.KITKAT;

    /**
     * Formats time in milliseconds to hh:mm:ss string format.
     *
     * @param millis
     * @return
     */
    public static String formatMillis(int millis) {
        String result = "";
        int hr = millis / 3600000;
        millis %= 3600000;
        int min = millis / 60000;
        millis %= 60000;
        int sec = millis / 1000;
        if (hr > 0) {
            result += hr + ":";
        }
        if (min >= 0) {
            if (min > 9) {
                result += min + ":";
            } else {
                result += "0" + min + ":";
            }
        }
        if (sec > 9) {
            result += sec;
        } else {
            result += "0" + sec;
        }
        return result;
    }

    /**
     * A utility method to show a simple error dialog. The textual content of the dialog is
     * provided through the passed-in resource id.
     *
     * @param context
     * @param resourceId
     */
    public static final void showErrorDialog(Context context, int resourceId) {
        //showErrorDialog(context, context.getString(resourceId));
        showToast(context.getApplicationContext(), resourceId);
    }

    /**
     * A utility method to show a simple error dialog.
     *
     * @param context
     * @param message The message to be shown in the dialog
     */
    public static final void showErrorDialog(Context context, String message) {
        new AlertDialog.Builder(context).setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    /**
     * Shows a (long) toast.
     *
     * @param context
     * @param resourceId
     */
    public static void showToast(Context context, int resourceId) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }

    /**
     * Returns the URL of an image for the {@link MediaInformation} at the given level. Level
     * should be a number between 0 and <code>n - 1</code> where <code>n</code> is the number of
     * images for that given item.
     *
     * @param info
     * @param level
     * @return
     */
    public static String getImageUrl(MediaInfo info, int level) {
        Uri uri = getImageUri(info, level);
        if (null != uri) {
            return uri.toString();
        }
        return null;
    }

    /**
     * Returns the {@code Uri} address of an image for the {@link MediaInformation} at the given
     * level. Level should be a number between 0 and <code>n - 1</code> where <code>n</code> is the
     * number of images for that given item.
     *
     * @param info
     * @param level
     * @return
     */
    public static Uri getImageUri(MediaInfo info, int level) {
        MediaMetadata mm = info.getMetadata();
        if (null != mm && mm.getImages().size() > level) {
            return mm.getImages().get(level).getUrl();
        }
        return null;
    }

    /**
     * Saves a string value under the provided key in the preference manager. If <code>value</code>
     * is <code>null</code>, then the provided key will be removed from the preferences.
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveStringToPreference(Context context, String key, String value) {
        if(context == null) return;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if(pref == null) return;

        if (null == value) {
            // we want to remove
            pref.edit().remove(key).apply();
        } else {
            pref.edit().putString(key, value).apply();
        }
    }

    /**
     * Saves a float value under the provided key in the preference manager. If <code>value</code>
     * is <code>Float.MIN_VALUE</code>, then the provided key will be removed from the preferences.
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveFloatToPreference(Context context, String key, float value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (Float.MIN_VALUE == value) {
            // we want to remove
            pref.edit().remove(key).apply();
        } else {
            pref.edit().putFloat(key, value).apply();
        }

    }

    /**
     * Saves a long value under the provided key in the preference manager. If <code>value</code>
     * is <code>Long.MIN_VALUE</code>, then the provided key will be removed from the preferences.
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveLongToPreference(Context context, String key, long value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (Long.MIN_VALUE == value) {
            // we want to remove
            pref.edit().remove(key).apply();
        } else {
            pref.edit().putLong(key, value).apply();
        }

    }

    /**
     * Retrieves a String value from preference manager. If no such key exists, it will return
     * <code>null</code>.
     *
     * @param context
     * @param key
     * @return
     */
    public static String getStringFromPreference(Context context, String key) {
        return getStringFromPreference(context, key, null);
    }

    /**
     * Retrieves a String value from preference manager. If no such key exists, it will return
     * <code>defaultValue</code>.
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getStringFromPreference(Context context, String key, String defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(key, defaultValue);
    }

    /**
     * Retrieves a float value from preference manager. If no such key exists, it will return
     * <code>Float.MIN_VALUE</code>.
     *
     * @param context
     * @param key
     * @return
     */
    public static float getFloatFromPreference(Context context, String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getFloat(key, Float.MIN_VALUE);
    }

    /**
     * Retrieves a boolean value from preference manager. If no such key exists, it will return the
     * value provided as <code>defaultValue</code>
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBooleanFromPreference(Context context, String key,
            boolean defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(key, defaultValue);
    }

    /**
     * Retrieves a long value from preference manager. If no such key exists, it will return the
     * value provided as <code>defaultValue</code>
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static long getLongFromPreference(Context context, String key,
            long defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getLong(key, defaultValue);
    }

    /**
     * A utility method to validate that the appropriate version of the Google Play Services is
     * available on the device. If not, it will open a dialog to address the issue. The dialog
     * displays a localized message about the error and upon user confirmation (by tapping on
     * dialog) will direct them to the Play Store if Google Play services is out of date or
     * missing, or to system settings if Google Play services is disabled on the device.
     *
     * @param activity
     * @return
     */
    public static boolean checkGooglePlayServices(final Activity activity) {
        final int googlePlayServicesCheck = GooglePlayServicesUtil.isGooglePlayServicesAvailable(
                activity);
        switch (googlePlayServicesCheck) {
            case ConnectionResult.SUCCESS:
                return true;
            default:
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesCheck,
                        activity, 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        activity.finish();
                    }
                });
                dialog.show();
        }
        return false;
    }

    /**
     * @deprecated See <code>checkGooglePlayServices</code>
     */
    public static boolean checkGooglePlaySevices(final Activity activity) {
        return checkGooglePlayServices(activity);
    }

    /**
     * Builds and returns a {@link Bundle} which contains a select subset of data in the
     * {@link MediaInfo}. Since {@link MediaInfo} is not {@link Parcelable}, one can use this
     * container bundle to pass around from one activity to another.
     *
     * @param info
     * @return
     * @see <code>toMediaInfo()</code>
     */
    public static Bundle fromMediaInfo(MediaInfo info) {
        if (null == info) {
            return null;
        }

        MediaMetadata md = info.getMetadata();
        Bundle wrapper = new Bundle();
        wrapper.putString(MediaMetadata.KEY_TITLE, md.getString(MediaMetadata.KEY_TITLE));
        wrapper.putString(MediaMetadata.KEY_SUBTITLE, md.getString(MediaMetadata.KEY_SUBTITLE));
        wrapper.putString(KEY_URL, info.getContentId());
        wrapper.putString(MediaMetadata.KEY_STUDIO, md.getString(MediaMetadata.KEY_STUDIO));
        wrapper.putString(KEY_CONTENT_TYPE, info.getContentType());
        wrapper.putInt(KEY_STREAM_TYPE, info.getStreamType());
        wrapper.putLong(KEY_STREAM_DURATION, info.getStreamDuration());
        if (!md.getImages().isEmpty()) {
            ArrayList<String> urls = new ArrayList<String>();
            for (WebImage img : md.getImages()) {
                urls.add(img.getUrl().toString());
            }
            wrapper.putStringArrayList(KEY_IMAGES, urls);
        }
        JSONObject customData = info.getCustomData();
        if (null != customData) {
            wrapper.putString(KEY_CUSTOM_DATA, customData.toString());
        }
        if (info.getMediaTracks() != null && !info.getMediaTracks().isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray();
                for (MediaTrack mt : info.getMediaTracks()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(KEY_TRACK_NAME, mt.getName());
                    jsonObject.put(KEY_TRACK_CONTENT_ID, mt.getContentId());
                    jsonObject.put(KEY_TRACK_ID, mt.getId());
                    jsonObject.put(KEY_TRACK_LANGUAGE, mt.getLanguage());
                    jsonObject.put(KEY_TRACK_TYPE, mt.getType());
                    jsonObject.put(KEY_TRACK_SUBTYPE, mt.getSubtype());
                    if (null != mt.getCustomData()) {
                        jsonObject.put(KEY_TRACK_CUSTOM_DATA, mt.getCustomData().toString());
                    }
                    jsonArray.put(jsonObject);
                }
                wrapper.putString(KEY_TRACKS_DATA, jsonArray.toString());
            } catch (JSONException e) {
                LOGE(TAG, "fromMediaInfo(): Failed to convert Tracks data to json", e);
            }
        }

        return wrapper;
    }

    /**
     * Builds and returns a {@link MediaInfo} that was wrapped in a {@link Bundle} by
     * <code>fromMediaInfo</code>.
     *
     * @param wrapper
     * @return
     * @see <code>fromMediaInfo()</code>
     */
    public static MediaInfo toMediaInfo(Bundle wrapper) {
        if (null == wrapper) {
            return null;
        }

        MediaMetadata metaData = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        metaData.putString(MediaMetadata.KEY_SUBTITLE,
                wrapper.getString(MediaMetadata.KEY_SUBTITLE));
        metaData.putString(MediaMetadata.KEY_TITLE, wrapper.getString(MediaMetadata.KEY_TITLE));
        metaData.putString(MediaMetadata.KEY_STUDIO, wrapper.getString(MediaMetadata.KEY_STUDIO));
        ArrayList<String> images = wrapper.getStringArrayList(KEY_IMAGES);
        if (null != images && !images.isEmpty()) {
            for (String url : images) {
                Uri uri = Uri.parse(url);
                metaData.addImage(new WebImage(uri));
            }
        }
        String customDataStr = wrapper.getString(KEY_CUSTOM_DATA);
        JSONObject customData = null;
        if (!TextUtils.isEmpty(customDataStr)) {
            try {
                customData = new JSONObject(customDataStr);
            } catch (JSONException e) {
                LOGE(TAG, "Failed to deserialize the custom data string: custom data= "
                        + customDataStr);
            }
        }
        List<MediaTrack> mediaTracks = null;
        if (wrapper.getString(KEY_TRACKS_DATA) != null) {
            try {
                JSONArray jsonArray = new JSONArray(wrapper.getString(KEY_TRACKS_DATA));
                mediaTracks = new ArrayList<MediaTrack>();
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = (JSONObject) jsonArray.get(i);
                        MediaTrack.Builder builder = new MediaTrack.Builder(
                                jsonObj.getLong(KEY_TRACK_ID), jsonObj.getInt(KEY_TRACK_TYPE));
                        if (jsonObj.has(KEY_TRACK_NAME)) {
                            builder.setName(jsonObj.getString(KEY_TRACK_NAME));
                        }
                        if (jsonObj.has(KEY_TRACK_SUBTYPE)) {
                            builder.setSubtype(jsonObj.getInt(KEY_TRACK_SUBTYPE));
                        }
                        if (jsonObj.has(KEY_TRACK_CONTENT_ID)) {
                            builder.setContentId(jsonObj.getString(KEY_TRACK_CONTENT_ID));
                        }
                        if (jsonObj.has(KEY_TRACK_LANGUAGE)) {
                            builder.setLanguage(jsonObj.getString(KEY_TRACK_LANGUAGE));
                        }
                        if (jsonObj.has(KEY_TRACKS_DATA)) {
                            builder.setCustomData(
                                    new JSONObject(jsonObj.getString(KEY_TRACKS_DATA)));
                        }
                        mediaTracks.add(builder.build());
                    }
                }
            } catch (JSONException e) {
                LOGE(TAG, "Failed to build media tracks from the wrapper bundle", e);
            }
        }
        return new MediaInfo.Builder(wrapper.getString(KEY_URL))
                .setStreamType(wrapper.getInt(KEY_STREAM_TYPE))
                .setContentType(wrapper.getString(KEY_CONTENT_TYPE))
                .setMetadata(metaData)
                .setCustomData(customData)
                .setMediaTracks(mediaTracks)
                .setStreamDuration(wrapper.getLong(KEY_STREAM_DURATION))
                .build();
    }

    /**
     * Returns the SSID of the wifi connection, or <code>null</code> if there is no wifi.
     *
     * @param context
     * @return
     */
    public static String getWifiSsid(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null != wifiInfo) {
            return wifiInfo.getSSID();
        }
        return null;
    }

    /**
     * Scale and center-crop a bitmap to fit the given dimensions.
     */
    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap destination = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(destination);
        canvas.drawBitmap(source, null, targetRect, null);

        return destination;
    }
}
