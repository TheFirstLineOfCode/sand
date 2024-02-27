package com.thefirstlineofcode.sand.client.edge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.xmpp.Constants;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.FeatureNotImplemented;
import com.thefirstlineofcode.basalt.xmpp.im.stanza.Presence;
import com.thefirstlineofcode.chalk.core.AuthFailureException;
import com.thefirstlineofcode.chalk.core.IChatClient;
import com.thefirstlineofcode.chalk.core.StandardChatClient;
import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.chalk.core.stream.UsernamePasswordToken;
import com.thefirstlineofcode.chalk.network.ConnectionException;
import com.thefirstlineofcode.chalk.network.IConnectionListener;
import com.thefirstlineofcode.sand.client.ibtr.IRegistration;
import com.thefirstlineofcode.sand.client.ibtr.IbtrPlugin;
import com.thefirstlineofcode.sand.client.ibtr.RegistrationException;
import com.thefirstlineofcode.sand.client.thing.AbstractThing;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;

public abstract class AbstractEdgeThing extends AbstractThing implements IEdgeThing, IConnectionListener {
	private static final String ATTRIBUTE_NAME_STREAM_CONFIG = "stream_config";
	private static final String ATTRIBUTE_NAME_REGISTERED_EDGE_THING = "registered_edge_thing";
	private static final String INTERNET_CONNECTIVITY_TEST_ADDRESS = "http://www.baidu.com";
	private static final String SAND_EDGE_CONFIG_DIR = ".com.thefirstlineofcode.sand.client.edge";
	
	private static final Logger logger = isLogConfiguratorConfigured() ? LoggerFactory.getLogger(AbstractEdgeThing.class) : null;
	
	private static Boolean isLogConfiguratorConfigured;
	
	protected StandardStreamConfig streamConfig;
	protected RegisteredEdgeThing registeredEdgeThing;
	
	protected StandardChatClient chatClient;
	protected Thread autoReconnectThread;
	
	protected List<IEdgeThingListener> edgeThingListeners;
	protected List<IConnectionListener> connectionListeners;
	
	protected boolean started;
	protected boolean stopToReconnect;
	
	protected ConsoleThread consoleThread;
	protected boolean startConsole;
	
	public AbstractEdgeThing(String model) {
		this(model, null, false);
	}
	
	public AbstractEdgeThing(String model, boolean startConsole) {
		this(model, null, startConsole);
	}
	
	public AbstractEdgeThing(String model, StandardStreamConfig streamConfig) {
		this(model, streamConfig, false);
	}
	
	public AbstractEdgeThing(String model, StandardStreamConfig streamConfig, boolean startConsole) {
		super(model);
		
		this.streamConfig = streamConfig;
		this.startConsole = startConsole;
		
		powered = true;
		batteryPower = 100;
		
		edgeThingListeners = new ArrayList<>();
		connectionListeners = new ArrayList<>();
		
		started = false;
		stopToReconnect = true;
	}
	
	private static boolean isLogConfiguratorConfigured() {
		if (isLogConfiguratorConfigured != null)
			return isLogConfiguratorConfigured;
		
		Class<?> logConfiguratorClass;
		try {			
			logConfiguratorClass = Class.forName("com.thefirstlineofcode.chalk.logger.LogConfigurator");
			
			Method mIsConfigured = logConfiguratorClass.getMethod("isConfigured", new Class<?>[] {});
			isLogConfiguratorConfigured = Boolean.TRUE.equals(mIsConfigured.invoke(null, new Object[] {}));
		} catch (Exception e) {
			isLogConfiguratorConfigured = false;
		}
		
		return isLogConfiguratorConfigured;
	}
	
	private String getStreamConfigString() {
		return String.format("%s,%s,%s", streamConfig.getHost(), streamConfig.getPort(), streamConfig.isTlsPreferred() ? "true" : "false");
	}
	
