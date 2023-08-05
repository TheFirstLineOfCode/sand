package com.thefirstlineofcode.sand.client.thing.obx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.thefirstlineofcode.basalt.oxm.IOxmFactory;
import com.thefirstlineofcode.basalt.oxm.OxmService;
import com.thefirstlineofcode.basalt.oxm.binary.AbstractBinaryXmppProtocolConverter;
import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.binary.BinaryXmppProtocolConverter;
import com.thefirstlineofcode.basalt.oxm.binary.BxmppConversionException;
import com.thefirstlineofcode.basalt.oxm.binary.BxmppExtension;
import com.thefirstlineofcode.basalt.oxm.binary.DefaultBxmppExtension;
import com.thefirstlineofcode.basalt.oxm.binary.IBinaryXmppProtocolConverter;
import com.thefirstlineofcode.basalt.oxm.binary.Namespace;
import com.thefirstlineofcode.basalt.oxm.binary.ReduplicateBxmppReplacementException;
import com.thefirstlineofcode.basalt.oxm.binary.ReplacementBytes;
import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.CocTranslatorFactory;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.parsing.FlawedProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.MessageProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.basalt.xmpp.im.stanza.Message;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.actuator.LanExecution;
import com.thefirstlineofcode.sand.protocols.actuator.oxm.LanExecutionParserFactory;
import com.thefirstlineofcode.sand.protocols.actuator.oxm.LanExecutionTranslatorFactory;
import com.thefirstlineofcode.sand.protocols.sensor.LanReport;
import com.thefirstlineofcode.sand.protocols.sensor.oxm.LanReportParserFactory;
import com.thefirstlineofcode.sand.protocols.sensor.oxm.LanReportTranslatorFactory;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanNotification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.oxm.LanNotificationParserFactory;
import com.thefirstlineofcode.sand.protocols.thing.tacp.oxm.LanNotificationTranslatorFactory;

public class ObxFactory implements IObxFactory {
	private static final byte[] MESSAGE_WRAPPER_DATA = new byte[] {(byte)0x60, (byte)0, (byte)1};
	
	private static ObxFactory instance;
	
	private IOxmFactory oxmFactory;
	private AbstractBinaryXmppProtocolConverter<?> binaryXmppProtocolConverter;
	private List<Class<?>> registeredObjectTypes;
	private List<Class<?>> registeredLanActionTypes;
	private List<Class<?>> registeredLanSupportedEventTypes;
	private List<Class<?>> registeredLanFollowedEventTypes;
	private List<Class<?>> registeredLanDataTypes;
	
	private ObxFactory() {
		oxmFactory = OxmService.createStandardOxmFactory();
		registeredObjectTypes = new ArrayList<>();
		registeredLanActionTypes = new ArrayList<>();
		registeredLanSupportedEventTypes = new ArrayList<>();
		registeredLanFollowedEventTypes = new ArrayList<>();
		registeredLanDataTypes = new ArrayList<>();
		
		String[] sandConfigFiles = loadBxmppExtensionConfigurations("META-INF/sand-bxmpp-extensions.txt");
		String[] appConfigFiles = loadBxmppExtensionConfigurations("META-INF/iot-lan-bxmpp-extensions.txt");
		
		List<String> allConfigFiles = new ArrayList<>();
		allConfigFiles.addAll(Arrays.asList(sandConfigFiles));
		allConfigFiles.addAll(Arrays.asList(appConfigFiles));
		
		binaryXmppProtocolConverter = createBinaryXmppProtocolConverter(allConfigFiles.toArray(new String[allConfigFiles.size()]));
		
		registerPredefinedProtocols();
	}
	
	public static IObxFactory getInstance() {
		if (instance == null)
			instance = new ObxFactory();
		
		return instance;
	}
	
