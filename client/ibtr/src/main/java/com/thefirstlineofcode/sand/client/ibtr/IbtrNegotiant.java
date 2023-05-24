package com.thefirstlineofcode.sand.client.ibtr;

import java.util.List;

import com.thefirstlineofcode.basalt.xmpp.core.stream.Feature;
import com.thefirstlineofcode.chalk.core.stream.AbstractStreamer;
import com.thefirstlineofcode.chalk.core.stream.INegotiationContext;
import com.thefirstlineofcode.chalk.core.stream.NegotiationException;
import com.thefirstlineofcode.chalk.core.stream.negotiants.AbstractStreamNegotiant;
import com.thefirstlineofcode.chalk.network.ConnectionException;
import com.thefirstlineofcode.sand.protocols.ibtr.Register;

public class IbtrNegotiant extends AbstractStreamNegotiant {
	
	
	@Override
	protected void doNegotiate(INegotiationContext context) throws ConnectionException, NegotiationException {
		@SuppressWarnings("unchecked")
		List<Feature> features = (List<Feature>)context.getAttribute(AbstractStreamer.NEGOTIATION_KEY_FEATURES);
		
		for (Feature feature : features) {
			if (feature instanceof Register) {
				return;
			}
		}
		
		throw new NegotiationException(this, IbtrError.NOT_SUPPORTED);
	}

}
