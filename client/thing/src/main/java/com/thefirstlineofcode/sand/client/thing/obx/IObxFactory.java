package com.thefirstlineofcode.sand.client.thing.obx;

import com.thefirstlineofcode.basalt.oxm.binary.BxmppConversionException;
import com.thefirstlineofcode.basalt.oxm.binary.IBinaryXmppProtocolConverter;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

public interface IObxFactory {
	IBinaryXmppProtocolConverter getBinaryXmppProtocolConverter();
	void registerLanAction(Class<?> lanActionType);
	boolean unregisterLanAction(Class<?> lanActionType);
	void registerLanSupportedEvent(Class<?> lanSupportedEventType);
	boolean unregisterLanSupportedEvent(Class<?> lanSupportedEventType);
	void registerLanFollowedEvent(Class<?> lanFollowedEventType);
	boolean unregisterLanFollowedEvent(Class<?> lanFollowedEventType);
	void registerLanData(Class<?> lanDataType);
	boolean unregisterLanData(Class<?> lanDataType);
	Protocol readProtocol(byte[] data);
	byte[] toBinary(Object obj);
	String toXml(byte[] data) throws BxmppConversionException;
	Object toObject(byte[] data) throws BxmppConversionException;
	<T> T toObject(Class<T> type, byte[] data) throws BxmppConversionException;
}
