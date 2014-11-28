package pct.droid.youtube;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a video stream
 */
public class VideoStream {
	
	protected String mUrl;
	
	/**
	 * Construct a video stream from one of the strings obtained 
	 * 	from the "url_encoded_fmt_stream_map" parameter if the video_info 
	 * @param streamStr - one of the strings from "url_encoded_fmt_stream_map"
	 */
	public VideoStream(String streamStr){
		String[] args = streamStr.split("&");
		Map<String,String> argMap = new HashMap<String, String>();
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

	public String getUrl(){
		return mUrl;
	}

}