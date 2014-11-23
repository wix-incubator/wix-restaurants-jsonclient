package com.wix.restaurants.jsonclient;

import java.io.IOException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

	public <T> T post(String url, String filename, BinaryFile binaryFile, TypeReference<T> responseType) throws IOException {
		// Remove this when Google HTTP Java Client supports multipart/form-data
		// @see https://code.google.com/p/google-http-java-client/issues/detail?id=107
		final String boundary = Long.toHexString(System.currentTimeMillis());
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final PrintStream printer = new PrintStream(baos, false, "UTF-8");
			try {
				// Send binary file.
				printer.println("--" + boundary);
				printer.println("Content-Disposition: form-data; name=\"source\"; filename=\"" + filename + "\"");
				printer.println("Content-Type: " + binaryFile.getContentType());
				printer.println("Content-Transfer-Encoding: binary");
				printer.println();
				printer.write(binaryFile.getContent());
				printer.println();

				// End of multipart/form-data.
				printer.println("--" + boundary + "--");
			} finally {
				printer.close();
			}
		} finally {
			baos.close();
		}

		final HttpContent content = new ByteArrayContent("multipart/form-data; boundary=" + boundary, baos.toByteArray());
		final HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(url), content);
		if (connectTimeout != null) {
			request.setConnectTimeout(connectTimeout.intValue());
		}
		if (readTimeout != null) {
			request.setReadTimeout(readTimeout);
		}
		request.getHeaders().setAccept("application/json");

		request.setThrowExceptionOnExecuteError(false);
		request.setFollowRedirects(false); // Works around Google HTTP Java Client attempting to send content on SEE_OTHER
		final HttpResponse response = request.execute();
		try {
			return followAndParseJsonResponse(response, responseType);
		} finally {
			response.ignore();
		}
	}

	private <T> T followAndParseJsonResponse(HttpResponse response, TypeReference<T> responseType) throws IOException {
		final String location = response.getHeaders().getLocation();
		if (location == null) {
			return parseJsonResponse(response, responseType);
		}

		final HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(location));
		if (connectTimeout != null) {
			request.setConnectTimeout(connectTimeout.intValue());
		}
		if (readTimeout != null) {
			request.setReadTimeout(readTimeout);
		}
		request.getHeaders().setAccept("application/json");

		request.setThrowExceptionOnExecuteError(false);
		final HttpResponse followedResponse = request.execute();
		try {
			return followAndParseJsonResponse(followedResponse, responseType);
		} finally {
			response.ignore();
		}
	}
}
