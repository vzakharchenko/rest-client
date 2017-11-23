
package com.modeln.restclient;

import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.util.Map;

public class WebRequest {
    private String url;
    private HttpMethod httpMethod;
    private MultiValueMap<String, String> httpHeaders;
    private Object requestBody;
    private Type responseClass;
    private Class<?> returnType;
    private Map<String, Object> uriVariables;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public MultiValueMap<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(MultiValueMap<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public Type getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Type responseClass) {
        this.responseClass = responseClass;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Map<String, Object> getUriVariables() {
        return uriVariables;
    }

    public void setUriVariables(Map<String, Object> uriVariables) {
        this.uriVariables = uriVariables;
    }

    @Override
    public String toString() {
        return "WebRequest{" + "url='" + url + '\'' + ", httpMethod=" + httpMethod + ", httpHeaders=" + httpHeaders + ", requestBody=" + requestBody + ", responseClass=" + responseClass + ", returnType=" + returnType + ", uriVariables=" + uriVariables + '}';
    }
}
