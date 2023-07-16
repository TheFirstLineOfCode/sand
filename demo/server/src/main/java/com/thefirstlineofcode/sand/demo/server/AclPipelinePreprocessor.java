package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlinelinecode.sand.protocols.concentrator.AddNode;
import com.thefirstlinelinecode.sand.protocols.concentrator.Node;
import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xeps.ping.Ping;
import com.thefirstlineofcode.basalt.xmpp.core.JabberId;
import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.InternalServerError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAllowed;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.NotAuthorized;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.auth.IAccountManager;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.parsing.IPipelinePreprocessor;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;
import com.thefirstlineofcode.sand.protocols.location.LocateThings;
import com.thefirstlineofcode.sand.protocols.operator.ApproveFollow;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredThing;
import com.thefirstlineofcode.sand.protocols.webrtc.signaling.Signal;
import com.thefirstlineofcode.sand.server.concentrator.IConcentrator;
import com.thefirstlineofcode.sand.server.concentrator.IConcentratorFactory;
import com.thefirstlineofcode.sand.server.things.IThingManager;
import com.thefirstlineofcode.sand.server.things.Thing;

public class AclPipelinePreprocessor implements IPipelinePreprocessor {
	@BeanDependency
	private IAccountManager accountManager;
	
	@BeanDependency
	private IThingManager thingManager;
	
	@BeanDependency
	private IConcentratorFactory concentratorFactory;
	
	@BeanDependency
	private IAclService aclService;
	
	@Override
	public String beforeParsing(JabberId from, String message) {
		return message;
	}

	@Override
	public Object afterParsing(JabberId from, Object object) {
		if (!(object instanceof Iq))
			return object;
		
		Iq iq = (Iq)object;
		if (iq.getObject() instanceof Execution) {
			return afterParsingExecution(from, iq);
		} else if (iq.getObject() instanceof LocateThings) {
			return afterParsingLocateThings(from, iq, (LocateThings)iq.getObject());
		} else if (iq.getObject() instanceof Ping) {
			return afterParsingPing(from, iq);
		} else if (iq.getObject() instanceof Signal) {
			return afterParsingSignal(from, iq);
		} else if (isConcentratorProtocolGroupObject(iq.getObject())) {
			return afterParsingConcentratorGroupObject(from, iq);			
		} else if (iq.getObject() instanceof ApproveFollow) {
			return afterParsingApproveFollow(from, iq);
		} else {
			return object;
		}
	}
	
	private Object afterParsingApproveFollow(JabberId from, Iq iq) {
		String approver = from.getNode();
		
		ApproveFollow approveFollow = iq.getObject();
		JabberId friend = approveFollow.getFriend();
		String thingId = getThingId(friend);
		
		if (!isOwner(approver, thingId))
			throw new ProtocolException(new NotAuthorized("Not owner."));
		
		return iq;
	}
	
	private Object afterParsingConcentratorGroupObject(JabberId from, Iq iq) {
		if (iq.getType() == Iq.Type.GET)
			return iq;
		
		if (iq.getObject() instanceof AddNode)
			return iq;
			
		String sender = from.getNode();
		String thingId = getThingId(iq.getTo());
		
		if (!isOwner(sender, thingId))
			throw new ProtocolException(new NotAuthorized("Not owner."));
		
		return iq;
	}

	private boolean isConcentratorProtocolGroupObject(Object obj) {
		if (obj == null)
			return false;
		
		ProtocolObject pObject = obj.getClass().getAnnotation(ProtocolObject.class);
		if (pObject == null)
			throw new ProtocolException(new InternalServerError("Not project object."));
		
		return pObject.namespace().equals("urn:leps:tuxp:concentrator");
	}

	private Object afterParsingPing(JabberId from, Iq iq) {		
		if (!isOwnerOrController(from.getNode(), getThingId(iq.getTo())))
			throw new ProtocolException(new NotAuthorized("Neither owner nor controller of thing."));
		
		return iq;
	}
	
	private Object afterParsingSignal(JabberId from, Iq iq) {
		if (isThing(from) && isUser(iq.getTo()))
			return iq;
		
		if (!isOwnerOrController(from.getNode(), getThingId(iq.getTo())))
			throw new ProtocolException(new NotAuthorized("Neither owner nor controller of thing."));
		
		return iq;
	}
	
	private boolean isUser(JabberId to) {
		return accountManager.exists(to.getBareIdString());
	}

	private boolean isThing(JabberId from) {
		return thingManager.thingIdExists(from.getBareIdString());
	}

