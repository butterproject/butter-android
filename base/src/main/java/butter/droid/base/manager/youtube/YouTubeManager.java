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

/**
 * The pct.droid.base.youtube package contains code that is based on https://code.google.com/p/android-youtube-player/
 */

package butter.droid.base.manager.youtube;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.support.annotation.IntDef;
import butter.droid.base.manager.youtube.model.Format;
import butter.droid.base.manager.youtube.model.VideoStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YouTubeManager {

  public static final int QUALITY_LOW_3GPP = 13; //3GPP (MPEG-4 encoded) Low quality
  public static final int QUALITY_MEDIUM_3GPP = 17; //3GPP (MPEG-4 encoded) Medium quality
  public static final int QUALITY_NORMAL_MP4 = 18; //MP4 (H.264 encoded) Normal quality
  public static final int QUALITY_HIGH_MP4 = 22; //MP4 (H.264 encoded) High quality
  public static final int QUALITY_ULTRA_HIGH_MP4 = 37; //MP4 (H.264 encoded) High quality

  private static final String YOUTUBE_VIDEO_INFORMATION_URL = "https://www.youtube.com/get_video_info?&video_id=";

  private static final Pattern YOUTUBE_URL_PATTERN = Pattern
      .compile("^.*((youtu.be/)|(v/)|(/u/w/)|(embed/)|(watch\\?))\\??v?=?([^#&?]*).*", Pattern.CASE_INSENSITIVE);
  private static final Pattern YOUTUBE_URL_VIDEO_ID_EXTRACTOR_PATTERN = Pattern
      .compile("^.*((youtu.be\\/)|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#\\&\\?]*).*", Pattern.CASE_INSENSITIVE);

  private static final int[] SUPPORTED_FORMATS_IDS = {
      QUALITY_LOW_3GPP,
      QUALITY_MEDIUM_3GPP,
      QUALITY_NORMAL_MP4,
      QUALITY_HIGH_MP4,
      QUALITY_ULTRA_HIGH_MP4
  };

  private final OkHttpClient client;

  @Inject
  public YouTubeManager(OkHttpClient okHttpClient) {
    this.client = okHttpClient;
  }

  private static int getSupportedFallbackId(int oldId) {
    int fallbackId = oldId;
    for (int i = SUPPORTED_FORMATS_IDS.length - 1; i >= 0; i--) {
      if (oldId == SUPPORTED_FORMATS_IDS[i] && i > 0) {
        fallbackId = SUPPORTED_FORMATS_IDS[i - 1];
      }
    }
    return fallbackId;
  }

  public boolean isYouTubeUrl(String youtubeUrl) {
    final Matcher matcher = YOUTUBE_URL_PATTERN.matcher(youtubeUrl);
    return matcher.matches();
  }

  public String getYouTubeVideoId(String youtubeUrl) {
    String videoId = "";
    if (youtubeUrl != null && youtubeUrl.trim().length() > 0 && youtubeUrl.startsWith("http")) {
      final Matcher matcher = YOUTUBE_URL_VIDEO_ID_EXTRACTOR_PATTERN.matcher(youtubeUrl);
      if (matcher.matches()) {
        final String groupIndex1 = matcher.group(7);
        if (groupIndex1 != null && groupIndex1.length() == 11) {
          videoId = groupIndex1;
        }
      }
    }
    return videoId;
  }

  /**
   * Calculate the YouTube URL to load the video.  Includes retrieving a token that YouTube
   * requires to play the video.
   *
   * @param quality quality of the video.  13=low, 37=high
   * @param fallback whether to fallback to lower quality in case the supplied quality is not available
   * @param videoId the id of the video
   * @return the url string that will retrieve the video
   */
  public String calculateYouTubeUrl(@VideoQuality int quality, boolean fallback, String videoId) throws IOException {
    String uriStr = null;

    final Request.Builder request = new Request.Builder();
    request.url(YOUTUBE_VIDEO_INFORMATION_URL + videoId);
    final Call call = client.newCall(request.build());
    final Response response = call.execute();
    if (!response.isSuccessful()) {
      return null;
    }

    final String infoStr = response.body().string();

    final String[] args = infoStr.split("&");
    final Map<String, String> argMap = new HashMap<>();
    for (String arg : args) {
      final String[] valStrArr = arg.split("=");
      if (valStrArr.length >= 2) {
        argMap.put(valStrArr[0], URLDecoder.decode(valStrArr[1]));
      }
    }

    //Find out the URI string from the parameters

    //Populate the list of formats for the video
    final String fmtList = URLDecoder.decode(argMap.get("fmt_list"), "utf-8");
    final List<Format> formats = new ArrayList<>();
    if (null != fmtList) {
      final String formatStrs[] = fmtList.split(",");

      for (String lFormatStr : formatStrs) {
        final Format format = new Format(lFormatStr);
        formats.add(format);
      }
    }

    //Populate the list of streams for the video
    final String streamList = argMap.get("url_encoded_fmt_stream_map");
    if (null != streamList) {
      final String streamStrs[] = streamList.split(",");
      final List<VideoStream> streams = new ArrayList<>();
      for (String streamStr : streamStrs) {
        final VideoStream lStream = new VideoStream(streamStr);
        streams.add(lStream);
      }

      //Search for the given format in the list of video formats
      // if it is there, select the corresponding stream
      // otherwise if fallback is requested, check for next lower format
      Format searchFormat = new Format(quality);
      while (!formats.contains(searchFormat) && fallback) {
        final int oldId = searchFormat.getId();
        final int newId = getSupportedFallbackId(oldId);
        if (oldId == newId) {
          break;
        }
        searchFormat = new Format(newId);
      }

      final int index = formats.indexOf(searchFormat);
      if (index >= 0) {
        final VideoStream searchStream = streams.get(index);
        uriStr = searchStream.getUrl();
      }

    }
    //Return the URI string. It may be null if the format (or a fallback format if enabled)
    // is not found in the list of formats for the video
    return uriStr;
  }

  @Retention(SOURCE)
  @IntDef({QUALITY_LOW_3GPP, QUALITY_MEDIUM_3GPP, QUALITY_NORMAL_MP4, QUALITY_HIGH_MP4, QUALITY_ULTRA_HIGH_MP4})
  public @interface VideoQuality {

  }
}
