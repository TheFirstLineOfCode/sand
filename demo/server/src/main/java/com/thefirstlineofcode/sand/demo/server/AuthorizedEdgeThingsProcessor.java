package com.thefirstlineofcode.sand.demo.server;

import java.util.ArrayList;
import java.util.List;

import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedEdgeThing;
import com.thefirstlineofcode.sand.demo.protocols.AuthorizedEdgeThings;
import com.thefirstlineofcode.sand.demo.protocols.LanNode;
import com.thefirstlineofcode.sand.protocols.location.ThingLocation;
import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.IConcentratorFactory;
import com.thefirstlineofcode.sand.server.location.ILocationService;
import com.thefirstlineofcode.sand.server.things.IThingManager;

public class AuthorizedEdgeThingsProcessor implements IXepProcessor<Iq, AuthorizedEdgeThings> {
	@BeanDependency
	private IAclService aclService;
	
	@BeanDependency
	private ILocationService locationService;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;

	@Override
	public void process(IProcessingContext context, Iq iq, AuthorizedEdgeThings xep) {
		AccessControlList acl = aclService.getUserAcl(context.getJid().getNode());
		if (acl.getEntries() == null || acl.getEntries().size() == 0) {
			context.write(Iq.createResult(iq, new AuthorizedEdgeThings()));
		} else {
			List<String> thingIds = getAclThingIds(acl);
			List<ThingLocation> thingLocations = locationService.locateThings(thingIds);
			
			List<AuthorizedEdgeThing> things = getAuthorizedEdgeThings(acl, thingLocations);
			
			context.write(Iq.createResult(iq, new AuthorizedEdgeThings(things)));
		}
	}
	
	private List<String> getAclThingIds(AccessControlList acl) {
		List<String> thingIds = new ArrayList<>();
		
		for (AccessControlEntry ace : acl.getEntries()) {
			thingIds.add(ace.getThingId());
		}
		
		return thingIds;
	}

	private List<AuthorizedEdgeThing> getAuthorizedEdgeThings(AccessControlList acl, List<ThingLocation> thingLocations) {
		List<AuthorizedEdgeThing> things = new ArrayList<>();
		for (int i = 0; i < thingLocations.size(); i++) {
			AuthorizedEdgeThing thing = new AuthorizedEdgeThing();
			thing.setThingId(thingLocations.get(i).getThingId());
			thing.setThingName(thingLocations.get(i).getLocation());
			thing.setModel(thingManager.getModel(thing.getThingId()));
			thing.setRole(getRole(acl, thing.getThingId()));
			
			thing.setConcentrator(thingManager.getModelDescriptor(thingManager.getModel(
					thing.getThingId())).isConcentrator());
			if (thing.isConcentrator()) {
				IConcentrator concentrator = concentratorFactory.getConcentrator(thing.getThingId());
				List<LanNode> lanNodes = new ArrayList<>();
				Node[] nodes = concentrator.getNodes();
				if (nodes != null && nodes.length > 0) {
					for (Node node : nodes) {
						LanNode lanNode = new LanNode();
						lanNode.setThingId(node.getThingId());
						lanNode.setModel(node.getModel());
						lanNode.setLanId(node.getLanId());
						lanNode.setCommunicationNet(node.getCommunicationNet());
						lanNodes.add(lanNode);
					}
				}
				thing.setNodes(lanNodes);
			}
			
			things.add(thing);
		}
		
		return things;
	}
	
	private Role getRole(AccessControlList acl, String thingId) {
		for (AccessControlEntry ace : acl.getEntries()) {
			if (ace.getThingId().equals(thingId))
				return ace.getRole();
		}
		
		return null;
	}
}
