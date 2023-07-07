package com.thefirstlineofcode.sand.emulators.lora.gateway;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FontUIResource;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.thefirstlinelinecode.sand.protocols.concentrator.SyncNodes;
import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UnexpectedRequest;
import com.thefirstlineofcode.chalk.core.AuthFailureException;
import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.chalk.core.StandardChatClient;
import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.chalk.core.stream.UsernamePasswordToken;
import com.thefirstlineofcode.chalk.network.ConnectionException;
import com.thefirstlineofcode.chalk.network.IConnectionListener;
import com.thefirstlineofcode.sand.client.actuator.IActuator;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.actuator.IExecutorFactory;
import com.thefirstlineofcode.sand.client.concentrator.ErrorCodeToXmppErrorsConverter;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator.AddNodeError;
import com.thefirstlineofcode.sand.client.concentrator.ILanExecutionErrorConverter;
import com.thefirstlineofcode.sand.client.concentrator.LanNode;
import com.thefirstlineofcode.sand.client.concentrator.SyncNodesExecutor;
import com.thefirstlineofcode.sand.client.friends.FriendsPlugin;
import com.thefirstlineofcode.sand.client.friends.IFollowProcessor;
import com.thefirstlineofcode.sand.client.friends.IFollowService;
import com.thefirstlineofcode.sand.client.ibtr.IRegistration;
import com.thefirstlineofcode.sand.client.ibtr.IbtrPlugin;
import com.thefirstlineofcode.sand.client.ibtr.RegistrationException;
import com.thefirstlineofcode.sand.client.lora.dac.ILoraDacClient;
import com.thefirstlineofcode.sand.client.lora.dac.ILoraDacService;
import com.thefirstlineofcode.sand.client.lora.gateway.ILoraGateway;
import com.thefirstlineofcode.sand.client.lora.gateway.LoraGatewayPlugin;
import com.thefirstlineofcode.sand.client.thing.AbstractThing;
import com.thefirstlineofcode.sand.client.thing.IBatteryPowerListener;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.client.things.simple.light.ISimpleLight;
import com.thefirstlineofcode.sand.emulators.commons.Constants;
import com.thefirstlineofcode.sand.emulators.commons.IThingEmulator;
import com.thefirstlineofcode.sand.emulators.commons.IThingEmulatorFactory;
import com.thefirstlineofcode.sand.emulators.commons.StreamConfigInfo;
import com.thefirstlineofcode.sand.emulators.commons.ui.AboutDialog;
import com.thefirstlineofcode.sand.emulators.commons.ui.AbstractThingEmulatorPanel;
import com.thefirstlineofcode.sand.emulators.commons.ui.StatusBar;
import com.thefirstlineofcode.sand.emulators.commons.ui.StreamConfigDialog;
import com.thefirstlineofcode.sand.emulators.commons.ui.UiUtils;
import com.thefirstlineofcode.sand.emulators.lora.gateway.log.LogConsolesDialog;
import com.thefirstlineofcode.sand.emulators.lora.gateway.things.ThingInternalFrame;
import com.thefirstlineofcode.sand.emulators.lora.network.ILoraChip;
import com.thefirstlineofcode.sand.emulators.lora.network.ILoraNetwork;
import com.thefirstlineofcode.sand.emulators.lora.network.LoraCommunicator;
import com.thefirstlineofcode.sand.emulators.lora.network.LoraCommunicatorFactory;
import com.thefirstlineofcode.sand.emulators.lora.simple.light.SimpleLight;
import com.thefirstlineofcode.sand.emulators.lora.things.AbstractLoraThingEmulator;
import com.thefirstlineofcode.sand.emulators.lora.things.AbstractLoraThingEmulator.DacState;
import com.thefirstlineofcode.sand.emulators.lora.things.AbstractLoraThingEmulatorFactory;
import com.thefirstlineofcode.sand.emulators.models.Lge01ModelDescriptor;
import com.thefirstlineofcode.sand.emulators.models.Sle01ModelDescriptor;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.lora.gateway.ChangeWorkingMode;
import com.thefirstlineofcode.sand.protocols.lora.gateway.WorkingMode;
import com.thefirstlineofcode.sand.protocols.thing.BadAddressException;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchStateChanged;

