/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.utils;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.videolan.libvlc.util.AndroidUtil;
import timber.log.Timber;

public final class StreamInfoUtils {

    private static final String CONTENT_PROVIDER_GMAIL = "gmail-ls";
    private static final String CONTENT_PROVIDER_K9 = "com.fsck.k9.attachmentprovider";

    private static final String FOLDER_DOWNLOADS = "Downloads";

    private StreamInfoUtils() {
        // no instances
    }

    @Nullable public static String getActionViewVideoLocation(@NonNull ContentResolver contentResolver, @NonNull Intent intent) {

        Uri data = intent.getData();

        if (data != null && ContentResolver.SCHEME_CONTENT.equals(data.getScheme())) {
            return getContentProviderVideoLocation(contentResolver, data);
        } else if (intent.getDataString() != null) { /* External application */
            return getDataStringVideoLocation(intent.getDataString(), data);
        } else {
            return null;
        }

    }

    @Nullable private static String getContentProviderVideoLocation(ContentResolver contentResolver, Uri data) {

        // Mail-based apps - download the stream to a temporary file and play it
        if (CONTENT_PROVIDER_K9.equals(data.getHost()) || CONTENT_PROVIDER_GMAIL.equals(data.getHost())) {
            return getMailAttachmentVideoLocation(contentResolver, data);
        } else if (MediaStore.AUTHORITY.equals(data.getAuthority())) { // Media or MMS URI
            return getMediaStoreVideoLocation(contentResolver, data);
        } else {
            return getParcelFileDescriptorVideoLocation(contentResolver, data);
        }

    }

    @Nullable private static String getDataStringVideoLocation(String location, Uri data) {
        // Remove VLC prefix if needed
        if (location.startsWith("vlc://")) {
            return AndroidUtil.LocationToUri(location.substring(6)).toString();
        } else {
            if (data.getScheme() == null) {
                return AndroidUtil.PathToUri(data.getPath()).toString();
            } else {
                return data.toString();
            }
        }

    }

    @Nullable private static String getMailAttachmentVideoLocation(ContentResolver contentResolver, Uri data) {
        InputStream is = null;
        OutputStream os = null;
        try {

            Cursor cursor = contentResolver.query(data, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                String filename = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                cursor.close();
                Timber.i("Getting file " + filename + " from content:// URI");

                String fileLocation = Environment.getExternalStorageDirectory().getPath() + "/" + FOLDER_DOWNLOADS + "/" + filename;

                is = contentResolver.openInputStream(data);
                os = new FileOutputStream(fileLocation);
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, bytesRead);
                }

                return AndroidUtil.PathToUri(fileLocation).toString();
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                // do nothing
            }
        }

    }

    @Nullable private static String getMediaStoreVideoLocation(ContentResolver contentResolver, Uri data) {

        try {

            Cursor cursor = contentResolver.query(data, new String[]{MediaStore.Video.Media.DATA}, null, null, null);

            if (cursor != null) {
                try {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    if (cursor.moveToFirst()) {
                        return AndroidUtil.PathToUri(cursor.getString(columnIndex)).toString();
                    } else {
                        return null;
                    }
                } finally {
                    cursor.close();
                }
            } else if (data.getScheme() == null) {
                return AndroidUtil.PathToUri(data.getPath()).toString();
            } else { // other content-based URI (probably file pickers)
                return data.toString();
            }
        } catch (Exception e) {
            if (data.getScheme() == null) {
                return AndroidUtil.PathToUri(data.getPath()).toString();
            }  else {
                return data.toString();
            }
        }

    }

    @Nullable private static String getParcelFileDescriptorVideoLocation(ContentResolver contentResolver, Uri data) {

        ParcelFileDescriptor inputPFD = null;
        try {
            inputPFD = contentResolver.openFileDescriptor(data, "r");

            return AndroidUtil.LocationToUri("fd://" + inputPFD.getFd()).toString();
        } catch (FileNotFoundException e) {
            return null;
        }

    }

}
