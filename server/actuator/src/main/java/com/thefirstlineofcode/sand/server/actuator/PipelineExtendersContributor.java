package com.thefirstlineofcode.sand.server.actuator;

import java.util.Map.Entry;

import org.pf4j.Extension;

import com.thefirstlineofcode.basalt.xmpp.core.IqProtocolChain;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.ProtocolParserFactory;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.ProtocolTranslatorFactory;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.actuator.oxm.ExecutionParserFactory;
import com.thefirstlineofcode.sand.protocols.actuator.oxm.ExecutionTranslatorFactory;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	@BeanDependency
	private IThingManager thingManager;
		
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		ExecutionListener executionListener = new ExecutionListener();
		
		configurator.
			registerParserFactory(
					new ProtocolParserFactory<>(new IqProtocolChain(Execution.PROTOCOL), new ExecutionParserFactory())).
			registerSingletonXepProcessor(
					new IqProtocolChain(Execution.PROTOCOL), new ExecutionProcessor()).
			registerTranslatorFactory(
					new ProtocolTranslatorFactory<>(Execution.class, new ExecutionTranslatorFactory())).
			registerEventListener(
					ExecutionEvent.class, executionListener).
			registerIqResultProcessor(
					executionListener);
		
		for (String model : thingManager.getModels()) {
			IThingModelDescriptor modelDescriptor = thingManager.getModelDescriptor(model);
			if (modelDescriptor.isActuator()) {
				for (Entry<Protocol, Class<?>> entry : modelDescriptor.getSupportedActions().entrySet()) {
					configurator.registerCocParser(
							new IqProtocolChain(Execution.PROTOCOL).next(entry.getKey()), entry.getValue());
					configurator.registerCocParser(new IqProtocolChain(entry.getKey()), entry.getValue());
					configurator.registerCocTranslator(entry.getValue());
				}
				
				for (Entry<Protocol, Class<?>> entry : modelDescriptor.getSupportedActionResults().entrySet()) {
					configurator.registerCocParser(new IqProtocolChain(entry.getKey()), entry.getValue());
					configurator.registerCocTranslator(entry.getValue());
				}
			}
		}
	}
}
