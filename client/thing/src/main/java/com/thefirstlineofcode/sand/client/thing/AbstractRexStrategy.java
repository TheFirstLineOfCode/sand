package com.thefirstlineofcode.sand.client.thing;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRexStrategy implements IRexStrategy {
	protected List<IRexListener> rexListeners;
	protected List<IAckListener> ackListeners;
	
	public AbstractRexStrategy() {
		rexListeners = new ArrayList<>();
		ackListeners = new ArrayList<>();
	}
	
	@Override
	public void addRexListener(IRexListener rexListener) {
		if (!rexListeners.contains(rexListener))
			this.rexListeners.add(rexListener);
	}
	
	@Override
	public boolean removeRexListener(IRexListener rexListener) {
		return rexListeners.remove(rexListener);
	}
	
	@Override
	public void addAckListener(IAckListener ackListener) {
		if (!ackListeners.contains(ackListener))
			ackListeners.add(ackListener);
	}
	
	@Override
	public boolean removeAckListener(IAckListener ackListener) {
		return ackListeners.remove(ackListener);
	}
}
