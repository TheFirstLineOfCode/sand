package com.thefirstlineofcode.sand.demo.app.android;

import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RecordedVideosActivity extends AppCompatActivity {
	private static final Logger logger = LoggerFactory.getLogger(RecordedVideosActivity.class);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recorded_videos);
		
		Parcelable[] parcelables = getIntent().getParcelableArrayExtra("recorded-videos");
		List<RecordedVideo> recordedVideos = new ArrayList<RecordedVideo>();
		for (int i = 0; i < parcelables.length; i++) {
			recordedVideos.add(i, ((RecordedVideoParcelable)parcelables[i]).getRecordedVideo());
		}
		
		ListView lvRecordedVideos = findViewById(R.id.recorded_videos_view);
		lvRecordedVideos.setAdapter(new RecordedVideosAdapter(this, recordedVideos));
	}
}
