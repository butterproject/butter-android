package pct.droid.providers.subs;

import android.content.Context;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import pct.droid.providers.BaseProvider;
import pct.droid.providers.media.MediaProvider;
import pct.droid.providers.media.types.Media;
import pct.droid.subs.FatalParsingException;
import pct.droid.subs.FormatASS;
import pct.droid.subs.FormatSRT;
import pct.droid.subs.TimedTextObject;
import pct.droid.utils.FileUtils;
import pct.droid.utils.StorageUtils;

public abstract class SubsProvider extends BaseProvider {

    private static List<String> SUB_EXTENSIONS = Arrays.asList("srt", "ssa", "ass");

    public HashMap<String, HashMap<String, String>> getList(String imdbId) {
        return getList(new String[] { imdbId });
    }

    public abstract HashMap<String, HashMap<String, String>> getList(String[] imdbIds);

    public static Call download(final Context context, final Media media, final String languageCode, final Callback callback) {
        OkHttpClient client = new OkHttpClient();
        if(media.subtitles.containsKey(languageCode)) {
            try {
                Request request = new Request.Builder().url(media.subtitles.get(languageCode)).build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        callback.onFailure(request, e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (response.isSuccessful()) {
                            InputStream inputStream = null;
                            boolean failure = false;
                            try {
                                File cacheDirectory = StorageUtils.getIdealCacheDirectory(context);
                                File subsDirectory = new File(cacheDirectory, "subs");

                                String fileName = media.videoId + "-" + languageCode;
                                File srtPath = new File(subsDirectory, fileName + ".srt");

                                subsDirectory.mkdirs();
                                if (srtPath.exists()) {
                                    File to = new File(subsDirectory, "temp" + System.currentTimeMillis());
                                    srtPath.renameTo(to);
                                    to.delete();
                                }

                                inputStream = response.body().byteStream();
                                String urlString = response.request().urlString();

                                if (urlString.endsWith(".zip")) {
                                    SubsProvider.unpack(inputStream, srtPath);
                                } else if(SubsProvider.isSubFormat(urlString)) {
                                    parseFormatAndSave(urlString, srtPath, inputStream);
                                } else {
                                    callback.onFailure(response.request(), new IOException("FatalParsingException"));
                                    failure = true;
                                }
                            } catch (FatalParsingException e) {
                                e.printStackTrace();
                                callback.onFailure(response.request(), new IOException("FatalParsingException"));
                                failure = true;
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                callback.onFailure(response.request(), e);
                                failure = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                                callback.onFailure(response.request(), e);
                                failure = true;
                            } finally {
                                if (inputStream != null)
                                    inputStream.close();

                                if (!failure) callback.onResponse(response);
                            }
                        } else {
                            callback.onFailure(response.request(), new IOException("Unknown error"));
                        }
                    }
                });

                return call;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void unpack(InputStream is, File srtPath) throws IOException, FatalParsingException {
        String filename;
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;

        while ((ze = zis.getNextEntry()) != null) {
            filename = ze.getName();
            if(filename.contains("_MACOSX")) return;

            if(isSubFormat(filename)) {
                parseFormatAndSave(filename, srtPath, zis);
                try {
                    zis.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        zis.close();
    }

    private static boolean isSubFormat(String filename) {
        for(String ext : SUB_EXTENSIONS) {
            if(filename.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    private static void parseFormatAndSave(String inputUrl, File srtPath, InputStream inputStream) throws IOException {
        TimedTextObject subtitleObject = null;
        String[] inputText = FileUtils.inputstreamToCharsetString(inputStream).split("\n|\r\n");

        if(inputUrl.endsWith(".ass") || inputUrl.contains(".ssa")) {
            FormatASS formatASS = new FormatASS();
            subtitleObject = formatASS.parseFile(inputUrl, inputText);
        } else if(inputUrl.endsWith(".srt")) {
            FormatSRT formatSRT = new FormatSRT();
            subtitleObject = formatSRT.parseFile(inputUrl, inputText);
        }

        if(subtitleObject != null) {
            FileUtils.saveStringFile(subtitleObject.toSRT(), srtPath);
        }
    }
}