package com.thefirstlineofcode.sand.protocols.thing.tacp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ThingsTinyIdTest {
	@Test
	public void all() {
		int lanId = 24;
		int hours = 11;
		int minutes = 23;
		int seconds = 52;
		int milliseconds = 997;
		
		ThingsTinyId requestId = ThingsTinyId.createInstance(lanId, ThingsTinyId.Type.REQUEST,
				hours, minutes, seconds, milliseconds);
		assertEquals(ThingsTinyId.Type.REQUEST, requestId.getType());
		assertEquals(lanId, requestId.getLanId());
		assertEquals(hours, requestId.getHours());
		assertEquals(minutes, requestId.getMinutes());
		assertEquals(seconds, requestId.getSeconds());
		assertEquals(milliseconds, requestId.getMilliseconds());
		
		ITraceId responseId = requestId.createResponseId();
		assertTrue(requestId.isAnswer(responseId));
		assertEquals(ThingsTinyId.Type.RESPONSE, responseId.getType());
		assertTrue(ThingsTinyId.isAnswerId(requestId.getBytes(), responseId.getBytes()));
		assertEquals(ThingsTinyId.Type.RESPONSE, ThingsTinyId.getType(responseId.getBytes()));
		
		
		ITraceId errorId = requestId.createErrorId();
		assertTrue(requestId.isAnswerId(errorId.getBytes()));
		assertEquals(ThingsTinyId.Type.ERROR, errorId.getType());
		assertTrue(ThingsTinyId.isAnswerId(requestId.getBytes(), errorId.getBytes()));
		assertEquals(ThingsTinyId.Type.ERROR, ThingsTinyId.getType(errorId.getBytes()));
	}
}
