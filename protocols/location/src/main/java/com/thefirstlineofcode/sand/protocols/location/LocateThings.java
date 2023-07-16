package com.thefirstlineofcode.sand.protocols.location;

import java.util.List;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.Array;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.TextOnly;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.Validate;
import com.thefirstlineofcode.basalt.oxm.coc.validation.annotations.ValidationClass;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;

@ValidationClass
@ProtocolObject(namespace = "urn:leps:tuxp:location", localName = "locate-things")
public class LocateThings {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:location", "locate-things");
	
	@Array(value = String.class, elementName = "thing-id")
	@TextOnly
	private List<String> thingIds;
	
	@Array(value = ThingLocation.class, elementName = "thing-location")
	private List<ThingLocation> thingLocations;
	
	public LocateThings() {}

	public List<String> getThingIds() {
		return thingIds;
	}

	public void setThingIds(List<String> thingIds) {
		this.thingIds = thingIds;
	}

	public List<ThingLocation> getThingLocations() {
		return thingLocations;
	}

	public void setThingLocations(List<ThingLocation> thingLocations) {
		this.thingLocations = thingLocations;
	}
	
	@Validate("/")
	public void validateLocateThings(LocateThings locateThings) {
		if (locateThings.getThingIds() != null && locateThings.getThingLocations() != null)
			throw new ProtocolException(new BadRequest("Only one child element('thing-ids' or 'thing-locations') is allowed."));
	}
}
