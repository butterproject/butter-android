package pct.droid.utils;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

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

    /* NEEDS IMPROVEMENTS */
    public static String inputstreamToCharsetString(InputStream inputStream) throws IOException {
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

        String detectedCharset = charsetDetector.getDetectedCharset();
        charsetDetector.reset();
        if (detectedCharset == null || detectedCharset.isEmpty()) {
            detectedCharset = "UTF-8";
        } else if("MACCYRILLIC".equals(detectedCharset)) {
            detectedCharset = "Windows-1256";
        }

        byte[] stringBytes = byteArrayOutputStream.toByteArray();
        String charsetString = new String(byteArrayOutputStream.toByteArray(), detectedCharset);
        Charset charset = Charset.forName(detectedCharset);
        CharsetDecoder decoder = charset.newDecoder();

        try {
            CharBuffer cbuf = decoder.decode(ByteBuffer.wrap(stringBytes));
            return cbuf.toString();
        } catch (CharacterCodingException e) {
            return new String(stringBytes, detectedCharset);
        }
    }

    public static void saveStringFile(InputStream inputStream, File path) throws IOException {
        String outputString = inputstreamToCharsetString(inputStream);
        saveStringToFile(outputString, path, "UTF-8");
    }

    public static void saveStringFile(String inputStr, File path) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inputStr.getBytes());
        saveStringFile(inputStream, path);
    }

    public static void saveStringFile(String[] inputStr, File path) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for(String str : inputStr) {
            stringBuilder.append(str).append("\n");
        }
        saveStringFile(stringBuilder.toString(), path);
    }

    public static void saveStringToFile(String string, File path, String encoding) throws IOException {
        if(path.exists()) {
            path.delete();
        }

        if((path.getParentFile().mkdirs() || path.getParentFile().exists()) && path.createNewFile()) {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), encoding));
            writer.write(string);
            writer.close();
        }
    }

}
