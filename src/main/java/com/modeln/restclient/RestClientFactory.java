

package com.modeln.restclient;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class RestClientFactory<T> {

    private Class<T> wsClientInterface;
    private String baseUrl;
    private ResponseErrorHandler errorHandler;

    private Authentication authentication = new DefaultAuthentication();

    private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();


    private RestClientFactory(Class<T> wsClientInterface, String baseUrl) {
        this.wsClientInterface = wsClientInterface;
        this.baseUrl = baseUrl;
    }

    public static <T> RestClientFactory<T> createProxyFactory(Class<T> wsClientInterface, String baseUrl) {
        return new RestClientFactory<>(wsClientInterface, baseUrl);
    }


    public RestClientFactory<T> addErrorHandler(ResponseErrorHandler errorHandler){
        this.errorHandler = errorHandler;
        return this;
    }

    public RestClientFactory<T> addHeader(String headerName, String headerValue){
        headers.add(headerName,headerValue);
        return this;
    }

    public RestClientFactory<T> setAuthentication(Authentication authentication){
        this.authentication = authentication;
        return this;
    }

    public T createClient() {
        InvocationHandler handler = createInvocationHandler(wsClientInterface, baseUrl, authentication, errorHandler);
        return (T) Proxy.newProxyInstance(getClassLoader(), new Class[]{wsClientInterface}, handler);
    }

    private InvocationHandler createInvocationHandler(Class<?> wsClientInterface, String baseUrl, Authentication authentication, ResponseErrorHandler errorHandler) {
        RestClientInvocationHandler handler = new RestClientInvocationHandler(baseUrl, wsClientInterface, authentication, errorHandler);
        handler.init();
        return handler;
    }

    private static ClassLoader getClassLoader() {
        return RestClientFactory.class.getClassLoader();
    }


    private final class DefaultAuthentication implements Authentication {

        @Override
        public MultiValueMap<String, String> enrichHeaders(MultiValueMap<String, String> headers) {
            return headers;
        }
    }
}
