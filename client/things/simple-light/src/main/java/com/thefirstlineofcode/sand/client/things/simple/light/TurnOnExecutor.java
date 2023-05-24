package com.thefirstlineofcode.sand.client.things.simple.light;

import com.thefirstlineofcode.basalt.xmpp.core.ProtocolException;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.StanzaError;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UndefinedCondition;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.error.UnexpectedRequest;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;
import com.thefirstlineofcode.sand.protocols.things.simple.light.TurnOn;

/**
 * @author xb.zou
 */
public class TurnOnExecutor implements IExecutor<TurnOn> {
    private final ISimpleLight light;

    public TurnOnExecutor(ISimpleLight light) {
        this.light = light;
    }

    @Override
    public Object execute(Iq iq, TurnOn action) throws ProtocolException {
        if (light.getSwitchState() != SwitchState.CONTROL) {
            throw new ProtocolException(new UnexpectedRequest(ThingsUtils.getExecutionErrorDescription(
                    light.getThingModel(), ISimpleLight.ERROR_CODE_NOT_REMOTE_CONTROL_STATE)));
        }

        if (light.getLightState() == ISimpleLight.LightState.OFF) {
            try {
                light.turnOn();
            } catch (ExecutionException e) {
                throw new ProtocolException(new UndefinedCondition(StanzaError.Type.CANCEL,
                        ThingsUtils.getExecutionErrorDescription(light.getThingModel(), e.getErrorNumber())));

            }
        }
        
        return null;
    }
}
