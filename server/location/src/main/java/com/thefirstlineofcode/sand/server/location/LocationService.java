package com.thefirstlineofcode.sand.server.location;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.ItemNotFound;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.sand.protocols.location.ThingLocation;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;
import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.IConcentratorFactory;
import com.thefirstlineofcode.sand.server.things.IThingManager;

@Component
@Transactional
public class LocationService implements ILocationService {
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	@Override
	public List<ThingLocation> locateThings(List<String> thingIds) {
		List<ThingLocation> thingLocations = new ArrayList<>();
		
		for (String thingId : thingIds) {
			if (!thingManager.thingIdExists(thingId)) {
				throw new ProtocolException(new ItemNotFound(String.format(
						"The thing which's ID is '%s' not exists. ", thingId)));
			}
			
			ThingLocation thingLocation = new ThingLocation();
			thingLocation.setThingId(thingId);
			
			String location = getLocationByThingId(thingId);
			if (location == null)
				throw new ProtocolException(new ItemNotFound("Can't fetch location for this thing."));
			
			thingLocation.setLocation(location);
			thingLocations.add(thingLocation);
		}
		
		return thingLocations;
	}
	
	@Override
	public String getLocationByThingId(String thingId) {
		String location;
		String thingName = thingManager.getEdgeThingManager().getThingNameByThingId(thingId);
		if (thingName != null) {
			location = thingName;
		} else {
			String concentratorThingName = concentratorFactory.getConcentratorThingNameByNodeThingId(thingId);
			if (concentratorThingName == null)
				return null;
			
			String concentratorThingId = thingManager.getEdgeThingManager().getThingIdByThingName(concentratorThingName);
			location = String.format("%s/%d", concentratorThingId,
					concentratorFactory.getConcentrator(concentratorThingId).getNodeByThingId(thingId).getLanId());
		}
		
		return location;
	}
	
	@Override
	public String getThingIdByJid(JabberId jid) {
		if (jid.getResource() == null)
			return getThingIdByLocation(jid.getNode());
		
		return getThingIdByLocation(String.format("%s/%s", jid.getNode(), jid.getResource()));
	}

	@Override
	public String getThingIdByLocation(String location) {
		if (location == null)
			throw new IllegalArgumentException("Null location.");
		
		int indexOfSlash = location.indexOf('/');
		if (indexOfSlash == -1)
			return thingManager.getEdgeThingManager().getThingIdByThingName(location);
		
		if (indexOfSlash == location.length() - 1)
			throw new IllegalArgumentException("Illegal location string.");
		
		String edgeThingName = location.substring(0, indexOfSlash);
		if (!thingManager.getEdgeThingManager().thingNameExists(edgeThingName)) {
			return null;
		}
		
		String sLanId = location.substring(indexOfSlash + 1, location.length());		
		if (RegisteredEdgeThing.DEFAULT_RESOURCE_NAME.equals(sLanId)) {
			return thingManager.getEdgeThingManager().getThingIdByThingName(edgeThingName);
		} else {
			int lanId;
			
			try {
				lanId = Integer.parseInt(sLanId);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("Invalid LAN ID string: %s.", sLanId));
			}
			
			IConcentrator concentrator = concentratorFactory.getConcentrator(thingManager.getEdgeThingManager().getThingIdByThingName(edgeThingName));
			Node node = concentrator.getNodeByLanId(lanId);
			if (node == null)
				return null;
			
			return node.getThingId();
		}
	}

}
