package com.wix.restaurants.jsonclient;

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.wix.restaurants.json.Json;

/** A client for a RESTful web-service that supports JSON representations. */
public class JsonClient {
    private final HttpRequestFactory requestFactory;
    private final Integer connectTimeout;
    private final Integer readTimeout;
    private final Integer maxRetries;

    public JsonClient(HttpRequestFactory requestFactory, Integer connectTimeout, Integer readTimeout, Integer maxRetries) {
    	this.requestFactory = requestFactory;
    	this.connectTimeout = connectTimeout;
    	this.readTimeout = readTimeout;
    	this.maxRetries = maxRetries;
    }
    
    public <T> T post(String url, Object requestObj, TypeReference<T> responseType) throws IOException {
    	final HttpContent content = new ByteArrayContent("application/json", Json.bytify(requestObj));
    	
    	final HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(url), content);
        if (connectTimeout != null) {
        	request.setConnectTimeout(connectTimeout.intValue());
        }
        if (readTimeout != null) {
        	request.setReadTimeout(readTimeout);
        }
        if (maxRetries != null) {
        	request.setNumberOfRetries(maxRetries);
        }
        request.getHeaders().setAccept("application/json");

        request.setThrowExceptionOnExecuteError(false);
        final HttpResponse response = request.execute();
        try {
            return parseJsonResponse(response, responseType);
        } finally {
        	response.ignore();
        }
    }
    
    private static <T> T parseJsonResponse(HttpResponse response, TypeReference<T> responseType) throws IOException {
    	if (!response.isSuccessStatusCode()) {
            throw new JsonClientException(response.getStatusCode(), response.parseAsString());
    	}
    	
    	return Json.parse(response.parseAsString(), responseType);
    }
}