	protected StandardStreamConfig getStreamConfig(Map<String, String> attributes) {
		String sStreamConfig = attributes.get(ATTRIBUTE_NAME_STREAM_CONFIG);
		if (sStreamConfig == null) {
			return null;
		}
		
		StringTokenizer st = new StringTokenizer(sStreamConfig, ",");
		if (st.countTokens() != 3) {
			if (isLogConfiguratorConfigured())
				logger.error("Can't read stream config. Not a valid stream config string.");
			
			throw new IllegalArgumentException("Can't read stream config. Not a valid stream config string.");
		}
		
		StandardStreamConfig streamConfig = createStreamConfig(st);
		streamConfig.setResource(RegisteredEdgeThing.DEFAULT_RESOURCE_NAME);
		
		return streamConfig;
	}
	
	protected StandardStreamConfig createStreamConfig(StringTokenizer st) {
		String host = st.nextToken().trim();
		int port = Integer.parseInt(st.nextToken().trim());
		boolean tlsRequired = Boolean.parseBoolean(st.nextToken().trim());
		
		return new StandardStreamConfig(host, port, tlsRequired);
	}
	
	@Override
	public StandardStreamConfig getStreamConfig() {
		if (streamConfig != null)
			return streamConfig;
		
		if (attributes == null)
			attributes = loadThingAttributes();
		
		if (attributes == null)
			return null;
		
		streamConfig = getStreamConfig(attributes);
		
		return streamConfig;
	}
	
	@Override
	public void setStreamConfig(StandardStreamConfig streamConfig) {
		this.streamConfig = streamConfig;
		
		if (attributes == null)
			attributes = loadThingAttributes();
		
		if (streamConfig == null) {			
			if (attributes != null && attributes.containsKey(ATTRIBUTE_NAME_STREAM_CONFIG)) {
				attributes.remove(ATTRIBUTE_NAME_STREAM_CONFIG);
				saveAttributes(attributes);
			}
			
			return;
		}

		if (attributes == null)
			attributes= new HashMap<>();
		
		attributes.put(ATTRIBUTE_NAME_STREAM_CONFIG, getStreamConfigString());
		saveAttributes(attributes);

	}

	@Override
	public void start() {
		try {
			if (attributes == null)
				attributes = loadThingAttributes();
			
			processAttributes(attributes);
			doStart();
		} catch (Exception e) {
			if (isLogConfiguratorConfigured())
				logger.error("Some thing is wrong. The program can't run correctly.", e);
			
			throw new RuntimeException("Some thing is wrong. The program can't run correctly.", e);
		}
	}
	
	protected void processAttributes(Map<String, String> attributes) {
		if (streamConfig == null) {
			streamConfig = getStreamConfig(attributes);
		} else {
			attributes.put(ATTRIBUTE_NAME_STREAM_CONFIG, getStreamConfigString());
			attributesChanged = true;
		}
		
		registeredEdgeThing = getRegisteredEdgeThing(attributes);
		
		if (doProcessAttributes(attributes))
			attributesChanged = true;
		
		if (attributesChanged)
			saveAttributes(attributes);
	}

	protected void doStart() {
		if (!isPowered())
			return;
		
		if (started)
			stop();
		
		if (streamConfig == null)
			throw new IllegalStateException("Null stream config.");
		
		if (isLogConfiguratorConfigured()) {
			logger.info("I'm an edge thing[thing_id='{}', host='{}', port='{}', tls_preferred='{}'].",
					thingId, this.streamConfig.getHost(), this.streamConfig.getPort(), this.streamConfig.isTlsPreferred());
		} else {
			System.out.println(String.format("I'm an edge thing[thing_id='%s', host='%s', port='%s', tls_preferred='%s'].",
					thingId, this.streamConfig.getHost(), this.streamConfig.getPort(), this.streamConfig.isTlsPreferred()));
		}
		
		if (!isHostLocalLanAddress()) {
			checkInternetConnectivity(10);	
		}
		
		if (!isRegistered()) {
			register();
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!isRegistered())
				return;
		}
		
		synchronized (this) {
			connect();
		}
		
		if (isLogConfiguratorConfigured())
			logger.info("The thing has started.");
		else
			System.out.println("The thing has started.");
		
		started = true;
		
