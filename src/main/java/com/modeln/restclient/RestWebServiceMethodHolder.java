

package com.modeln.restclient;

import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

public class RestWebServiceMethodHolder {
    private String path;
    private Map<String, Integer> uriVariableParameters;
    private Map<String, Integer> queryParameters;
    private Map<String, Integer> headerParameters;
    private HttpMethod httpMethod;
    private MultiValueMap<String, String> httpHeaders;
    private Integer requestParameterIndex;
    private Type responseClass;

    public RestWebServiceMethodHolder(String path, HttpMethod httpMethod) {

        this.path = path;
        this.httpMethod = httpMethod;

        httpHeaders = new LinkedMultiValueMap<>();
    }

    public String getPath() {
        return path;
    }

    public Map<String, Integer> getUriVariableParameters() {
        return uriVariableParameters;
    }

    public void setUriVariableParameters(Map<String, Integer> uriParameters) {
        this.uriVariableParameters = uriParameters;
    }

    public Map<String, Integer> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, Integer> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public Map<String, Integer> getHeaderParameters() {
        return headerParameters;
    }

    public void setHeaderParameters(Map<String, Integer> headerParameters) {
        this.headerParameters = headerParameters;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public MultiValueMap<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void addHeader(String key, String value) {
        httpHeaders.add(key, value);
    }

    public Integer getRequestParameterIndex() {
        return requestParameterIndex;
    }

    public void setRequestParameterIndex(Integer requestParameterIndex) {
        this.requestParameterIndex = requestParameterIndex;
    }

    public Type getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Type responseClass) {
        this.responseClass = responseClass;
    }

    @Override
    public String toString() {
        return "RestWebServiceMethodHolder{" + "path='" + path + '\'' + ", uriVariableParameters=" + uriVariableParameters + ", queryParameters=" + queryParameters + ", headerParameters=" + headerParameters + ", httpMethod=" + httpMethod + ", httpHeaders=" + httpHeaders + ", requestParameterIndex=" + requestParameterIndex + ", responseClass=" + responseClass + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestWebServiceMethodHolder)) return false;

        RestWebServiceMethodHolder that = (RestWebServiceMethodHolder) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(httpMethod, that.httpMethod) &&
                Objects.equals(requestParameterIndex, that.requestParameterIndex) &&
                Objects.equals(headerParameters, that.headerParameters) &&
                Objects.equals(httpHeaders, that.httpHeaders) &&
                Objects.equals(queryParameters, that.queryParameters) &&
                Objects.equals(uriVariableParameters, that.uriVariableParameters);
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        return result;
    }
}