package com.thefirstlineofcode.sand.client.things.simple.camera;
import java.io.File;

import com.thefirstlineofcode.sand.client.thing.IThing;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakePhoto;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakeVideo;

public interface ISimpleCamera extends IThing {
	public static final int ERROR_CODE_PHOTO_WAS_NOT_TAKEN = -1;
	public static final int FAILED_TO_UPLOAD_PHOTO = -2;
	public static final int ERROR_CODE_VIDEO_WAS_NOT_TAKEN = -3;
	public static final int FAILED_TO_UPLOAD_VIDEO = -4;
	
	File takePhoto(TakePhoto takePhoto) throws ExecutionException;
	File takeVideo(TakeVideo takeVideo) throws ExecutionException;
}
