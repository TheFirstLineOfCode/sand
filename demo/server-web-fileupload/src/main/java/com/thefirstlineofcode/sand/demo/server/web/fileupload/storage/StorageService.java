package com.thefirstlineofcode.sand.demo.server.web.fileupload.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	void init() throws StorageException;
	void store(MultipartFile file) throws StorageException;
	Stream<Path> loadAll() throws StorageException;
	Resource loadAsResource(String filename) throws StorageException ;
	void deleteAll() throws StorageException;
	void delete(String filename) throws StorageException;

}
