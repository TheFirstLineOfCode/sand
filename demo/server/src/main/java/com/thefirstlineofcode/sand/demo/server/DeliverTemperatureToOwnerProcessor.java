package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.demo.protocols.DeliverTemperatureToOwner;

public class DeliverTemperatureToOwnerProcessor implements IXepProcessor<Iq, DeliverTemperatureToOwner> {
	@BeanDependency
	private CelsiusDegreeListener celsiusDegreeListener;
	
	@Override
	public void process(IProcessingContext context, Iq iq, DeliverTemperatureToOwner deliverTemperatureToOwner) {
		if (iq.getType() != Iq.Type.SET)
			throw new ProtocolException(new BadRequest("IQ type should be 'SET'."));
		
		if (deliverTemperatureToOwner.isEnabled())
			celsiusDegreeListener.enableDeliverTemperatureToOwner(true);
		else
			celsiusDegreeListener.enableDeliverTemperatureToOwner(false);
		
		context.write(Iq.createResult(iq));
	}

}
