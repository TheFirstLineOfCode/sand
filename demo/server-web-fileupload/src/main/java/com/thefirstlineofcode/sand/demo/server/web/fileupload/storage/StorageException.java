package com.thefirstlineofcode.sand.demo.server.web.fileupload.storage;

public class StorageException extends Exception {

	private static final long serialVersionUID = 5089047755272393851L;

	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
