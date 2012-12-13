package org.eclipse.jetty.nosql.rest.remote;

public class RestClient extends HttpClient {

    public HttpResponse get(String urlString) {
        try {
            return doHttp("GET", urlString, null, 200, null);
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void post(String string, String content, String vClock) {
        try {
            doHttp("POST", string, content, 204, vClock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    
    public void put(String string, String content, String vClock) {
        try {
            doHttp("PUT", string, content, 204, vClock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public void delete(String string, String vClock) {
        try {
            doHttp("DELETE", string, null, 204, vClock);
        } catch (NotFoundException e) {
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
