package com.thefirstlineofcode.sand.server.things;

import java.util.Date;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.protocols.thing.IThingModelDescriptor;

public interface IThingManager {
	void authorize(String thingId, String authorizer, Date expiredTime);
	ThingAuthorization getAuthorization(String thingId);
	void cancelAuthorization(String thingId);
	ThingRegistered register(String thingId);
	void create(Thing thing);
	void remove(String thingIds);
	Thing getByThingId(String thingId);
	Thing getByThingName(String thingName);
	boolean thingIdExists(String thingId);
	boolean thingNameExists(String thingName);
	String getThingNameByThingId(String thingId);
	String getThingIdByThingName(String thingName);
	void registerModel(IThingModelDescriptor modelDescriptor);
	String[] getModels();
	IThingModelDescriptor getModelDescriptor(String model);
	IThingModelDescriptor unregisterMode(String model);
	boolean isRegistered(String thingId);
	boolean isConcentrator(String model);
	boolean isActuator(String model);
	boolean isSensor(String model);
	boolean isActionSupported(String model, Protocol protocol);
	boolean isEventSupported(String model, Protocol protocol);
	boolean isEventFollowed(String model, Protocol protocol);
	boolean isActionSupported(String model, Class<?> actionType);
	boolean isEventSupported(String model, Class<?> eventType);
	boolean isEventFollowed(String model, Class<?> eventType);
	Class<?> getActionType(String model, Protocol protocol);
	Class<?> getSupportedEventType(String model, Protocol protocol);
	Class<?> getFollowedEventType(String model, Protocol protocol);
	boolean isValid(String thingId);
	String getModel(String thingId);
}