	private void registerPredefinedProtocols() {
		ProtocolChain lanExecuteProtocolChain = new MessageProtocolChain(LanExecution.PROTOCOL);
		oxmFactory.register(lanExecuteProtocolChain, new LanExecutionParserFactory());
		oxmFactory.register(LanExecution.class, new LanExecutionTranslatorFactory());
		
		ProtocolChain lanNotificationProtocolChain = new MessageProtocolChain(LanNotification.PROTOCOL);
		oxmFactory.register(lanNotificationProtocolChain, new LanNotificationParserFactory());
		oxmFactory.register(LanNotification.class, new LanNotificationTranslatorFactory());
		
		ProtocolChain lanReportProtocolChain = new MessageProtocolChain(LanReport.PROTOCOL);
		oxmFactory.register(lanReportProtocolChain, new LanReportParserFactory());
		oxmFactory.register(LanReport.class, new LanReportTranslatorFactory());
	}
	
	private String[] loadBxmppExtensionConfigurations(String extensionsConfigurationFile) {
		URL bxmppExtensionsConfigurationFile = getClass().getClassLoader().getResource(extensionsConfigurationFile);
		if (bxmppExtensionsConfigurationFile == null)
			return new String[0];
		
		List<String> configurationFiles = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(bxmppExtensionsConfigurationFile.openStream()));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				configurationFiles.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return configurationFiles.toArray(new String[configurationFiles.size()]); 
	}
	
	private AbstractBinaryXmppProtocolConverter<?> createBinaryXmppProtocolConverter(String[] configFiles) {
		AbstractBinaryXmppProtocolConverter<?> binaryXmppProtocolConverter = new BinaryXmppProtocolConverter();
		try {
			if (configFiles == null || configFiles.length == 0)
				return binaryXmppProtocolConverter;
			
			DefaultBxmppExtension defaultBxmppExtension = new DefaultBxmppExtension();
			loadBxmppCoreExtension(defaultBxmppExtension);
			loadBxmppImExtension(defaultBxmppExtension);
			
			binaryXmppProtocolConverter.register(defaultBxmppExtension);
			
			for (String configFile : configFiles) {
				BxmppExtension bxmppExtension = loadBxmppExtensionFromPropertiesFile("META-INF/" + configFile);
				binaryXmppProtocolConverter.register(bxmppExtension);
			}
			
			return binaryXmppProtocolConverter;
		} catch (IOException e) {
			throw new RuntimeException("IO exception.", e);
		} catch (ReduplicateBxmppReplacementException e) {
			throw new RuntimeException("Reduplicate BXMPP replacement.", e);
		}
	}

	private void loadBxmppCoreExtension(BxmppExtension defaultBxmppExtension)
			throws IOException, ReduplicateBxmppReplacementException {
		URL bxmppCoreUrl = getClass().getClassLoader().getResource("META-INF/bxmpp-core.properties");
		if (bxmppCoreUrl == null) {
			throw new RuntimeException("bxmpp-core properties file not found.");
		}
		
		Properties properties = new Properties();
		properties.load(new BufferedReader(new InputStreamReader(bxmppCoreUrl.openStream())));
		
		String provider = "BXMPP-Core";
		for (Object oRepalcementByte : properties.keySet()) {
			String sReplacementBytes = (String)oRepalcementByte;
			String keyword = (String)properties.get(sReplacementBytes);
			
			ReplacementBytes replacementBytes = ReplacementBytes.parse(sReplacementBytes);				
			replacementBytes.setProvider(provider);
			
			if (!ReplacementBytes.isNamespaceReplacementBytes(replacementBytes)) {
				defaultBxmppExtension.register(replacementBytes, keyword);
			}
		}
	}
	
	private void loadBxmppImExtension(BxmppExtension defaultBxmppExtension)
			throws IOException, ReduplicateBxmppReplacementException {
		URL bxmppImUrl = getClass().getClassLoader().getResource("META-INF/bxmpp-im.properties");
		if (bxmppImUrl == null) {
			throw new RuntimeException("bxmpp-im properties file not found.");
		}
		
		Properties properties = new Properties();
		properties.load(new BufferedReader(new InputStreamReader(bxmppImUrl.openStream())));
		
		String provider = "BXMPP-IM";
		for (Object oRepalcementByte : properties.keySet()) {
			String sReplacementBytes = (String)oRepalcementByte;
			String keyword = (String)properties.get(sReplacementBytes);
			
			ReplacementBytes replacementBytes = ReplacementBytes.parse(sReplacementBytes);				
			replacementBytes.setProvider(provider);
			
			if (!ReplacementBytes.isNamespaceReplacementBytes(replacementBytes)) {
				defaultBxmppExtension.register(replacementBytes, keyword);
			}
		}
	}
	
	private BxmppExtension loadBxmppExtensionFromPropertiesFile(String configurationFilePath)
			throws IOException, ReduplicateBxmppReplacementException {
		URL configurationFileUrl = getClass().getClassLoader().getResource(configurationFilePath);
		String provider = getProvider(configurationFileUrl);
		
		Properties properties = new Properties();
		properties.load(new BufferedReader(new InputStreamReader(configurationFileUrl.openStream())));
		
		Namespace namespace = findNamespace(properties);
		if (namespace == null)
			throw new RuntimeException("Can't find namespace in BXMPP extension configuration file.");
		
		BxmppExtension bxmppExtension = new BxmppExtension(namespace);
		for (Object oRepalcementByte : properties.keySet()) {
			String sReplacementBytes = (String)oRepalcementByte;
			String keyword = (String)properties.get(sReplacementBytes);
			
			ReplacementBytes replacementBytes = ReplacementBytes.parse(sReplacementBytes);				
			replacementBytes.setProvider(provider);
			
			if (!ReplacementBytes.isNamespaceReplacementBytes(replacementBytes)) {
				bxmppExtension.register(replacementBytes, keyword);
			}
		}
		
		return bxmppExtension;
	}
	
	private Namespace findNamespace(Properties properties) {
		for (Object oRepalcementByte : properties.keySet()) {
			String sReplacementBytes = (String)oRepalcementByte;
			byte[] bytes = BinaryUtils.getBytesFromHexString(sReplacementBytes);
			if (bytes.length == 2)
				return new Namespace(new ReplacementBytes(bytes[0], bytes[1]), properties.getProperty(sReplacementBytes));
		}
		
		return null;
	}

	private String getProvider(URL url) {
		String path = url.getPath();
		int fileNameEndPosition = path.indexOf(".jar!/");
		if (fileNameEndPosition == -1) {
			fileNameEndPosition = path.indexOf("/target/classes/META-INF/");
		}
		
		path = path.substring(0, fileNameEndPosition);
		int fileNameStartPosition = path.lastIndexOf("/");
		if (fileNameEndPosition != -1) {
			return path.substring(fileNameStartPosition + 1);
		} else {
			return path;
		}
	}
	
	@Override
	public byte[] toBinary(Object obj) {
		registerObjectTypeIfNeed(obj.getClass());
		
		Message message = new Message();
		message.setObject(obj);
		
		byte[] data;
		try {
			data = binaryXmppProtocolConverter.toBinary(oxmFactory.getTranslatingFactory().translate(message));
		} catch (BxmppConversionException e) {
			throw new RuntimeException("????Can't convert protocol object to BXMPP data!", e);
		}
		
		byte[] pureActionData = new byte[data.length - 3];
		pureActionData[0] = data[0];
		for (int i = 1; i < data.length - 3; i++) {
			pureActionData[i] = data[i + 3];
		}
		
		return pureActionData;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void registerObjectTypeIfNeed(Class<?> type) {
		if (LanExecution.class == type ||
				LanNotification.class == type ||
					LanReport.class == type)
			return;
		
		if (registeredLanActionTypes.contains(type))
			return;
		
		if (registeredLanSupportedEventTypes.contains(type))
			return;
		
		if (!registeredObjectTypes.contains(type)) {
			ProtocolObject protocolObject = type.getAnnotation(ProtocolObject.class);
			if (protocolObject == null) {
				throw new IllegalArgumentException(String.format("Type '%s' isn't a protocol object type.", type.getName()));
			}
			
			ProtocolChain protocolChain = new MessageProtocolChain(
					new Protocol(protocolObject.namespace(), protocolObject.localName()));
			oxmFactory.register(protocolChain, new CocParserFactory<>(type));
			oxmFactory.register(type, new CocTranslatorFactory(type));
			
			registeredObjectTypes.add(type);
		}
	}

	@Override
	public <T> T toObject(Class<T> type, byte[] data) throws BxmppConversionException {
		registerObjectTypeIfNeed(type);
		
		T obj = getMessage(data).getObject();
		if (FlawedProtocolObject.isFlawed(obj)) {
			throw new ProtocolException(new ServiceUnavailable(String.format(
					"Flawed protocol object: %s.", obj.toString())));
		}
		
		return obj;
	}
	
	@Override
	public IBinaryXmppProtocolConverter getBinaryXmppProtocolConverter() {
		return binaryXmppProtocolConverter;
	}

	@Override
	public void registerLanAction(Class<?> lanActionType) {
		if (registeredLanActionTypes.contains(lanActionType))
			return;
		
		ProtocolObject protocolObject = lanActionType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null) {
			throw new IllegalArgumentException(String.format("LAN action type %s isn't a protocol object type.", lanActionType.getName()));
		}
		
		ProtocolChain executionProtocolChain = new MessageProtocolChain(Execution.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.register(executionProtocolChain, new CocParserFactory<>(lanActionType));
		
		ProtocolChain lanExecutionProtocolChain = new MessageProtocolChain(LanExecution.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.register(lanExecutionProtocolChain, new CocParserFactory<>(lanActionType));
		oxmFactory.register(lanActionType, new CocTranslatorFactory<>(lanActionType));
		
		registeredLanActionTypes.add(lanActionType);
	}
	
	@Override
	public void registerLanSupportedEvent(Class<?> lanSupportedEventType) {
		if (registeredLanSupportedEventTypes.contains(lanSupportedEventType))
			return;
		
		ProtocolObject protocolObject = lanSupportedEventType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null) {
			throw new IllegalArgumentException(String.format("LAN supported event type %s isn't a protocol object type.", lanSupportedEventType.getName()));
		}
		
		ProtocolChain lanNotificationProtocolChain = new MessageProtocolChain(LanNotification.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.register(lanNotificationProtocolChain, new CocParserFactory<>(lanSupportedEventType));
		oxmFactory.register(lanSupportedEventType, new CocTranslatorFactory<>(lanSupportedEventType));
		
		registeredLanSupportedEventTypes.add(lanSupportedEventType);
	}
	
	@Override
	public void registerLanFollowedEvent(Class<?> lanFollowedEventType) {
		if (registeredLanFollowedEventTypes.contains(lanFollowedEventType))
			return;
		
		ProtocolObject protocolObject = lanFollowedEventType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null) {
			throw new IllegalArgumentException(String.format("LAN followed event type %s isn't a protocol object type.", lanFollowedEventType.getName()));
		}
		
		ProtocolChain lanNotificationProtocolChain = new MessageProtocolChain(LanNotification.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.register(lanNotificationProtocolChain, new CocParserFactory<>(lanFollowedEventType));
		oxmFactory.register(lanFollowedEventType, new CocTranslatorFactory<>(lanFollowedEventType));
		
		
		registeredLanFollowedEventTypes.add(lanFollowedEventType);
	}
	
	@Override
	public void registerLanData(Class<?> lanDataType) {
		if (registeredLanDataTypes.contains(lanDataType))
			return;
		
		ProtocolObject protocolObject = lanDataType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null)
			throw new IllegalArgumentException(String.format("LAN data type %s isn't a protocol object type.", lanDataType.getName()));
		
		ProtocolChain lanDataProtocolChain = new MessageProtocolChain(LanReport.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.register(lanDataProtocolChain, new CocParserFactory<>(lanDataType));
		oxmFactory.register(lanDataType, new CocTranslatorFactory<>(lanDataType));
		
		registeredLanDataTypes.add(lanDataType);
	}

	@Override
	public Object toObject(byte[] data) throws BxmppConversionException {
		Object obj = getMessage(data).getObject();
		if (FlawedProtocolObject.isFlawed(obj)) {
			throw new ProtocolException(new ServiceUnavailable(String.format(
					"Flawed protocol object: %s.", obj.toString())));
		}
		
		return obj;
	}

	private Message getMessage(byte[] data) throws BxmppConversionException {
		byte[] wrappedByMessageData = new byte[data.length + 3];
		
		wrappedByMessageData[0] = data[0];
		for (int i = 0; i < 3; i++) {
			wrappedByMessageData[i + 1] = MESSAGE_WRAPPER_DATA[i];
		}
		
		for (int i = 0; i < data.length - 1; i++) {
			wrappedByMessageData[i + 4] = data[i + 1];
		}
		
		String xml = binaryXmppProtocolConverter.toXml(wrappedByMessageData);
		return (Message)oxmFactory.getParsingFactory().parse(xml);
	}

	@Override
	public Protocol readProtocol(byte[] data) {
		return binaryXmppProtocolConverter.readProtocol(data);
	}

	@Override
	public boolean unregisterLanAction(Class<?> lanActionType) {
		if (!registeredLanActionTypes.contains(lanActionType))
			return false;
		
		ProtocolObject protocolObject = lanActionType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null) {
			throw new IllegalArgumentException(String.format("Type '%s' isn't a protocol object type.", lanActionType.getName()));
		}
		
		ProtocolChain lanExecutionProtocolChain = new MessageProtocolChain(LanExecution.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.unregister(lanExecutionProtocolChain);
		oxmFactory.unregister(lanActionType);
		
		registeredLanActionTypes.remove(lanActionType);
		
		return true;
	}
	
	@Override
	public boolean unregisterLanSupportedEvent(Class<?> lanSupportedEventType) {
		if (!registeredLanSupportedEventTypes.contains(lanSupportedEventType))
			return false;
		
		ProtocolObject protocolObject = lanSupportedEventType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null) {
			throw new IllegalArgumentException(String.format("Type '%s' isn't a protocol object type.", lanSupportedEventType.getName()));
		}
		
		oxmFactory.unregister(lanSupportedEventType);
		ProtocolChain lanNotificationProtocolChain = new MessageProtocolChain(LanNotification.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.unregister(lanNotificationProtocolChain);
		
		registeredLanSupportedEventTypes.remove(lanSupportedEventType);
		
		return true;
	}
	
	@Override
	public boolean unregisterLanFollowedEvent(Class<?> lanFollowedEventType) {
		if (!registeredLanFollowedEventTypes.contains(lanFollowedEventType))
			return false;
		
		ProtocolObject protocolObject = lanFollowedEventType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null) {
			throw new IllegalArgumentException(String.format("Type '%s' isn't a protocol object type.", lanFollowedEventType.getName()));
		}
		
		oxmFactory.unregister(lanFollowedEventType);
		ProtocolChain lanNotificationProtocolChain = new MessageProtocolChain(LanNotification.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.unregister(lanNotificationProtocolChain);
		
		registeredLanFollowedEventTypes.remove(lanFollowedEventType);
		
		return true;
	}
	
	@Override
	public boolean unregisterLanData(Class<?> lanDataType) {
		if (!registeredLanDataTypes.contains(lanDataType))
			return false;
		
		ProtocolObject protocolObject = lanDataType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null) {
			throw new IllegalArgumentException(String.format("Type '%s' isn't a protocol object type.", lanDataType.getName()));
		}
		
		oxmFactory.unregister(lanDataType);
		ProtocolChain lanReportProtocolChain = new MessageProtocolChain(LanReport.PROTOCOL).
				next(new Protocol(protocolObject.namespace(), protocolObject.localName()));
		oxmFactory.unregister(lanReportProtocolChain);
		
		registeredLanDataTypes.remove(lanDataType);
		
		return true;
	}
	
	@Override
	public String toXml(byte[] data) throws BxmppConversionException {
		return binaryXmppProtocolConverter.toXml(data);
	}
}
