
package com.modeln.restclient;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

class RestClientInvocationHandler implements InvocationHandler {
    private RestTemplate restTemplate = new RestTemplate();
    private Class<?> wsClientInterface;
    private Map<Method, RestWebServiceMethodHolder> interfaceMetadata;
    private String baseUrl;
    private Authentication authentication;


    public RestClientInvocationHandler(String baseUrl, Class<?> wsClientInterface, Authentication authentication, ResponseErrorHandler errorHandler) {
        this.wsClientInterface = wsClientInterface;
        this.baseUrl = baseUrl;
        this.authentication = authentication;
        if (errorHandler!=null){
            restTemplate.setErrorHandler(errorHandler);
        }
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory() {
            @Override
            protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {

                if (httpMethod == HttpMethod.GET) {
                    return new HttpGetWithEntity(uri);
                }
                return super.createHttpUriRequest(httpMethod, uri);
            }
        });
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter<Source>());
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        messageConverters.add(new MappingJackson2HttpMessageConverter());
        restTemplate.setMessageConverters(messageConverters);
    }

    Authentication getAuthentication() {
        return authentication;
    }

    void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    void init() {
        interfaceMetadata = parseMethods();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (isObjectMethod(method)) {
            return implementObjectMethods(proxy, method, args);
        }

        RestWebServiceMethodHolder info = interfaceMetadata.get(method);

        WebRequest webRequest = resolve(baseUrl, info, args, method.getReturnType());
        if (authentication != null) {
            webRequest.setHttpHeaders(authentication.enrichHeaders(webRequest.getHttpHeaders()));
        }
        return invoke(webRequest).getResult();
    }

    private boolean isObjectMethod(Method method) {
        try {
            Object.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (SecurityException ex) {
            return false;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }


    private Object implementObjectMethods(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        if (methodName.equals("equals")) {
            return proxy == args[0];
        } else if (methodName.equals("hashCode")) {
            return System.identityHashCode(proxy);
        } else if (methodName.equals("toString")) {
            return wsClientInterface.getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
        } else {
            return null;
        }
    }

    private Map<Method, RestWebServiceMethodHolder> parseMethods() {
        Method[] methods = wsClientInterface.getMethods();
        Map<Method, RestWebServiceMethodHolder> metadata = new HashMap<>(methods.length);

        for (Method method : methods) {
            metadata.put(method, parseMethod(method));
        }

        return Collections.unmodifiableMap(metadata);
    }

    private RestWebServiceMethodHolder parseMethod(Method method) {
        RestWebServiceMethodHolder info = SpringWebAnnotationHelper.getInstance().resolveRestWSMethod(wsClientInterface, method);
        return info;
    }

    public WebResponse invoke(WebRequest request) {
        ResponseEntity<?> responseEntity;
        HttpHeaders headers = toHttpHeaders(request);
        Type responseClass = request.getResponseClass();
        if (responseClass instanceof Resource) {
            responseEntity = restTemplate.exchange(
                    request.getUrl(),
                    request.getHttpMethod(),
                    new HttpEntity<>(request.getRequestBody(), headers),
                    InputStream.class,
                    request.getUriVariables());
        } else if (responseClass instanceof Class) {
            List<MultipartFile> files = getMultiparts(request);
            HttpEntity<Object> requestEntity;
            if (!CollectionUtils.isEmpty(files) || request.getRequestBody() == null) {
                requestEntity = new HttpEntity<>(buildMultipartMAp(files), headers);
            } else {
                requestEntity = new HttpEntity<>(request.getRequestBody(), headers);
            }

            Class aClass = (Class) responseClass;
            responseEntity = restTemplate.exchange(
                    request.getUrl(),
                    request.getHttpMethod(),
                    requestEntity,
                    ResponseEntity.class.isAssignableFrom(aClass) ? Void.class : aClass,
                    request.getUriVariables());
        } else {
            responseEntity = restTemplate.exchange(
                    request.getUrl(),
                    request.getHttpMethod(),
                    new HttpEntity<>(request.getRequestBody(), headers),
                    (ParameterizedTypeReference) new ParameterizedTypeWrapperSpring((ParameterizedTypeImpl) responseClass),
                    request.getUriVariables());
        }


        MultiValueMap copyMultiValueMap = new LinkedMultiValueMap();
        copyMultiValueMap.setAll(responseEntity.getHeaders());
        return new WebResponse(
                responseEntity.getStatusCode().value(),
                copyMultiValueMap,
                request.getReturnType() == ResponseEntity.class ? responseEntity : responseEntity.getBody());
    }

    private List<MultipartFile> getMultiparts(WebRequest request) {
        ArrayList<MultipartFile> result = new ArrayList<>();

        Map<String, Object> uriVariables = request.getUriVariables();
        for (Map.Entry<String, Object> entry : uriVariables.entrySet()) {
            if (entry.getValue() instanceof MultipartFile) {
                result.add((MultipartFile) entry.getValue());
            }
        }
        return result;
    }

    private MultiValueMap<String, Object> buildMultipartMAp(List<MultipartFile> file) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        ByteArrayResource contentsAsResource;
        try {

            for (MultipartFile mpf : file) {
                contentsAsResource = new MultipartResource(mpf.getBytes(), mpf.getOriginalFilename());
                map.add(mpf.getName(), contentsAsResource);
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return map;
    }

    private HttpHeaders toHttpHeaders(WebRequest request) {
        if (request.getHttpHeaders() == null || request.getHttpHeaders().isEmpty()) {
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHttpHeaders());
        return headers;
    }


    public static WebRequest resolve(String baseUrl, RestWebServiceMethodHolder webServiceMethod, Object[] args, Class<?> returnType) {
        WebRequest webRequest = new WebRequest();

        webRequest.setUrl(getUriString(baseUrl, webServiceMethod, args));
        webRequest.setHttpMethod(webServiceMethod.getHttpMethod());
        webRequest.setRequestBody(getRequest(webServiceMethod, args));
        webRequest.setHttpHeaders(getHttpHeaders(webServiceMethod, args));
        webRequest.setResponseClass(webServiceMethod.getResponseClass());
        webRequest.setUriVariables(getUriVariables(webServiceMethod, args));
        webRequest.setReturnType(returnType);

        return webRequest;
    }

    private static String getUriString(String baseUrl, RestWebServiceMethodHolder webServiceMethod, Object[] args) {
        String path = webServiceMethod.getPath();
        Map<String, Integer> uriVariableParameters = webServiceMethod.getUriVariableParameters();

        if (uriVariableParameters != null && !uriVariableParameters.isEmpty()) {
            for (Map.Entry<String, Integer> s : uriVariableParameters.entrySet()) {
                Object arg = args[s.getValue()];
                path = path.replace("{" + s.getKey() + "}", arg.toString());
            }
        }

        return new UrlBuilder().
                setBaseUrl(baseUrl).
                setPath(path).
                setQueryParameters(webServiceMethod.getQueryParameters().keySet()).
                build();
    }

    private static Object getRequest(RestWebServiceMethodHolder info, Object[] args) {
        return info.getRequestParameterIndex() != null ? args[info.getRequestParameterIndex()] : null;
    }

    private static MultiValueMap<String, String> getHttpHeaders(RestWebServiceMethodHolder info, Object[] args) {
        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<String, String>();
        httpHeaders.putAll(info.getHttpHeaders());
        for (Map.Entry<String, Integer> header : info.getHeaderParameters().entrySet()) {
            if (header.getValue() != null) {
                httpHeaders.add(header.getKey(), objectToString(args[header.getValue()]));
            }
        }
        return httpHeaders;
    }

    private static String objectToString(Object value) {
        return BeanUtilsBean2.getInstance().getConvertUtils().convert(value);
    }

    private static Map<String, Object> getUriVariables(RestWebServiceMethodHolder info, Object[] args) {
        Map<String, Integer> uriVariableParameters = info.getUriVariableParameters();
        Map<String, Integer> queryParameters = info.getQueryParameters();
        Map<String, Object> map = new HashMap<String, Object>(uriVariableParameters.size() + queryParameters.size());

        for (Map.Entry<String, Integer> entry : uriVariableParameters.entrySet()) {
            map.put(entry.getKey(), args[entry.getValue()]);
        }

        for (Map.Entry<String, Integer> entry : queryParameters.entrySet()) {

            Object arg = args[entry.getValue()];
            map.put(entry.getKey(), arg);
        }

        return map;
    }

    private static class MultipartResource extends ByteArrayResource {
        private String fileName;

        public MultipartResource(byte[] byteArray, String fileName) {
            super(byteArray);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            MultipartResource that = (MultipartResource) o;

            return fileName.equals(that.fileName);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + fileName.hashCode();
            return result;
        }
    }
}
