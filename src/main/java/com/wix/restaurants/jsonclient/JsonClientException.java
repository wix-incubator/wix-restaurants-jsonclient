package com.wix.restaurants.jsonclient;

import java.io.IOException;

/** An exception thrown by a RESTful HTTP server, with an optional returned value. */
public class JsonClientException extends IOException {
	private static final long serialVersionUID = 1L;
	
    private final int httpErrorCode;
    private final String response;
    
	public JsonClientException(int httpErrorCode, String response) {
        this.httpErrorCode = httpErrorCode;
        this.response = response;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }
    
    public String getResponse() {
        return response;
    }
}