		if (startConsole) {			
			System.out.println("Starting console...");
			startConsoleThread();
		}
	}

	private void checkInternetConnectivity(int retryTimes) {
		int i = 0;
		while (!checkInternetConnectivity()) {
			i++;
			
			if (isLogConfiguratorConfigured())
				logger.info("No internet connection. Waiting for a while then trying again....");
			else
				System.out.println("No internet connection. Waiting for a while then trying again....");				
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (i == retryTimes) {
				if (isLogConfiguratorConfigured())
					logger.error("No internet connection. The thing can't be started.");
				else
					System.out.println("No internet connection. The thing can't be started.");
				
				throw new IllegalStateException("No internet connection. The program will exit.");
			}
		}
	}
	
	private boolean isHostLocalLanAddress() {
		return streamConfig.getHost().equals("localhost") ||
				streamConfig.getHost().equals("127.0.0.1") ||
				streamConfig.getHost().startsWith("192.168.");
	}

	protected boolean checkInternetConnectivity() {
		try {
			URL url = new URL(INTERNET_CONNECTIVITY_TEST_ADDRESS);
			URLConnection connection = url.openConnection();
            connection.connect();
            
            return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void startConsoleThread() {
		consoleThread = new ConsoleThread();
		new Thread(consoleThread, "Thing Console Thread").start();
	}
	
	private class ConsoleThread implements Runnable {
		private volatile boolean stop = false;
		
		@Override
		public void run() {
			printConsoleHelp();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				try {
					String command = in.readLine();;
					
					if (stop)
						break;
					
					if (command == null) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						continue;
					} else if ("help".equals(command)) {
						printConsoleHelp();
					} else if ("exit".equals(command)) {
						stop();
					} else {
						System.out.println(String.format("Unknown command: '%s'", command));
						printConsoleHelp();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void printConsoleHelp() {
			System.out.println("Commands:");
			System.out.println("help        Display help information.");
			System.out.println("exit        Exit program.");
			System.out.print("$");
		}
	}

	@Override
	public void connect() {
		if (chatClient == null) {
			chatClient = createChatClient();
			chatClient.register(EdgeThingPlugin.class);
			registerIotPlugins();
			
			for (IConnectionListener connectionListener : connectionListeners) {
				chatClient.addConnectionListener(connectionListener);
			}
			chatClient.addConnectionListener(this);
		}
		
		if (isLogConfiguratorConfigured())
			logger.info("The thing tries to connect to server.");
		else
			System.out.println("The thing tries to connect to server.");
		
		try {
			chatClient.connect(new UsernamePasswordToken(registeredEdgeThing.getThingName(),
					registeredEdgeThing.getCredentials()));
			
			if (isConnected()) {
				for (IEdgeThingListener edgeThingListener : edgeThingListeners) {
					edgeThingListener.connected(chatClient);
				}
				
				connected(chatClient);
			}
		} catch (ConnectionException e) {
			removeConnectionListenersFromChatClient(chatClient);
			
			for (IEdgeThingListener edgeThingListener : edgeThingListeners) {
				edgeThingListener.FailedToConnect(e);
			}
			
			FailedToConnect(e);
		} catch (AuthFailureException e) {
			removeConnectionListenersFromChatClient(chatClient);
			
			chatClient.close();
			
			for (IEdgeThingListener edgeThingListener : edgeThingListeners) {
				edgeThingListener.failedToAuth(e);
			}
			
			failedToAuth(e);
		}
	}

	protected void removeConnectionListenersFromChatClient(IChatClient chatClient) {
		for (IConnectionListener connectionListener : connectionListeners) {
			chatClient.removeConnectionListener(connectionListener);
		}		
		chatClient.removeConnectionListener(this);
	}

	protected StandardChatClient createChatClient() {
		return new StandardChatClient(createStreamConfigWithResource());
	}

	protected StandardStreamConfig createStreamConfigWithResource() {
		StandardStreamConfig cloned = new StandardStreamConfig(streamConfig.getHost(), streamConfig.getPort());
		cloned.setTlsPreferred(streamConfig.isTlsPreferred());
		cloned.setResource(RegisteredEdgeThing.DEFAULT_RESOURCE_NAME);
		
		return cloned;
	}
	
	protected void startAutoReconnectThread() {
		stopToReconnect = false;
		if (autoReconnectThread != null && autoReconnectThread.isAlive())
			return;
		
		autoReconnectThread = new Thread(new AutoReconnectThread(),
				String.format("%s Auto Reconnect Thread", thingId));			
		
		autoReconnectThread.start();
	}

	protected void registered(RegisteredEdgeThing registeredEdgeThing) {
		attributes.put(ATTRIBUTE_NAME_REGISTERED_EDGE_THING, getRegisteredEdgeThingString(registeredEdgeThing));
		saveAttributes(attributes);
		
		this.registeredEdgeThing = registeredEdgeThing;
		
		if (isLogConfiguratorConfigured())
			logger.info("The edge thing has registered. Thing name is '{}'.", registeredEdgeThing.getThingName());
		else
			System.out.println(String.format("The edge thing has registered. Thing name is '%s'.", registeredEdgeThing.getThingName()));
	}
	
	@Override
	public void stop() {
		if (!isPowered())
			return;
		
		if (!started)
			return;
		
		stopAutoReconnectThread();
		
		synchronized (this) {
			disconnect();
		}
		
		if (isLogConfiguratorConfigured())
			logger.info("The thing has stopped.");
		else
			System.out.println("The thing has stopped.");
		
		started = false;
		stopConsoleThread();
	}

	private void stopConsoleThread() {
		if (consoleThread != null) {
			consoleThread.stop = true;
		}
	}
	
	protected void disconnect() {
		if (isConnected()) {
			chatClient.close();
		}
		
		for (IEdgeThingListener edgeThingListener : edgeThingListeners) {
			edgeThingListener.disconnected();
		}
		disconnected();
	}

	protected void stopAutoReconnectThread() {
		stopToReconnect = true;
		while (autoReconnectThread != null &&
				autoReconnectThread.isAlive()) {			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		autoReconnectThread = null;
	}
	
	@Override
	public boolean isRegistered() {
		return registeredEdgeThing != null;
	}
	
	@Override
	public void register() {
		IChatClient chatClient = new StandardChatClient(streamConfig);
		chatClient.register(IbtrPlugin.class);
		
		if (isLogConfiguratorConfigured())
			logger.info("The thing tries to register to server.");
		else
			System.out.println("The thing tries to register to server.");
		
		IRegistration registration = null;
		try {
			registration = chatClient.createApi(IRegistration.class);
			for (IConnectionListener listener : connectionListeners) {
				registration.addConnectionListener(listener);
			}
			registration.addConnectionListener(this);
			
			registeredEdgeThing = registration.register(thingId, loadRegistrationCode());
			if (registeredEdgeThing == null)
				return;
			
			registered(registeredEdgeThing);
			for (IEdgeThingListener edgeThingListener : edgeThingListeners) {
				edgeThingListener.registered(registeredEdgeThing);
			}
		} catch (RegistrationException e) {
			for (IEdgeThingListener edgeThingListener : edgeThingListeners) {
				edgeThingListener.registerExceptionOccurred(e);
			}
			registrationExceptionOccurred(e);
		} finally {
			if (registration != null) {
				for (IConnectionListener listener : connectionListeners) {
					registration.removeConnectionListener(listener);
				}
				registration.removeConnectionListener(this);
			}
			
			chatClient.close();
		}
	}
	
	private String getRegisteredEdgeThingString(RegisteredEdgeThing registeredEdgeThing) {
		return String.format("%s,%s,%s", registeredEdgeThing.getThingName(),
				registeredEdgeThing.getCredentials(), BinaryUtils.encodeToBase64(registeredEdgeThing.getSecretKey()));
	}

	@Override
	public synchronized boolean isConnected() {
		return chatClient != null && chatClient.isConnected();
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isStopped() {
		return !started;
	}
	
	@Override
	public void addEdgeThingListener(IEdgeThingListener edgeThingListener) {
		if (!edgeThingListeners.contains(edgeThingListener))
			edgeThingListeners.add(edgeThingListener);
	}
	
	@Override
	public boolean removeEdgeThingListener(IEdgeThingListener edgeThingListener) {
		return edgeThingListeners.remove(edgeThingListener);
	}
	
	@Override
	public void addConnectionListener(IConnectionListener connectionListener) {
		if (!connectionListeners.contains(connectionListener))
			connectionListeners.add(connectionListener);
	}

	@Override
	public boolean removeConnectionListener(IConnectionListener connectionListener) {
		return connectionListeners.remove(connectionListener);
	}
	
	@Override
	public void messageReceived(String message) {}

	@Override
	public void heartBeatsReceived(int length) {}

	@Override
	public void messageSent(String message) {}
	
	@Override
	public void exceptionOccurred(ConnectionException exception) {
		disconnect();
	}
	
	private class AutoReconnectThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (stopToReconnect)
					return;
				
				synchronized (AbstractEdgeThing.this) {
					if (!isConnected()) {
						if (isLogConfiguratorConfigured())
							logger.info("The thing has disconnected. Try to reconnect to server....");
						else
							System.out.println("The thing has disconnected. Try to reconnect to server....");
						
						connect();
					}
				}
				
				for (int i = 0; i < 10; i++) {
					if (stopToReconnect)
						return;
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	protected void connected(IChatClient chatClient) {
		if (isLogConfiguratorConfigured())
			logger.info("The thing has connected to server.");
		else
			System.out.println("The thing has connected to server.");
		
		startIotComponents();
		startAutoReconnectThread();
		
		// Initial presence
		chatClient.getChatServices().getPresenceService().send(new Presence());
	}

	protected void FailedToConnect(ConnectionException e) {
		if (isLogConfiguratorConfigured()) {
			logger.error("The thing failed to connect to server.", e);
		} else {
			System.out.println("The thing failed to connect to server.");
			e.printStackTrace();
		}
	}
	
	protected void failedToAuth(AuthFailureException e) {
		if (isLogConfiguratorConfigured())
			logger.error("The thing failed to auth to server.", e);
		
		throw new RuntimeException("Failed to auth to server.", e);
	}
	
	protected void disconnected() {
		stopIotComponents();
		
		if (isLogConfiguratorConfigured())
			logger.info("The thing has disconnected from server.");
		else
			System.out.println("The thing has disconnected from server.");
	}
	
	protected void registrationExceptionOccurred(RegistrationException e) {
		if (isLogConfiguratorConfigured()) {
			logger.error("Registration exception occurred.", e);
		} else {
			System.out.println("Registration exception occurred.");
			e.printStackTrace();
		}
	}
	
	protected RegisteredEdgeThing getRegisteredEdgeThing(Map<String, String> attributes) {
		String sRegisteredEdgeThing = attributes.get(ATTRIBUTE_NAME_REGISTERED_EDGE_THING);
		if (sRegisteredEdgeThing == null)
			return null;
		
		StringTokenizer st = new StringTokenizer(sRegisteredEdgeThing, ",");
		if (st.countTokens() != 3)
			throw new RuntimeException("Invalid registered edge thing string!");
			
		RegisteredEdgeThing registeredEdgeThing = new RegisteredEdgeThing();
		registeredEdgeThing.setThingName(st.nextToken().trim());
		registeredEdgeThing.setCredentials(st.nextToken().trim());
		registeredEdgeThing.setSecretKey(BinaryUtils.decodeFromBase64(st.nextToken().trim()));
		
		return registeredEdgeThing;
	}
	
	@Override
	public void shutdownSystem(boolean restart) throws ExecutionException {
		if (!isLinux()) {
			throw new ProtocolException(new FeatureNotImplemented("Shutdown system action only supported on linux platform."));
		}
		
		runInNewProcess(getShutdownCmdArray(restart));
	}

	private String[] getShutdownCmdArray(boolean restart) {
		List<String> cmdList = new ArrayList<>();
		cmdList.add("sudo");
		cmdList.add("shutdown");
		if (restart) {
			cmdList.add("-r");
		} else {
			cmdList.add("-h");
		}
		cmdList.add("now");
		
		String[] cmdArray = cmdList.toArray(new String[0]);
		return cmdArray;
	}

	protected void runInNewProcess(String[] cmdArray) {
		try {
			ProcessBuilder pb = new ProcessBuilder(cmdArray).
					redirectInput(Redirect.INHERIT).
					redirectError(Redirect.INHERIT).
					redirectOutput(Redirect.INHERIT);
			Map<String, String> env = pb.environment();
			for (String key : System.getenv().keySet()) {
				env.put(key, System.getenv(key));
			}
			
			Process process = pb.start();
			process.waitFor();
		} catch (IOException e) {
			throw new RuntimeException("Can't run runtime process.", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Runtime process execution error.", e);
		}
	}
	
	protected boolean isLinux() {
		return "Linux".equals(System.getProperty("os.name"));
	}

	@Override
	public IChatClient getChatClient() {
		return chatClient;
	}
	
	@Override
	protected Map<String, String> loadThingAttributes() {
		Path attributesFilePath = getAttributesFilePath();
		
		if (!Files.exists(attributesFilePath, LinkOption.NOFOLLOW_LINKS)) {
			if (isLogConfiguratorConfigured())
				logger.info("Attributes file not existed. Ignore to load attributes.");
			else
				System.out.println("Attributes file not existed. Ignore to load attributes.");
			
			return null;
		}
		
		Properties properties = new Properties();
		Reader reader = null;
		try {
			reader = Files.newBufferedReader(attributesFilePath, Charset.forName(Constants.DEFAULT_CHARSET));
			properties.load(reader);			
		} catch (Exception e) {
			if (isLogConfiguratorConfigured())
				logger.error("Can't load attributes from file '{}'.", attributesFilePath.toAbsolutePath(), e);
			
			throw new RuntimeException(String.format("Can't load attributes from file '%s'.",
					attributesFilePath.toAbsolutePath()), e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		Map<String, String> attributes = new HashMap<>();
		for (String propertyName : properties.stringPropertyNames()) {
			attributes.put(propertyName, properties.getProperty(propertyName));
		}
		
		return attributes;
	}
	
	@Override
	protected void saveAttributes(Map<String, String> attributes) {
		Properties properties = new Properties();
		for (String attributeName : attributes.keySet()) {
			properties.put(attributeName, attributes.get(attributeName));
		}
		
		Path attributesFilePath = getAttributesFilePath();
		Path attributesBakFilePath = Paths.get(attributesFilePath.getParent().toAbsolutePath().toString(), attributesFilePath.toFile().getName() + ".bak");
		if (Files.exists(attributesFilePath, LinkOption.NOFOLLOW_LINKS)) {			
			try {
				Files.move(attributesFilePath, attributesBakFilePath);
			} catch (IOException e) {
				if (isLogConfiguratorConfigured())
					logger.error("Can't backup attributes file.", e);
				
				throw new RuntimeException("Can't backup attributes file.", e);
			}
		}
		
		if (!Files.exists(attributesFilePath.getParent(), LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createDirectories(attributesFilePath.getParent());
			} catch (IOException e) {
				if (isLogConfiguratorConfigured())
					logger.error("Can't create directory {}.", attributesFilePath.getParent().toAbsolutePath(), e);
				
				throw new RuntimeException(String.format("Can't create directory %s.", attributesFilePath.getParent().toAbsolutePath()), e);
			}
		}
		
		Writer writer = null;
		try {
			writer = Files.newBufferedWriter(attributesFilePath, Charset.forName(Constants.DEFAULT_CHARSET),
					StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			properties.store(writer, null);
			
			if (isLogConfiguratorConfigured())
				logger.info("Attributes are saved to {}.", attributesFilePath.toAbsolutePath());
			else
				System.out.println(String.format("Attributes are saved to %s.", attributesFilePath.toAbsolutePath()));
		} catch (Exception e) {
			if (isLogConfiguratorConfigured())
				logger.error("Can't save attributes to file '{}'.", attributesFilePath.toAbsolutePath(), e);
			
			throw new RuntimeException(String.format("Can't save attributes to file '%s'.",
					attributesFilePath.toAbsolutePath()), e);
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		if (Files.exists(attributesBakFilePath, LinkOption.NOFOLLOW_LINKS)) {			
			try {
				Files.delete(attributesBakFilePath);
			} catch (IOException e) {
				if (isLogConfiguratorConfigured())
					logger.error("Can't delete attributes backup file.", e);
				
				throw new RuntimeException("Can't delete attributes backup file.", e);
			}
		}
	}
	
	protected Path getAttributesFilePath() {
		String userHome = System.getProperty("user.home");
		Path attributesFilePath = Paths.get(userHome, SAND_EDGE_CONFIG_DIR + "/" + model + "-" + "attributes.properties");
		
		return attributesFilePath;
	}
	
	protected boolean doProcessAttributes(Map<String, String> attributes) {
		return false;
	}
	
	protected abstract void registerIotPlugins();
	protected abstract void startIotComponents();
	protected abstract void stopIotComponents();
}
