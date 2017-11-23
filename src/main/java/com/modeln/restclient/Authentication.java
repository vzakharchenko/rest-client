
package com.modeln.restclient;


import org.springframework.util.MultiValueMap;

public interface Authentication {
    MultiValueMap<String, String> enrichHeaders(MultiValueMap<String, String> headers);
}
