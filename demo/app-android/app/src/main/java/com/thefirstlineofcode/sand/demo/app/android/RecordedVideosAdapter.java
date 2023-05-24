package com.thefirstlineofcode.sand.demo.app.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class RecordedVideosAdapter extends ArrayAdapter<RecordedVideo> {
	private Context context;
	private RecordedVideo[] recordedVideos;
	
	public RecordedVideosAdapter(Context context, RecordedVideo[] recordedVdeos) {
		super(context, -1, recordedVdeos);
		this.context = context;
		this.recordedVideos = recordedVdeos;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.recorded_video_view, parent, false);
		
		RecordedVideo recordedVideo = recordedVideos[position];
		
		TextView tvVideoName = (TextView) rowView.findViewById(R.id.tv_video_name);
		tvVideoName.setText(recordedVideo.getVideoName());
		
		TextView tvRecordingReson = (TextView) rowView.findViewById(R.id.tv_recording_reason);
		tvRecordingReson.setText("Recorded by " + (recordedVideo.getRecordingReason() == VideoRecorded.RecordingReason.IOT_EVENT ?
				"IoT Event" : "User Action"));
		
		TextView tvRecordingTime = (TextView)rowView.findViewById(R.id.tv_recording_time);
		tvRecordingTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recordedVideo.getRecordingTime()));
		
		return rowView;
	}
}
