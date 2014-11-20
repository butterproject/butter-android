/*****************************************************************************
 * AndroidDevices.java
 *****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.vlc.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.InputDevice;
import android.view.MotionEvent;

import org.videolan.libvlc.LibVlcUtil;

import java.util.HashSet;

import pct.droid.PopcornApplication;

public class AndroidDevices {
    public final static String TAG = "VLC/Util/AndroidDevices";

    final static boolean hasNavBar;
    final static boolean hasTsp;

    static {
        HashSet<String> devicesWithoutNavBar = new HashSet<String>();
        devicesWithoutNavBar.add("HTC One V");
        devicesWithoutNavBar.add("HTC One S");
        devicesWithoutNavBar.add("HTC One X");
        devicesWithoutNavBar.add("HTC One XL");
        hasNavBar = LibVlcUtil.isJellyBeanMR1OrLater() || (LibVlcUtil.isICSOrLater()
                && !devicesWithoutNavBar.contains(android.os.Build.MODEL));
        hasTsp = PopcornApplication.getAppContext().getPackageManager().hasSystemFeature("android.hardware.touchscreen");
    }

    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean hasNavBar()
    {
        return hasNavBar;
    }

    /** hasCombBar test if device has Combined Bar : only for tablet with Honeycomb or ICS */
    public static boolean hasCombBar() {
        return (!AndroidDevices.isPhone()
                && ((VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) &&
                    (VERSION.SDK_INT <= VERSION_CODES.JELLY_BEAN)));
    }

    public static boolean isPhone(){
        TelephonyManager manager = (TelephonyManager)PopcornApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if(manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE){
            return false;
        }else{
            return true;
        }
    }

    public static boolean hasTsp() {
        return hasTsp;
    }

    @TargetApi(VERSION_CODES.HONEYCOMB_MR1)
    public static float getCenteredAxis(MotionEvent event,
            InputDevice device, int axis) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value = event.getAxisValue(axis);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }
}
