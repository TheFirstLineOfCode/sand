package com.thefirstlineofcode.sand.client.lpwanconcentrator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlinelinecode.sand.protocols.concentrator.RemoveNode;
import com.thefirstlinelinecode.sand.protocols.lpwan.concentrator.friends.LanFollow;
import com.thefirstlinelinecode.sand.protocols.lpwan.concentrator.friends.LanFollows;
import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.basalt.oxm.binary.BxmppConversionException;
import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.IError;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Stanza;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.InternalServerError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAcceptable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.RemoteServerTimeout;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UndefinedCondition;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UnexpectedRequest;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.ITask;
import com.thefirstlineofcode.chalk.core.IUnidirectionalStream;
import com.thefirstlineofcode.sand.client.actuator.Actuator;
import com.thefirstlineofcode.sand.client.concentrator.Concentrator;
import com.thefirstlineofcode.sand.client.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.client.concentrator.LanNode;
import com.thefirstlineofcode.sand.client.concentrator.NodeNotFoundException;
import com.thefirstlineofcode.sand.client.friends.IFollowProcessor;
import com.thefirstlineofcode.sand.client.sensor.IReportService;
import com.thefirstlineofcode.sand.client.sensor.IReporter;
import com.thefirstlineofcode.sand.client.thing.INotificationService;
import com.thefirstlineofcode.sand.client.thing.INotifier;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.client.thing.TimeBasedRexStrategy;
import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationListener;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.client.thing.obx.ObxFactory;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.actuator.LanExecution;
import com.thefirstlineofcode.sand.protocols.sensor.LanReport;
import com.thefirstlineofcode.sand.protocols.sensor.Report.QoS;
import com.thefirstlineofcode.sand.protocols.thing.BadAddressException;
import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;
import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ITraceId;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanAnswer;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanNotification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;

public class LpwanConcentrator extends Actuator implements ILpwanConcentrator {
	private static final Logger logger = LoggerFactory.getLogger(LpwanConcentrator.class);
	
	private static final long DEFAULT_VALUE_OF_DEFAULT_LAN_EXECUTION_TIMEOUT = 1000 * 10;
	private static final int DEFAULT_LAN_EXECUTION_TIMEOUT_CHECK_INTERVAL = 500;
		
	private static final int DEFAULT_ACK_REQUIRED_LAN_NOTIFICATIONS_POOL_SIZE = 512;
	
	private static final int DEFAULT_ACK_REQUIRED_LAN_REPORTS_POOL_SIZE = 512;
	
	private static final int DEFAULT_NOTIFICATION_INIT_REX_INTERVAL = 200;
	private static final int DEFAULT_NOTIFICATION_REX_TIMEOUT = 2000;
	
	protected Map<CommunicationNet, ICommunicator<?, ? extends ILanAddress, byte[]>> communicators;
	protected Map<CommunicationNet, LanCommunicationListener<?, ?>> netToLanCommunicationListeners;
	protected Map<Integer, List<LanExecutionTraceInfo>> lanNodeToLanExecutionTraceInfos;
	protected long defaultLanExecutionTimeout;
	protected int lanExecutionTimeoutCheckInterval;
	protected ExpiredLanExecutionsChecker expiredLanExecutionsChecker;
	
	protected Concentrator concentrator;
		
	protected Map<String, IThingModelDescriptor> lanThingModelDescriptors = new HashMap<>();
	
	protected boolean lanRouting;
	
	protected int ackRequiredLanNotificationsPoolSize;
	protected Map<ThingsTinyId, LanNotification> ackRequiredLanNotificationsPool;
	protected long notificationInitRexInternal;
	protected long notificationRexTimeout;
	
	protected int ackRequiredLanReportsPoolSize;
	protected Map<ThingsTinyId, LanReport> ackRequiredLanReportsPool;
	
	protected List<ILanNotificationPreprocessor> lanNotificationPreprocessors;
	protected List<ILanReportPreprocessor> lanReportPreprocessors;
	
	protected INotifier notifier;
	protected IReporter reporter;
	
	protected long initRexInternal;
	protected long rexTimeout;
	
	protected LanFollows lanFollows;
	
	protected IFollowProcessor followProcessor;
	
	protected QoS defaultDataQoS;
	protected Map<Class<?>, QoS> dataTypeToQoSs;
	
	private StandardErrorCodeMapping standardErrorCodeMapping;
	
	public LpwanConcentrator(IChatServices chatServices)  {
		super(chatServices);
		
		concentrator = new Concentrator(chatServices);
		
		communicators = new HashMap<>();
		netToLanCommunicationListeners = new HashMap<>();
		lanNodeToLanExecutionTraceInfos = new HashMap<>();
		defaultLanExecutionTimeout = DEFAULT_VALUE_OF_DEFAULT_LAN_EXECUTION_TIMEOUT;
		lanExecutionTimeoutCheckInterval = DEFAULT_LAN_EXECUTION_TIMEOUT_CHECK_INTERVAL;
		
		lanRouting = false;
		
		ackRequiredLanNotificationsPoolSize = DEFAULT_ACK_REQUIRED_LAN_NOTIFICATIONS_POOL_SIZE;
		ackRequiredLanNotificationsPool = new AckRequiredLanNotificationsLruPool<>();
		
		notificationInitRexInternal = DEFAULT_NOTIFICATION_INIT_REX_INTERVAL;
		notificationRexTimeout = DEFAULT_NOTIFICATION_REX_TIMEOUT;
		
		ackRequiredLanReportsPoolSize = DEFAULT_ACK_REQUIRED_LAN_REPORTS_POOL_SIZE;
		ackRequiredLanReportsPool = new AckRequiredLanReportsLruPool<>();
		
		lanNotificationPreprocessors = new ArrayList<>();
		lanReportPreprocessors = new ArrayList<>();
		
		defaultDataQoS = QoS.AT_MOST_ONCE;
		dataTypeToQoSs = new HashMap<>();
		
		INotificationService notificationService = chatServices.createApi(INotificationService.class);
		notifier = notificationService.getNotifier();
		
		IReportService reportService = chatServices.createApi(IReportService.class);
		reporter = reportService.getReporter();
		
		standardErrorCodeMapping = new StandardErrorCodeMapping();
	}
	
