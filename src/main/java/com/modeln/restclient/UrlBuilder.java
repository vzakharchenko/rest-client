package com.modeln.restclient;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public class UrlBuilder {
    private String baseUrl;
    private String path;
    private Set<String> queryParameters;

    public UrlBuilder setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public UrlBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public UrlBuilder setQueryParameters(Set<String> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public String build() {
        return new InternalBuilder(baseUrl, path, queryParameters).build();
    }

    @Override
    public String toString() {
        return build();
    }

    private static class InternalBuilder {
        private final String baseUrl;
        private final String path;
        private final Set<String> queryParameters;
        private final StringBuilder builder;

        private InternalBuilder(String baseUrl, String path, Set<String> queryParameters) {
            this.baseUrl = baseUrl;
            this.path = path;
            this.queryParameters = queryParameters;

            builder = new StringBuilder();
        }

        private String build() {
            addPath(baseUrl);
            addPath(path);

            addQueryParameters();

            return builder.toString();
        }

        private void addPath(String path) {
            int startIndex = builder.length();
            if (StringUtils.isNotBlank(path)) {
                builder.append(path);
            } else {
                builder.append('/');
            }

            boolean startFromProtocol = false;
            if (startIndex == 0) {
                startFromProtocol = builder.indexOf("https://") == 0 || builder.indexOf("http://") == 0;
            }

            if (!startFromProtocol && builder.charAt(startIndex) != '/') {
                builder.insert(startIndex, '/');
            }

            if (builder.length() > 1 && builder.charAt(builder.length() - 1) == '/') {
                builder.deleteCharAt(builder.length() - 1);
            }

            if (startIndex > 0 && startIndex < builder.length() &&
                    builder.charAt(startIndex) == '/' &&
                    builder.charAt(startIndex - 1) == '/') {
                builder.deleteCharAt(startIndex);
            }
        }

        private void addQueryParameters() {
            if (queryParameters != null && !queryParameters.isEmpty()) {
                if (builder.indexOf("?") == -1) {
                    builder.append('?');
                }

                for (String queryParameter : queryParameters) {
                    builder.append(queryParameter).append("={").append(queryParameter).append("}&");
                }
            }

            if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '&') {
                builder.deleteCharAt(builder.length() - 1);
            }
        }
    }
}
