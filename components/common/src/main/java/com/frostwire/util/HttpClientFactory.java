package com.frostwire.util;



public class HttpClientFactory {
    
    public static HttpClient newInstance(HttpClientType type) {
        switch (type) {
        case Apache:
            throw new UnsupportedOperationException();
        case PureJava:
            return new FWHttpClient();
        default:
            throw new IllegalArgumentException();
        }
    }
}
