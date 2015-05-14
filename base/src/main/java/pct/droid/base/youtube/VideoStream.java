/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.youtube;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a video stream
 */
public class VideoStream {

    protected String mUrl;

    /**
     * Construct a video stream from one of the strings obtained
     * from the "url_encoded_fmt_stream_map" parameter if the video_info
     *
     * @param streamStr - one of the strings from "url_encoded_fmt_stream_map"
     */
    public VideoStream(String streamStr) {
        String[] args = streamStr.split("&");
        Map<String, String> argMap = new HashMap<String, String>();
        for (String arg : args) {
            String[] argsValues = arg.split("=");
            if (argsValues != null) {
                if (argsValues.length >= 2) {
                    argMap.put(argsValues[0], argsValues[1]);
                }
            }
        }
        mUrl = argMap.get("url");
    }

    public String getUrl() {
        return mUrl;
    }

}