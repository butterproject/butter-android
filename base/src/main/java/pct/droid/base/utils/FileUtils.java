package pct.droid.base.utils;

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

import pct.droid.base.io.UnicodeBOMInputStream;

public class FileUtils {

    /**
     * Get contents of a file as String
     *
     * @param filePath File path as String
     * @return Contents of the file
     * @throws IOException
     */
    public static String getContentsAsString(String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    /**
     * Convert an {@link InputStream} to a String
     *
     * @param inputStream InputStream
     * @return String contents of the InputStream
     * @throws IOException
     */
    private static String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Delete every item below the File location
     *
     * @param file Location
     */
    public static void recursiveDelete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            if(children == null) return;
            for (int i = 0; i < children.length; i++) {
                recursiveDelete(new File(file, children[i]));
            }
        }
        file.delete();
    }

    /**
     * Get the charset of the contents of an {@link InputStream}
     *
     * @param inputStream {@link InputStream}
     * @return Charset String name
     * @throws IOException
     */
    public static String inputstreamToCharsetString(InputStream inputStream) throws IOException {
        UniversalDetector charsetDetector = new UniversalDetector(null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        UnicodeBOMInputStream bomInputStream = new UnicodeBOMInputStream(inputStream);
        bomInputStream.skipBOM();
        byte data[] = new byte[1024];
        int count;
        while ((count = bomInputStream.read(data)) != -1) {
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
        } else if ("MACCYRILLIC".equals(detectedCharset)) {
            detectedCharset = "Windows-1256";
        }

        byte[] stringBytes = byteArrayOutputStream.toByteArray();
        Charset charset = Charset.forName(detectedCharset);
        CharsetDecoder decoder = charset.newDecoder();

        try {
            CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(stringBytes));
            return charBuffer.toString();
        } catch (CharacterCodingException e) {
            return new String(stringBytes, detectedCharset);
        }
    }

    /**
     * Save {@link InputStream} to {@link File}
     *
     * @param inputStream InputStream that will be saved
     * @param path        Path of the file
     * @throws IOException
     */
    public static void saveStringFile(InputStream inputStream, File path) throws IOException {
        String outputString = inputstreamToCharsetString(inputStream);
        saveStringToFile(outputString, path, "UTF-8");
    }

    /**
     * Save {@link String} to {@link File}
     *
     * @param inputStr String that will be saved
     * @param path     Path of the file
     * @throws IOException
     */
    public static void saveStringFile(String inputStr, File path) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inputStr.getBytes());
        saveStringFile(inputStream, path);
    }

    /**
     * Save {@link String} array  to {@link File}
     *
     * @param inputStr String array that will be saved
     * @param path     {@link File}
     * @throws IOException
     */
    public static void saveStringFile(String[] inputStr, File path) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : inputStr) {
            stringBuilder.append(str).append("\n");
        }
        saveStringFile(stringBuilder.toString(), path);
    }

    /**
     * Save {@link String} to {@link File} witht the specified encoding
     *
     * @param string {@link String}
     * @param path   Path of the file
     * @param string Encoding
     * @throws IOException
     */
    public static void saveStringToFile(String string, File path, String encoding) throws IOException {
        if (path.exists()) {
            path.delete();
        }

        if ((path.getParentFile().mkdirs() || path.getParentFile().exists()) && (path.exists() || path.createNewFile())) {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), encoding));
            writer.write(string);
            writer.close();
        }
    }

}
