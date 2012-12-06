package org.eclipse.jetty.nosql.rest.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClient {

    private static final Logger logger = Logger.getLogger("HttpClient");
    private static final String ENCODING = "UTF-8";
    private final int connectionTimeoutInMilliseconds;
    private final int readTimeoutInMilliseconds;
    private final String clientId;

    public HttpClient() {
        connectionTimeoutInMilliseconds = Integer.valueOf(System.getProperty(Constants.HTTP_CONNECTION_TIMEOUT, "500"));
        readTimeoutInMilliseconds = Integer.valueOf(System.getProperty(Constants.HTTP_READ_TIMEOUT, "2000"));
        clientId = System.getProperty(Constants.REST_CLIENT_ID, "jetty-nosql-rest");
    }

    public static class HttpResponse {

        public final String response;
        public final String vClock;

        public HttpResponse(String response, String vClock) {
            this.response = response;
            this.vClock = vClock;
        }
    }

    public HttpResponse doHttp(String operation, String urlStr, String content, int expectedCode, String vClock) throws Exception {
        logger.fine("doHttp: URL" + urlStr + " Operation: " + operation);
        final URL url = new URL(urlStr);

        final HttpURLConnection conn = createConnection(operation, url);

        setRequestProperties(conn, vClock);
        conn.setDoInput(true);
        writeContent(content, conn);

        return parseResponse(conn, expectedCode);
    }

    private HttpResponse parseResponse(HttpURLConnection conn, final int expectedCode) throws Exception {
        
        checkResponseCode(expectedCode, conn);
        final String response = readResponse(conn);

        HttpResponse httpResponse = new HttpResponse(response, conn.getHeaderField("X-Riak-Vclock"));

        return httpResponse;
    }

    private HttpURLConnection createConnection(String operation, final URL url) throws IOException, ProtocolException {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(operation);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setReadTimeout(readTimeoutInMilliseconds);
        conn.setConnectTimeout(connectionTimeoutInMilliseconds);
        conn.setInstanceFollowRedirects(true);
        return conn;
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
        final StringBuilder sb = new StringBuilder();

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), ENCODING))) {
            doRead(sb, rd);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Got IOException while reading response: ", ioe);
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), ENCODING))) {
                doRead(sb, rd);
            }
        }
        return sb.toString();
    }

    private static void doRead(final StringBuilder sb, BufferedReader rd) throws IOException {
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
    }

    private static void writeContent(String content, HttpURLConnection conn) throws IOException, UnsupportedEncodingException {
        if (content != null) {
            conn.setDoOutput(true);
            try (OutputStream out = conn.getOutputStream()) {
                try (OutputStreamWriter writer = new OutputStreamWriter(out, ENCODING)) {
                    writer.write(content);
                }
            }
        }
    }

    private void setRequestProperties(HttpURLConnection conn, String vClock) {
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Riak-ClientId", clientId);
        if (vClock != null) {
            conn.setRequestProperty("X-Riak-Vclock", vClock);
        }
    }
}
