package com.modeln.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SpringWebAnnotationHelper {
    private static final Logger logger = LoggerFactory.getLogger(SpringWebAnnotationHelper.class);
    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String HTTP_METHOD_NOT_SPECIFIED = "Http method not specified";
    private static final String PARAMETER_WITH_REQUEST_BODY_ANNOTATION_IS_NOT_UNIQUE = "Parameter with RequestBody annotation is not unique!";
    private static final String REST_CLIENT_CAN_T_IDENTIFY_NAME_BY_PARAMETER_NAME = "REST client can't identify name by parameter name";
    private static final String CAN_T_DETERMINE_NAME_OF_THE_HEADER_URI_QUERY_PARAMETER = "Can't determine name of the Header/URI/Query parameter!";

    private static final SpringWebAnnotationHelper springWebAnnotationHelper = new SpringWebAnnotationHelper();

    public static synchronized SpringWebAnnotationHelper getInstance(){
        return springWebAnnotationHelper;
    }

    private SpringWebAnnotationHelper() {
    }

    public RestWebServiceMethodHolder resolveRestWSMethod(Class<?> wsClientInterface, Method method) {
        if (!method.isAnnotationPresent(RequestMapping.class)) {
            logger.debug(wsClientInterface + " is not web interface");
            return null;
        }

        RequestMapping classMapping = wsClientInterface.getAnnotation(RequestMapping.class);
        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);


        RestWebServiceMethodHolder info = new RestWebServiceMethodHolder(
                getWSPath(classMapping, methodMapping),
                getHttpMethod(classMapping, methodMapping));
        info.setResponseClass(getReturnType(method));
        info.setRequestParameterIndex(getRequestIndex(method));
        info.setHeaderParameters(getParametersWithAnnotation(method, RequestHeader.class));
        info.setUriVariableParameters(getParametersWithAnnotation(method, PathVariable.class));
        info.setQueryParameters(getParametersWithAnnotation(method, RequestParam.class));
        String[] produced = methodMapping.produces();
        if (produced != null) {
            info.getHttpHeaders().put(ACCEPT, Arrays.asList(produced));
        }

        String[] value = methodMapping.consumes();
        if (value != null) {
            info.getHttpHeaders().put(CONTENT_TYPE, Arrays.asList(value));
        }
        return info;
    }


    Type getReturnType(Method method) {
        Type type = getReturnType0(method);
        if (type == Object.class) {
            return String.class;
        }
        return type;
    }

    private Type getReturnType0(Method method) {
        if (method.isAnnotationPresent(ResponseBody.class)) {
            Type returnType = method.getGenericReturnType();

            if (returnType instanceof ParameterizedTypeImpl) {
                ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) returnType;
                if (method.getReturnType() == ResponseEntity.class) {
                    return parameterizedType.getActualTypeArguments()[0];
                }
                return parameterizedType;
            } else {
                return returnType;
            }
        } else {
            return method.getReturnType();
        }
    }


    private String getWSPath(RequestMapping classMapping, RequestMapping methodMapping) {
        String path = getPath(methodMapping);
        return new UrlBuilder().
                setBaseUrl(getPath(classMapping)).
                setPath(path).
                build();
    }

    private String getPath(RequestMapping requestMapping) {
        String[] strings = requestMapping.value().length > 0 ? requestMapping.value() : requestMapping.path();
        return strings.length > 0 ? strings[0] : null;
    }

    private static HttpMethod getHttpMethod(RequestMapping classMapping, RequestMapping methodMapping) {
        if (methodMapping.method().length > 0) {
            return HttpMethod.valueOf(methodMapping.method()[0].name());
        }

        if (classMapping.method().length > 0) {
            return HttpMethod.valueOf(classMapping.method()[0].name());
        }

        throw new IllegalStateException(HTTP_METHOD_NOT_SPECIFIED);
    }

    private static Integer getRequestIndex(Method method) {
        Integer requestIndex = null;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            if (hasAnnotation(annotations, RequestBody.class)) {
                if (requestIndex == null) {
                    requestIndex = i;
                } else {
                    throw new IllegalStateException(PARAMETER_WITH_REQUEST_BODY_ANNOTATION_IS_NOT_UNIQUE);
                }
            }
        }
        return requestIndex;
    }

    private static boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == annotationClass) {
                return true;
            }
        }

        return false;
    }

    private Map<String, Integer> getParametersWithAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        Map<String, Integer> parameters = new HashMap<String, Integer>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            if (hasAnnotation(annotations, annotationClass)) {
                String name = identifyNameByAnnotation(annotations);
                if (StringUtils.isEmpty(name)) {
                    throw new IllegalStateException(REST_CLIENT_CAN_T_IDENTIFY_NAME_BY_PARAMETER_NAME);
                }
                parameters.put(name, i);
            }
        }
        return parameters;
    }

    private String identifyNameByAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == RequestParam.class) {
                RequestParam requestParam = (RequestParam) annotation;
                String value = StringUtils.isEmpty(requestParam.value()) ? requestParam.name() : requestParam.value();
                return StringUtils.isEmpty(value) && requestParam.required() ? requestParam.defaultValue() : value;
            }

            if (annotation.annotationType() == RequestHeader.class) {
                RequestHeader requestHeader = (RequestHeader) annotation;
                String value = StringUtils.isEmpty(requestHeader.value()) ? requestHeader.name() : requestHeader.value();
                return StringUtils.isEmpty(value) && requestHeader.required() ? requestHeader.defaultValue() : value;
            }

            if (annotation.annotationType() == PathVariable.class) {
                PathVariable pathVariable = (PathVariable) annotation;
                return StringUtils.isEmpty(pathVariable.value()) ? pathVariable.name() : pathVariable.value();
            }
        }

        throw new IllegalStateException(CAN_T_DETERMINE_NAME_OF_THE_HEADER_URI_QUERY_PARAMETER);
    }
}
