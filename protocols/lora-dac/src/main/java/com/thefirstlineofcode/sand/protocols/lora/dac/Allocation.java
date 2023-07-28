package com.thefirstlineofcode.sand.protocols.lora.dac;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:lora-dac", localName="allocation")
public class Allocation {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:lora-dac", "allocation");
	
	private int uplinkChannelBegin;
	private int uplinkChannelEnd;
	private byte[] uplinkAddress;
	private byte[] allocatedAddress;
	
	public Allocation() {}
	
	public Allocation(int uplinkChannelBegin, int uplinkChannelEnd,
			byte[] uplinkAddress, byte[] allocatedAddress) {
		this.uplinkChannelBegin = uplinkChannelBegin;
		this.uplinkChannelEnd = uplinkChannelEnd;
		this.uplinkAddress = uplinkAddress;
		this.allocatedAddress = allocatedAddress;
	}
	
	public int getUplinkChannelBegin() {
		return uplinkChannelBegin;
	}
	
	public void setUplinkChannelBegin(int uplinkChannelBegin) {
		this.uplinkChannelBegin = uplinkChannelBegin;
	}
	
	public int getUplinkChannelEnd() {
		return uplinkChannelEnd;
	}
	
	public void setUplinkChannelEnd(int uplinkChannelEnd) {
		this.uplinkChannelEnd = uplinkChannelEnd;
	}
	
	public byte[] getUplinkAddress() {
		return uplinkAddress;
	}
	
	public void setUplinkAddress(byte[] uplinkAddress) {
		this.uplinkAddress = uplinkAddress;
	}
	
	public byte[] getAllocatedAddress() {
		return allocatedAddress;
	}
	
	public void setAllocatedAddress(byte[] allocatedAddress) {
		this.allocatedAddress = allocatedAddress;
	}	
}
