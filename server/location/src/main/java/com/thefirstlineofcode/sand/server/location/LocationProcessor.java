package com.thefirstlineofcode.sand.server.location;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ServiceUnavailable;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.config.IConfiguration;
import com.thefirstlineofcode.granite.framework.core.config.IConfigurationAware;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.protocols.location.ThingLocation;
import com.thefirstlineofcode.sand.protocols.location.LocateThings;

public class LocationProcessor implements IXepProcessor<Iq, LocateThings>, IConfigurationAware {
	private static final String CONFIG_KEY_ENABLED = "enabled";
	
	@BeanDependency
	private ILocationService locationService;
	
	private boolean enabled;
	
	@Override
	public void process(IProcessingContext context, Iq iq, LocateThings xep) {
		if (enabled) {
			doProcess(context, iq, xep);
		} else {
			ServiceUnavailable error = StanzaError.create(iq, ServiceUnavailable.class);
			context.write(error);
		}
	}

	private void doProcess(IProcessingContext context, Iq iq, LocateThings xep) {
		if (iq.getType() != Iq.Type.GET)
			throw new ProtocolException(new BadRequest("IQ type should be 'GET'."));
		
		List<String> thingIds = xep.getThingIds();
		
		if (thingIds == null || thingIds.size() == 0)
			throw new ProtocolException(new BadRequest("Null thing IDs or zero length thing IDs."));
		
		List<ThingLocation> thingLocations = locationService.locateThings(thingIds);
		
		xep = new LocateThings();
		xep.setThingLocations(thingLocations);
		Iq result = new Iq(Iq.Type.RESULT, xep, iq.getId());
		
		context.write(result);
	}
	
	@Override
	public void setConfiguration(IConfiguration configuration) {
		enabled = configuration.getBoolean(CONFIG_KEY_ENABLED, false);
	}
}
