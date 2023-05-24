package com.thefirstlineofcode.sand.client.things.simple.light;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UndefinedCondition;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UnexpectedRequest;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.things.simple.light.Flash;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;

public class FlashExecutor implements IExecutor<Flash> {
	private ISimpleLight light;
	
	public FlashExecutor(ISimpleLight light) {
		this.light = light;
	}

	@Override
	public Object execute(Iq iq, Flash action) {
		if (light.getSwitchState() != SwitchState.CONTROL)
			throw new ProtocolException(new UnexpectedRequest(ThingsUtils.getExecutionErrorDescription(
					light.getThingModel(), ISimpleLight.ERROR_CODE_NOT_REMOTE_CONTROL_STATE)));
		
		int repeat = action.getRepeat();
		if (repeat == 0)
			repeat = 1;
		
		try {
			light.flash(repeat);
		} catch (ExecutionException e) {
			throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
					ThingsUtils.getExecutionErrorDescription(light.getThingModel(), e.getErrorNumber())));
		}
		
		return null;
	}
}
