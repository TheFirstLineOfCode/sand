package com.thefirstlineofcode.sand.server.friends;

import org.pf4j.Extension;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.routing.ProtocolTranslatorFactory;
import com.thefirstlineofcode.sand.protocols.thing.tacp.Notification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.oxm.NotificationTranslatorFactory;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator {
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.
			registerTranslatorFactory(
					new ProtocolTranslatorFactory<Notification>(Notification.class,
							new NotificationTranslatorFactory())
		);
	}
}
