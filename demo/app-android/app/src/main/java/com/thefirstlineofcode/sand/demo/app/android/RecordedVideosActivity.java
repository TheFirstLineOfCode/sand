package com.thefirstlineofcode.sand.demo.app.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.thefirstlineofcode.chalk.core.ErrorException;
import com.thefirstlineofcode.sand.demo.client.IRecordedVideosService;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakeVideo;

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
		RecordedVideo[] recordedVideos = new RecordedVideo[parcelables.length];
		for (int i = 0; i < parcelables.length; i++) {
			recordedVideos[i] = ((RecordedVideoParcelable) parcelables[i]).getRecordedVideo();
		}
		
		ListView lvRecordedVideos = findViewById(R.id.recorded_videos_view);
		lvRecordedVideos.setAdapter(new RecordedVideosAdapter(this, recordedVideos));
		lvRecordedVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AlertDialog.Builder videoDialogBuilder = new AlertDialog.Builder(RecordedVideosActivity.this);
				View videoView = LayoutInflater.from(RecordedVideosActivity.this).inflate(R.layout.video_view, null, false);
				VideoView vvVideo = videoView.findViewById(R.id.vv_video);
				
				videoDialogBuilder.setView(videoView);
				videoDialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				runOnUiThread(() -> {
					Uri videoUri = Uri.parse(recordedVideos[position].getVideoUrl());
					vvVideo.setVideoURI(videoUri);
					
					vvVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							vvVideo.start();
						}
					});
					
					AlertDialog videoDialog = videoDialogBuilder.create();
					videoDialog.show();
				});
			}
		});
	}
}
