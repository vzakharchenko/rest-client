
package com.modeln.restclient;

import org.springframework.util.MultiValueMap;

public class WebResponse {
    private int status;
    private MultiValueMap<String, String> httpHeaders;
    private Object result;

    public WebResponse(int status, MultiValueMap<String, String> httpHeaders, Object result) {
        this.status = status;
        this.httpHeaders = httpHeaders;
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    @SuppressWarnings("unused")
    public MultiValueMap<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "WebResponse{" + "status=" + status + ", httpHeaders=" + httpHeaders + ", result=" + result + '}';
    }
}
