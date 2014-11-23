package com.wix.restaurants.jsonclient;

/** A raw file. */
public class BinaryFile {
	private final String contentType;
    private final byte[] content;
    
    public BinaryFile(String contentType, byte[] content) {
        this.contentType = contentType;
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }
    
    @Override
	public String toString() {
		return "BinaryFile [contentType=" + contentType + ", contentLength=" + content.length + "]";
	}
}
