package pct.droid.providers.subs;

import android.content.Context;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import pct.droid.providers.BaseProvider;
import pct.droid.providers.media.MediaProvider;
import pct.droid.utils.FileUtils;
import pct.droid.utils.StorageUtils;

public abstract class SubsProvider extends BaseProvider {

    public HashMap<String, HashMap<String, String>> getList(String imdbId) {
        return getList(new String[] { imdbId });
    }

    public abstract HashMap<String, HashMap<String, String>> getList(String[] imdbIds);

    public static Call download(final Context context, final MediaProvider.Video video, final String languageCode, final Callback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(video.subtitles.get(languageCode)).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(request, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful()) {
                    FileOutputStream fileOutputStream = null;
                    InputStream inputStream = null;
                    boolean failure = false;
                    try {
                        File cacheDirectory = StorageUtils.getIdealCacheDirectory(context);
                        File subsDirectory = new File(cacheDirectory, "subs");

                        String fileName = video.imdbId + "-" + languageCode;
                        File zipPath = new File(subsDirectory, fileName + ".zip");
                        File srtPath = new File(subsDirectory, fileName + ".srt");

                        subsDirectory.mkdirs();

                        if(zipPath.exists()) {
                            File to = new File(subsDirectory, "temp" + System.currentTimeMillis());
                            zipPath.renameTo(to);
                            to.delete();
                        }
                        if(srtPath.exists()) {
                            File to = new File(subsDirectory, "temp2" + System.currentTimeMillis());
                            srtPath.renameTo(to);
                            to.delete();
                        }

                        if(response.request().urlString().contains(".zip")) {
                            SubsProvider.unpack(response.body().byteStream(), srtPath);
                        } else {
                            FileUtils.saveStringFile(response.body().byteStream(), srtPath);
                        }
                        zipPath.delete();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        callback.onFailure(response.request(), e);
                        failure = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.onFailure(response.request(), e);
                        failure = true;
                    } finally {
                        if(fileOutputStream != null)
                            fileOutputStream.close();
                        if(inputStream != null)
                            inputStream.close();

                        if(!failure) callback.onResponse(response);
                    }
                } else {
                    callback.onFailure(response.request(), new IOException("Unknown error"));
                }
            }
        });

        return call;
    }

    private static void unpack(InputStream is, File srtFile) throws IOException {
        String filename;
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;

        while ((ze = zis.getNextEntry()) != null)
        {
            filename = ze.getName();
            if(filename.contains("_MACOSX")) return;

            if(filename.contains(".srt")) {
                FileUtils.saveStringFile(zis, srtFile);
                zis.closeEntry();
            }
        }

        zis.close();
    }
}
