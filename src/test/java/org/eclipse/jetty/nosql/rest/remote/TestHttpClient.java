package org.eclipse.jetty.nosql.rest.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class TestHttpClient {
    private static final Logger logger = Logger.getLogger("HttpClient");

    private TestHttpClient() {
    }

    public static class HttpResponse {

        private String response;
        private Map<String, String> cookies = new HashMap<String, String>();

        public String getResponse() {
            return response;
        }

        public void addCookie(String cookieName, String cookieValue) {
            cookies.put(cookieName, cookieValue);
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public Map<String, String> getCookies() {
            return cookies;
        }

    }

    public static HttpResponse doHttp(String operation, String urlStr, String content, int expectedCode, Map<String, String> cookies) throws Exception {
        logger.info("doHttp: URL" + urlStr + " Operation: " + operation);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(operation);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);

        setRequestProperties(conn);

        setCookies(cookies, conn);

        conn.setDoInput(true);
        writeContent(content, conn);

        checkResponseCode(expectedCode, conn);

        final String response = readResponse(conn);
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponse(response);
        getCookies(conn, httpResponse);
        conn.disconnect();

        return httpResponse;
    }

    private static void checkResponseCode(int expectedCode, HttpURLConnection conn) throws IOException {

        int response = conn.getResponseCode();
        logger.info("Got response: " + response + " expected: " + expectedCode);
        if (response == 404 && expectedCode != 404) {
            throw new NotFoundException();
        }
        if (response != expectedCode) {
            throw new IOException(conn.getResponseMessage());
        }
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        return sb.toString();
    }

    private static void writeContent(String content, HttpURLConnection conn) throws IOException, UnsupportedEncodingException {
        if (content != null) {
            conn.setDoOutput(true);
            OutputStream out = conn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(content);
            writer.close();
            out.close();
        }
    }

    private static void setRequestProperties(HttpURLConnection conn) {
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
    }

    private static void setCookies(Map<String, String> cookies, HttpURLConnection conn) {
        if (cookies != null) {
            StringBuffer cookieSB = new StringBuffer();
            for (Entry<String, String> entry : cookies.entrySet()) {
                cookieSB.append(entry.getKey() + "=" + entry.getValue() + "; ");
            }
            conn.setRequestProperty("Cookie", cookieSB.toString());
        }
    }

    private static void getCookies(HttpURLConnection conn, HttpResponse httpResponse) {
        String headerName;
        for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = conn.getHeaderField(i);
                cookie = cookie.substring(0, cookie.indexOf(";"));
                String cookieName = cookie.substring(0, cookie.indexOf("="));
                String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
                httpResponse.addCookie(cookieName, cookieValue);
            }
        }
    }
}
