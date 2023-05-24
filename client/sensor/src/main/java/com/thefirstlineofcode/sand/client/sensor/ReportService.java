package com.thefirstlineofcode.sand.client.sensor;

import java.util.HashMap;
import java.util.Map;

import com.thefirstlineofcode.basalt.oxm.coc.CocParserFactory;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.IChatServices;
import com.thefirstlineofcode.chalk.core.stanza.IIqListener;
import com.thefirstlineofcode.sand.protocols.sensor.Report;

public class ReportService implements IReportService, IIqListener {
	private IChatServices chatServices;
	private IReporter reporter;
	private Map<Class<?>, IDataProcessor<?>> dataToProcessors;
	private boolean started;
	
	public ReportService(IChatServices chatServices) {
		this.chatServices = chatServices;
		
		dataToProcessors = new HashMap<>();
		started = false;
	}
	
	@Override
	public <T> void listen(Class<T> dataType, IDataProcessor<T> dataProcessor) {
		ProtocolObject protocolObject = dataType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null)
			throw new IllegalArgumentException("Isn't data type a protocol object?");
		
		Protocol protocol = new Protocol(protocolObject.namespace(), protocolObject.localName());
		chatServices.getOxmFactory().register(new IqProtocolChain(Report.PROTOCOL).next(protocol),
				new CocParserFactory<>(dataType));
		
		dataToProcessors.put(dataType, dataProcessor);
	}

	@Override
	public IReporter getReporter() {
		if (reporter != null)
			return reporter;
		
		reporter = new Reporter(chatServices);
		return reporter;
	}

	@Override
	public void received(Iq iq) {
		Report report = iq.getObject();
		Object data = report.getData();
		
		IDataProcessor<?> dataProcessor = dataToProcessors.get(data.getClass());
		if (dataProcessor != null)
			processDataByProcessor(iq.getFrom(), data, dataProcessor);
	}

	@SuppressWarnings("unchecked")
	private <T> void processDataByProcessor(JabberId from, Object data, IDataProcessor<?> dataProcessor) {
		((IDataProcessor<T>)dataProcessor).processData(from, (T)data);
	}

	@Override
	public void start() {
		if (started)
			return;
		
		chatServices.getIqService().addListener(Report.PROTOCOL, this);
		started = true;
	}

	@Override
	public void stop() {
		chatServices.getIqService().removeListener(Report.PROTOCOL);
		started = false;
	}

	@Override
	public void stopListening(Class<?> dataType) {
		ProtocolObject protocolObject = dataType.getAnnotation(ProtocolObject.class);
		if (protocolObject == null)
			throw new IllegalArgumentException("Isn't data type a protocol object?");
		
		Protocol protocol = new Protocol(protocolObject.namespace(), protocolObject.localName());
		chatServices.getOxmFactory().unregister(new IqProtocolChain(Report.PROTOCOL).next(protocol));
		
		dataToProcessors.remove(dataType);
	}

	@Override
	public boolean isStarted() {
		return started;
	}

}
