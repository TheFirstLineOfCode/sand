package com.thefirstlineofcode.sand.demo.server.web.fileupload.storage;

public class StorageFileNotFoundException extends StorageException {

	private static final long serialVersionUID = -3634724420589580691L;

	public StorageFileNotFoundException(String message) {
		super(message);
	}

	public StorageFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
