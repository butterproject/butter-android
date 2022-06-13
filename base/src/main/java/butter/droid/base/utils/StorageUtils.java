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

package butter.droid.base.utils;

import androidx.core.content.ContextCompat;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import butter.droid.base.compat.Compatibility;

public class StorageUtils extends Compatibility {

    public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";

    /**
     * @return {@code true} if external storage is available and writable. {@code false} otherwise.
     */
    private static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * @return A map of all storage locations available
     */
    public static Map<String, File> getAllStorageLocations(Context context) {
        Map<String, File> map = new HashMap<>(10);

        List<String> mMounts = new ArrayList<>(10);
        List<String> mVold = new ArrayList<>(10);

        File[] fs = ContextCompat.getExternalFilesDirs(context, null);
        for (int i=0;i< fs.length;i++) {
            String element = fs[i].getParent();
            mMounts.add(element);
        }

        List<String> mountHash = new ArrayList<>(10);

        for (String mount : mMounts) {
            File root = new File(mount);
            if (root.exists() && root.isDirectory() && root.canWrite()) {
                File[] list = root.listFiles();
                StringBuilder hash = new StringBuilder("[");
                if (list != null) {
                    for (File f : list) {
                        hash.append(f.getName().hashCode()).append(":").append(f.length()).append(", ");
                    }
                }
                hash.append("]");
                if (!mountHash.contains(hash.toString())) {
                    String key = SD_CARD + "_" + map.size();
                    if (map.size() == 0) {
                        key = SD_CARD;
                    } else if (map.size() == 1) {
                        key = EXTERNAL_SD_CARD;
                    }
                    mountHash.add(hash.toString());
                    map.put(key, root);
                }
            }
        }

        mMounts.clear();

        if (map.isEmpty()) {
            map.put(SD_CARD, Environment.getExternalStorageDirectory());
        }
        return map;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static long getAvailable(final File dir) {
        final StatFs statFs = new StatFs(dir.getAbsolutePath());
        if (hasApi(Build.VERSION_CODES.JELLY_BEAN_MR2)) {
            return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        }
        return ((long) statFs.getAvailableBlocks()) * statFs.getBlockSize();

    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static long getTotal(final File dir) {
        final StatFs statFs = new StatFs(dir.getAbsolutePath());
        if (hasApi(Build.VERSION_CODES.JELLY_BEAN_MR2)) {
            return statFs.getBlockCountLong() * statFs.getBlockSizeLong();
        }
        return ((long) statFs.getBlockCount()) * statFs.getBlockSize();

    }

    /**
     * @return Available internal memory
     */
    public static long getAvailableInternalMemorySize() {
        return getAvailable(Environment.getDataDirectory());
    }

    /**
     * @return Total internal memory
     */
    private static long getTotalInternalMemorySize() {
        return getTotal(Environment.getDataDirectory());
    }

    /**
     * @return Available external memory
     */
    public static long getAvailableExternalMemorySize() {
        if (isExternalStorageAvailable()) {
            return getAvailable(Environment.getExternalStorageDirectory());
        } else {
            return 0;
        }
    }

    /**
     * @return Total external memory
     */
    private static long getTotalExternalMemorySize() {
        if (isExternalStorageAvailable()) {
            return getTotal(Environment.getExternalStorageDirectory());
        } else {
            return 0;
        }
    }

    /**
     * Get ideal cache directory based on available
     *
     * @return Ideal file location for caching
     */
    public static File getIdealCacheDirectory(Context context) {
        Map<String, File> dirs = getAllStorageLocations(context);
        if (dirs.containsKey(EXTERNAL_SD_CARD)) {
            return dirs.get(EXTERNAL_SD_CARD);
        }
        return dirs.get(SD_CARD);
    }

    public static String getInternalSdCardPath(){
        return System.getenv("EXTERNAL_STORAGE");
    }

    public static String getExternalSdCardPath(){
        return System.getenv("SECONDARY_STORAGE");
    }

    /**
     * Format size in string form
     *
     * @param size Size in bytes
     * @return Size in stinrg format with suffix
     */
    public static String formatSize(int size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }
}
