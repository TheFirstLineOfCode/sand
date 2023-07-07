package com.thefirstlineofcode.sand.demo.things.lgsc01;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlinelinecode.sand.protocols.concentrator.ResetNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.SyncNodes;
import com.thefirstlinelinecode.sand.protocols.concentrator.friends.PullLanFollows;
import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UndefinedCondition;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UnexpectedRequest;
import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.sand.client.actuator.IActuator;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.actuator.IExecutorFactory;
import com.thefirstlineofcode.sand.client.concentrator.ErrorCodeToXmppErrorsConverter;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.concentrator.ILanExecutionErrorConverter;
import com.thefirstlineofcode.sand.client.concentrator.PullLanFollowsExecutor;
import com.thefirstlineofcode.sand.client.concentrator.ResetNodeExecutor;
import com.thefirstlineofcode.sand.client.concentrator.SyncNodesExecutor;
import com.thefirstlineofcode.sand.client.edge.AbstractEdgeThing;
import com.thefirstlineofcode.sand.client.edge.ResponseInAdvanceExecutor;
import com.thefirstlineofcode.sand.client.edge.ShutdownSystemExecutor;
import com.thefirstlineofcode.sand.client.edge.StopExecutor;
import com.thefirstlineofcode.sand.client.friends.IFollowProcessor;
import com.thefirstlineofcode.sand.client.friends.IFollowService;
import com.thefirstlineofcode.sand.client.lora.dac.ILoraDacService;
import com.thefirstlineofcode.sand.client.lora.dac.ResetLoraDacServiceExecutor;
import com.thefirstlineofcode.sand.client.lora.gateway.ChangeWorkingModeExecutor;
import com.thefirstlineofcode.sand.client.lora.gateway.ILoraGateway;
import com.thefirstlineofcode.sand.client.lora.gateway.LoraGatewayPlugin;
import com.thefirstlineofcode.sand.client.thing.INotificationService;
import com.thefirstlineofcode.sand.client.thing.INotifier;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.client.things.simple.camera.ISimpleCamera;
import com.thefirstlineofcode.sand.client.things.simple.camera.SimpleCameraPlugin;
import com.thefirstlineofcode.sand.client.things.simple.light.ISimpleLight;
import com.thefirstlineofcode.sand.client.webcam.IWebcam.Capability;
import com.thefirstlineofcode.sand.client.webcam.Webcam;
import com.thefirstlineofcode.sand.client.webcam.WebcamPlugin;
import com.thefirstlineofcode.sand.demo.protocols.Lgsc01ModelDescriptor;
import com.thefirstlineofcode.sand.demo.protocols.Sl02ModelDescriptor;
import com.thefirstlineofcode.sand.demo.protocols.Str01ModelDescriptor;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded;
import com.thefirstlineofcode.sand.demo.protocols.VideoRecorded.RecordingReason;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.edge.ShutdownSystem;
import com.thefirstlineofcode.sand.protocols.edge.Stop;
import com.thefirstlineofcode.sand.protocols.lora.dac.ResetLoraDacService;
import com.thefirstlineofcode.sand.protocols.lora.gateway.ChangeWorkingMode;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakePhoto;
import com.thefirstlineofcode.sand.protocols.things.simple.camera.TakeVideo;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchStateChanged;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoraGatewayAndCamera extends AbstractEdgeThing implements ISimpleCamera, IFollowProcessor {
	public static final String THING_MODEL = Lgsc01ModelDescriptor.MODEL_NAME;
	public static final String SOFTWARE_VERSION = "1.0.0-BETA2";
	
	private static final String ATTRIBUTE_NAME_REQUESTED_WEBCAM_CAPABILITY = "requested_webcam_capability";
	private static final Capability DEFAULLT_REQUESTED_WEBCAM_CAPABILITY = new Capability(640, 480, 30);
	
	private static final String ATTRIBUTE_NAME_COMMUNICATOR_HAS_CONFIGURED = "communicator_has_configured";
	
	private static final Logger logger = LoggerFactory.getLogger(LoraGatewayAndCamera.class);
	
	private ILoraGateway loraGateway;
	private Webcam webcam;
	private WebcamConfig webcamConfig;
	private ICommunicator<LoraAddress, LoraAddress, byte[]> communicator;
	private IActuator actuator;
	private INotifier notifier;
	
	private String uploadUrl;
	private String downloadUrl;
	
	private boolean disableCamera;
	private boolean disableLoraGateway;
		
	public LoraGatewayAndCamera(WebcamConfig webcamConfig, ICommunicator<LoraAddress, LoraAddress, byte[]> communicator,
			boolean disableCamera, boolean disableLoraGateway, boolean startConsole) {
		this(webcamConfig, null, communicator, disableCamera, disableLoraGateway, startConsole);
	}
	
	public LoraGatewayAndCamera(WebcamConfig webcamConfig, StandardStreamConfig streamConfig,
			ICommunicator<LoraAddress, LoraAddress, byte[]> communicator, boolean disableCamera,
				boolean disableLoraGateway, boolean startConsole) {
		super(THING_MODEL, streamConfig, startConsole);
		
		this.webcamConfig = webcamConfig;
		this.communicator = communicator;
		this.disableCamera = disableCamera;
		this.disableLoraGateway = disableLoraGateway;
	}
	
	@Override
	protected boolean doProcessAttributes(Map<String, String> attributes) {
		uploadUrl = String.format("http://%s:8080/file-upload", this.streamConfig.getHost());
		downloadUrl = String.format("http://%s:8080/files/", this.streamConfig.getHost());
		
		boolean attributesChanged = false;
		
		if (!disableLoraGateway) {
			attributesChanged = initializeAndConfigureCommunicator(attributes);
		}
		
		if (webcamConfig.requestedCapability != null) {
			attributes.put(ATTRIBUTE_NAME_REQUESTED_WEBCAM_CAPABILITY, String.format("%s,%s,%s", webcamConfig.requestedCapability.width,
					webcamConfig.requestedCapability.height, webcamConfig.requestedCapability.maxFps));
			attributesChanged = true;
		} else {
			webcamConfig.requestedCapability = getRequestedWebcamCapability(attributes);			
		}
		
		return attributesChanged;
	}

	private boolean initializeAndConfigureCommunicator(Map<String, String> attributes) {
		boolean attributesChanged = false;
		
		boolean communicatorHasConfigured = false;
		String sCommunicatorHasConfigured = attributes.get(ATTRIBUTE_NAME_COMMUNICATOR_HAS_CONFIGURED);
		if (sCommunicatorHasConfigured != null) {
			communicatorHasConfigured = Boolean.parseBoolean(sCommunicatorHasConfigured);							
		}
		
		if (!communicator.isInitialized())
			communicator.initialize();
		
		if (!communicatorHasConfigured) {
			communicator.configure();
			
			attributes.put(ATTRIBUTE_NAME_COMMUNICATOR_HAS_CONFIGURED, "true");
			attributesChanged = true;
		}
		
		return attributesChanged;
	}

	private Capability getRequestedWebcamCapability(Map<String, String> attributes) {
		String sRequestedWebcamCapability = attributes.get(ATTRIBUTE_NAME_REQUESTED_WEBCAM_CAPABILITY);
		if (sRequestedWebcamCapability == null)
			return DEFAULLT_REQUESTED_WEBCAM_CAPABILITY;
		
		return getRequestedWebcamCapability(sRequestedWebcamCapability);
	}

	@Override
	public String getSoftwareVersion() {
		return SOFTWARE_VERSION;
	}
	
	@Override
	protected void registerIotPlugins() {
		chatClient.register(LoraGatewayPlugin.class);
		chatClient.register(SimpleCameraPlugin.class);
		chatClient.register(WebcamPlugin.class);
	}
	
	@Override
	protected void startIotComponents() {
		if (!disableCamera)
			startWebcam();
		
		if (!disableLoraGateway) {
			startLoraGateway();
			configureFollowService();
		}
		
		startActuator();
	}

	private void configureFollowService() {
		// TODO Auto-generated method stub
		if (loraGateway == null)
			return;
		
		IFollowService followService= loraGateway.getConcentrator();
		followService.registerFollowedEvent(SwitchStateChanged.PROTOCOL, SwitchStateChanged.class);
		followService.setFollowProcessor(this);
	}

	private void startActuator() {
		if (actuator == null) {			
			if (disableLoraGateway || loraGateway == null)
				actuator = chatClient.createApi(IActuator.class);
			else
				actuator = loraGateway.getConcentrator();
			
			registerExecutors(actuator);
		}
		
		actuator.start();
	}
	
	protected void startWebcam() {
		if (webcam == null)
			webcam = chatClient.createApiImpl(Webcam.class);
		
		webcam.setNotStartWebrtcNativeService(webcamConfig.dontRunWebrtcNativeService);
		webcam.setWebrtcNativeServicePath(webcamConfig.webrtcNativeServicePath);
		webcam.setRequestedCapability(webcamConfig.requestedCapability);
		
		webcam.start();
	}
	
	public Webcam getWebcam() {
		return webcam;
	}

	protected void startLoraGateway() {
		if (loraGateway == null) {
			loraGateway = chatClient.createApi(ILoraGateway.class);
			loraGateway.setCommunicator(communicator);
			
			loraGateway.getConcentrator().registerLanThingModel(new Sl02ModelDescriptor());
			loraGateway.getConcentrator().registerLanExecutionErrorConverter(getSl02ModelLanExecutionErrorConverter());
			loraGateway.getConcentrator().registerLanThingModel(new Str01ModelDescriptor());
		}
		
		loraGateway.start();
	}
	
	private ILanExecutionErrorConverter getSl02ModelLanExecutionErrorConverter() {
		return new ErrorCodeToXmppErrorsConverter(Sl02ModelDescriptor.MODEL_NAME, getSl02ModelErrorCodeToErrorTypes());
	}
	
	private Map<Integer, Class<? extends IError>> getSl02ModelErrorCodeToErrorTypes() {
		Map<Integer, Class<? extends IError>> errorCodeToXmppErrors = new HashMap<>();
		errorCodeToXmppErrors.put(ISimpleLight.ERROR_CODE_NOT_REMOTE_CONTROL_STATE,
				UnexpectedRequest.class);
		errorCodeToXmppErrors.put(ISimpleLight.ERROR_CODE_INVALID_REPEAT_ATTRIBUTE_VALUE,
				BadRequest.class);
		
		return errorCodeToXmppErrors;
	}
	
	private void registerExecutors(IActuator actuator) {
		registerExecutorsForEdgeThing(actuator);
		
		if (!disableLoraGateway)
			registerExecutorsForLoraGateway(actuator);
		
		if (!disableCamera)
			registerExecutorsForCamera(actuator);
	}

	private void registerExecutorsForEdgeThing(IActuator actuator) {
		actuator.registerExecutorFactory(createStopExecutatorFactory());
		actuator.registerExecutorFactory(createShutdownSystemExecutatorFactory());
	}

	private IExecutorFactory<?> createShutdownSystemExecutatorFactory() {
		return new IExecutorFactory<ShutdownSystem>() {
			private IExecutor<ShutdownSystem> executor = new ResponseInAdvanceExecutor<ShutdownSystem>(
					new ShutdownSystemExecutor(LoraGatewayAndCamera.this), LoraGatewayAndCamera.this);
			
			@Override
			public Protocol getProtocol() {
				return ShutdownSystem.PROTOCOL;
			}
			
			@Override
			public Class<ShutdownSystem> getActionType() {
				return ShutdownSystem.class;
			}
			
			@Override
			public IExecutor<ShutdownSystem> create() {
				return executor;
			}
		};
	}

	private IExecutorFactory<?> createStopExecutatorFactory() {
		return new IExecutorFactory<Stop>() {
			private IExecutor<Stop> executor = new ResponseInAdvanceExecutor<Stop>(
					new StopExecutor(LoraGatewayAndCamera.this), LoraGatewayAndCamera.this);
			
			@Override
			public Protocol getProtocol() {
				return Stop.PROTOCOL;
			}
			
			@Override
			public Class<Stop> getActionType() {
				return Stop.class;
			}
			
			@Override
			public IExecutor<Stop> create() {
				return executor;
			}
		};
	}

	private void registerExecutorsForLoraGateway(IActuator actuator) {
		actuator.registerExecutorFactory(createChangeWorkingModeExecutatorFactory(loraGateway));
		actuator.registerExecutorFactory(createResetDacServiceExecutatorFactory(loraGateway.getDacService()));
		actuator.registerExecutorFactory(createResetNodeServiceExecutatorFactory(loraGateway.getConcentrator()));
		actuator.registerExecutorFactory(createSyncNodesExecutatorFactory(loraGateway.getConcentrator()));
		actuator.registerExecutorFactory(createPullLanFollowsExecutatorFactory(loraGateway.getConcentrator()));
	}
	
	private IExecutorFactory<?> createSyncNodesExecutatorFactory(IConcentrator concentrator) {
		return new IExecutorFactory<SyncNodes>() {
			private IExecutor<SyncNodes> executor = new SyncNodesExecutor(
					chatClient.getChatServices(), concentrator);
			
			@Override
			public Protocol getProtocol() {
				return SyncNodes.PROTOCOL;
			}
			
			@Override
			public Class<SyncNodes> getActionType() {
				return SyncNodes.class;
			}
			
			@Override
			public IExecutor<SyncNodes> create() {
				return executor;
			}
		};
	}
	
	private IExecutorFactory<?> createPullLanFollowsExecutatorFactory(IConcentrator concentrator) {
		return new IExecutorFactory<PullLanFollows>() {
			private IExecutor<PullLanFollows> executor = new PullLanFollowsExecutor(
					chatClient.getChatServices(), concentrator);
			
			@Override
			public Protocol getProtocol() {
				return PullLanFollows.PROTOCOL;
			}
			
			@Override
			public Class<PullLanFollows> getActionType() {
				return PullLanFollows.class;
			}
			
			@Override
			public IExecutor<PullLanFollows> create() {
				return executor;
			}
		};
	}
	
	private IExecutorFactory<?> createResetNodeServiceExecutatorFactory(IConcentrator concentrator) {
		return new IExecutorFactory<ResetNode>() {
			private IExecutor<ResetNode> executor = new ResetNodeExecutor(concentrator);
			
			@Override
			public Protocol getProtocol() {
				return ResetNode.PROTOCOL;
			}
			
			@Override
			public Class<ResetNode> getActionType() {
				return ResetNode.class;
			}
			
			@Override
			public IExecutor<ResetNode> create() {
				return executor;
			}
		};
	}

	private IExecutorFactory<?> createChangeWorkingModeExecutatorFactory(ILoraGateway loraGateway) {
		return new IExecutorFactory<ChangeWorkingMode>() {
			private IExecutor<ChangeWorkingMode> executor = new ChangeWorkingModeExecutor(loraGateway);

			@Override
			public Protocol getProtocol() {
				return ChangeWorkingMode.PROTOCOL;
			}

			@Override
			public Class<ChangeWorkingMode> getActionType() {
				return ChangeWorkingMode.class;
			}

			@Override
			public IExecutor<ChangeWorkingMode> create() {
				return executor;
			}
		};
	}
	
	private IExecutorFactory<?> createResetDacServiceExecutatorFactory(ILoraDacService<?> loraDacService) {
		return new IExecutorFactory<ResetLoraDacService>() {
			private IExecutor<ResetLoraDacService> executor = new ResetLoraDacServiceExecutor(loraDacService);

			@Override
			public Protocol getProtocol() {
				return ResetLoraDacService.PROTOCOL;
			}

			@Override
			public Class<ResetLoraDacService> getActionType() {
				return ResetLoraDacService.class;
			}

			@Override
			public IExecutor<ResetLoraDacService> create() {
				return executor;
			}
		};
	}

	private void registerExecutorsForCamera(IActuator actuator) {
		actuator.registerExecutorFactory(createTakePhotoExecutatorFactory());
		actuator.registerExecutorFactory(createTakeVideoExecutatorFactory());
	}
	
	private IExecutorFactory<?> createTakeVideoExecutatorFactory() {
		return new IExecutorFactory<TakeVideo>() {
			private IExecutor<TakeVideo> executor = new TakeVideoExecutor();
			
			@Override
			public IExecutor<TakeVideo> create() {
				return executor;
			}

			@Override
			public Protocol getProtocol() {
				return TakeVideo.PROTOCOL;
			}

			@Override
			public Class<TakeVideo> getActionType() {
				return TakeVideo.class;
			}
			
		};
	}
	
	private class TakeVideoExecutor implements IExecutor<TakeVideo> {

		@Override
		public Object execute(Iq iq, TakeVideo takeVideo) throws ProtocolException {
			return takeVideo(takeVideo);
		}

		private Object takeVideo(TakeVideo takeVideo) {
			try {
				Date recordingTime = Calendar.getInstance().getTime();
				File video = LoraGatewayAndCamera.this.takeVideo(takeVideo);
				uploadFileToFilesServer(video);
				
				String videoUrl = downloadUrl + video.getName();
				getNotifier().notifyWithAck(new VideoRecorded(video.getName(), videoUrl, recordingTime, RecordingReason.USER_ACTION));
				
				return new TakeVideo(video.getName(), videoUrl);
			} catch (ExecutionException e) {
				logger.error(String.format("Exception is thrown when executing take video action. Global action error code: %s.",
						ThingsUtils.getGlobalErrorCode(THING_MODEL, e.getErrorNumber())), e);
				throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
						ThingsUtils.getExecutionErrorDescription(getThingModel(), e.getErrorNumber())));
			} catch (IOException e) {
				logger.error("Failed to upload video.", e);
				throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
						ThingsUtils.getExecutionErrorDescription(getThingModel(), FAILED_TO_UPLOAD_VIDEO)));
			}
		}
	}
	
	private void uploadFileToFilesServer(File file) throws IOException {
		Response response = null;
		try {				
			OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES).build();
			response = client.newCall(getVideoUploadRequest(file)).execute();
			if (response.code() != 200) {
				logger.error("Failed to upload video. HTTP response status code: {}.", response.code());
				throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
						ThingsUtils.getExecutionErrorDescription(getThingModel(), FAILED_TO_UPLOAD_VIDEO)));
			}
		} finally {
			if (response != null)
				response.close();
		}
	}
	
	private Request getVideoUploadRequest(File video) {
		RequestBody requestBody = new MultipartBody.Builder().
				setType(MultipartBody.FORM).
				addFormDataPart("file", video.getName(), RequestBody.create(
						video, MediaType.parse("application/octet-stream"))).
				build();
		
		return new Request.Builder().url(uploadUrl).post(requestBody).build();
	}
	
	private IExecutorFactory<?> createTakePhotoExecutatorFactory() {
		return new IExecutorFactory<TakePhoto>() {
			private IExecutor<TakePhoto> executor = new TakePhotoExecutor();
			
			@Override
			public IExecutor<TakePhoto> create() {
				return executor;
			}

			@Override
			public Protocol getProtocol() {
				return TakePhoto.PROTOCOL;
			}

			@Override
			public Class<TakePhoto> getActionType() {
				return TakePhoto.class;
			}
			
		};
	}
	
	private class TakePhotoExecutor implements IExecutor<TakePhoto> {

		@Override
		public Object execute(Iq iq, TakePhoto takePhoto) throws ProtocolException {
			Response response = null;
			try {
				File photo = LoraGatewayAndCamera.this.takePhoto(takePhoto);
				
				OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.MINUTES).build();
				response = client.newCall(getPhotoUploadRequest(photo)).execute();
				if (response.code() != 200) {
					logger.error("Failed to upload photo. HTTP response status code: {}.", response.code());
					throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
							ThingsUtils.getExecutionErrorDescription(getThingModel(), FAILED_TO_UPLOAD_PHOTO)));
				}
				
				return new TakePhoto(photo.getName(), downloadUrl + photo.getName());
			} catch (ExecutionException e) {
				logger.error(String.format("Exception is thrown when executing take photo action. Global action error code: %s.",
						ThingsUtils.getGlobalErrorCode(THING_MODEL, e.getErrorNumber())), e);
				throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
						ThingsUtils.getExecutionErrorDescription(getThingModel(), e.getErrorNumber())));
			} catch (IOException e) {
				logger.error("Failed to upload photo.", e);
				throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
						ThingsUtils.getExecutionErrorDescription(getThingModel(), FAILED_TO_UPLOAD_PHOTO)));
			} finally {
				if (response != null)
					response.close();
			}
		}
		
		private Request getPhotoUploadRequest(File photo) {
			RequestBody requestBody = new MultipartBody.Builder().
					setType(MultipartBody.FORM).
					addFormDataPart("file", photo.getName(), RequestBody.create(
							photo, MediaType.parse("application/octet-stream"))).
					build();
			
			return new Request.Builder().url(uploadUrl).post(requestBody).build();
		}
	}
	
	@Override
	protected void stopIotComponents() {
		if (actuator != null) {			
			actuator.stop();
			actuator = null;
		}
		
		if (loraGateway != null) {
			loraGateway.stop();
			loraGateway = null;
		}
		
		if (webcam != null) {
			webcam.stop();
			webcam = null;
		}
	}
	
	@Override
	protected String loadThingId() {
		return THING_MODEL + "-" + ThingsUtils.generateRandomId(8);
	}
	
	@Override
	protected String loadRegistrationCode() {
		return "abcdefghigkl";
	}

	@Override
	public File takePhoto(TakePhoto takePhoto) throws ExecutionException {
		String photoPath = getPhotoOutputPath();
		createMediaDirectoryIfItNotExisted(photoPath);
		
		int prepareTime = takePhoto.getPrepareTime() == null ? 1000 : takePhoto.getPrepareTime();
		runInNewProcess(getTakePhotoCmdArray(photoPath, prepareTime));
		
		File photo = new File(photoPath);
		if (!photo.exists()) {
			logger.error("Photo file wasn't taken. Photo path: " + photoPath + ".");
			throw new ExecutionException(ERROR_CODE_PHOTO_WAS_NOT_TAKEN);
		}
		
		logger.info("Photo was taken. Photo path: " + photoPath + ".");
		return photo;
	}

	private void createMediaDirectoryIfItNotExisted(String mediaPath) {
		File fMedia = new File(mediaPath);
		if (!fMedia.getParentFile().exists()) {
			try {
				Files.createDirectories(fMedia.toPath());
			} catch (IOException e) {
				throw new RuntimeException("Can't create photo directory.", e);
			}
		}
	}
	
	@Override
	public File takeVideo(TakeVideo takeVideo) throws ExecutionException {
		String videoPath = getVideoOutputPath();
		createMediaDirectoryIfItNotExisted(videoPath);
		
		int durationTime = takeVideo.getDurationTime() == null ? 10000 : takeVideo.getDurationTime();
		runInNewProcess(getTakeVideoCmdArray(videoPath, durationTime));
		
		File video = new File(videoPath);
		if (!video.exists()) {
			logger.error("Video file wasn't taken. Video path: " + videoPath + ".");
			throw new ExecutionException(ERROR_CODE_VIDEO_WAS_NOT_TAKEN);
		}
		
		logger.info("Video was taken. Video path: " + videoPath + ".");
		return video;
	}
	
	private String[] getTakePhotoCmdArray(String photoPath, int prepareTime) {
		List<String> cmdList = new ArrayList<>();
		cmdList.add("fswebcam");
		cmdList.add("--delay");
		cmdList.add(Integer.toString(prepareTime / 1000));
		cmdList.add("--resolution");
		cmdList.add("800x600");
		cmdList.add("--jpeg");
		cmdList.add("90");
		cmdList.add("--no-banner");
		cmdList.add(photoPath);
		
		return cmdList.toArray(new String[0]);
	}
	
	private String[] getTakeVideoCmdArray(String videoPath, int durationTime) {
		List<String> cmdList = new ArrayList<>();
		cmdList.add("ffmpeg");
		cmdList.add("-f");
		cmdList.add("v4l2");
		cmdList.add("-input_format");
		cmdList.add("rawvideo");
		cmdList.add("-i");
		cmdList.add("/dev/video0");
		cmdList.add("-c");
		cmdList.add("libvpx");
		cmdList.add("-r");
		cmdList.add("20");
		cmdList.add("-s");
		cmdList.add("640x480");
		cmdList.add("-t");
		cmdList.add(String.format("00:00:%02d", durationTime / 1000));
		cmdList.add(videoPath);
		
		return cmdList.toArray(new String[0]);
	}

	private String getPhotoOutputPath() {
		Calendar calendar = Calendar.getInstance();
		String fileName = String.format("/home/pi/tmp/%s-%s-%s-%s-%s-%s.jpg",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DATE),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.MILLISECOND));
		
		return fileName;
	}
	
	private String getVideoOutputPath() {
		Calendar calendar = Calendar.getInstance();
		String fileName = String.format("/home/pi/tmp/%s-%s-%s-%s-%s-%s.webm",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DATE),
				calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.MILLISECOND));
		
		return fileName;
	}
	
	public static Capability getRequestedWebcamCapability(String sRequestedWebcamCapability) {
		if (sRequestedWebcamCapability == null)
			return DEFAULLT_REQUESTED_WEBCAM_CAPABILITY;
		
		StringTokenizer st = new StringTokenizer(sRequestedWebcamCapability, ",");
		if (st.countTokens() != 3)
			throw new IllegalArgumentException("Illegal webcam capability format. Capability format: WIDTH,HEIGHT,MAX_FPS.");
		
		try {
			int width = Integer.parseInt(st.nextToken());
			int height = Integer.parseInt(st.nextToken());
			int maxFps = Integer.parseInt(st.nextToken());
			
			return new Capability(width, height, maxFps);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal webcam capability format.", e);
		}
	}

	@Override
	public void process(JabberId friend, Object event) {
		SwitchStateChanged switchStateChanged = (SwitchStateChanged)event;
		
		if (switchStateChanged.getPrevious() == SwitchState.OFF &&
					switchStateChanged.getCurrent() == SwitchState.ON) {			
			try {
				Date recordingTime = Calendar.getInstance().getTime();
				File video = takeVideo(new TakeVideo(10000));
				uploadFileToFilesServer(video);
				
				String videoUrl = downloadUrl + video.getName();
				getNotifier().notifyWithAck(new VideoRecorded(video.getName(), videoUrl, recordingTime, RecordingReason.IOT_EVENT));
			} catch (Exception e) {
				if (logger.isErrorEnabled())
					logger.error("Failed to record video.", e);
			}
		}
	}

	private INotifier getNotifier() {
		if (notifier != null)
			return notifier;
		
		INotificationService notificationService = chatClient.createApi(INotificationService.class);
		notifier = notificationService.getNotifier();
		notifier.registerSupportedEvent(VideoRecorded.class);
		
		return notifier;
	}
}
