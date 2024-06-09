package com.thefirstlineofcode.sand.demo.app.android;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.thefirstlinelinecode.sand.protocols.concentrator.RemoveNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.ResetNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.SyncNodes;
import com.thefirstlinelinecode.sand.protocols.concentrator.friends.PullLanFollows;
import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Stanza;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stream.error.StreamError;
import com.thefirstlineofcode.basalt.xmpp.im.stanza.Presence;
import com.thefirstlineofcode.chalk.core.ErrorException;
import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.IErrorListener;
import com.thefirstlineofcode.chalk.core.ITask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.chalk.im.stanza.IPresenceListener;
import com.thefirstlineofcode.sand.client.operator.IOperator;
import com.thefirstlineofcode.sand.client.remoting.IRemoting;
import com.thefirstlineofcode.sand.client.sensor.IDataProcessor;
import com.thefirstlineofcode.sand.client.sensor.IReportService;
import com.thefirstlineofcode.sand.client.thing.IEventProcessor;
import com.thefirstlineofcode.sand.client.thing.INotificationService;
import com.thefirstlineofcode.sand.demo.client.IAuthorizedEdgeThingsService;
import com.thefirstlineofcode.sand.demo.client.INetConfigService;
import com.thefirstlineofcode.sand.demo.client.IRecordedVideosService;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedEdgeThing;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedEdgeThings;
import com.thefirstlineofcode.sand.demo.protocols.DeliverTemperatureToOwner;
import com.thefirstlineofcode.sand.demo.protocols.RecordedVideo;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;
import com.thefirstlineofcode.sand.protocols.edge.ShutdownSystem;
import com.thefirstlineofcode.sand.protocols.edge.Stop;
import com.thefirstlineofcode.sand.protocols.lora.dac.ResetLoraDacService;
import com.thefirstlineofcode.sand.protocols.lora.gateway.ChangeWorkingMode;
import com.thefirstlineofcode.sand.protocols.lora.gateway.WorkingMode;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakePhoto;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakeVideo;
import com.thefirstlineofcode.sand.protocols.things.simple.light.Flash;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchStateChanged;
import com.thefirstlineofcode.sand.protocols.things.simple.light.TurnOff;
import com.thefirstlineofcode.sand.protocols.things.simple.light.TurnOn;
import com.thefirstlineofcode.sand.protocols.things.simple.temperature.reporter.CelsiusDegree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements IOperator.Listener,
		IAuthorizedEdgeThingsService.Listener, IErrorListener,
			INetConfigService.NetConfigEventsListener {
	private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
	
	private String host;
	private ThingsAdapter thingsAdapter;
	private File downloadDir;
	
	private IReportService reportService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Toolbar tbToolbar = findViewById(R.id.tb_tool_bar);
		tbToolbar.setTitle(R.string.app_name);
		setSupportActionBar(tbToolbar);
		
		downloadDir = new File(getCacheDir(), "download-dir");
		if (!downloadDir.exists()) {
			try {
				Files.createDirectory(downloadDir.toPath());
			} catch (IOException e) {
				throw new RuntimeException("Can't create download directory.", e);
			}
		}
		
		IChatClient chatClient = ChatClientSingleton.get(this);
		// Initial presence.
		chatClient.getChatServices().getPresenceService().send(new Presence());
		
		reportService = chatClient.createApi(IReportService.class);
		
		listenNetConfigEvents();
		listenVideoRecordedEvent();
		retrieveAuthorizedThings();
	}
	
	private void listenVideoRecordedEvent() {
		IChatClient chatClient = ChatClientSingleton.get(this);
		INotificationService notificationService = chatClient.createApi(INotificationService.class);
		notificationService.listen(VideoRecorded.class, new IEventProcessor<VideoRecorded>() {
			@Override
			public void processEvent(JabberId notifier, VideoRecorded videoRecorded) {
				String videoUrl = videoRecorded.getVideoUrl();
				Spanned spanned = Html.fromHtml("A video recorded because an IoT event. Video address is <a href=\"" +
						videoUrl + "\">" + videoUrl + "</a>", FROM_HTML_MODE_COMPACT);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Video Recorded").setMessage(spanned).
						setPositiveButton(string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								
								AlertDialog.Builder videoDialogBuilder = new AlertDialog.Builder(MainActivity.this);
								View videoView = LayoutInflater.from(MainActivity.this).inflate(R.layout.video_view, null, false);
								VideoView vvVideo = videoView.findViewById(R.id.vv_video);
								
								videoDialogBuilder.setView(videoView);
								videoDialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								
								runOnUiThread(() -> {
									Uri videoUri = Uri.parse(videoRecorded.getVideoUrl());
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
						}).
						setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}
						);
				
				runOnUiThread(() -> {
					AlertDialog dialog = builder.create();
					dialog.show();
				});
			}
		});
	}
	
	private void listenNetConfigEvents() {
		IChatClient chatClient = ChatClientSingleton.get(this);
		INetConfigService netConfigService =
				chatClient.createApi(INetConfigService.class);
		netConfigService.startToListenNetConfigEvents(this);
	}
	
	private void retrieveAuthorizedThings() {
		runOnUiThread(() ->
				Toast.makeText(this,
						getString(R.string.retrieving_your_things),
						Toast.LENGTH_SHORT).show());
		
		ProgressBar pbRetrievingThings = findViewById(R.id.pb_retrieving_things);
		pbRetrievingThings.setVisibility(View.VISIBLE);
		
		IChatClient chatClient = ChatClientSingleton.get(this);
		host = chatClient.getStreamConfig().getHost();
		IAuthorizedEdgeThingsService authorizedEdgeThingsService =
				chatClient.createApi(IAuthorizedEdgeThingsService.class);
		authorizedEdgeThingsService.addListener(this);
		
		authorizedEdgeThingsService.retrieve();
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = new Intent(this, AppExitMonitor.class);
		startService(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.toolbar, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.item_refresh_things) {
			refreshThings();
			return true;
		} else if (item.getItemId() == R.id.item_authorize_thing) {
			authorizeThing();
			return true;
		} else if (item.getItemId() == R.id.item_logout) {
			logout();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void refreshThings() {
		retrieveAuthorizedThings();
	}

	private void authorizeThing() {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
		integrator.setOrientationLocked(false);

		integrator.initiateScan();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result != null && result.getContents() != null) {
			authorizeThing(result.getContents());
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void authorizeThing(String ThingId) {
		IChatClient chatClient = ChatClientSingleton.get(this);
		IOperator operator = chatClient.createApi(IOperator.class);
		if (!operator.getListeners().contains(this))
			operator.addListener(this);

		operator.authorize(ThingId);
	}
	
	private void confirmConcentration(String concentratorThingName, String nodeThingId) {
		IChatClient chatClient = ChatClientSingleton.get(this);
		IOperator operator = chatClient.createApi(IOperator.class);
		if (!operator.getListeners().contains(this))
			operator.addListener(this);
		
		operator.confirm(concentratorThingName, nodeThingId);
	}

	private void logout() {
		finish();
		
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra(getString(R.string.auto_login), false);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		ChatClientSingleton.destroy();
		super.onDestroy();
	}

	@Override
	public void authorized(String ThingId) {
		runOnUiThread(() ->
				Toast.makeText(MainActivity.this,
						getString(R.string.thing_has_authorized, ThingId),
						Toast.LENGTH_LONG).show());
	}

	@Override
	public void confirmed(String concentratorId, String nodeId, int lanId) {
		// NOOP
	}
	
	@Override
	public void approved(JabberId friend, Protocol event, JabberId follower) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Thing Followed").setMessage(
						String.format("The thing which's JID is '%s' has been followed by thing which's JID is '%s'.",
								friend.toString(), follower.toString())).
				setPositiveButton(string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	@Override
	public void canceled(String ThingId) {
		// NOOP
	}

	@Override
	public void canceled(String concentratorId, String nodeId) {
		// NOOP
	}

	@Override
	public void occurred(IOperator.AuthorizationError error, String ThingId) {
		runOnUiThread(() ->
				Toast.makeText(MainActivity.this,
						getString(R.string.failed_to_authorize_thing, error.getReason()),
						Toast.LENGTH_LONG).show());

	}

	@Override
	public void occurred(IOperator.ConfirmationError error, String concentratorId, String nodeId) {
		runOnUiThread(() ->
				Toast.makeText(MainActivity.this,
						getString(R.string.failed_to_confirm_node_addition, error.getReason()),
						Toast.LENGTH_LONG).show());
	}
	
	@Override
	public void occurred(IOperator.ApprovalError error, JabberId friend, Protocol event, JabberId follower) {
		runOnUiThread(() ->
				Toast.makeText(MainActivity.this,
						getString(R.string.failed_to_approve_follow, error.getReason()),
						Toast.LENGTH_LONG).show());
	}
	
	@Override
	public void retrieved(AuthorizedEdgeThings authorizedEdgeThings) {
		runOnUiThread(() -> {
			ProgressBar pbRetrievingThings = findViewById(R.id.pb_retrieving_things);
			pbRetrievingThings.setVisibility(View.INVISIBLE);
			
			List<AuthorizedEdgeThing> lThings = authorizedEdgeThings.getThings();
			
			AuthorizedEdgeThing[] things = new AuthorizedEdgeThing[0];
			if (lThings != null && lThings.size() != 0)
				things = lThings.toArray(new AuthorizedEdgeThing[0]);
			
			ExpandableListView elvThings = findViewById(R.id.elv_things);
			if (thingsAdapter == null) {
				thingsAdapter = new ThingsAdapter(MainActivity.this, host, things);
				elvThings.setAdapter(thingsAdapter);
				
				if (things == null || things.length == 0) {
					runOnUiThread(() ->
							Toast.makeText(MainActivity.this,
									getString(R.string.no_authorized_things_received),
									Toast.LENGTH_LONG).show()
					);
					
					return;
				}
				
				expandNodes(elvThings, things);
			} else {
				thingsAdapter.setThings(things);
				thingsAdapter.notifyDataSetChanged();
				
				expandNodes(elvThings, things);
			}
			
			IChatClient chatClient = ChatClientSingleton.get(this);
			chatClient.getChatServices().getPresenceService().addListener(new IPresenceListener() {
				
				@Override
				public void received(Presence presence) {
					AuthorizedEdgeThing[] things = thingsAdapter.getThings();
					if (things == null || things.length == 0) {
						return;
					}
					
					for (int i = 0; i < things.length; i++) {
						if (things[i].getThingName().equals(presence.getFrom().getNode())) {
							final String thingId = things[i].getThingId();
							runOnUiThread(() -> Toast.makeText(MainActivity.this,
									String.format("Thing %s is avaiable now.", thingId),
									Toast.LENGTH_LONG).show());
						}
					}
				}
			});
		});
	}
	
	private void expandNodes(ExpandableListView elvThings, AuthorizedEdgeThing[] things) {
		for (int i = 0; i < things.length; i++) {
			AuthorizedEdgeThing thing = things[i];
			if (thing.isConcentrator() && thing.getNodes() != null &&
					thing.getNodes().size() > 0)
				elvThings.expandGroup(i);
		}
	}
	
	@Override
	public void occurred(StanzaError error) {
		ProgressBar pbRetrievingThings = (ProgressBar)findViewById(R.id.pb_retrieving_things);
		pbRetrievingThings.setVisibility(View.INVISIBLE);
		
		runOnUiThread(() ->
				Toast.makeText(MainActivity.this,
						getString(R.string.stanza_error_occurred, error.getDefinedCondition()),
						Toast.LENGTH_LONG).show()
		);
	}
	
	@Override
	public void timeout() {
		ProgressBar pbRetrievingThings = findViewById(R.id.pb_retrieving_things);
		pbRetrievingThings.setVisibility(View.INVISIBLE);
		
		runOnUiThread(() -> Toast.makeText(MainActivity.this,
				getString(R.string.retrieve_authorized_things_timeout),
				Toast.LENGTH_LONG).show());
	}
	
	public void takeAPhoto(JabberId target) {
		logger.info("Take a photo from camera {}.", target);
		
		IChatClient chatClient = ChatClientSingleton.get(MainActivity.this);
		IRemoting remoting = chatClient.createApi(IRemoting.class);
		
		remoting.execute(target, new TakePhoto(), 30 * 1000, new IRemoting.Callback() {
			@Override
			public void executed(Object xep) {
				String photoUrl = ((TakePhoto)xep).getPhotoUrl();
				Spanned spanned = Html.fromHtml("Your photo was taken. Download address is <a href=\"" +
						photoUrl + "\">" + photoUrl + "</a>", FROM_HTML_MODE_COMPACT);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Your Photo").setMessage(spanned).
					setPositiveButton(string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							
							AlertDialog.Builder photoDialogBuilder = new AlertDialog.Builder(MainActivity.this);
							View photoView = LayoutInflater.from(MainActivity.this).inflate(R.layout.photo_view, null, false);
							ImageView ivPhoto = photoView.findViewById(R.id.iv_photo);
							ProgressBar pbDownloadingPhoto = photoView.findViewById(R.id.pb_downloading_photo);
							photoDialogBuilder.setView(photoView);
							photoDialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
							
							runOnUiThread(() -> {
								AlertDialog photoDialog = photoDialogBuilder.create();
								photoDialog.show();
							});
							
							DownloadingMediaTask downloadingMediaTask = new DownloadingMediaTask(
									MainActivity.this, pbDownloadingPhoto, ivPhoto, downloadDir);
							TakePhoto takePhoto = (TakePhoto)xep;
							downloadingMediaTask.execute(takePhoto.getPhotoUrl(), takePhoto.getPhotoFileName());
						}
					}).
					setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}
				);
				
				runOnUiThread(() -> {
					AlertDialog dialog = builder.create();
					dialog.show();
				});
			}
			
			@Override
			public void occurred(StanzaError error) {
				runOnUiThread(() -> Toast.makeText(MainActivity.this,
						"Take photo execution error: " + error.toString(),
						Toast.LENGTH_LONG).show());
			}
			
			@Override
			public void timeout() {
				runOnUiThread(() -> Toast.makeText(MainActivity.this,
						"Take photo execution timeout.",
						Toast.LENGTH_LONG).show());
			}
		});
	}
	
	@Override
	public void tryToRegisterWithoutAuthoration(String thingId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Not Authorized Registration").setMessage(
				String.format("A thing which's thing ID is '%s' tried to register without authorization. Do you want to authorize it?", thingId)).
				setPositiveButton(R.string.yes_authorize_it, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						authorizeThing(thingId);
					}
				}).
				setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}
				);
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	@Override
	public void edgeThingRegistered(String thingId, String thingName, String authorizer, Date registrationTime) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Thing Registered").setMessage(
						String.format("A edge thing which's thing ID is '%s' has registered. Do you want to refresh things?", thingId)).
				setPositiveButton(R.string.yes_refresh_things, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						refreshThings();
					}
				}).
				setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}
				);
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	@Override
	public void requestToConfirm(String concentratorThingName, String nodeThingId, Date requestedTime) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Request to Confirm").setMessage(
						String.format("Concentrator which's thing name is '%s' requested to add thing which's ID is '%s' as it's node.",
								concentratorThingName, nodeThingId)).
				setPositiveButton(R.string.yes_confirm_it, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						confirmConcentration(concentratorThingName, nodeThingId);
					}
				}).
				setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}
				);
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	@Override
	public void nodeAdded(String concentratorThingName, String nodeThingId, int lanId, Date addedTime) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Node Added").setMessage(
						String.format("A thing which's thing ID is '%s' has been added to concentrator which's thing name is '%s'. Do you want to refresh things?",
								nodeThingId, concentratorThingName)).
				setPositiveButton(R.string.yes_refresh_things, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						refreshThings();
					}
				}).
				setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}
				);
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	private static class DownloadingMediaTask extends AsyncTask<String, Void, File> {
		private final Context context;
		private ProgressBar pbDownloadingMedia;
		private View vMedia;
		private File downloadDir;
		
		public DownloadingMediaTask(Context context, ProgressBar pbDownloadingMedia,
									View vMedia, File downloadDir) {
			this.context = context;
			this.pbDownloadingMedia = pbDownloadingMedia;
			this.vMedia = vMedia;
			this.downloadDir = downloadDir;
		}
		
		@Override
		protected void onPreExecute() {
			pbDownloadingMedia.setVisibility(View.VISIBLE);
			vMedia.setVisibility(View.INVISIBLE);
		}
		
		@Override
		protected File doInBackground(String... urlAndFileName) {
			OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES).build();
			Call call = client.newCall(getMediaDownloadingRequest(urlAndFileName[0]));
			Response response;
			try {
				response = call.execute();
			} catch (IOException e) {
				return null;
			}
			
			if (response.code() != 200) {
				return null;
			}
			
			return downloadMedia(response, urlAndFileName[1]);
		}
		
		@Override
		protected void onPostExecute(File downloadMedia) {
			if (downloadMedia == null) {
				Toast.makeText(context,
						context.getString(R.string.failed_to_download_media),
						Toast.LENGTH_LONG).show();
			} else {
				pbDownloadingMedia.setVisibility(View.GONE);
				if (vMedia instanceof ImageView) {
					ImageView ivPhoto = (ImageView)vMedia;
					ivPhoto.setImageURI(Uri.fromFile(downloadMedia));
					ivPhoto.setVisibility(View.VISIBLE);
				} else {
					VideoView vvVideo = (VideoView)vMedia;
					vvVideo.setVideoURI(Uri.fromFile(downloadMedia));
					vvVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							vvVideo.start();
						}
					});
					vvVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
						@Override
						public boolean onError(MediaPlayer mp, int what, int extra) {
							return false;
						}
					});
					vvVideo.setVisibility(View.VISIBLE);
				}

			}
		}
		
		private Request getMediaDownloadingRequest(String photoUrl) {
			return new Request.Builder().url(photoUrl).build();
		}
		
		private File downloadMedia(Response response, String mediaFileName) {
			File downloadedMedia = new File(downloadDir, mediaFileName);
			InputStream input = null;
			BufferedOutputStream output = null;
			byte[] buf = new byte[2048];
			int len;
			try {
				input = response.body().byteStream();
				output = new BufferedOutputStream(new FileOutputStream(downloadedMedia));
				while ((len = input.read(buf, 0, 2048)) != -1) {
					output.write(buf, 0, len);
				}
				
				return downloadedMedia;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return null;
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void takeAVideo(JabberId target) {
		logger.info("Take a video from camera {}.", target);
		
		IChatClient chatClient = ChatClientSingleton.get(MainActivity.this);
		IRemoting remoting = chatClient.createApi(IRemoting.class);
		
		remoting.execute(target, new TakeVideo(5000), 60 * 1000, new IRemoting.Callback() {
			@Override
			public void executed(Object xep) {
				String videoUrl = ((TakeVideo)xep).getVideoUrl();
				Spanned spanned = Html.fromHtml("Your video was taken. Video address is <a href=\"" +
						videoUrl + "\">" + videoUrl + "</a>", FROM_HTML_MODE_COMPACT);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("Your Video").setMessage(spanned).
						setPositiveButton(string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								
								AlertDialog.Builder videoDialogBuilder = new AlertDialog.Builder(MainActivity.this);
								View videoView = LayoutInflater.from(MainActivity.this).inflate(R.layout.video_view, null, false);
								VideoView vvVideo = videoView.findViewById(R.id.vv_video);
								
								videoDialogBuilder.setView(videoView);
								videoDialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								
								runOnUiThread(() -> {
									TakeVideo takeVideo = (TakeVideo)xep;
									Uri videoUri = Uri.parse(takeVideo.getVideoUrl());
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
						}).
						setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}
						);
				
				runOnUiThread(() -> {
					AlertDialog dialog = builder.create();
					dialog.show();
				});
			}
			
			@Override
			public void occurred(StanzaError error) {
				remotingErrorOccurred(MainActivity.this, error, "Take video execution error: " + error.toString());
			}
			
			@Override
			public void timeout() {
				runOnUiThread(() -> Toast.makeText(MainActivity.this,
						"Take video execution timeout.",
						Toast.LENGTH_LONG).show());
			}
		});
	}
	
	public void showRecordedVideos(String recorderThingId) {
		logger.info("Show recorded videos of camera which's thing ID is {}.", recorderThingId);
		
		IRecordedVideosService recordedVideosService = ChatClientSingleton.get(this).createApi(
				IRecordedVideosService.class);
		List<RecordedVideo> recordedVideos = null;
		try {
			recordedVideos = recordedVideosService.getRecordedVideos(recorderThingId);
		} catch (ErrorException e) {
			runOnUiThread(() -> {
				Toast.makeText(MainActivity.this, getString(R.string.stanza_error_occurred, e.getError().getDefinedCondition()), Toast.LENGTH_LONG).show();
			});
			
			return;
		}
		
		if (recordedVideos == null || recordedVideos.isEmpty()) {
			runOnUiThread(() -> {
				Toast.makeText(MainActivity.this, "No recorded videos found.", Toast.LENGTH_LONG).show();
			});
			
			return;
		}
		
		Intent intent = new Intent(this, RecordedVideosActivity.class);
		RecordedVideoParcelable[] parcelables = new RecordedVideoParcelable[recordedVideos.size()];
		for (int i = 0; i < recordedVideos.size(); i++) {
			parcelables[i] = new RecordedVideoParcelable(recordedVideos.get(i));
		}
		
		intent.putExtra("recorded-videos", parcelables);
		startActivity(intent);
	}
	
	public void openLiveSteaming(JabberId target) {
		logger.info("Open live streaming of camera {}.", target);
		
		Intent intent = new Intent(this, LiveStreamingActivity.class);
		intent.putExtra("camera-jid", target.toString());
		startActivity(intent);
	}
	
	public void stop(JabberId target) {
		controlThing(target, new Stop(), "Stop");
	}
	
	public void shutdownSystem(JabberId target) {
		ShutdownSystem shutdownSystem = new ShutdownSystem();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Shutdown System").setMultiChoiceItems(new String[] {"Restart after shutdown"}, new boolean[] {false},
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						shutdownSystem.setRestart(isChecked);
					}
				}
			).setPositiveButton(string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						controlThing(target, shutdownSystem, "Shutdown system");
					}
				}
			).setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			}
		);
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	public void flash(JabberId target) {
		logger.info("Flash light {}.", target);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose repeat times").setItems(new String[] {"1", "2", "5"},
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						Flash flash = new Flash();
						if (which == 0) {
							flash.setRepeat(1);
						} else if (which == 1) {
							flash.setRepeat(2);
						} else {
							flash.setRepeat(5);
						}

						controlThing(target, flash, "Flash");
					}
				});
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	public void turnOn(JabberId target) {
		logger.info("Turn on light {}.", target);
		controlThing(target, new TurnOn(), "Turn on");
	}
	
	public void turnOff(JabberId target) {
		logger.info("Turn off light {}.", target);
		controlThing(target, new TurnOff(), "Turn off");
	}
	
	public void resetThing(JabberId target) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Reset Thing").setMessage("Are you sure you want to reset the thing?").
				setPositiveButton(R.string.yes_reset_it, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						logger.info("Reset thing {}.", target);
						JabberId jidConcentrator = new JabberId(target.getNode(),
								target.getDomain(), RegisteredEdgeThing.DEFAULT_RESOURCE_NAME);
						controlThing(jidConcentrator, new ResetNode(target.getResource()), "Reset thing");
					}
				}).
				setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}
				);
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	public void removeNode(JabberId target) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Remove Node").setMessage("Are you sure you want to remove the node?").
				setPositiveButton(R.string.yes_remove_it, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						logger.info("Remove node {}.", target);
						
						IChatClient chatClient = ChatClientSingleton.get(MainActivity.this);
						IChatServices chatServices = chatClient.getChatServices();
						chatServices.getTaskService().execute(new ITask<Iq>() {
							@Override
							public void trigger(IUnidirectionalStream<Iq> stream) {
								JabberId jidConcentrator = new JabberId(target.getNode(),
										target.getDomain(), RegisteredEdgeThing.DEFAULT_RESOURCE_NAME);
								
								Iq removeNode = new Iq(Iq.Type.SET, new RemoveNode(Integer.parseInt(target.getResource())));
								removeNode.setTo(jidConcentrator);
								
								stream.send(removeNode);
							}
							
							@Override
							public void processResponse(IUnidirectionalStream<Iq> stream, Iq stanza) {
								AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
								builder.setTitle("Node Removed").setMessage(
												String.format("The node which's LAN ID is '%s' has been removed from concentrator which's thing name is '%s'. Do you want to refresh things?",
														target.getResource(), target.getNode())).
										setPositiveButton(R.string.yes_refresh_things, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
												refreshThings();
											}
										}).
										setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
													@Override
													public void onClick(DialogInterface dialog, int which) {
														dialog.dismiss();
													}
												}
										);
								
								runOnUiThread(() -> {
									AlertDialog dialog = builder.create();
									dialog.show();
								});
							}
							
							@Override
							public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
								runOnUiThread(() -> Toast.makeText(MainActivity.this,
										"Remove node error: " +  error.toString(),
										Toast.LENGTH_LONG).show());
								
								return true;
							}
							
							@Override
							public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
								runOnUiThread(() -> Toast.makeText(MainActivity.this,
										"Remove node timeout.",
										Toast.LENGTH_LONG).show());
								
								return true;
							}
							
							@Override
							public void interrupted() {}
						});
					}
				}).
				setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}
				);
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
		
	}
	
	public void followThing(JabberId friend) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Follow Thing").setMessage("Are you sure you want to follow the thing?").
				setPositiveButton(R.string.yes_follow_it, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						logger.info("Follow thing {}.", friend);
						
						IChatClient chatClient = ChatClientSingleton.get(MainActivity.this);
						IChatServices chatServices = chatClient.getChatServices();
						IOperator operator = chatServices.createApi(IOperator.class);
						if (!operator.getListeners().contains(MainActivity.this))
							operator.addListener(MainActivity.this);
						
						JabberId follower = new JabberId(friend.getNode(),
								friend.getDomain(), RegisteredEdgeThing.DEFAULT_RESOURCE_NAME);
						operator.approve(friend, SwitchStateChanged.PROTOCOL, follower);
					}
				}).setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}
				);
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	public void watchTemperature(JabberId reporter) {
		logger.info("Watch temperature {}.", reporter);
		IChatClient chatClient = ChatClientSingleton.get(this);
		chatClient.getChatServices().getTaskService().execute(new ITask<Stanza>() {
			@Override
			public void trigger(IUnidirectionalStream<Stanza> stream) {
				Iq enableDeliverTemperatureToOwner = new Iq(Iq.Type.SET, new DeliverTemperatureToOwner(true));
				stream.send(enableDeliverTemperatureToOwner);
			}
			
			@Override
			public void processResponse(IUnidirectionalStream<Stanza> stream, Stanza stanza) {
				if (!reportService.isStarted())
					reportService.start();
				
				AlertDialog.Builder temperatureDialogBuilder = new AlertDialog.Builder(MainActivity.this);
				View temperatureView = LayoutInflater.from(MainActivity.this).inflate(R.layout.temperature_view, null, false);
				TextView tvTemperature = temperatureView.findViewById(R.id.tv_temperature);
				temperatureDialogBuilder.setView(temperatureView);
				temperatureDialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						IChatClient chatClient = ChatClientSingleton.get(MainActivity.this);
						chatClient.getChatServices().getTaskService().execute(new ITask<Iq>() {
							@Override
							public void trigger(IUnidirectionalStream<Iq> stream) {
								Iq disableDeliverTemperatureToOwner = new Iq(Iq.Type.SET, new DeliverTemperatureToOwner(false));
								stream.send(disableDeliverTemperatureToOwner);
							}
							
							@Override
							public void processResponse(IUnidirectionalStream<Iq> stream, Iq stanza) {
								reportService.stopListening(CelsiusDegree.class);
								reportService.stop();
								
								dialog.dismiss();
							}
							
							@Override
							public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
								return false;
							}
							
							@Override
							public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
								return false;
							}
							
							@Override
							public void interrupted() {}
						});
					}
				});
				
				runOnUiThread(() -> {
					AlertDialog temperatureDialog = temperatureDialogBuilder.create();
					temperatureDialog.show();
				});
				
				reportService.listen(CelsiusDegree.class, new TemperatureProcessor(tvTemperature));
			}
			
			@Override
			public boolean processError(IUnidirectionalStream<Stanza> stream, StanzaError error) {
				return false;
			}
			
			@Override
			public boolean processTimeout(IUnidirectionalStream<Stanza> stream, Stanza stanza) {
				return false;
			}
			
			@Override
			public void interrupted() {}
		});
	}
	
	private class TemperatureProcessor implements IDataProcessor<CelsiusDegree> {
		private TextView tvTemperature;
		
		public TemperatureProcessor(TextView tvTemperature) {
			this.tvTemperature = tvTemperature;
		}
		
		@Override
		public void processData(JabberId reporter, CelsiusDegree celsiusDegree) {
			runOnUiThread(() -> {
				tvTemperature.setText(String.format("%4.2f ℃", celsiusDegree.getValue()));
			});
		}
	}
	
	private void controlThing(JabberId target, Object action, String actionDescription) {
		IChatClient chatClient = ChatClientSingleton.get(this);
		IRemoting remoting = chatClient.createApi(IRemoting.class);
		remoting.execute(target, action, new RemotingCallback(this, actionDescription));
	}

	private static class RemotingCallback implements IRemoting.Callback {
		private final Activity activity;
		private final String actionDescription;

		public RemotingCallback(Activity activity, String actionDescription) {
			this.activity = activity;
			this.actionDescription = actionDescription;
		}

		@Override
		public void executed(Object xep) {
			activity.runOnUiThread(() -> Toast.makeText(activity,
					actionDescription + " executed.",
					Toast.LENGTH_LONG).show());
		}

		@Override
		public void occurred(StanzaError error) {
			String errorText = actionDescription + " execution error: " +
					(error.getText() == null ? error.toString() : error.getText().getText());
			remotingErrorOccurred(activity, error, errorText);
		}

		@Override
		public void timeout() {
			activity.runOnUiThread(() -> Toast.makeText(activity,
					actionDescription + " execution timeout.",
					Toast.LENGTH_LONG).show());
		}
	}
	
	private static void remotingErrorOccurred(Activity activity, StanzaError error, String errorText) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Remoting Error").setMessage(errorText).
				setPositiveButton(string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		
		activity.runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	public void changeWorkingMode(JabberId target) {
		logger.info("Change working mode of gateway {}.", target);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose working mode").setItems(new String[] {"ROUTER", "DAC"},
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						WorkingMode workingMode = WorkingMode.ROUTER;
						if (which == 1) {
							workingMode = WorkingMode.DAC;
						}
						
						controlThing(target, new ChangeWorkingMode(workingMode), "Change working mode");
					}
				});
		
		runOnUiThread(() -> {
			AlertDialog dialog = builder.create();
			dialog.show();
		});
	}
	
	public void syncNodes(JabberId target) {
		logger.info("Sync nodes of gateway {}.", target);
		controlThing(target, new SyncNodes(), "Sync nodes");
	}
	
	public void pullLanFollows(JabberId target) {
		logger.info("Pull LAN follows of gateway {}.", target);
		controlThing(target, new PullLanFollows(), "Pull LAN follows");
	}
	
	public void resetDacService(JabberId target) {
		logger.info("Reset DAC service of gateway {}.", target);
		controlThing(target, new ResetLoraDacService(), "Reset DAC service");
	}
	
	public void queryWatchState(JabberId target) {
		logger.info("Query state of watch which's JID is {}.", target);
	}
	
	public void monitorHeartRate(JabberId target) {
		logger.info("Monitor heart rate of watch which's JID is {}.", target);
	}
	
	public void sendMessage(JabberId target) {
		logger.info("Send message to {}.", target);
	}
	
	@Override
	public void occurred(IError error) {
		runOnUiThread(() -> {
			if (error instanceof StreamError) {
				Toast.makeText(MainActivity.this, getString(R.string.stream_error_occurred, error.getDefinedCondition()), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(MainActivity.this, getString(R.string.stanza_error_occurred, error.getDefinedCondition()), Toast.LENGTH_LONG).show();
			}
		});
	}
}
