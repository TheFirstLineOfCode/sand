package com.thefirstlineofcode.sand.server.actuator;

import com.thefirstlineofcode.granite.framework.core.pipeline.stages.event.IEvent;
import com.thefirstlineofcode.sand.protocols.actuator.Execution;

public class ExecutionEvent implements IEvent {
	private String thingId;
	private Integer lanId;
	private Execution execution;
	private IExecutionCallback callback;
	
	public ExecutionEvent(String thingId, Execution execution) {
		this(thingId, null, execution);
	}
	
	public ExecutionEvent(String thingId, Integer lanId, Execution execute) {
		this(thingId, null, execute, null);
	}
	
	public ExecutionEvent(String thingId, Integer lanId, Execution execution, IExecutionCallback callback) {
		this.thingId = thingId;
		this.lanId = lanId;
		this.execution = execution;
		this.callback = callback;
	}
	
	public String getThingId() {
		return thingId;
	}
	
	public Integer getLanId() {
		return lanId;
	}
	
	public Execution getExecution() {
		return execution;
	}
	
	public IExecutionCallback getExecutionCallback() {
		return callback;
	}
	
	@Override
	public Object clone() {
		return new ExecutionEvent(thingId, lanId, execution, callback);
	}
}
