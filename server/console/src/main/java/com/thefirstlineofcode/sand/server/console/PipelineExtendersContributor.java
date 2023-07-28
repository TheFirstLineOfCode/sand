package com.thefirstlineofcode.sand.server.console;

import org.pf4j.Extension;

import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentService;
import com.thefirstlineofcode.granite.framework.core.adf.IApplicationComponentServiceAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.IPipelineExtendersConfigurator;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.PipelineExtendersConfigurator;
import com.thefirstlineofcode.sand.server.concentrator.NodeConfirmedEvent;

@Extension
public class PipelineExtendersContributor extends PipelineExtendersConfigurator
		implements IApplicationComponentServiceAware {
	private NodeConfirmedListener nodeConfirmedListener;
	
	@Override
	protected void configure(IPipelineExtendersConfigurator configurator) {
		configurator.registerEventListener(NodeConfirmedEvent.class, nodeConfirmedListener);
	}
	
	@Override
	public void setApplicationComponentService(IApplicationComponentService appComponentService) {
		nodeConfirmedListener = appComponentService.getAppComponent("node.confirmed.listener", NodeConfirmedListener.class);
	}
}
