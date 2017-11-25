/*
 * HttpMessage
 * Connect SDK
 * 
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 13 Feb 2014
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class HttpMessage {
    public final static String CONTENT_TYPE_HEADER = "Content-Type";
    public final static String CONTENT_TYPE_TEXT_XML = "text/xml; charset=utf-8";
    public final static String CONTENT_TYPE_APPLICATION_PLIST = "application/x-apple-binary-plist";
    public final static String UDAP_USER_AGENT = "UDAP/2.0";
    public final static String LG_ELECTRONICS = "LG Electronics";
    public final static String USER_AGENT = "User-Agent";
    public final static String SOAP_ACTION = "\"urn:schemas-upnp-org:service:AVTransport:1#%s\"";
    public final static String SOAP_HEADER = "Soapaction";
    public final static String NEW_LINE = "\r\n";

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
