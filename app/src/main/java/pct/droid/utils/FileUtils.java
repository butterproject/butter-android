package pct.droid.utils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileUtils {

    public static String getContentsAsString(String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static void recursiveDelete(File file) {
        if(file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                recursiveDelete(new File(file, children[i]));
            }
        }
        file.delete();
    }

    public static void saveStringFile(InputStream inputStream, File path) throws IOException {
        if(path.createNewFile()) {
            UniversalDetector charsetDetector = new UniversalDetector(null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte data[] = new byte[1024];
            int count;
            while ((count = inputStream.read(data)) != -1) {
                if (!charsetDetector.isDone()) {
                    charsetDetector.handleData(data, 0, count);
                }
                byteArrayOutputStream.write(data, 0, count);
            }
            charsetDetector.dataEnd();

            String charset = charsetDetector.getDetectedCharset();
            charsetDetector.reset();
            if (charset == null || charset.isEmpty()) {
                charset = "UTF-8";
            } else if("MACCYRILLIC".equals(charset)) {
                charset = "Windows-1256";
            }

            String outputString = new String(byteArrayOutputStream.toByteArray(), charset);
            saveStringToFile(outputString, path, "utf-8");
        }
    }

    public static void saveStringToFile(String string, File path, String encoding) throws IOException {
        if(path.exists() || path.createNewFile()) {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), encoding));
            writer.write(string);
            writer.close();
        }
    }

}
