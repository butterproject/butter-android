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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import butter.droid.base.Constants;
import butter.droid.base.providers.media.models.Media;

public class FavouriteUtils {
    private static final String TAG = "FavouriteUtils";

    public static ArrayList<Media> getFavourites(Context context, Class provider) {
        Map<String, Media> map = getFavouriteMap(context, provider);
        return new ArrayList<Media>(map.values());
    }

    public static Boolean isFavourite(Context context, Class provider, Media media) {
        return isFavourite(context, provider, media.videoId);
    }

    public static Boolean isFavourite(Context context, Class provider, String videoId) {
        Set<String> ids = getFavouriteMap(context, provider).keySet();
        return ids.contains(videoId);
    }

    public static void addFavourite(Context context, Class provider, Media media) {
        Map<String, Media> map = getFavouriteMap(context, provider);
        map.put(media.videoId, media);
        setFavouriteMap(context, provider, map);
    }

    public static void removeFavourite(Context context, Class provider, Media media) {
        removeFavourite(context, provider, media.videoId);
    }

    public static void removeFavourite(Context context, Class provider, String videoId) {
        Map<String, Media> map = getFavouriteMap(context, provider);
        map.remove(videoId);
        setFavouriteMap(context, provider, map);
    }

    public static Map<String, Media> getFavouriteMap(Context context, Class provider) {
        SharedPreferences prefs = getPrefs(context);
        String serialized = prefs.getString(provider.getName(), "{}");

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String,Media>> typeRef = new TypeReference<HashMap<String,Media>>() {};
        try {
            return mapper.readValue(serialized, typeRef);
        } catch (IOException e) {
            Log.e(TAG, "Failed to deserialize favourites", e);
            return new HashMap<String,Media>();
        }
    }

    public static void setFavouriteMap(Context context, Class provider, Map<String, Media> map) {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String,Media>> typeRef = new TypeReference<HashMap<String,Media>>() {};
        String serialized;
        try {
            serialized = mapper.writerFor(typeRef).writeValueAsString(map);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to serialize favourites", e);
            return;
        }

        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(provider.getName(), serialized);
        editor.commit();
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_FILE_FAVOURITES, Context.MODE_PRIVATE);
    }
}
