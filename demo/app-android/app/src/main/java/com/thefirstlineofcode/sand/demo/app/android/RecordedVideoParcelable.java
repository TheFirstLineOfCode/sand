package com.thefirstlineofcode.sand.demo.app.android;

import android.os.Parcel;
import android.os.Parcelable;

import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;

import java.util.Calendar;

public class RecordedVideoParcelable implements Parcelable {
	private RecordedVideo recordedVideo;
	
	public RecordedVideoParcelable(RecordedVideo recordedVideo) {
		this.recordedVideo = recordedVideo;
	}
	
	public RecordedVideo getRecordedVideo() {
		return recordedVideo;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(recordedVideo.getVideoName());
		dest.writeLong(recordedVideo.getRecordingTime().getTime());
		dest.writeInt(recordedVideo.getRecordingReason().ordinal());
		dest.writeString(recordedVideo.getVideoUrl());
	}
	
	public static final Parcelable.Creator<RecordedVideoParcelable> CREATOR =
			new Parcelable.Creator<RecordedVideoParcelable>() {
		public RecordedVideoParcelable createFromParcel(Parcel in) {
			return new RecordedVideoParcelable(in);
		}
		
		public RecordedVideoParcelable[] newArray(int size) {
			return new RecordedVideoParcelable[size];
		}
	};
	
	private RecordedVideoParcelable(Parcel in) {
		recordedVideo = new RecordedVideo();
		
		recordedVideo.setVideoName(in.readString());
		
		Calendar recordingTime = Calendar.getInstance();
		recordingTime.setTimeInMillis(in.readLong());
		recordedVideo.setRecordingTime(recordingTime.getTime());
		
		VideoRecorded.RecordingReason recordingReason = VideoRecorded.RecordingReason.IOT_EVENT;
		int reasonOrdinal = in.readInt();
		if (VideoRecorded.RecordingReason.USER_ACTION.ordinal() == reasonOrdinal)
			recordingReason = VideoRecorded.RecordingReason.USER_ACTION;
		recordedVideo.setRecordingReason(recordingReason);
		
		recordedVideo.setVideoUrl(in.readString());
	}
}
