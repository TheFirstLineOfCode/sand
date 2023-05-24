package com.thefirstlineofcode.sand.protocols.thing.tacp;

import java.util.Calendar;

public class ThingsTinyId implements ITraceId {
	private byte[] bytes;
	
	private ThingsTinyId(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("Null bytes.");
		}
		
		if (bytes.length != 5)
			throw new IllegalArgumentException("Things tiny ID bytes must be an five bytes array.");
		
		this.bytes = bytes;
	}
	
	@Override
	public Type getType() {
		int iType = (bytes[1] & 0xff) >> 6;
		
		for (Type type : Type.values()) {
			if (type.ordinal() == iType)
				return type;
		}
		
		throw new RuntimeException("Illegal type for traceable ID. Type ordinal value is " + iType + ".");
	}
	
	public int getLanId() {
		return bytes[0] & 0xff;
	}
	
	public int getHours() {
		return bytes[1] & 0X3f;
	}
	
	public int getMinutes() {
		return bytes[2] & 0xff;
	}
	
	public int getSeconds() {
		return (bytes[3] & 0xff) >> 2;
	}
	
	public int getMilliseconds() {
		int rightest2BitsOfByte3 = bytes[3] & 0xff;
		rightest2BitsOfByte3 = (rightest2BitsOfByte3 << 6) & 0xff;
		rightest2BitsOfByte3 = (rightest2BitsOfByte3 >> 6) & 0xff;
		
		return (rightest2BitsOfByte3 << 8) | (bytes[4] & 0xff);
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
	public static ThingsTinyId createInstance(int lanId, Type type) {
		Calendar calendar = Calendar.getInstance();		
		return createInstance(lanId, type, calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND),
				calendar.get(Calendar.MILLISECOND));
	}
	
	public static ThingsTinyId createInstance(int lanId, Type type, int hours, int minutes,
			int seconds, int milliseconds) {
		if (lanId < 0 || lanId > 255)
			throw new IllegalArgumentException(String.format("Value of LAN ID must be in 0~255. But it's %d.", lanId));
		
		if (hours < 0 || hours > 23)
			throw new IllegalArgumentException(String.format("Value of hours must be in 0~23. But it's %d.", hours));
		
		if (minutes < 0 || minutes > 59)
			throw new IllegalArgumentException(String.format("Value of minutes must be in 0~59. But it's %d.", minutes));
		
		if (seconds < 0 || seconds > 59)
			throw new IllegalArgumentException(String.format("Value of seconds must be in 0~59. But it's %d.", seconds));
		
		if (milliseconds < 0 || milliseconds > 999)
			throw new IllegalArgumentException(String.format("Value of milliseconds must be in 0~999. But it's %d.", milliseconds));
		
		byte[] bytes = new byte[5];
		
		bytes[0] = (byte)lanId;
		
		int leftest2BitsOfByte1 = type.ordinal();
		bytes[1] = (byte)((leftest2BitsOfByte1 << 6) | hours);
		
		bytes[2] = (byte)minutes;
		
		int leftest6BitsOfByte3 = seconds;
		int rightest2BitsOfBytes3 = milliseconds >> 8;
		bytes[3] = (byte)((leftest6BitsOfByte3 << 2) | rightest2BitsOfBytes3);
		
		bytes[4] = (byte)(milliseconds & 0xff);
		
		return new ThingsTinyId(bytes);
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		
		hash += 13 * hash + bytes[0];
		hash += 13 * hash + bytes[1];
		hash += 13 * hash + bytes[2];
		hash += 13 * hash + bytes[3];
		hash += 13 * hash + bytes[4];
		
		return hash;
	}
	
	@Override
	public String toString() {
		return String.format("ThingsTinyId[%s, %s, %s:%s:%s.%s]", getLanId(), getType(),
				getHours(), getMinutes(), getSeconds(), getMilliseconds());
	}
	
	@Override
	public boolean isAnswer(ITraceId answerId) {
		if (!(answerId instanceof ThingsTinyId))
			return false;
		
		if (answerId.getType() != ITraceId.Type.RESPONSE &&
				answerId.getType() != ITraceId.Type.ERROR)
			return false;
		
		ThingsTinyId other = (ThingsTinyId)answerId;
		
		return (other.getHours() == this.getHours()) &&
				(other.getMinutes() == this.getMinutes()) &&
				(other.getSeconds() == this.getSeconds()) &&
				(other.getMilliseconds() == this.getMilliseconds());
	}
	
	private boolean isAnswerType(ITraceId answerId, Type type) {
		if (!(answerId instanceof ThingsTinyId))
			return false;
		
		if (answerId.getType() != type)
			return false;
		
		ThingsTinyId other = (ThingsTinyId)answerId;
		
		return (other.getHours() == this.getHours()) &&
				(other.getMinutes() == this.getMinutes()) &&
				(other.getSeconds() == this.getSeconds()) &&
				(other.getMilliseconds() == this.getMilliseconds());
	}

	@Override
	public boolean isAnswerId(byte[] answerId) {
		ThingsTinyId tinyId = new ThingsTinyId(answerId);
		return isAnswerType(tinyId, Type.RESPONSE) || isAnswerType(tinyId, Type.ERROR);
	}
	
	public static boolean isAnswerId(byte[] requestId, byte[] answerId) {
		if (answerId.length != 5 || requestId.length != 5)
			return false;
		
		if (answerId[0] != requestId[0] ||
				answerId[2] != requestId[2] ||
				answerId[3] != requestId[3] ||
				answerId[4] != requestId[4])
			return false;
		
		int requestType = (requestId[1] >> 6) & 0xff;
		if (requestType != 0)
			return false;
		
		int rightest6BitOfRequestByte1 = requestId[1] & 0xff;
		rightest6BitOfRequestByte1 = rightest6BitOfRequestByte1 << 2;
		rightest6BitOfRequestByte1 = rightest6BitOfRequestByte1 >> 2;
		
		return getAnswerTypeByte(requestId[1], ITraceId.Type.RESPONSE) == answerId[1] ||
				getAnswerTypeByte(requestId[1], ITraceId.Type.ERROR) == answerId[1];
	}
	
	public static byte[] createResponseId(byte[] requestId) {
		return new byte[] {
				requestId[0],
				getAnswerTypeByte(requestId[1], ITraceId.Type.RESPONSE),
				requestId[2],
				requestId[3],
				requestId[4]};
	}
	
	public static byte[] createErrorId(byte[] requestId) {
		return new byte[] {
				requestId[0],
				getAnswerTypeByte(requestId[1], ITraceId.Type.ERROR),
				requestId[2],
				requestId[3],
				requestId[4]};
	}
	
	private static byte getAnswerTypeByte(byte requestIdTypeByte, ITraceId.Type type) {
		int resonseIdTypeByte = requestIdTypeByte;
		resonseIdTypeByte = resonseIdTypeByte << 2;
		resonseIdTypeByte = resonseIdTypeByte >> 2;
		
		int leftest2BitsOfAnswerIdByte1 = type.ordinal() << 6;
		resonseIdTypeByte = resonseIdTypeByte | leftest2BitsOfAnswerIdByte1;
		
		return (byte)resonseIdTypeByte;
	}
	
	public static ITraceId.Type getType(byte[] tinyId) {
		int idType = (tinyId[1] & 0xff) >> 6;
		for (Type type : ITraceId.Type.values()) {
			if (type.ordinal() == idType)
				return type;
		}
		
		throw new IllegalArgumentException("Can't determine tiny ID type.");
	}
	
	@Override
	public ITraceId createResponseId() {
		return createAnswerId(Type.RESPONSE);
	}

	private ITraceId createAnswerId(Type type) {
		return new ThingsTinyId(new byte[] {bytes[0], getAnswerTypeByte(bytes[1], type), bytes[2], bytes[3], bytes[4]});
	}

	@Override
	public ITraceId createErrorId() {
		return createAnswerId(Type.ERROR);
	}
	
	public static byte[] createRequestId(int lanId) {
		return createInstance(lanId, ITraceId.Type.REQUEST).getBytes();
	}

	public static byte[] createRequestId(int lanId, int hours, int minutes,
			int seconds, int milliseconds) {
		return createInstance(lanId, ITraceId.Type.REQUEST, hours, minutes, seconds, milliseconds).getBytes();
	}
	
	public static ThingsTinyId createInstance(byte[] bytes) {
		return new ThingsTinyId(bytes);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ThingsTinyId) {
			ThingsTinyId other = (ThingsTinyId)obj;
			
			return other.bytes[0] == bytes[0] &&
					other.bytes[1] == bytes[1] &&
					other.bytes[2] == bytes[2] &&
					other.bytes[3] == bytes[3] &&
					other.bytes[4] == bytes[4];
		}
		
		return false;
	}
}
