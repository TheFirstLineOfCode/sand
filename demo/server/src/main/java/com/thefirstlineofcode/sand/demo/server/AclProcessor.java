package com.thefirstlineofcode.sand.demo.server;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.BadRequest;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.Forbidden;
import com.thefirstlineofcode.granite.framework.core.annotations.BeanDependency;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IProcessingContext;
import com.thefirstlineofcode.granite.framework.core.pipeline.stages.processing.IXepProcessor;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlEntry;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList;
import com.thefirstlineofcode.sand.demo.protocols.AccessControlList.Role;

public class AclProcessor implements IXepProcessor<Iq, AccessControlList> {
	@BeanDependency
	private IAclService aclService;

	@Override
	public void process(IProcessingContext context, Iq iq, AccessControlList xep) {
		// TODO Auto-generated method stub
		if (iq.getType() == Iq.Type.GET) {
			if (xep.getEntries() != null && xep.getEntries().size() != 0)
				throw new ProtocolException(new BadRequest("Access control list entries must be null when IQ type is set to 'get'."));
			
			getAccessControlList(context, iq, xep);
		} else if (iq.getType() == Iq.Type.SET) {
			if (xep.getEntries() == null || xep.getEntries().isEmpty())
				throw new ProtocolException(new BadRequest("Access control list entries mustn't be empty when IQ type is set to 'set'."));
			
			setAccessControlList(context, iq, xep);
		} else {
			throw new ProtocolException(new BadRequest("Attribute 'type' should be set to 'get' or 'set'."));
		}
	}

	private void setAccessControlList(IProcessingContext context, Iq iq, AccessControlList xep) {
		// TODO Auto-generated method stub
		
	}

	private void getAccessControlList(IProcessingContext context, Iq iq, AccessControlList xep) {
		AccessControlList acl = null;
		if (xep.getThingId() == null) {
			acl = aclService.getUserAcl(context.getJid().getNode());
		} else {
			Role role = aclService.getRole(context.getJid().getBareIdString(), xep.getThingId());
			if (role == null)
				throw new ProtocolException(new Forbidden());
			
			if (role == AccessControlList.Role.OWNER) {
				acl = aclService.getOwnerAcl(xep.getThingId());
			} else {
				AccessControlEntry entry = new AccessControlEntry();
				entry.setThingId(xep.getThingId());
				entry.setUser(context.getJid().getBareIdString());
				entry.setRole(role);
				
				acl = new AccessControlList();
				acl.add(entry);
			}
		}
		
		context.write(new Iq(Iq.Type.RESULT, acl, iq.getId()));
	}

}