	private Object afterParsingExecution(JabberId from, Iq iq) {
		if (iq.getType() == Iq.Type.RESULT)
			return iq;
		
		String sender = from.getNode();
		String thingId = getThingId(iq.getTo());
		
		Execution execution = iq.getObject();
		if (isTacpAction(execution.getAction())) {
			if (!isOwner(sender, thingId))
				throw new ProtocolException(new NotAuthorized("TACP actions can be sent by owner only."));
			
			return iq;
		}
		
		if (!isOwnerOrController(sender, thingId))
			throw new ProtocolException(new NotAuthorized("Neither owner nor controller of thing."));
		
		return iq;
	}

	private boolean isTacpAction(Object action) {
		ProtocolObject pObject = action.getClass().getAnnotation(ProtocolObject.class);
		if (pObject == null)
			throw new ProtocolException(new InternalServerError("The action isn't a project object."));
		
		return pObject.namespace().startsWith("urn:leps:tuxp:");
	}

	private Object afterParsingLocateThings(JabberId from, Iq iq, LocateThings locateThings) {
		if (iq.getType() == Iq.Type.RESULT)
			return iq;
		
		if (iq.getType() != Iq.Type.GET )
			throw new ProtocolException(new BadRequest("IQ type for LEPs location protocol Must be Iq.Type.GET."));
		
		if (locateThings.getThingIds() == null || locateThings.getThingIds().size() == 0)
			throw new ProtocolException(new BadRequest("Null thing IDs or zero length thing IDs."));
		
		for (String thingId : locateThings.getThingIds()) {			
			Role role = aclService.getRole(from.getNode(), thingId);
			
			if (Role.OWNER != role && Role.CONTROLLER != role) {
				throw new ProtocolException(new NotAuthorized(String.format("You need authorization to locate the thing. Thing ID of the thing is '%s'.", thingId)));
			}
		}
		
		return iq;
	}
	
	private boolean isOwner(String sender, String thingId) {
		if (!accountManager.exists(sender)) {
			// Did a thing send the message?
			throw new ProtocolException(new NotAllowed());
		}
		
		return Role.OWNER == aclService.getRole(sender, thingId);
	}
	
	private boolean isOwnerOrController(String sender, String thingId) {
		if (!accountManager.exists(sender)) {
			// Did a thing send the message?
			throw new ProtocolException(new NotAllowed());
		}
		
		Role role = aclService.getRole(sender, thingId);
		return Role.OWNER == role || Role.CONTROLLER == role;
	}

	private String getThingId(JabberId to) {
		if (to == null)
			throw new ProtocolException(new BadRequest("Null target."));
		
		String thingId = null;
		if (to.getResource() != null && !RegisteredThing.DEFAULT_RESOURCE_NAME.equals(to.getResource())) {
			// The thing is a LAN node.
			String concentratorThingName = to.getNode();
			String sLanId = to.getResource();
			
			Thing concentratorThing = thingManager.getByThingName(concentratorThingName);
			if (concentratorThing == null) {
				throw new ProtocolException(new BadRequest(String.format("Concentrator which's thing name is '%s' not exists.", concentratorThingName)));
			}
			
			if (!thingManager.isConcentrator(concentratorThing.getModel())) {
				throw new ProtocolException(new BadRequest(String.format("Thing which's thing name is '%s' isn't a concentrator.", concentratorThingName)));				
			}
			
			int lanId;
			try {
				lanId = Integer.parseInt(sLanId);
			} catch (NumberFormatException e) {
				throw new ProtocolException(new BadRequest("Invalid LAN ID string."));
			}
			
			IConcentrator concentrator = concentratorFactory.getConcentrator(concentratorThing.getThingId());
			Node node = concentrator.getNodeByLanId(lanId);
			if (node == null) {
				throw new ProtocolException(new BadRequest(String.format("There isn't a node which's LAN ID is '%s' under Concentrator which's thing name is '%s'.", concentratorThingName)));
			}
			
			Thing nodeThing = thingManager.getByThingId(node.getThingId());
			if (nodeThing == null)
				throw new RuntimeException(String.format("Thing which's thing ID is %s not exists.", node.getThingId()));
			
			thingId = nodeThing.getThingId();
		} else {
			// The thing is an edge thing.
			Thing thing = thingManager.getByThingName(to.getNode());
			if (thing == null)
				throw new ProtocolException(new BadRequest(String.format("Thing which's thing name is '%s' not exists.", to.getNode())));
				
			thingId = thing.getThingId();
		}
		
		return thingId;
	}
}
