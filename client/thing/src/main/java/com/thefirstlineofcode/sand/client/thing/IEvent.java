package com.thefirstlineofcode.sand.client.thing;

public interface IEvent<S, E> {
	S getSource();
	Class<E> getEventType();
	E getEvent();
}
