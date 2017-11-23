package com.modeln.restclient;

import org.springframework.core.ParameterizedTypeReference;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.List;

public class ParameterizedTypeWrapperSpring extends ParameterizedTypeReference<List<String>> {

    private ParameterizedTypeImpl parameterizedType;

    public ParameterizedTypeWrapperSpring(ParameterizedTypeImpl parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    @Override
    public Type getType() {
        return parameterizedType;
    }
}
