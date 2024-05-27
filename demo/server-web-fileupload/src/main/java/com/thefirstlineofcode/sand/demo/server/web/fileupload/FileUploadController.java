package com.thefirstlineofcode.sand.demo.server.web.fileupload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thefirstlineofcode.sand.demo.server.web.fileupload.storage.StorageException;
import com.thefirstlineofcode.sand.demo.server.web.fileupload.storage.StorageFileNotFoundException;
import com.thefirstlineofcode.sand.demo.server.web.fileupload.storage.StorageService;

@Controller
public class FileUploadController {
	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}
	
	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		Resource file = null;
		try {
			file = storageService.loadAsResource(filename);
		} catch (StorageException e) {
			return ResponseEntity.internalServerError().body(null);
		}
		
		if (file == null)
			return ResponseEntity.notFound().build();
		
		if (filename.endsWith(".jpg")) {			
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=\"" + file.getFilename() + "\"").body(file);
		} else {
			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).
				contentType(MediaTypeFactory.getMediaType(file).orElse(MediaType.APPLICATION_OCTET_STREAM)).
				body(file);
		}
	}

	@PostMapping("/file-upload")
	public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) {
		try {
			storageService.store(file);
		} catch (StorageException e) {
			return ResponseEntity.internalServerError().body(null);
		}
		
		return ResponseEntity.ok().body(null);
	}
	
	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
