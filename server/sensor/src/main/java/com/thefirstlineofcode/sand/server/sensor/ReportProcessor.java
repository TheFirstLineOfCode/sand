package com.thefirstlineofcode.sand.server.sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.FeatureNotImplemented;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.annotations.Dependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.granite.framework.core.repository.IInitializable;
import com.thefirstlineofcode.sand.protocols.sensor.Report;
import com.thefirstlineofcode.sand.protocols.sensor.Report.QoS;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class ReportProcessor implements IXepProcessor<Iq, Report>, IReportDispatcher, IInitializable {
	private Logger logger = LoggerFactory.getLogger(ReportProcessor.class);
	
	@Dependency(IReportDispatcher.NAME_APP_COMPONENT_REPORT_DISPATCHER)
	private ReportDispatcher reportDispatcher;
	
	private Map<Class<?>, List<IDataListener<?>>> dataToListeners;
	
	@BeanDependency
	private IThingManager thingManager;
	
	public ReportProcessor() {
		dataToListeners = new HashMap<>();
	}
	
	@Override
	public void init() {
		reportDispatcher.setReal(this);
	}
	
	@Override
	public void process(IProcessingContext context, Iq iq, Report report) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		if (report.getQos() == QoS.AT_MOST_ONCE) {
			deliverData(context, iq, report);
		} else if (report.getQos() == QoS.AT_LEAST_ONCE) {
			Iq ack = new Iq(Iq.Type.RESULT, iq.getId());
			context.write(ack);
			
			deliverData(context, iq, report);
		} else { // QoS.EXACTLY_ONCE
			throw new ProtocolException(new FeatureNotImplemented());
		}
	}

	private void deliverData(IProcessingContext context, Iq iq, Report report) {
		JabberId reporter = getReporter(context, iq);
		
		Object data = report.getData();
		List<IDataListener<?>> dataListeners = dataToListeners.get(data.getClass());
		
		if (dataListeners == null)
			return;
		
		for (IDataListener<?> dataListener : dataListeners) {
			try {				
				dataReceived(context, dataListener, reporter, data);
			} catch (Exception e) {
				if (logger.isErrorEnabled())
					logger.error("Error occurred when reporting data to data listener.", e);
			}
		}
	}
	
	private JabberId getReporter(IProcessingContext context, Iq iq) {
		JabberId reporter = iq.getFrom();
		
		if (reporter == null)
			return context.getJid();
		
		JabberId sender = context.getJid();
		if (sender.getResource() != null && !RegisteredThing.DEFAULT_RESOURCE_NAME.equals(sender.getResource()))
			throw new ProtocolException(new BadRequest("Isn't the sender a edge thing?"));
		
		if (!sender.getBareId().equals(iq.getFrom().getBareId()))
			throw new ProtocolException(new BadRequest(String.format("Illegal reporter '%s' sent by '%s'.", reporter, sender)));
			
		return iq.getFrom();
	}

	@SuppressWarnings("unchecked")
	private <T> void dataReceived(IProcessingContext context, IDataListener<T> dataListener, JabberId reporter, Object data) {
		dataListener.dataReceived(context, reporter, (T)data);
	}
	
	@Override
	public synchronized <T> void addDataListener(Class<T> dataType, IDataListener<T> dataListener) {
		List<IDataListener<?>> dataListeners = dataToListeners.get(dataType);
		if (dataListeners == null) {
			dataListeners = new ArrayList<>();
			dataToListeners.put(dataType, dataListeners);
		}
		
		if (!dataListeners.contains(dataListener))
			dataListeners.add(dataListener);
	}

	@Override
	public synchronized <T> boolean removeDataListener(Class<T> dataType, IDataListener<T> dataListener) {
		List<IDataListener<?>> dataListeners = dataToListeners.get(dataType);
		if (dataListeners == null)
			return false;
		
		boolean removed = dataListeners.remove(dataListener);
		if (dataListeners.isEmpty())
			dataToListeners.remove(dataType);
		
		return removed;
	}
}
