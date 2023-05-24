package com.thefirstlineofcode.sand.protocols.thing;

public interface IAddress {
	byte[] getBytes();
	String toAddressString();
	CommunicationNet getCommunicationNet();
}
