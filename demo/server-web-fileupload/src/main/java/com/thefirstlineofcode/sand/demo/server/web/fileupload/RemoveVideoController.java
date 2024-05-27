package com.thefirstlineofcode.sand.demo.server.web.fileupload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thefirstlineofcode.sand.demo.server.web.fileupload.storage.StorageException;
import com.thefirstlineofcode.sand.demo.server.web.fileupload.storage.StorageFileNotFoundException;
import com.thefirstlineofcode.sand.demo.server.web.fileupload.storage.StorageService;

@RestController
@RequestMapping("remove-video")
public class RemoveVideoController {
	private final StorageService storageService;

	@Autowired
	public RemoveVideoController(StorageService storageService) {
		this.storageService = storageService;
	}
	
	@DeleteMapping("/{video-name}")
	public ResponseEntity<String> removeVideo(@PathVariable("video-name") String videoName) {
		try {
			storageService.delete(videoName);
			
			return ResponseEntity.ok().body(null);
		} catch(StorageFileNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} catch (StorageException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
		
	}
}