public class Gateway extends JFrame implements ActionListener, InternalFrameListener,
		ComponentListener, WindowListener, IGateway, IConnectionListener, ILoraDacService.Listener,
		IConcentrator.Listener, IFollowProcessor {
	private static final String NAME_GATEWAY_EMULATOR = "LoRa Gateway Emulator";

	private static final long serialVersionUID = -7894418812878036627L;
	
	private static final String THING_MODEL = Lge01ModelDescriptor.MODEL_NAME;
	
	private static final int ALWAYS_FULL_POWER = 100;
	
	// File Menu
	private static final String MENU_TEXT_FILE = "File";
	private static final String MENU_NAME_FILE = "file";
	private static final String MENU_ITEM_TEXT_NEW = "New";
	private static final String MENU_ITEM_NAME_NEW = "new";
	private static final String MENU_ITEM_TEXT_OPEN_FILE = "Open file...";
	private static final String MENU_ITEM_NAME_OPEN_FILE = "open_file";
	private static final String MENU_ITEM_TEXT_SAVE = "Save";
	private static final String MENU_ITEM_NAME_SAVE = "save";
	private static final String MENU_ITEM_TEXT_SAVE_AS = "Save As...";
	private static final String MENU_ITEM_NAME_SAVE_AS = "save_as";
	private static final String MENU_ITEM_NAME_QUIT = "quit";
	private static final String MENU_ITEM_TEXT_QUIT = "Quit";
	
	// Edit Menu
	private static final String MENU_TEXT_EDIT = "Edit";
	private static final String MENU_NAME_EDIT = "edit";
	private static final String MENU_ITEM_TEXT_POWER_ON = "Power On";
	private static final String MENU_ITEM_NAME_POWER_ON = "power_on";
	private static final String MENU_ITEM_TEXT_POWER_OFF = "Power Off";
	private static final String MENU_ITEM_NAME_POWER_OFF = "power_off";
	private static final String MENU_ITEM_TEXT_RESET = "Reset";
	private static final String MENU_ITEM_NAME_RESET = "reset";
	private static final String MENU_ITEM_TEXT_DELETE = "Delete";
	private static final String MENU_ITEM_NAME_DELETE = "delete";
	
	// Tools Menu
	private static final String MENU_TEXT_TOOLS = "Tools";
	private static final String MENU_NAME_TOOLS = "tools";
	private static final String MENU_ITEM_TEXT_REGISTER = "Register";
	private static final String MENU_ITEM_NAME_REGISTER = "register";
	private static final String MENU_ITEM_TEXT_CONNECT = "Connect";
	private static final String MENU_ITEM_NAME_CONNECT = "connect";
	private static final String MENU_ITEM_TEXT_DISCONNECT = "Disconnect";
	private static final String MENU_ITEM_NAME_DISCONNECT = "disconnect";
	
	private static final String MENU_ITEM_TEXT_DAC_MODE = "DAC Mode";
	private static final String MENU_ITEM_NAME_RECONFIGURE_ADDRESS = "reconfigure_address";
	private static final String MENU_ITEM_TEXT_RECONFIGURE_ADDRESS = "Reconfigure Address";
	
	private static final String MENU_ITEM_TEXT_SHOW_LOG_CONSOLE = "Show Log Console";
	private static final String MENU_ITEM_NAME_SHOW_LOG_CONSOLE = "show_log_console";
	
	// Help Menu
	private static final String MENU_TEXT_HELP = "Help";
	private static final String MENU_NAME_HELP = "help";
	private static final String MENU_ITEM_TEXT_ABOUT = "About";
	private static final String MENU_ITEM_NAME_ABOUT = "about";
	private static final String MENU_ITEM_TEXT_GENERATE_QR_CODE_PIC = "Generate QR code picture";
	private static final String MENU_ITEM_NAME_GENERATE_QR_CODE_PIC = "generate_qr_code_picture";
	
	private JCheckBoxMenuItem dacModeMenuItem;
	
	private String thingId;
	private RegisteredThing registeredThing;
	private StandardStreamConfig streamConfig;
	
	private List<AbstractLoraThingEmulatorFactory<?>> thingFactories;
	private Map<String, List<AbstractLoraThingEmulator>> allThings;
	private boolean dirty;
	
	private ILoraNetwork network;
	private LoraCommunicator gatewayCommunicator;
	
	private JDesktopPane desktop;
	private JMenuBar menuBar;
	private StatusBar statusBar;
	private LogConsolesDialog logConsolesDialog;
	
	private File configFile;
	
	private IChatClient chatClient;
	private boolean autoReconnect;
		
	private ILoraGateway loraGateway;
	
	public Gateway(ILoraNetwork network) {
		super(NAME_GATEWAY_EMULATOR);
		
		this.network = network;
		gatewayCommunicator = createGatewayCommunicator();
		gatewayCommunicator.initialize();
		
		thingId = generateThingId();
		
		thingFactories = new ArrayList<>();
		allThings = new HashMap<>();
		dirty = false;
		autoReconnect = false;
		
		registerThingEmulatorFactory(new LightEmulatorFactory());
		
		LoraCommunicatorFactory.create(network);
		
		new Thread(new AutoReconnectThread(), "Gateway Auto Reconnect Thread").start();
				
		setupUi();
	}
	
	private class LightEmulatorFactory extends AbstractLoraThingEmulatorFactory<SimpleLight> {
		public LightEmulatorFactory() {
			super(Sle01ModelDescriptor.MODEL_NAME, Sle01ModelDescriptor.DESCRIPTION);
		}

		@Override
		public SimpleLight create() {
			StandardChatClient standardChatClient = (StandardChatClient)chatClient;
			ILoraDacClient dacClient = standardChatClient.createApi(ILoraDacClient.class);
			
			return new SimpleLight(dacClient);
		}
	}
	
	protected LoraCommunicator createGatewayCommunicator() {
		ILoraChip chip = null;
		chip = network.createChip(new LoraAddress(new byte[] {0x00, 0x00,
				ILoraDacService.DEFAULT_THING_COMMUNICATION_CHANNEL}));
		
		return new LoraCommunicator(chip);
	}

	protected String generateThingId() {
		return getThingModel() + "-" + ThingsUtils.generateRandomId(8);
	}
	
	private class AutoReconnectThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (autoReconnect && !isConnected()) {
					connect(false);
				}
				
				try {
					Thread.sleep(1000 * 20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private IConnectionListener getLogConsoleInternetConnectionListener() {
		if (logConsolesDialog == null)
			return null;
		
		return logConsolesDialog.getInternetConnectionListener();
	}
	
	private void setupUi() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		setDefaultUiFont(new javax.swing.plaf.FontUIResource("Serif", Font.PLAIN, 20));
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 1024) / 2, (screenSize.height - 600) / 2, 1024, 600);
		
		desktop = new JDesktopPane();
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		add(desktop, BorderLayout.CENTER);
		
		menuBar = createMenuBar();
		setJMenuBar(menuBar);
		
		statusBar = new StatusBar(this);
		add(statusBar, BorderLayout.SOUTH);
		
		updateStatus();
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
	}

	private void updateStatus() {
		statusBar.setText(getStatus());
	}
	
	private void setDefaultUiFont(FontUIResource fur) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put (key, fur);
		}
	}
	
	public void setThingId(String thingId) {
		if (thingId == null)
			throw new IllegalArgumentException("Null thing id.");
		
		this.thingId = thingId;
	}
	
	private String getStatus() {
		StringBuilder sb = new StringBuilder();
		if (registeredThing == null) {
			sb.append("Unregistered. ");
		} else {
			sb.append("Registered: ").append(registeredThing.getThingName()).append(". ");
			if (chatClient != null && chatClient.isConnected()) {
				sb.append("Connected. ");
				
				sb.append(getModeString());
			} else {
				sb.append("Disconnected. ");
			}
		}
		
		sb.append("Thing ID: ").append(thingId);
		
		return sb.toString();
	}
	
	private String getModeString() {
		if (!isConnected() || loraGateway == null)
			return "";
		
		if (loraGateway.getConcentrator().isStarted()) {
			return "Mode: W. ";
		} else if (loraGateway.getDacService().isStarted()) {
			return "Mode: A. ";
		} else {
			return "Mode: Unknown. ";
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		
		if (MENU_ITEM_NAME_NEW.equals(actionCommand)) {
			createNewThing();
		} else if (MENU_ITEM_NAME_OPEN_FILE.equals(actionCommand)) {
			openFile();
		} else if (MENU_ITEM_NAME_SAVE.equals(actionCommand)) {
			save();
		} else if (MENU_ITEM_NAME_SAVE_AS.equals(actionCommand)) {
			saveAs();
		} else if (MENU_ITEM_NAME_QUIT.equals(actionCommand)) {
			quit();
		} else if (MENU_ITEM_NAME_POWER_ON.equals(actionCommand)) {
			powerOn();
		} else if (MENU_ITEM_NAME_POWER_OFF.equals(actionCommand)) {
			powerOff();
		} else if (MENU_ITEM_NAME_RESET.equals(actionCommand)) {
			reset();
		} else if (MENU_ITEM_NAME_DELETE.equals(actionCommand)) {
			delete();
		} else if (MENU_ITEM_NAME_REGISTER.equals(actionCommand)) {
			register();
		} else if (MENU_ITEM_NAME_CONNECT.equals(actionCommand)) {
			connect();
		} else if (MENU_ITEM_NAME_DISCONNECT.equals(actionCommand)) {
			disconnect();
		} else if (MENU_ITEM_NAME_RECONFIGURE_ADDRESS.equals(actionCommand)) {
			reconfigureAddress();
		} else if (MENU_ITEM_NAME_SHOW_LOG_CONSOLE.equals(actionCommand)) {
			showLogConsoleDialog();
		} else if (MENU_ITEM_NAME_ABOUT.equals(actionCommand)) {
			showAboutDialog();
		} else if (MENU_ITEM_NAME_GENERATE_QR_CODE_PIC.equals(actionCommand)) {
			generateQrCodePic();
		} else {
			throw new IllegalArgumentException("Illegal action command: " + actionCommand);
		}
	}
	
	private void reconfigureAddress() {
		getSelectedFrame().getThing().reset();
		loraGateway.getDacService().reset();
		
		getSelectedFrame().getThing().powerOff();
		getSelectedFrame().getThing().powerOn();
	}
	
	@Override
	public synchronized void setToRouterMode() {
		loraGateway.setWorkingMode(WorkingMode.ROUTER);
		
		refreshDacModeRelativeMenus();
		updateStatus();
	}
	
	@Override
	public synchronized void setToDacMode() {
		loraGateway.setWorkingMode(WorkingMode.DAC);
		
		refreshDacModeRelativeMenus();
		updateStatus();
	}

	private void refreshDacModeRelativeMenus() {
		if (!isConnected()) {
			dacModeMenuItem.setSelected(false);
			dacModeMenuItem.setEnabled(false);
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_RECONFIGURE_ADDRESS).setEnabled(false);
			
			return;
		}
		
		dacModeMenuItem.setEnabled(true);
		if (loraGateway == null || loraGateway.getDacService().isStopped()) {
			dacModeMenuItem.setSelected(false);
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_RECONFIGURE_ADDRESS).setEnabled(false);
		} else {
			dacModeMenuItem.setSelected(true);
			ThingInternalFrame thingInternalFrame = getSelectedFrame();
			if (thingInternalFrame != null && !thingInternalFrame.getThing().isAddressConfigured()) {
				UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_RECONFIGURE_ADDRESS).setEnabled(true);
			} else {
				UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_RECONFIGURE_ADDRESS).setEnabled(false);
			}
		}	
	}
	
	@Override
	public synchronized void disconnect() {
		boolean dirty = false;
		
		if (autoReconnect) {
			autoReconnect = false;
			dirty = true;
		}
		
		if (isConnected()) {
			doDisconnect();
			dirty = true;
		}
		
		if (dirty) {
			refreshConnectionStateRelativeMenus();
			UiUtils.showNotification(this, "Message", "Gateway has disconnected.");
		}
		refreshDacModeRelativeMenus();
		updateStatus();
	}

	private void doDisconnect() {
		if (loraGateway != null && loraGateway.isStarted())
			loraGateway.stop();
		
		if (chatClient != null) {
			chatClient.close();
		}		
	}
	
	private void stopListeningInLogConsole(IChatClient chatClient) {
		if (chatClient != null && hasAlreadyConnectionListenerExisted(chatClient, getLogConsoleInternetConnectionListener()))
			chatClient.removeConnectionListener(getLogConsoleInternetConnectionListener());
	}
	
	@Override
	public void connect() {
		connect(true);
		
		refreshDacModeRelativeMenus();
		updateStatus();
	}
	
	private void connect(boolean dirty) {
		if (chatClient != null && chatClient.isConnected())
			throw new IllegalStateException("Gateway has already connected.");
		
		try {
			doConnect();
			
			if (loraGateway == null) {
				loraGateway = chatClient.createApi(ILoraGateway.class);
				configureLoraGateway(loraGateway);
				configureFollowService(loraGateway.getConcentrator());
			}
			
			if (!loraGateway.isStarted())
				startLoraGateway(loraGateway);
			
			UiUtils.showNotification(this, "Message", "Gateway has connected.");
		} catch (ConnectionException e) {
			if (chatClient != null)
				chatClient.close();
			
			JOptionPane.showMessageDialog(this, "Connection error. Error type: " + e.getType(), "Connect Error", JOptionPane.ERROR_MESSAGE);
		} catch (AuthFailureException e) {
			JOptionPane.showMessageDialog(this, "Authentication failed.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void configureFollowService(IFollowService followService) {
		followService.registerFollowedEvent(SwitchStateChanged.PROTOCOL, SwitchStateChanged.class);
		followService.setFollowProcessor(this);
	}

	private void configureLoraGateway(ILoraGateway loraGateway) {
		loraGateway.setCommunicator(gatewayCommunicator);
		
		registerExecutors(loraGateway.getConcentrator());
		
		IConcentrator concentrator = loraGateway.getConcentrator();
		concentrator.registerLanThingModel(new Sle01ModelDescriptor());
		concentrator.registerLanExecutionErrorConverter(getSle01ModelLanExecutionErrorConverter());
		concentrator.addListener(this);
		
		ILoraDacService<LoraAddress> dacService = loraGateway.getDacService();
		dacService.addListener(this);
	}

	private void doConnect() throws ConnectionException, AuthFailureException {
		boolean oldAutoReconnect = this.autoReconnect;
		autoReconnect = false;
		
		try {			
			if (chatClient == null) {
				chatClient = createChatClient();
				chatClient.addConnectionListener(this);
				listenInternetInLogConsole(chatClient);
			}
			
			
			if (!isConnected()) {
				chatClient.connect(new UsernamePasswordToken(registeredThing.getThingName().toString(), registeredThing.getCredentials()));	
			}
		} finally {
			autoReconnect = oldAutoReconnect;
		}
		
		refreshConnectionStateRelativeMenus();
		refreshDacModeRelativeMenus();
		updateStatus();
		
		if (!isConnected())
			return;
		
		autoReconnect = true;
	}
	
	private void listenInternetInLogConsole(IChatClient chatClient) {
		IConnectionListener logConsoleListener = getLogConsoleInternetConnectionListener();
		if (logConsoleListener != null && chatClient != null &&
				!hasAlreadyConnectionListenerExisted(chatClient, logConsoleListener))
			chatClient.addConnectionListener(logConsoleListener);
	}

	private boolean hasAlreadyConnectionListenerExisted(IChatClient chatClient, IConnectionListener listener) {
		for (IConnectionListener aListener : chatClient.getConnectionListeners()) {
			if (aListener == listener)
				return true;
		}
		
		return false;
	}
	
	private IChatClient createChatClient() {
		if (registeredThing == null)
			throw new IllegalStateException("Thing identity is null. Please register your gateway.");
		
		StandardStreamConfig streamConfigWithResource = createStreamConfigWithResource();
		IChatClient chatClient = new StandardChatClient(streamConfigWithResource);
		
		registerPlugins(chatClient);
		
		return chatClient;
	}
	
	private void startLoraGateway(ILoraGateway loraGateway) {
		if (loraGateway.getDacService().isStarted())
			throw new IllegalStateException("DAC service isn't being in stopped state.");
		
		loraGateway.start();
	}

	private void registerExecutors(IActuator actuator) {
		actuator.registerExecutorFactory(new IExecutorFactory<ChangeWorkingMode>() {
			@Override
			public IExecutor<ChangeWorkingMode> create() {
				return new ChangeWorkingModeExecutor();
			}
			
			@Override
			public Protocol getProtocol() {
				return ChangeWorkingMode.PROTOCOL;
			}
			
			@Override
			public Class<ChangeWorkingMode> getActionType() {
				return ChangeWorkingMode.class;
			}
		});
		
		actuator.registerExecutorFactory(new IExecutorFactory<SyncNodes>() {
			@Override
			public IExecutor<SyncNodes> create() {
				return new SyncNodesExecutor(chatClient.getChatServices(), loraGateway.getConcentrator());
			}

			@Override
			public Protocol getProtocol() {
				return SyncNodes.PROTOCOL;
			}

			@Override
			public Class<SyncNodes> getActionType() {
				return SyncNodes.class;
			}
		});
	}
	
	private class ChangeWorkingModeExecutor implements IExecutor<ChangeWorkingMode> {

		@Override
		public Object execute(Iq iq, ChangeWorkingMode action) throws ProtocolException {
			if (action.getWorkingMode().equals(WorkingMode.DAC))
				setToDacMode();
			else
				setToRouterMode();
			
			return null;
		}
		
	}
	
	public ILanExecutionErrorConverter getSle01ModelLanExecutionErrorConverter() {
		return new ErrorCodeToXmppErrorsConverter(Sle01ModelDescriptor.MODEL_NAME, getSle01ModelErrorCodeToErrorTypes());
	}

	private Map<Integer, Class<? extends IError>> getSle01ModelErrorCodeToErrorTypes() {
		Map<Integer, Class<? extends IError>> errorCodeToXmppErrors = new HashMap<>();
		errorCodeToXmppErrors.put(ISimpleLight.ERROR_CODE_NOT_REMOTE_CONTROL_STATE,
				UnexpectedRequest.class);
		errorCodeToXmppErrors.put(ISimpleLight.ERROR_CODE_INVALID_REPEAT_ATTRIBUTE_VALUE,
				BadRequest.class);
		
		return errorCodeToXmppErrors;
	}
	
	private void registerPlugins(IChatClient chatClient) {
		chatClient.register(LoraGatewayPlugin.class);
		chatClient.register(FriendsPlugin.class);
	}

	private StandardStreamConfig createStreamConfigWithResource() {
		StandardStreamConfig cloned = new StandardStreamConfig(streamConfig.getHost(), streamConfig.getPort());
		cloned.setTlsPreferred(streamConfig.isTlsPreferred());
		cloned.setResource(String.valueOf(IConcentrator.LAN_ID_CONCENTRATOR));
		
		return cloned;
	}

	private void showLogConsoleDialog() {
		logConsolesDialog = new LogConsolesDialog(this, network, gatewayCommunicator, allThings);			
		logConsolesDialog.addWindowListener(this);
		logConsolesDialog.setVisible(true);
		
		listenInternetInLogConsole(chatClient);
		
		UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_SHOW_LOG_CONSOLE).setEnabled(false);
	}
	
	@Override
	public void register() {
		if (registeredThing != null)
			throw new IllegalStateException("Gateway has already registered.");
		
		if (streamConfig == null) {
			StreamConfigDialog streamConfigDialog = new StreamConfigDialog(this);
			UiUtils.showDialog(this, streamConfigDialog);
			
			streamConfig = streamConfigDialog.getStreamConfig();
		}
		
		if (streamConfig != null) {
			doRegister();	
		}
		
		if (registeredThing != null)
			UiUtils.showNotification(this, "Message", "Gateway has registered.");
	}
	
	private void doRegister() {
		if (streamConfig == null)
			throw new IllegalArgumentException("Null stream config.");
		
		IChatClient chatClient = new StandardChatClient(streamConfig);
		chatClient.register(IbtrPlugin.class);
		IRegistration registration = chatClient.createApi(IRegistration.class);
		listenRegisrationInLogConsole(registration);
		
		try {
			registeredThing = registration.register(thingId, "abcdefghijkl");
		} catch (RegistrationException e) {
			JOptionPane.showMessageDialog(this, "Can't register thing. Error: " + e.getError(), "Registration Error", JOptionPane.ERROR_MESSAGE);
		} finally {			
			registration.removeConnectionListener(getLogConsoleInternetConnectionListener());
			chatClient.close();
		}
		
		setDirty(true);
		refreshGatewayInstanceRelativeMenus();
		updateStatus();
	}

	private void listenRegisrationInLogConsole(IRegistration registration) {
		IConnectionListener logListener = getLogConsoleInternetConnectionListener();
		if (logListener != null) {
			registration.addConnectionListener(logListener);
		}
	}

	private void delete() {
		// TODO Auto-generated method stub
		
	}

	private void reset() {
		ThingInternalFrame selectedFrame = getSelectedFrame();
		if (selectedFrame == null) {
			refreshThingSelectionRelativeMenus();
			return;
		}
		
		selectedFrame.getThing().reset();
		refreshThingSelectionRelativeMenus();
	}
	
	private void powerOff() {
		getSelectedFrame().getThing().powerOff();
		refreshPowerRelativeMenus();
	}

	private ThingInternalFrame getSelectedFrame() {
		ThingInternalFrame thingFrame = (ThingInternalFrame)desktop.getSelectedFrame();
		return thingFrame;
	}
	
	private void powerOn() {
		getSelectedFrame().getThing().powerOn();
		refreshPowerRelativeMenus();
	}

	private void saveAs() {
		JFileChooser fileChooser = createGatewayInfoFileChooser();
		fileChooser.setDialogTitle("Choose a file to save your LoRa gateway info");
		File defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();
		fileChooser.setSelectedFile(new File(defaultDirectory, thingId + ".lgi"));
		
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			saveToFile(fileChooser.getSelectedFile());
		}
	}

	private JFileChooser createGatewayInfoFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".lgi");
			}

			@Override
			public String getDescription() {
				return "LoRa gateway info file (.lgi)";
			}
		});
		
		return fileChooser;
	}

	private void save() {
		if (configFile == null)
			saveAs();
		else
			saveToFile(configFile);
	}

	private void saveToFile(File file) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e3) {
				throw new RuntimeException("Can't create gateway info file " + file.getPath());
			}
		}
		
		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(file));
			output.writeObject(thingId);
			if (streamConfig != null) {
				output.writeObject(new StreamConfigInfo(streamConfig.getHost(), streamConfig.getPort(), streamConfig.isTlsPreferred()));
			} else {
				output.writeObject(null);
			}
			
			if (registeredThing != null) {
				output.writeObject(registeredThing);
			} else {
				output.writeObject(null);
			}
			
			output.writeBoolean(autoReconnect);
			
			if (loraGateway == null) {
				output.writeInt(0);
			} else {				
				IConcentrator concentrator = loraGateway.getConcentrator();
				Collection<LanNode> nodes = concentrator.getNodes();
				output.writeInt(nodes.size());
				
				if (nodes.size() > 0) {	
					for (LanNode node : nodes) {
						output.writeObject(node);
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(String.format("Gateway info file %s doesn't exist.", file.getPath()));
		} catch (IOException e) {
			throw new RuntimeException("Can't save gateway info file.", e);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		setDirty(false);
		if (!file.equals(configFile)) {
			setConfigFile(file);
		}
	}
	
	private void showAboutDialog() {
		UiUtils.showDialog(this, new AboutDialog(this, NAME_GATEWAY_EMULATOR, Constants.SOFTWARE_VERSION));
	}
	
	private void generateQrCodePic() {
		String thingId = JOptionPane.showInputDialog(this, "Please input a thing ID for QR code picture", "Generate QR code picture",
				JOptionPane.QUESTION_MESSAGE);
		
		if (thingId == null || thingId.isEmpty()) {
			return;
		}
		
		JFileChooser fileChooser = createJpgFileChooser();
		fileChooser.setDialogTitle("Choose a file to save your QR code picture");
		File defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();
		fileChooser.setSelectedFile(new File(defaultDirectory, thingId + ".jpg"));
		
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			generateQrCodePic(thingId, fileChooser.getSelectedFile());
		}
	}
	
	private void generateQrCodePic(String thingId, File file) {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BufferedOutputStream output = null;
		try {
			output = new BufferedOutputStream(new FileOutputStream(file));
			BitMatrix matrix = qrCodeWriter.encode(thingId, BarcodeFormat.QR_CODE, 200, 200);
			MatrixToImageWriter.writeToStream(matrix, "JPEG", output);
		} catch (Exception e) {
			throw new RuntimeException("Can't generate QR code picture.", e);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private JFileChooser createJpgFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".jpg");
			}

			@Override
			public String getDescription() {
				return "JPEG file (.jpg)";
			}
		});
		
		return fileChooser;
	}

	private void quit() {
		autoReconnect = false;
		doDisconnect();
		
		if (!dirty)
			System.exit(0);
		
		int result = JOptionPane.showConfirmDialog(this, "Gateway info has changed. Do you want to save the change?");
		if (result == JOptionPane.CANCEL_OPTION) {
			return;
		} else if (result == JOptionPane.NO_OPTION) {
			System.exit(0);
		} else {
			save();
			System.exit(0);
		}
	}

	private void openFile() {
		if (dirty) {
			int result = JOptionPane.showConfirmDialog(this, "Gateway info has changed. Do you want to save the change?");
			if (result == JOptionPane.CANCEL_OPTION) {
				return;
			} else if (result == JOptionPane.YES_OPTION) {
				save();				
			} else {
				// NO-OP
			}
		}
		
		JFileChooser fileChooser = createGatewayInfoFileChooser();
		fileChooser.setDialogTitle("Choose a gateway info file you want to open");
		File defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();
		fileChooser.setCurrentDirectory(defaultDirectory);
		
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			resetStatus();
			loadFromFile(fileChooser.getSelectedFile());
			changeGatewayStatusAndRunUiRefreshThread();
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {					
					JInternalFrame[] frames = Gateway.this.desktop.getAllFrames();
					for (JInternalFrame frame : frames) {
						frame.addComponentListener(Gateway.this);
						frame.addInternalFrameListener(Gateway.this);
					}
					
					refreshGatewayInstanceRelativeMenus();
					updateStatus();
				}
				
			});
		}
	}

	private void resetStatus() {
		closeConnnection();
		removeThings();
	}

	private void closeConnnection() {
		autoReconnect = false;
		if (isConnected())
			chatClient.close();
	}

	private void removeThings() {
		// Remove things from network.
		for (String thingName : allThings.keySet()) {
			List<AbstractLoraThingEmulator> things = allThings.get(thingName);
			if (things == null || things.size() == 0)
				continue;
			
			for (AbstractLoraThingEmulator thing : things) {
				LoraCommunicator communicator = (LoraCommunicator)thing.getCommunicator();
				network.removeChip(communicator.getAddress());
			}
		}
		
		// Remove all internal frames for thing's UI from desktop.
		desktop.removeAll();
		
		// Refresh desktop
		desktop.setVisible(false);
		desktop.setVisible(true);
	}
	
	private AbstractLoraThingEmulatorFactory<?> findThingFactoryByModel(String model) {
		for (AbstractLoraThingEmulatorFactory<?> thingFactory : thingFactories) {
			if (thingFactory.getThingModel().equals(model))
				return thingFactory;
		}
		
		return null;
	}

	private void loadFromFile(File file) {
		GatewayInfo gatewayInfo = readGatewayInfo(file);
		
		thingId = gatewayInfo.thingId;
		streamConfig = gatewayInfo.streamConfig;
		registeredThing = gatewayInfo.registeredThing;
		autoReconnect = gatewayInfo.autoReconnect;
		
		if (streamConfig == null)
			return;
		
		chatClient = createChatClient();
		
		loraGateway = chatClient.createApi(ILoraGateway.class);
		loraGateway.setCommunicator(gatewayCommunicator);
		IConcentrator concentrator = loraGateway.getConcentrator();
		concentrator.setNodes(gatewayInfo.nodes);
		
		List<LanNode> nodes = new ArrayList<>();
		for (LanNode node : gatewayInfo.nodes.values()) {
			nodes.add(node);
		}
		
		Collections.sort(nodes, new Comparator<LanNode>() {
			@Override
			public int compare(LanNode o1, LanNode o2) {
				return o1.getLanId() - o2.getLanId();
			}
		});
		
		ILoraDacService<?> loraDacService = loraGateway.getDacService();
		for (LanNode node : nodes) {
			String thingModel = node.getModel();
			if (thingModel == null)
				thingModel = getThingModel(node.getThingId());
			
			AbstractLoraThingEmulatorFactory<?> thingFactory = findThingFactoryByModel(thingModel);
			if (thingFactory == null)
				throw new RuntimeException(String.format("Can't find factory for thing. Thing model: %s.", node.getModel()));
			
			IThingEmulator thing = thingFactory.create();
			
			if (!(thing instanceof AbstractLoraThingEmulator)) {
				throw new RuntimeException("Not a lora thing emulator.");
			}
			
			AbstractLoraThingEmulator loraThing = (AbstractLoraThingEmulator)thing;
			
			int instanceIndex = addThingToAllThings(thingFactory, loraThing);			
			
			setThingId(loraThing, node.getThingId());
			showThing(loraThing, getThingInstanceName(thingFactory, instanceIndex), -1, 30 * instanceIndex, 30 * instanceIndex);
			
			try {
				loraThing.addressAllocated(loraDacService.getGatewayUplinkAddress(),
						loraDacService.getGatewayDownlinkAddress(), LoraAddress.parse(node.getAddress()));
			} catch (BadAddressException e) {
				throw new RuntimeException(String.format("Invalid LORA address string: %s.", node.getAddress()));
			}
			
			setDacState(loraThing, AbstractLoraThingEmulator.DacState.CONFIGURED);
			
			if (node.isConfirmed())
				loraThing.nodeAdded(node.getLanId());
			
			loraThing.getPanel().updateStatus(loraThing.getThingStatus());
			
			loraThing.powerOn();
			
			changeGatewayStatusAndRunUiRefreshThread();				
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JInternalFrame selectedFrame = Gateway.this.desktop.getSelectedFrame();				
					selectedFrame.addComponentListener(Gateway.this);
					selectedFrame.addInternalFrameListener(Gateway.this);			
				}
			});
			
			if (logConsolesDialog != null) {
				logConsolesDialog.createThingLogConsole(loraThing);
			}
		}
		
		refreshGatewayInstanceRelativeMenus();
		
		setConfigFile(file);
	}
	
	private void setDacState(AbstractLoraThingEmulator thing, DacState configured) {
		Field fDacState;
		try {
			fDacState = AbstractLoraThingEmulator.class.getDeclaredField("dacState");
		} catch (Exception e) {
			throw new RuntimeException("Can't fetch field 'dacState' from class AbstractLoraThingEmulator.", e);
		}
		
		boolean accessiable = fDacState.isAccessible();
		try {
			if (!accessiable)
				fDacState.setAccessible(true);
			
			fDacState.set(thing, configured);
		} catch (Exception e) {
			throw new RuntimeException("Can't set field 'dacState' to thing.", e);
		} finally {
			if (!accessiable)
				fDacState.setAccessible(accessiable);
		}
	}

	private String getThingModel(String thingId) {
		for (IThingEmulatorFactory<?> thingFactory : thingFactories) {
			if (thingId.startsWith(thingFactory.getThingModel()))
				return thingFactory.getThingModel();
		}
		
		return null;
	}

	private void setThingId(AbstractThing thing, String thingId) {
		Field fThingId;
		try {
			fThingId = AbstractThing.class.getDeclaredField("thingId");
		} catch (Exception e) {
			throw new RuntimeException("Can't fetch field 'thingId' from class AbstractThing.", e);
		}
		
		boolean accessiable = fThingId.isAccessible();
		try {
			if (!accessiable)
				fThingId.setAccessible(true);
			
			fThingId.set(thing, thingId);
		} catch (Exception e) {
			throw new RuntimeException("Can't set field 'thingId' to thing.", e);
		} finally {
			if (!accessiable)
				fThingId.setAccessible(accessiable);
		}
	}
	
	private void refreshConnectionStateRelativeMenus() {
		if (isConnected()) {
			UiUtils.getMenuItem(menuBar, MENU_NAME_FILE, MENU_ITEM_NAME_NEW).setEnabled(true);
			
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_CONNECT).setEnabled(false);
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_DISCONNECT).setEnabled(true);
			
			refreshGatewayInstanceRelativeMenus();
		} else {
			UiUtils.getMenuItem(menuBar, MENU_NAME_FILE, MENU_ITEM_NAME_NEW).setEnabled(false);
			
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_DISCONNECT).setEnabled(false);			
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_CONNECT).setEnabled(true);
		}
	}

	private void refreshGatewayInstanceRelativeMenus() {
		if (registeredThing != null) {
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_REGISTER).setEnabled(false);
			
			if (!isConnected()) {
				UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_CONNECT).setEnabled(true);
				UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_DISCONNECT).setEnabled(false);
			} else {
				UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_DISCONNECT).setEnabled(true);
				UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_CONNECT).setEnabled(false);
			}
		}
	}
	
	private void setConfigFile(File file) {
		if (configFile == null && file != null) {
			UiUtils.getMenuItem(menuBar, MENU_NAME_FILE, MENU_ITEM_NAME_SAVE_AS).setEnabled(true);
		}
		
		configFile = file;
	}

	private GatewayInfo readGatewayInfo(File file) {
		GatewayInfo gatewayInfo = new GatewayInfo();
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(file));
			
			gatewayInfo.thingId = (String)input.readObject();
			StreamConfigInfo streamConfigInfo = (StreamConfigInfo)input.readObject();
			gatewayInfo.streamConfig = streamConfigInfo == null ? null : createStreamConfig(streamConfigInfo);
			
			gatewayInfo.registeredThing = (RegisteredThing)input.readObject();
			
			gatewayInfo.autoReconnect = input.readBoolean();
			
			int nodesSize = input.readInt();
			if (nodesSize > 0) {				
				gatewayInfo.nodes = new LinkedHashMap<>();
				for (int i = 0; i < nodesSize; i++) {
					LanNode node = (LanNode)input.readObject();
					gatewayInfo.nodes.put(node.getLanId(), node);
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(String.format("Gateway info file %s doesn't exist.", file.getPath()));
		} catch (IOException e) {
			throw new RuntimeException("Can't load gateway info file.", e);
		} catch (ClassNotFoundException e) {
			// ???
			e.printStackTrace();
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return gatewayInfo;
	}
	
	private StandardStreamConfig createStreamConfig(StreamConfigInfo streamConfigInfo) {
		StandardStreamConfig streamConfig = new StandardStreamConfig(streamConfigInfo.host, streamConfigInfo.port);
		streamConfig.setTlsPreferred(streamConfigInfo.tlsPreferred);
		
		return streamConfig;
	}

	private class GatewayInfo {
		private String thingId;
		private StandardStreamConfig streamConfig;
		private RegisteredThing registeredThing;
		private boolean autoReconnect;
		private Map<Integer, LanNode> nodes;
	}

	private void createNewThing() {
		String thingInfo = (String)JOptionPane.showInputDialog(this, "Choose thing you want to create",
				"Choose thing", JOptionPane.QUESTION_MESSAGE, null, getThingInfos(), null);
		if (thingInfo == null)
			return;
		
		createThing(thingInfo);
		
		refreshDacModeRelativeMenus();
	}
	
	private AbstractLoraThingEmulator createThing(String thingInfo) {
		AbstractLoraThingEmulatorFactory<?> thingFactory = getThingFactory(thingInfo);
		IThingEmulator thing = thingFactory.create();
		
		if (!(thing instanceof AbstractLoraThingEmulator)) {
			throw new RuntimeException("Not a lora thing emulator.");
		}
		
		AbstractLoraThingEmulator loraThing = (AbstractLoraThingEmulator)thing;
		int instanceIndex = addThingToAllThings(thingFactory, loraThing);
		
		showThing(loraThing, getThingInstanceName(thingFactory, instanceIndex), -1, 30 * instanceIndex, 30 * instanceIndex);
		changeGatewayStatusAndRunUiRefreshThread();				
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JInternalFrame selectedFrame = Gateway.this.desktop.getSelectedFrame();				
				selectedFrame.addComponentListener(Gateway.this);
				selectedFrame.addInternalFrameListener(Gateway.this);			
			}
		});
		
		if (logConsolesDialog != null) {
			logConsolesDialog.createThingLogConsole(loraThing);
		}
		
		thing.powerOn();
		
		return loraThing;
	}

	private int addThingToAllThings(AbstractLoraThingEmulatorFactory<?> thingFactory,
			AbstractLoraThingEmulator loraThing) {
		List<AbstractLoraThingEmulator> things = getThings(thingFactory);
		
		int instanceIndex = things.size();
		things.add(loraThing);
		return instanceIndex;
	}

	private void showThing(AbstractLoraThingEmulator thing, String title, int layer, int x, int y) {
		showThing(thing, title, layer, x, y, true);
	}

	private void showThing(AbstractLoraThingEmulator thing, String title, int layer, int x, int y, boolean selected) {
		AbstractThingEmulatorPanel<?> thingPanel = thing.getPanel();
		ThingInternalFrame internalFrame = new ThingInternalFrame(thing, title);
		internalFrame.addComponentListener(this);
		internalFrame.setBounds(x, y, thingPanel.getPreferredSize().width, thingPanel.getPreferredSize().height);
		internalFrame.setVisible(true);
		
		desktop.add(internalFrame);
		
		try {
			if (layer != -1 ) {
				internalFrame.setLayer(layer);
			}
			internalFrame.setSelected(selected);
			// Deactivated event is only fired when calling internalFrame.setSelected(true).
			// So we need to call internalFrameActivated() method manually.
			internalFrameActivated(null);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		
		if (thing instanceof AbstractLoraThingEmulator)
			thingPanel.updateStatus(((AbstractLoraThingEmulator)thing).getThingStatus());
	}
	
	private void changeGatewayStatusAndRunUiRefreshThread() {
		try {
			SwingUtilities.invokeLater(new GatewayStatusChanger());
		} catch (Exception e) {
			throw new RuntimeException("Can't add component listener to thing internal frame.");
		}
	}
	
	private class GatewayStatusChanger implements Runnable {
		@Override
		public void run() {
			refreshPowerRelativeMenus();
		}
	}

	private String getThingInstanceName(IThingEmulatorFactory<?> factory, int thingsIndex) {
		return factory.getDescription() + " #" + thingsIndex;
	}

	private List<AbstractLoraThingEmulator> getThings(IThingEmulatorFactory<?> factory) {
		List<AbstractLoraThingEmulator> things = allThings.get(factory.getThingModel());
		if (things == null) {
			things = new ArrayList<>();
			allThings.put(factory.getThingModel(), things);
		}
		
		return things;
	}

	private AbstractLoraThingEmulatorFactory<?> getThingFactory(String thingInfo) {
		int dashIndex = thingInfo.indexOf(" - ");
		String thingModel = thingInfo.substring(0, dashIndex);
		for (AbstractLoraThingEmulatorFactory<?> thingFactory : thingFactories) {
			if (thingFactory.getThingModel().equals(thingModel))
				return thingFactory;
		}
		
		throw new IllegalArgumentException(String.format("Illegal thing model: %s.", thingModel));
	}

	private Object[] getThingInfos() {
		if (thingFactories.isEmpty())
			throw new IllegalStateException("No thing factory registered.");
		
		Object[] thingInfos = new Object[thingFactories.size()];
		
		for (int i = 0; i < thingFactories.size(); i++) {
			thingInfos[i] = thingFactories.get(i).getThingModel() + " - " + thingFactories.get(i).getDescription();
		}
		
		return thingInfos;
	}

	protected JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(createToolsMenu());
		menuBar.add(createHelpMenu());
		
		return menuBar;
    }

	private JMenu createEditMenu() {
		JMenu editMenu = new JMenu(MENU_TEXT_EDIT);
		editMenu.setName(MENU_NAME_EDIT);
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		editMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_POWER_ON, MENU_ITEM_TEXT_POWER_ON, -1, null, this, false));
		editMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_POWER_OFF, MENU_ITEM_TEXT_POWER_OFF, -1, null, this, false));
		editMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_RESET, MENU_ITEM_TEXT_RESET, -1, null, this, false));

		editMenu.addSeparator();

		editMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_DELETE, MENU_ITEM_TEXT_DELETE, -1, null, this, false));
		
		return editMenu;
	}
	
	private JMenu createToolsMenu() {
		JMenu toolsMenu = new JMenu(MENU_TEXT_TOOLS);
		toolsMenu.setName(MENU_NAME_TOOLS);
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		
		toolsMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_REGISTER, MENU_ITEM_TEXT_REGISTER, -1, null, this));
		
		toolsMenu.addSeparator();
		
		toolsMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_CONNECT, MENU_ITEM_TEXT_CONNECT, -1, null, this, false));
		toolsMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_DISCONNECT, MENU_ITEM_TEXT_DISCONNECT, -1, null, this, false));
		
		toolsMenu.addSeparator();
		
		createSetToAddressConfigurationModeMenuItem();
		toolsMenu.add(dacModeMenuItem);
		toolsMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_RECONFIGURE_ADDRESS,
				MENU_ITEM_TEXT_RECONFIGURE_ADDRESS, -1, null, this, false));
		
		toolsMenu.addSeparator();
		
		toolsMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_SHOW_LOG_CONSOLE, MENU_ITEM_TEXT_SHOW_LOG_CONSOLE, -1, null, this));
		
		return toolsMenu;
	}

	private void createSetToAddressConfigurationModeMenuItem() {
		dacModeMenuItem = new JCheckBoxMenuItem(
				MENU_ITEM_TEXT_DAC_MODE, false);
		dacModeMenuItem.addItemListener(new ItemListener() {	
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!isConnected())
					return;
				
				if (dacModeMenuItem.isSelected()) {
					setToDacMode();
				} else {
					setToRouterMode();
				}
				
				updateStatus();
			}
		});
		dacModeMenuItem.setEnabled(false);
	}

	private JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu(MENU_TEXT_HELP);
		helpMenu.setName(MENU_NAME_HELP);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		helpMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_ABOUT, MENU_ITEM_TEXT_ABOUT, -1, null, this));
		
		helpMenu.addSeparator();
		
		helpMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_GENERATE_QR_CODE_PIC, MENU_ITEM_TEXT_GENERATE_QR_CODE_PIC, -1, null, this));
		
		return helpMenu;
	}

	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu(MENU_TEXT_FILE);
		fileMenu.setName(MENU_NAME_FILE);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		fileMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_NEW, MENU_ITEM_TEXT_NEW, -1,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK), this, false));
		fileMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_OPEN_FILE, MENU_ITEM_TEXT_OPEN_FILE, -1, null, this));
		
		fileMenu.addSeparator();
		
		fileMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_SAVE, MENU_ITEM_TEXT_SAVE, -1,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), this, false));
		fileMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_SAVE_AS, MENU_ITEM_TEXT_SAVE_AS, -1, null, this, false));
		
		fileMenu.addSeparator();
		
		fileMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_QUIT, MENU_ITEM_TEXT_QUIT, -1, null, this));
		
		return fileMenu;
	}

	public void registerThingEmulatorFactory(IThingEmulatorFactory<?> thingFactory) {
		if (!(thingFactory instanceof AbstractLoraThingEmulatorFactory)) {
			throw new IllegalArgumentException("Not a lora thing emulator factory.");
		}
		
		for (IThingEmulatorFactory<?> existedThingFactory : thingFactories) {
			if (existedThingFactory.getClass().getName().equals(thingFactory.getClass().getName())) {
				// Already registered.
				return;
			}
		}
		
		thingFactories.add((AbstractLoraThingEmulatorFactory<?>)thingFactory);
	}

	@Override
	public void componentResized(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		if (e.getSource() instanceof JFrame) {
			quit();
		} else if (e.getSource() instanceof LogConsolesDialog) {
			stopListeningInLogConsole(chatClient);
			
			logConsolesDialog.setVisible(false);
			logConsolesDialog.dispose();
			logConsolesDialog = null;
			
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_SHOW_LOG_CONSOLE).setEnabled(true);
		} else {
			// NO-OP
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void internalFrameOpened(InternalFrameEvent e) {}

	@Override
	public void internalFrameClosing(InternalFrameEvent e) {}

	@Override
	public void internalFrameClosed(InternalFrameEvent e) {}

	@Override
	public void internalFrameIconified(InternalFrameEvent e) {}

	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {}

	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		refreshPowerRelativeMenus();
		refreshThingSelectionRelativeMenus();
	}
	
	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {}
	
	private void refreshThingSelectionRelativeMenus() {
		ThingInternalFrame thingFrame = getSelectedFrame();
		if (thingFrame == null) {
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_RESET).setEnabled(false);
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_RECONFIGURE_ADDRESS).setEnabled(false);
			
			return;
		}
		
		UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_RESET).setEnabled(true);
		
		refreshDacModeRelativeMenus();
	}

	private void refreshPowerRelativeMenus() {
		ThingInternalFrame thingFrame = getSelectedFrame();
		if (thingFrame == null)
			return;
		
		if (thingFrame.getThing().isPowered()) {
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_POWER_ON).setEnabled(false);
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_POWER_OFF).setEnabled(true);
		} else {
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_POWER_OFF).setEnabled(false);
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_POWER_ON).setEnabled(true);
		}
		
		refreshDacModeRelativeMenus();
	}
	
	private void setDirty(boolean dirty) {
		this.dirty = dirty;
		refreshDirtyRelativeMenus(dirty);
	}

	private void refreshDirtyRelativeMenus(boolean dirty) {
		JMenuItem saveMenuItem = UiUtils.getMenuItem(menuBar, MENU_NAME_FILE, MENU_ITEM_NAME_SAVE);
		if (dirty) {
			saveMenuItem.setEnabled(true);
		} else {
			saveMenuItem.setEnabled(false);
		}
	}

	@Override
	public boolean isRegistered() {
		return registeredThing != null;
	}

	@Override
	public boolean isConnected() {
		return chatClient != null && chatClient.isConnected();
	}
	
	@Override
	public final int getLanId() {
		return IConcentrator.LAN_ID_CONCENTRATOR;
	}

	@Override
	public String getThingId() {
		return thingId;
	}
	
	@Override
	public int getBatteryPower() {
		return ALWAYS_FULL_POWER;
	}

	@Override
	public boolean isPowered() {
		return true;
	}

	@Override
	public String getSoftwareVersion() {
		return Constants.SOFTWARE_VERSION;
	}

	@Override
	public synchronized void exceptionOccurred(ConnectionException exception) {
		if (exception.getType() == ConnectionException.Type.CONNECTION_CLOSED ||
				exception.getType() == ConnectionException.Type.IO_ERROR) {
			loraGateway.stop();
			chatClient.close();
			
			refreshConnectionStateRelativeMenus();
			refreshDacModeRelativeMenus();
			updateStatus();
			
			UiUtils.showNotification(this, "Message", "Gateway has disconnected.");
		}
	}
	
	@Override
	public void messageReceived(String message) {
		// NOOP
	}

	@Override
	public void messageSent(String message) {
		// NOOP
	}
	
	@Override
	public void occurred(AddNodeError error, LanNode source) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void nodeAdded(int lanId, LanNode node) {
		boolean found = false;
		for (Collection<AbstractLoraThingEmulator> things : allThings.values()) {
			for (AbstractLoraThingEmulator thing : things) {
				if (thing.getThingId().equals(node.getThingId())) {
					found = true;
					thing.nodeAdded(lanId);
					break;
				}
			}
			
			if (found)
				break;
		}
		
		setDirty(dirty);
	}

	@Override
	public void nodeReset(int lanId, LanNode node) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void nodeRemoved(int lanId, LanNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addressConfigured(String thingId, String registrationCode, LoraAddress address) {
		// NOOP
	}

	@Override
	public void addBatteryPowerListener(IBatteryPowerListener listener) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean removeBatteryPowerListener(IBatteryPowerListener listener) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void heartBeatsReceived(int length) {
		// Do nothing.
	}
	
	@Override
	public String getThingModel() {
		return THING_MODEL;
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() throws ExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void shutdownSystem(boolean restart) throws ExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(JabberId friend, Object event) {
		SwitchStateChanged switchStateChanged = (SwitchStateChanged)event;
		
		if (switchStateChanged.getPrevious() == SwitchState.OFF &&
					switchStateChanged.getCurrent() == SwitchState.ON)
			System.out.println("If I'm a camera. I will start to record 15 seconds video.");
	}
}
