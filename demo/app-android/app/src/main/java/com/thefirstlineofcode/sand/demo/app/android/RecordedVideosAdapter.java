package com.thefirstlineofcode.sand.demo.app.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.thefirstlineofcode.chalk.core.ErrorException;
import com.thefirstlineofcode.sand.demo.client.IRecordedVideosService;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;

import java.text.SimpleDateFormat;
import java.util.List;

public class RecordedVideosAdapter extends ArrayAdapter<RecordedVideo> {
	private Activity activity;
	private List<RecordedVideo> recordedVideos;
	
	public RecordedVideosAdapter(Activity activity, List<RecordedVideo> recordedVideos) {
		super(activity, -1, recordedVideos);
		this.activity = activity;
		this.recordedVideos = recordedVideos;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.recorded_video_view, parent, false);
		
		RecordedVideo recordedVideo = recordedVideos.get(position);
		
		TextView tvVideoName = (TextView) rowView.findViewById(R.id.tv_video_name);
		String videoDisplayName = recordedVideo.getVideoName();
		int dotIndex = videoDisplayName.indexOf(".");
		if (dotIndex != -1) {
			videoDisplayName = videoDisplayName.substring(0, dotIndex);
		}
		tvVideoName.setText(videoDisplayName);
		
		Button btOpen = (Button)rowView.findViewById(R.id.bt_open);
		btOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder videoDialogBuilder = new AlertDialog.Builder(activity);
				View videoView = LayoutInflater.from(activity).inflate(R.layout.video_view, null, false);
				VideoView vvVideo = videoView.findViewById(R.id.vv_video);
				
				videoDialogBuilder.setView(videoView);
				videoDialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				activity.runOnUiThread(() -> {
					Uri videoUri = Uri.parse(recordedVideo.getVideoUrl());
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
		
		Button btRemove = (Button)rowView.findViewById(R.id.bt_remove);
		btRemove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.runOnUiThread(() -> {
					IRecordedVideosService recordedVideosService = ChatClientSingleton.get(activity).createApi(
							IRecordedVideosService.class);
					try {
						recordedVideosService.removeRecordedVideo(recordedVideo.getVideoName());
					} catch (ErrorException e) {
						activity.runOnUiThread(() -> {
							Toast.makeText(activity, activity.getString(R.string.stanza_error_occurred, e.getError().getDefinedCondition()), Toast.LENGTH_LONG).show();
						});
						
						return;
					}
					
					if (videoRemoved(recordedVideo)) {
						activity.runOnUiThread(() -> {
							Toast.makeText(activity, String.format("Video '%s' has removed.", recordedVideo.getVideoName()), Toast.LENGTH_SHORT).show();
						});
						
						notifyDataSetChanged();
					}
				});
			}
		});
		
		TextView tvRecordingReason = (TextView) rowView.findViewById(R.id.tv_recording_reason);
		tvRecordingReason.setText("Recorded by " + (recordedVideo.getRecordingReason() == VideoRecorded.RecordingReason.IOT_EVENT ?
				"IoT Event" : "User Action"));
		
		TextView tvRecordingTime = (TextView)rowView.findViewById(R.id.tv_recording_time);
		tvRecordingTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recordedVideo.getRecordingTime()));
		
		return rowView;
	}
	
	private boolean videoRemoved(RecordedVideo recordedVideo) {
		int position = -1;
		for (int i = 0; i < recordedVideos.size(); i++) {
			if (recordedVideo.getVideoName().equals(recordedVideos.get(i).getVideoName())) {
					position = i;
					break;
			}
		}
		
		if (position != -1)
			recordedVideos.remove(position);
		
		return position != -1;
	}
}