	private class AckRequiredLanNotificationsLruPool<K, V> extends LinkedHashMap<K, V> {
		private static final long serialVersionUID = -6507465139591155232L;
		
		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
			return size() > ackRequiredLanNotificationsPoolSize;
		}
	}
	
	private class AckRequiredLanReportsLruPool<K, V> extends LinkedHashMap<K, V> {		
		private static final long serialVersionUID = 6697691388728340841L;

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
			return size() > ackRequiredLanReportsPoolSize;
		}
	}
	
	@Override
	public void start() {
		super.start();
		enableLanRouting();
		
		chatServices.getIqService().addListener(RemoveNode.PROTOCOL, this);
		chatServices.getIqService().addListener(Notification.PROTOCOL, this);
	}
	
	@Override
	public void stop() {
		chatServices.getIqService().removeListener(Notification.PROTOCOL);
		chatServices.getIqService().removeListener(RemoveNode.PROTOCOL);
		
		disableLanRouting();
		super.stop();
	}

	@Override
	public void addCommunicator(CommunicationNet net, ICommunicator<?, ?, byte[]> communicator) {
		if (communicators.containsKey(net)) {
			logger.warn("Reduplicate communicator for net: {}.", net);
			return;
		}
		
		communicators.put(net, communicator);
	}
	
	@SuppressWarnings("unchecked")
	protected <OA extends ILanAddress, PA extends ILanAddress> void removeLanExecutionAnswerListener(CommunicationNet net,
			ICommunicator<OA, PA, byte[]> communicator) {
		LanCommunicationListener<OA, PA> lanExecutionAnswerListener = (LanCommunicationListener<OA, PA>)netToLanCommunicationListeners.get(net);
		if (lanExecutionAnswerListener != null)
			communicator.removeCommunicationListener(lanExecutionAnswerListener);
	}
	
	@Override
	public void registerLanThingModel(IThingModelDescriptor modelDescriptor) {
		lanThingModelDescriptors.put(modelDescriptor.getModelName(), modelDescriptor);
		
		for (Entry<Protocol, Class<?>> entry : modelDescriptor.getSupportedActions().entrySet()) {
			registerLanAction(entry.getKey(), entry.getValue());
		}
		
		for (Entry<Protocol, Class<?>> entry : modelDescriptor.getSupportedEvents().entrySet()) {
			registerLanSupportedEvent(entry.getKey(), entry.getValue());
		}
		
		for (Entry<Protocol, Class<?>> entry : modelDescriptor.getFollowedEvents().entrySet()) {
			registerLanFollowedEvent(entry.getKey(), entry.getValue());
		}
		
		for (Entry<Protocol, Class<?>> entry : modelDescriptor.getSupportedData().entrySet()) {
			registerLanData(entry.getValue());
		}
		
		if (logger.isInfoEnabled())
			logger.info("LAN thing model '{}' has registered.", modelDescriptor.getModelName());
	}
	
	protected void registerLanAction(Protocol protocol, Class<?> actionType) {
		oxmFactory.register(new IqProtocolChain(Execution.PROTOCOL).next(protocol),
				new CocParserFactory<>(actionType));
		
		ObxFactory.getInstance().registerLanAction(actionType);
	}
	
	protected void registerLanSupportedEvent(Protocol protocol, Class<?> eventType) {
		ObxFactory.getInstance().registerLanSupportedEvent(eventType);
		
		notifier.registerSupportedEvent(eventType);
	}
	
	protected void registerLanFollowedEvent(Protocol protocol, Class<?> eventType) {
		oxmFactory.register(new IqProtocolChain(Notification.PROTOCOL).next(protocol),
				new CocParserFactory<>(eventType));
		
		ObxFactory.getInstance().registerLanFollowedEvent(eventType);
	}
	
	protected void registerLanData(Class<?> dataType) {
		ObxFactory.getInstance().registerLanData(dataType);
		
		reporter.registerSupportedData(dataType);
	}
	
	@SuppressWarnings("unchecked")
	protected <PA extends ILanAddress> void executeOnLanNode(Iq iq, Object action, boolean lanTraceable, Integer lanTimeout) {
		int lanId = Integer.parseInt(iq.getTo().getResource());
		LanNode node = getNode(lanId);
		if (node == null)
			throw new ProtocolException(new ItemNotFound(String.format("LAN node '%s' not existed.", iq.getTo())));
		
		if (!isLanActionSupported(node.getModel(), action.getClass())) {
			throw new ProtocolException(new ServiceUnavailable(String.format(
					"Action type %s not supported by thing '{}'.",
					action.getClass().getName(), iq.getTo())));
		}
		
		ICommunicator<?, PA, byte[]> communicator = (ICommunicator<?, PA, byte[]>)getCommunicator(node.getCommunicationNet());
		if (communicator == null) {
			throw new ProtocolException(new InternalServerError(String.format(
					"Communication net %s not supported by concentrator '{}'.",
					node.getCommunicationNet(), iq.getTo().getBareId())));
		}
		
		try {
			if (lanTraceable) {
				LanExecution lanExecution = new LanExecution(ThingsTinyId.createRequestId(0), action);
				traceLanExecution(iq.getFrom(), iq.getTo(), iq.getId(), node, lanExecution, lanTimeout);
				communicator.send((PA)node.getCommunicationNet().parse(node.getAddress()), ObxFactory.getInstance().toBinary(lanExecution));
			} else {
				communicator.send((PA)node.getCommunicationNet().parse(node.getAddress()), ObxFactory.getInstance().toBinary(action));			
			}
		} catch (BadAddressException e) {
			throw new ProtocolException(new InternalServerError(String.format("Bad communication network address: '%s'.", node.getAddress())));
		} catch (CommunicationException e) {
			throw new ProtocolException(new InternalServerError(String.format("Can't send request to node. Exception: %s.", e.getMessage())));
		}
	}

	protected synchronized void traceLanExecution(JabberId from, JabberId to, String stanzaId,
			LanNode node, LanExecution lanExecution, Integer lanTimeout) {
		List<LanExecutionTraceInfo> lanExecutionTraceInfos = lanNodeToLanExecutionTraceInfos.get(node.getLanId());
		if (lanExecutionTraceInfos == null) {
			lanExecutionTraceInfos = lanNodeToLanExecutionTraceInfos.get(node.getLanId());
			if (lanExecutionTraceInfos == null) {
				lanExecutionTraceInfos = new ArrayList<>();
				lanNodeToLanExecutionTraceInfos.put(node.getLanId(), lanExecutionTraceInfos);
			}
		}
		
		long expiredTime;
		if (lanTimeout != null) {
			expiredTime = Calendar.getInstance().getTime().getTime() + lanTimeout;
		} else {
			expiredTime = Calendar.getInstance().getTime().getTime() + defaultLanExecutionTimeout;
		}
		lanExecutionTraceInfos.add(new LanExecutionTraceInfo(from, to, stanzaId, node, lanExecution, expiredTime));
	}
	
	protected synchronized <PA extends ILanAddress> void received(CommunicationNet net, PA from, byte[] data) {
		if (!BinaryUtils.isLegalBxmppMessage(data)) {
			if (logger.isWarnEnabled())
				logger.warn("Illegal BXMPP message. Message: {}.", BinaryUtils.getHexStringFromBytes(data));
			
			return;
		}
		
		try {
			if (isLanAnswerMessage(data)) {
				lanAnswerReceived(net, from, data);
			} else if (isLanNotificationMessage(data)) {
				lanNotificationReceived(net, from, data);
			} else if (isLanReportMessage(data)) {
				lanReportReceived(net, from, data);
			} else {
				// Unknown type of LAN message received.
				if (logger.isWarnEnabled())
					logger.warn("Unknown type of LAN message received. Protocol data: {}.", BinaryUtils.getHexStringFromBytes(data));
			}			
		} catch (BxmppConversionException e) {
			if (logger.isErrorEnabled())
				logger.error("Illegal BXMPP data received.", e);
		}
	}

	protected <PA extends ILanAddress> void lanNotificationReceived(CommunicationNet net, PA from, byte[] data) throws BxmppConversionException {
		LanNotification lanNotification = ObxFactory.getInstance().toObject(LanNotification.class, data);
		
		if (lanNotification.getEvent() == null)
			throw new RuntimeException("????Null event!");
		
		ThingsTinyId traceId = ThingsTinyId.createInstance(lanNotification.getTraceId());
		int lanId = traceId.getLanId();
		if (lanId == 0)
			throw new RuntimeException("Is LAN ID 0?");
		
		if (!lanNotification.isAckRequired()) {
			processNoAckRequiredLanNotification(lanNotification);
		} else {			
			processAckRequiredLanNotification(traceId, lanId, lanNotification);
		}
	}
	
	protected <PA extends ILanAddress> void lanReportReceived(CommunicationNet net, PA from, byte[] data) throws BxmppConversionException {
		processLanReport(ObxFactory.getInstance().toObject(LanReport.class, data));
	}

	private void processLanFollows(LanNotification lanNotification) {
		if (lanFollows == null || lanFollows.getLanFollows().isEmpty())
			return;
		
		int notifierLanId = ThingsTinyId.createInstance(lanNotification.getTraceId()).getLanId();
		ProtocolObject pObj = lanNotification.getEvent().getClass().getAnnotation(ProtocolObject.class);
		if (pObj == null)
			throw new RuntimeException("Isn't event a project object?");
		Protocol eventProtocol = new Protocol(pObj.namespace(), pObj.localName());
		
		for (LanFollow lanFollow : lanFollows.getLanFollows()) {
			if (lanFollow.getFriendLanId() == notifierLanId &&
					lanFollow.getEvent().equals(eventProtocol)) {
				processLanFollow(notifierLanId, lanFollow.getFollowerLanId(), lanNotification);
			}
		}
	}

	private void processLanFollow(int notifierLanId, int followerLanId, LanNotification lanNotification) {
		if (followerLanId == 0) {
			if (followProcessor == null) {
				if (logger.isWarnEnabled())
					logger.warn("Can't deliver LAN follow to concenter itself because follow processor is null.");
				
				return;
			}
			
			JabberId concentratorJid = chatServices.getStream().getJid();
			followProcessor.process(new JabberId(concentratorJid.getNode(), concentratorJid.getDomain(),
					String.valueOf(notifierLanId)), lanNotification.getEvent());
		} else {
			// TODO Deliver notification to LAN node.
		}
	}

	protected void processNoAckRequiredLanNotification(LanNotification lanNotification) {
		LanNotification preprocessed = preprocessLanNotification(lanNotification);
		if (preprocessed == null)
			return;
		
		ThingsTinyId traceId = ThingsTinyId.createInstance(preprocessed.getTraceId());
		int lanId = traceId.getLanId();
		if (lanId == 0)
			throw new RuntimeException("Is LAN ID 0?");
		
		processLanFollows(lanNotification);
		
		notifier.notify(getLanNodeJid(lanId), preprocessed.getEvent());
	}

	protected LanNotification preprocessLanNotification(LanNotification lanNotification) {
		LanNotification preprocessed = lanNotification;
		for (ILanNotificationPreprocessor lanNotificationPreprocessor : lanNotificationPreprocessors) {
			preprocessed = lanNotificationPreprocessor.process(preprocessed);
			if (preprocessed == null)
				return null;
		}
		
		return preprocessed;
	}
	
	protected LanReport preprocessLanReport(LanReport lanReport) {
		LanReport preprocessed = lanReport;
		for (ILanReportPreprocessor lanReportPreprocessor : lanReportPreprocessors) {
			preprocessed = lanReportPreprocessor.process(preprocessed);
			if (preprocessed == null)
				return null;
		}
		
		return preprocessed;
	}

	protected JabberId getLanNodeJid(int lanId) {
		JabberId concentratorJid = chatServices.getStream().getJid();
		return new JabberId(concentratorJid.getNode(), concentratorJid.getDomain(), String.valueOf(lanId));
	}

	protected void processAckRequiredLanNotification(ThingsTinyId traceId, int lanId, LanNotification lanNotification) {						
		sendAnswerToLanNode(traceId);
		
		LanNotification preprocessed = preprocessLanNotification(lanNotification);
		if (preprocessed == null)
			return;
		
		if (!ackRequiredLanNotificationsPool.containsKey(traceId)) {
			ackRequiredLanNotificationsPool.put(traceId, null);
			
			processLanFollows(lanNotification);
			
			JabberId concentratorJid = chatServices.getStream().getJid();
			notifier.notifyWithAck(new JabberId(concentratorJid.getNode(), concentratorJid.getDomain(), String.valueOf(lanId)),
					lanNotification.getEvent(), new TimeBasedRexStrategy(notificationInitRexInternal, notificationRexTimeout));
		}
	}
	
	protected void processLanReport(LanReport lanReport) {		
		if (lanReport.getData() == null)
			throw new RuntimeException("????Null data!");
		
		ThingsTinyId traceId = ThingsTinyId.createInstance(lanReport.getTraceId());
		int lanId = traceId.getLanId();
		if (lanId == 0)
			throw new RuntimeException("Is LAN ID 0?");
		
		if (lanReport.isAckRequired())
			sendAnswerToLanNode(traceId);
		
		LanReport preprocessed = preprocessLanReport(lanReport);
		if (preprocessed == null)
			return;
		
		if (lanReport.isAckRequired()) {
			if (ackRequiredLanReportsPool.containsKey(traceId))
				return;
			
			ackRequiredLanReportsPool.put(traceId, null);
		}
		
		QoS qos = getDataQoS(lanReport.getData().getClass());
		if (qos == null)
			qos = defaultDataQoS;
		
		JabberId concentratorJid = chatServices.getStream().getJid();
		reporter.report(new JabberId(concentratorJid.getNode(), concentratorJid.getDomain(), String.valueOf(lanId)),
				lanReport.getData(), qos);
	}

	@SuppressWarnings("unchecked")
	protected <T extends ILanAddress> void sendAnswerToLanNode(ThingsTinyId traceId) {
		LanNode lanNode = getNode(traceId.getLanId());
		if (lanNode == null)
			throw new RuntimeException(String.format("LAN node not found. LAN ID: '%s'.", traceId.getLanId()));
		
		CommunicationNet net = lanNode.getCommunicationNet();
		try {
			T address = (T)net.parse(lanNode.getAddress());
			LanAnswer answer = new LanAnswer(traceId.createResponseId().getBytes());
			ICommunicator<?, T, byte[]> communicator = (ICommunicator<?, T, byte[]>)getCommunicator(net);;
			
			communicator.send(address, ObxFactory.getInstance().toBinary(answer));
		} catch (BadAddressException e) {
			throw new RuntimeException("Can't parse address of LAN node.", e);
		} catch (CommunicationException e) {
			if (logger.isErrorEnabled())
				logger.error("Failed to send LAN answer to LAN node.", e);
		}
	}

	private boolean isLanNotificationMessage(byte[] data) {
		return LanNotification.PROTOCOL.equals(ObxFactory.getInstance().readProtocol(data));
	}
	
	private boolean isLanReportMessage(byte[] data) {
		return LanReport.PROTOCOL.equals(ObxFactory.getInstance().readProtocol(data));
	}

	protected <PA extends ILanAddress> void lanAnswerReceived(CommunicationNet net, PA from, byte[] data) throws BxmppConversionException {
		LanAnswer answer = ObxFactory.getInstance().toObject(LanAnswer.class, data);
		for (LanNode node : concentrator.getNodes()) {
			if (!isFromNode(net, from, node))
				continue;
			
			synchronized (lanNodeToLanExecutionTraceInfos) {
				List<LanExecutionTraceInfo> lanExecutionTraceInfos = lanNodeToLanExecutionTraceInfos.get(node.getLanId());
				if (lanExecutionTraceInfos == null || lanExecutionTraceInfos.size() == 0) {
					if (from == null)
						continue;
					
					break;
				}
				
				LanExecutionTraceInfo requestedTraceInfo = null;
				for (LanExecutionTraceInfo lanExecutionTraceInfo : lanExecutionTraceInfos) {
					byte[] requestId = lanExecutionTraceInfo.lanExecution.getTraceId();
					if (!ThingsTinyId.isAnswerId(requestId, answer.getTraceId()))
						continue;
					
					processAnswer(lanExecutionTraceInfo, answer);
					requestedTraceInfo = lanExecutionTraceInfo;
				}
				
				if (requestedTraceInfo != null) {
					lanExecutionTraceInfos.remove(requestedTraceInfo);
				} else {
					if (logger.isWarnEnabled()) {
						logger.warn("Can't find the request trace ID which generate the answer ID: {}. Maybe the request is timeout.", BinaryUtils.getHexStringFromBytes(data));
					}
				}
			}
		}
	}
	
	protected void processAnswer(LanExecutionTraceInfo lanExecutionTraceInfo, LanAnswer answer) {
		ITraceId.Type answerType = ThingsTinyId.getType(answer.getTraceId());
		if (answerType == ITraceId.Type.RESPONSE) {									
			processLanExecutionResponse(lanExecutionTraceInfo.from, lanExecutionTraceInfo.to, lanExecutionTraceInfo.sanzaId);
		} else if (answerType == ITraceId.Type.ERROR) {									
			processLanExecutionError(lanExecutionTraceInfo, answer);
		} else {
			throw new RuntimeException("Not an answer ID???");
		}
	}

	protected <PA extends ILanAddress> boolean isFromNode(CommunicationNet net, PA from, LanNode node) {
		if (from == null)
			return true;
		
		try {
			return net.parse(node.getAddress()).equals(from);
		} catch (BadAddressException e) {
			if (logger.isErrorEnabled()) {
				logger.error(String.format("Bad address: %s.", node.getAddress()), e);
			}
			
			throw new RuntimeException("Bad address: " + node.getAddress());
		}
	}
	
	protected void processLanExecutionError(LanExecutionTraceInfo traceInfo, LanAnswer error) {
		if (error.getErrorNumber() == null)
			throw new RuntimeException("Null error number.");
		
		StanzaError e = errorCodeToError(traceInfo.node.getModel(), error.getErrorNumber());
		e.setId(traceInfo.sanzaId);
		setFromToAddresses(traceInfo.from, traceInfo.to, e);
		
		chatServices.getStream().send((IError)e);
	}

	protected void processLanExecutionResponse(JabberId from, JabberId to, String stanzaId) {
		Iq result = new Iq(Iq.Type.RESULT, stanzaId);
		setFromToAddresses(from, to, result);
		
		chatServices.getIqService().send(result);
	}
	
	protected void processExpiredLanExecution(JabberId from, JabberId to, String stanzaId) {
		Stanza timeout = new RemoteServerTimeout();
		timeout.setId(stanzaId);
		setFromToAddresses(from, to, timeout);
		
		chatServices.getStream().send(timeout);
	}
	
	protected void setFromToAddresses(JabberId from, JabberId to, Stanza stanza) {
		if (toLanNode(to))
			stanza.setFrom(to);
		
		if (from != null && !getHost().equals(from)) {
			stanza.setTo(from);
		}
	}
	
	private boolean isLanAnswerMessage(byte[] data) {
		return LanAnswer.PROTOCOL.equals(ObxFactory.getInstance().readProtocol(data));
	}

	@Override
	public void setDefaultLanExecutionTimeout(long timeout) {
		this.defaultLanExecutionTimeout = timeout;
	}

	@Override
	public long getDefaultLanExecutionTimeout() {
		return defaultLanExecutionTimeout;
	}

	@Override
	public void setLanExecutionTimeoutCheckInterval(int interval) {
		this.lanExecutionTimeoutCheckInterval = interval;
	}

	@Override
	public int getLanExecutionTimeoutCheckInterval() {
		return lanExecutionTimeoutCheckInterval;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> void execute(Iq iq, Execution execution) {	
		JabberId from = iq.getFrom();
		if (from == null) {
			from = getHost();
		}
		
		T action = (T)execution.getAction();
		if (toThingItself(iq.getTo())) {
			super.execute(iq, execution);
		} else if (toLanNode(iq.getTo())) {
			if (!lanRouting) {
				if (logger.isWarnEnabled()) {
					logger.warn("Can't deliver action because LAN routing is disabled.");
				}
				
				throw new ProtocolException(new UnexpectedRequest("Can't deliver action because LAN routing is disabled."));
			}
			
			if (logger.isInfoEnabled()) {
				logger.info("Try to execute the action {} which was sent from '{}' on LAN node '{}'.", action, from, iq.getTo());
			}
			
			executeOnLanNode(iq, action, execution.isLanTraceable(), execution.getLanTimeout());
		} else {
			if (logger.isErrorEnabled()) {
				logger.error("Can't find the thing which's JID is '{}' to execute the action which was sent from '{}'.",
						iq.getTo(), from);
			}
			
			throw new ProtocolException(new BadRequest(
					String.format("Can't find the thing which's JID is '%s' to execute the action which was sent from '%s'.",
							iq.getTo(), from)));
		}
	}
	
	protected boolean toLanNode(JabberId to) {
		return to != null && to.getResource() != null &&
				!String.valueOf(IConcentrator.LAN_ID_CONCENTRATOR).equals(to.getResource());
	}
	
	protected boolean toThingItself(JabberId to) {
		return to == null ||
				(to != null && to.getResource() == null) ||
				(to != null && to.getResource() != null &&
					String.valueOf(IConcentrator.LAN_ID_CONCENTRATOR).equals(to.getResource()));
	}
	
	public void addNode(LanNode node) {
		concentrator.addNode(node);
	}

	@Override
	public void requestServerToAddNode(String thingId, String registrationCode, int lanId, ILanAddress address) {
		concentrator.requestServerToAddNode(thingId, registrationCode, lanId, address);
	}
	
	@Override
	public void removeNode(int lanId) throws NodeNotFoundException {
		concentrator.removeNode(lanId);
	}
	
	@Override
	public void setNodes(Map<Integer, LanNode> nodes) {
		concentrator.setNodes(nodes);
	}
	
	@Override
	public Collection<LanNode> getNodes() {
		return concentrator.getNodes();
	}

	@Override
	public void addListener(IConcentrator.Listener listener) {
		concentrator.addListener(listener);
	}

	@Override
	public IConcentrator.Listener removeListener(IConcentrator.Listener listener) {
		return concentrator.removeListener(listener);
	}

	@Override
	public LanNode getNode(int lanId) {
		return concentrator.getNode(lanId);
	}

	@Override
	public void syncNodesWithServer(SyncNodesListener syncNodesListener) {
		concentrator.syncNodesWithServer(syncNodesListener);
	}

	@Override
	public int getBestSuitedNewLanId() {
		return concentrator.getBestSuitedNewLanId();
	}

	@Override
	public String getThingName() {
		return concentrator.getThingName();
	}
	
	@Override
	public boolean isLanDataSupported(String model, Protocol protocol) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		return lanThingModelDescriptors.get(model).getSupportedData().containsKey(protocol);
	}
	
	@Override
	public Class<?> getLanDataType(String model, Protocol protocol) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		if (!lanThingModelDescriptors.get(model).getSupportedData().containsKey(protocol)) {			
			throw new IllegalArgumentException(String.format("Unsupported data which's protocol is '%s' for thing which's model is '%s'.", protocol, model));
		}
		
		return lanThingModelDescriptors.get(model).getSupportedData().get(protocol);
	}
	
	@Override
	public boolean isLanActionSupported(String model, Protocol protocol) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		return lanThingModelDescriptors.get(model).getSupportedActions().containsKey(protocol);
	}

	@Override
	public Class<?> getLanActionType(String model, Protocol protocol) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		if (!lanThingModelDescriptors.get(model).getSupportedActions().containsKey(protocol)) {			
			throw new IllegalArgumentException(String.format("Unsupported action which's protocol is '%s' for thing which's model is '%s'.", protocol, model));
		}
		
		return lanThingModelDescriptors.get(model).getSupportedActions().get(protocol);
	}

	@Override
	public boolean isLanEventSupported(String model, Protocol protocol) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		return lanThingModelDescriptors.get(model).getSupportedEvents().containsKey(protocol);
	}
	
	@Override
	public boolean isLanEventFollowed(String model, Protocol protocol) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		return lanThingModelDescriptors.get(model).getFollowedEvents().containsKey(protocol);
	}
	
	@Override
	public Class<?> getLanEventType(String model, Protocol protocol) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		if (!lanThingModelDescriptors.get(model).getSupportedActions().containsKey(protocol)) {			
			throw new IllegalArgumentException(String.format("Unsupported event which's protocol is '%s' for thing which's model is '%s'.", protocol, model));
		}
		
		return lanThingModelDescriptors.get(model).getSupportedEvents().get(protocol);
	}
	
	@Override
	public boolean isLanDataSupported(String model, Class<?> dataType) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		return lanThingModelDescriptors.get(model).getSupportedData().containsValue(dataType);
	}
	
	@Override
	public boolean isLanActionSupported(String model, Class<?> actionType) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		return lanThingModelDescriptors.get(model).getSupportedActions().containsValue(actionType);
	}

	@Override
	public boolean isLanEventSupported(String model, Class<?> eventType) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		return lanThingModelDescriptors.get(model).getSupportedEvents().containsValue(eventType);
	}
	
	@Override
	public boolean isLanEventFollowed(String model, Class<?> eventType) {
		if (!lanThingModelDescriptors.containsKey(model))
			throw new IllegalArgumentException(String.format("Unsupported model: '%s'", model));
		
		return lanThingModelDescriptors.get(model).getFollowedEvents().containsValue(eventType);
	}
	
	private class LanExecutionTraceInfo {
		public JabberId from;
		public JabberId to;
		public String sanzaId;
		public LanNode node;
		public LanExecution lanExecution;
		public long expiredTime;
		
		public LanExecutionTraceInfo(JabberId from, JabberId to, String sanzaId, LanNode node,
				LanExecution lanExecution, long expiredTime) {
			this.from = from;
			this.to = to;
			this.sanzaId = sanzaId;
			this.node = node;
			this.lanExecution = lanExecution;
			this.expiredTime = expiredTime;
		}
	}
	
	@Override
	public ICommunicator<?, ? extends ILanAddress, byte[]> getCommunicator(CommunicationNet communicationNet) {
		return getCommunicatorByNet(communicationNet);
	}
	
	@SuppressWarnings("unchecked")
	protected <OA extends ILanAddress, PA extends ILanAddress> ICommunicator<OA, PA, byte[]> getCommunicatorByNet(CommunicationNet communicationNet) {
		ICommunicator<OA, PA, byte[]> communicator = null;
		synchronized(this) {
			communicator = (ICommunicator<OA, PA, byte[]>)communicators.get(communicationNet);
			
			if (!communicator.isListening()) {
				LanCommunicationListener<OA, PA> oldLanExecutionAnswerListener = (LanCommunicationListener<OA, PA>)netToLanCommunicationListeners.remove(communicationNet);
				if (oldLanExecutionAnswerListener != null)
					communicator.removeCommunicationListener(oldLanExecutionAnswerListener);
				
				LanCommunicationListener<OA, PA> lanCommuncationListener = new LanCommunicationListener<OA, PA>(communicationNet);
				netToLanCommunicationListeners.put(communicationNet, lanCommuncationListener);
				communicator.addCommunicationListener(lanCommuncationListener);
				communicator.startToListen();
			}
		}
		
		return communicator;
	}
	
	private class LanCommunicationListener<OA, PA extends ILanAddress> implements ICommunicationListener<OA, PA, byte[]> {
		private CommunicationNet communicationNet;
		
		public LanCommunicationListener(CommunicationNet communicationNet) {
			this.communicationNet = communicationNet;
		}
		
		@Override
		public void sent(PA to, byte[] data) {}

		@Override
		public void received(PA from, byte[] data) {
			LpwanConcentrator.this.received(communicationNet, from, data);
		}
		
		@Override
		public void occurred(CommunicationException e) {}

		@Override
		public void addressChanged(OA newAddress, OA oldAddress) {}
	}
	
	private class ExpiredLanExecutionsChecker implements Runnable {
		private boolean stop;
		
		public ExpiredLanExecutionsChecker() {
			stop = false;
		}
		
		public void stop() {
			stop = true;
		}

		@Override
		public void run() {
			while (!stop) {
				checkExpiredLanExecutions();
				
				try {
					Thread.sleep(lanExecutionTimeoutCheckInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void checkExpiredLanExecutions() {
			long currentTime = Calendar.getInstance().getTime().getTime();
			synchronized (LpwanConcentrator.this) {
				for (List<LanExecutionTraceInfo> traceInfos : lanNodeToLanExecutionTraceInfos.values()) {
					List<LanExecutionTraceInfo> expiredLanExecutions = new ArrayList<>();
					for (LanExecutionTraceInfo traceInfo : traceInfos) {
						if (Long.compare(currentTime, traceInfo.expiredTime) > 0) {
							expiredLanExecutions.add(traceInfo);
						}
					}
					
					if (expiredLanExecutions.size() > 0) {
						for (LanExecutionTraceInfo expiredLanExecution : expiredLanExecutions) {
							traceInfos.remove(expiredLanExecution);
							processExpiredLanExecution(expiredLanExecution.from, expiredLanExecution.to, expiredLanExecution.sanzaId);
						}
					}
				}
			}
		}
	}

	@Override
	public void cleanNodes() {
		concentrator.cleanNodes();
	}

	@Override
	public void enableLanRouting() {
		if (logger.isInfoEnabled()) {
			logger.info("Enable LAN routing....");
		}
		
		if (expiredLanExecutionsChecker == null) {			
			expiredLanExecutionsChecker = new ExpiredLanExecutionsChecker();
			new Thread(expiredLanExecutionsChecker).start();
		}
		
		for (CommunicationNet net: communicators.keySet())
			getCommunicator(net);
		
		lanRouting = true;
		
		if (logger.isInfoEnabled()) {
			logger.info("LAN routing has beean enabled.");
		}
	}

	@Override
	public void disableLanRouting() {
		if (logger.isInfoEnabled()) {
			logger.info("Disable LAN routing....");
		}
		
		if (expiredLanExecutionsChecker != null) {
			expiredLanExecutionsChecker.stop();
			expiredLanExecutionsChecker = null;
		}
		
		synchronized (this) {
			for (CommunicationNet net : communicators.keySet()) {
				ICommunicator<?, ?, byte[]> communicator = communicators.get(net);
				removeLanExecutionAnswerListener(net, communicator);
				communicator.stopToListen();
			}
		}
		
		lanRouting = false;
		
		if (logger.isInfoEnabled()) {
			logger.info("LAN routing has been disabled.");
		}
	}

	@Override
	public boolean isLanRoutingEnabled() {
		return lanRouting;
	}

	@Override
	public boolean isConfigured(String thingId) {
		for (LanNode node : concentrator.getNodes()) {
			if (node.getThingId().equals(thingId)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void received(Iq iq) {
		if (iq.getObject() instanceof RemoveNode) {
			RemoveNode removeNode = iq.getObject();
			try {
				removeNode(removeNode.getLanId());
			} catch (NodeNotFoundException e) {
				throw new ProtocolException(new ItemNotFound(String.format("LAN node which's LAN ID is '%s' not be found.",
						removeNode.getLanId())));
			}
			
			if (logger.isInfoEnabled()) {
				logger.info("LAN node which's LAN ID is '{}' has been removed.", removeNode.getLanId());
			}
			
			Iq result = Iq.createResult(iq);
			chatServices.getIqService().send(result);
		} else if (iq.getObject() instanceof Notification) {
			JabberId target = iq.getTo();
			if (toThingItself(target)) {
				if (followProcessor == null) {
					if (logger.isWarnEnabled())
						logger.warn("Can't deliver LAN follow to concenter itself because follow processor is null.");
					
					return;
				}
				
				Notification notification = iq.getObject();
				followProcessor.process(target, notification.getEvent());
			} else {
				Notification notification = iq.getObject();
				int lanId = Integer.parseInt(iq.getTo().getResource());
				LanNode lanNode = getNode(lanId);
				if (!isLanEventFollowed(lanNode.getModel(), notification.getEvent().getClass())) {
					if (logger.isWarnEnabled()) {
						logger.warn("Event which's type is {} not followed by thing '{}'.",
								notification.getEvent().getClass().getName(), iq.getTo());
					}
					
					throw new ProtocolException(new NotAcceptable(String.format(
							"Event which's type is %s not followed by thing '%s'.",
							notification.getEvent().getClass().getName(), iq.getTo())));
				}
				
				// TODO Deliver notification to LAN node.
			}
		} else {
			super.received(iq);
		}
	}
	
	@Override
	public void setAckRequiredLanNotificationsPoolSize(int size) {
		ackRequiredLanNotificationsPoolSize = size;
	}

	@Override
	public int getAckRequiredLanNotificationsPoolSize() {
		return ackRequiredLanNotificationsPoolSize;
	}

	@Override
	public void addLanNotificationPreprocessor(ILanNotificationPreprocessor lanNotificationPreprocessor) {
		if (!lanNotificationPreprocessors.contains(lanNotificationPreprocessor))
			lanNotificationPreprocessors.add(lanNotificationPreprocessor);
	}

	@Override
	public boolean removeLanNotificationPreprocessor(ILanNotificationPreprocessor lanNotificationPreprocessor) {
		return lanNotificationPreprocessors.remove(lanNotificationPreprocessor);
	}
	
	@Override
	public void addLanReportPreprocessor(ILanReportPreprocessor lanReportPreprocessor) {
		if (!lanReportPreprocessors.contains(lanReportPreprocessor))
			lanReportPreprocessors.add(lanReportPreprocessor);
	}
	
	@Override
	public boolean removeLanReportPreprocessor(ILanReportPreprocessor lanReportPreprocessor) {
		return lanReportPreprocessors.remove(lanReportPreprocessor);
	}

	@Override
	public void setNotificationRexInitInternal(long rexInitInternal) {
		this.notificationInitRexInternal = rexInitInternal;
	}

	@Override
	public long getNotificationRexInitInternal() {
		return notificationInitRexInternal;
	}

	@Override
	public void setNotificationRexTimeout(long rexTimeout) {
		notificationRexTimeout = rexTimeout;
	}

	@Override
	public long getNotificationRexTimeout() {
		return notificationRexTimeout;
	}

	@Override
	public void pullLanFollows() {
		pullLanFollows(null);
	}
	
	@Override
	public void pullLanFollows(PullLanFollowsListener pullLanFollowsListener) {
		chatServices.getTaskService().execute(new PullLanFollowsTask(pullLanFollowsListener));
	}
	
	private class PullLanFollowsTask implements ITask<Iq> {
		private PullLanFollowsListener pullLanFollowsListener;
		
		public PullLanFollowsTask(PullLanFollowsListener pullLanFollowsListener) {
			this.pullLanFollowsListener = pullLanFollowsListener;
		}
		
		@Override
		public void trigger(IUnidirectionalStream<Iq> stream) {
			stream.send(new Iq(Iq.Type.GET, new LanFollows()));
		}

		@Override
		public void processResponse(IUnidirectionalStream<Iq> stream, Iq iq) {
			lanFollows = iq.getObject();
			
			if (pullLanFollowsListener != null)
				pullLanFollowsListener.lanFollowsPulled();
		}

		@Override
		public boolean processError(IUnidirectionalStream<Iq> stream, StanzaError error) {
			if (pullLanFollowsListener != null)
				pullLanFollowsListener.occurred(error);
			
			return true;
		}

		@Override
		public boolean processTimeout(IUnidirectionalStream<Iq> stream, Iq stanza) {
			if (pullLanFollowsListener != null)
				pullLanFollowsListener.occurred(new RemoteServerTimeout("Pull nodes timeout."));
			
			return true;
		}

		@Override
		public void interrupted() {
			// NOOP
		}
		
	}

	@Override
	public void registerFollowedEvent(Protocol protocol, Class<?> eventType) {
		chatServices.getStream().getOxmFactory().register(
				new IqProtocolChain(Notification.PROTOCOL).next(protocol),
				new CocParserFactory<>(eventType));
	}

	@Override
	public void setFollowProcessor(IFollowProcessor followProcessor) {
		this.followProcessor = followProcessor;
	}

	@Override
	public void setDefaultDataQoS(QoS qos) {
		defaultDataQoS = qos;
	}

	@Override
	public QoS getDefaultDataQoS() {
		return defaultDataQoS;
	}

	@Override
	public void setDataQoS(Class<?> dataType, QoS qos) {
		dataTypeToQoSs.put(dataType, qos);
	}

	@Override
	public QoS getDataQoS(Class<?> dataType) {
		return dataTypeToQoSs.get(dataType);
	}

	@Override
	public void setAddNodeTimeout(long addNodeTimeout) {
		concentrator.setAddNodeTimeout(addNodeTimeout);
	}

	@Override
	public long getAddNodeTimeout() {
		return concentrator.getAddNodeTimeout();
	}
	
	protected StanzaError errorCodeToError(String modelName, int errorCode) {
		StanzaError error = standardErrorCodeMapping.codeToError(errorCode);
		if (error != null)
			return error;
		
		String errorDescription = ThingsUtils.getExecutionErrorDescription(modelName, errorCode);	
		return new UndefinedCondition(StanzaError.Type.CANCEL, errorDescription);
	}
}
