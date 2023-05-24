package com.thefirstlineofcode.sand.demo.server.fileupload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.util.FileSystemUtils;

import com.thefirstlineofcode.sand.demo.server.web.fileupload.FileUploadApplication;
import com.thefirstlineofcode.sand.demo.server.web.fileupload.storage.StorageException;
import com.thefirstlineofcode.sand.demo.server.web.fileupload.storage.StorageService;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SpringBootTest(classes = {FileUploadApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
public class FileUploadTests {
	private static final Path DOWNLOAD_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "sand-demo-download-dir");
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private StorageService storageService;
	
	@Test
	public void testFileUpload() throws IOException {
		OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES).build();
		Response response = client.newCall(getFileUploadRequest()).execute();
		
		assertEquals(response.code(), 200);
		
		try {
			Files.createDirectories(DOWNLOAD_DIR);
		} catch (IOException e) {
			throw new StorageException("Could not create download directory.", e);
		}
		
		Call call = client.newCall(getFileDownloadRequest());
		processResponse(call.execute());
		
		storageService.deleteAll();
		FileSystemUtils.deleteRecursively(DOWNLOAD_DIR.toFile());
	}

	private void processResponse(Response response) throws IOException {
		if (response.code() != 200)
			fail();
		
		BufferedOutputStream output = null;
		InputStream input = response.body().byteStream();
		byte[] buf = new byte[2048];
		int len;
		try {
			output = new BufferedOutputStream(new FileOutputStream(
					DOWNLOAD_DIR.resolve("file.jpg").toFile()));
			while ((len = input.read(buf, 0, 2048)) != -1) {
				output.write(buf, 0, len);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (output != null)
				output.close();
		}
	}
	
	private Request getFileDownloadRequest() {
		return new Request.Builder().url("http://localhost:" + port + "/files/file.jpg").build();
	}

	private Request getFileUploadRequest() {
		RequestBody requestBody = new MultipartBody.Builder().
				setType(MultipartBody.FORM).
				addFormDataPart("file", "file.jpg",
						RequestBody.create(new File("src/test/resources/file.jpg"),
								MediaType.parse("application/octet-stream"))).
				build();
		
		return new Request.Builder().url("http://localhost:" + port + "/file-upload").post(requestBody).build();
	}
}
