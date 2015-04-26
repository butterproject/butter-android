/*
 * HttpConnection
 * Connect SDK
 *
 * Copyright (c) 2015 LG Electronics.
 * Created by Oleksii Frolov on 20 Apr 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.connectsdk.etc.helper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP connection implementation based on this article http://android-developers.blogspot.com/2011/09/androids-http-clients.html
 * Also DefaultHttpClient has been deprecated since Android 5.1
 */
public abstract class HttpConnection {

    public static HttpConnection newInstance(URI uri) throws IOException {
        return new HttpURLConnectionClient(uri);
    }

    public static HttpConnection newSubscriptionInstance(URI uri) throws IOException {
        return new CustomConnectionClient(uri);
    }

    public abstract void setMethod(Method method) throws ProtocolException;

    public abstract int getResponseCode() throws IOException;

    public abstract String getResponseString() throws IOException;

    public abstract void execute() throws IOException;

    public abstract void setPayload(String payload);

    public abstract void setPayload(byte[] payload);

    public abstract void setHeader(String name, String value);

    public abstract String getResponseHeader(String name);

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    private static class HttpURLConnectionClient extends HttpConnection {

        private final HttpURLConnection connection;
        private byte[] payload;
        private String response;
        private int responseCode;

        private HttpURLConnectionClient(URI uri) throws IOException {
            this.connection = (HttpURLConnection) uri.toURL().openConnection();
        }

        @Override
        public void setMethod(Method method) throws ProtocolException {
            connection.setRequestMethod(method.name());
        }

        @Override
        public int getResponseCode() throws IOException {
            return responseCode;
        }

        @Override
        public String getResponseString() throws IOException {
            return response;
        }

        @Override
        public void execute() throws IOException {
            try {
                if (payload != null) {
                    BufferedOutputStream writer = new BufferedOutputStream(connection.getOutputStream());
                    writer.write(payload);
                    writer.flush();
                    writer.close();
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while (null != (line = reader.readLine())) {
                        sb.append(line);
                        sb.append("\r\n");
                    }
                    reader.close();
                    this.response = sb.toString();
                } catch (Exception e) {
                    // it's OK, we have a response code
                }
                responseCode = connection.getResponseCode();
            } finally {
                connection.disconnect();
            }
        }

        @Override
        public void setPayload(String payload) {
            this.payload = payload.getBytes();
            connection.setDoOutput(true);
        }

        @Override
        public void setPayload(byte[] payload) {
            this.payload = payload;
            connection.setDoOutput(true);
        }

        @Override
        public void setHeader(String name, String value) {
            connection.setRequestProperty(name, value);
        }

        @Override
        public String getResponseHeader(String name) {
            return connection.getHeaderField(name);
        }
    }

    private static class CustomConnectionClient extends HttpConnection {

        private final URI uri;
        private Method method;
        private String payload;
        private Map<String, String> headers = new HashMap<String, String>();
        private int code;
        private String response;
        private Map<String, String> responseHeaders = new HashMap<String, String>();

        private CustomConnectionClient(URI uri) {
            this.uri = uri;
        }

        @Override
        public void setMethod(Method method) throws ProtocolException {
            this.method = method;
        }

        @Override
        public int getResponseCode() throws IOException {
            return code;
        }

        @Override
        public String getResponseString() throws IOException {
            return response;
        }

        @Override
        public void execute() throws IOException {
            Socket socket = new Socket(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 80);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send request
            writer.println(method.name() + " " + uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "") + " HTTP/1.1");
            writer.println("Host:" + uri.getHost());
            for (Map.Entry<String, String> pair : headers.entrySet()) {
                writer.println(pair.getKey() + ":" + pair.getValue());
            }
            writer.println("");
            if (payload != null) {
                writer.print(payload);
            }
            writer.flush();

            // receive response
            StringBuilder sb = new StringBuilder();
            String line;
            line = reader.readLine();
            if (line != null) {
                String[] tokens = line.split(" ");
                if (tokens.length > 2) {
                    code = Integer.parseInt(tokens[1]);
                }
            }

            while (null != (line = reader.readLine())) {
                if (line.isEmpty()) {
                    break;
                }
                String[] pair = line.split(":");
                if (pair != null && pair.length == 2) {
                    responseHeaders.put(pair[0].trim(), pair[1].trim());
                }
            }

            while (null != (line = reader.readLine())) {
                sb.append(line);
                sb.append("\r\n");
            }
            response = sb.toString();
            socket.close();
        }

        @Override
        public void setPayload(String payload) {
            this.payload = payload;
        }

        @Override
        public void setPayload(byte[] payload) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHeader(String name, String value) {
            this.headers.put(name, value);
        }

        @Override
        public String getResponseHeader(String name) {
            return responseHeaders.get(name);
        }
    }
}