package com.thefirstlinelinecode.sand.protocols.lpwan.concentrator;

import java.util.Map;

import com.thefirstlinelinecode.sand.protocols.concentrator.ConcentratorDescriptor;
import com.thefirstlinelinecode.sand.protocols.lpwan.concentrator.friends.PullLanFollows;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.IThingTypeDescriptor;
import com.thefirstlineofcode.sand.protocols.thing.MultiTypeThingTypeDescriptor;

public class LpwanConcentratorDescriptor extends MultiTypeThingTypeDescriptor {
	public LpwanConcentratorDescriptor() {
		super("lpwan-concentrator", new IThingTypeDescriptor[] {new ConcentratorDescriptor()});
	}

	public Map<Protocol, Class<?>> getSupportedActions() {
		Map<Protocol, Class<?>> supportedActions = super.getSupportedActions();
		supportedActions.put(PullLanFollows.PROTOCOL, PullLanFollows.class);
		
		return supportedActions;
	}
}
