package com.thefirstlineofcode.sand.protocols.lora.dac;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tuxp:lora-dac", localName="allocation")
public class Allocation {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tuxp:lora-dac", "allocation");
	
	private int uplinkChannelBegin;
	private int uplinkChannelEnd;
	private byte uplinkAddressHighByte;
	private byte uplinkAddressLowByte;
	private byte[] allocatedAddress;
	
	public Allocation() {}
	
	public Allocation(int uplinkChannelBegin, int uplinkChannelEnd,
			byte uplinkAddressHighByte, byte uplinkAddressLowByte, byte[] allocatedAddress) {
		this.uplinkChannelBegin = uplinkChannelBegin;
		this.uplinkChannelEnd = uplinkChannelEnd;
		this.uplinkAddressHighByte = uplinkAddressHighByte;
		this.uplinkAddressLowByte = uplinkAddressLowByte;
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
	
	public byte getUplinkAddressHighByte() {
		return uplinkAddressHighByte;
	}
	
	public void setUplinkAddressHighByte(byte uplinkAddressHighByte) {
		this.uplinkAddressHighByte = uplinkAddressHighByte;
	}
	
	public byte getUplinkAddressLowByte() {
		return uplinkAddressLowByte;
	}
	
	public void setUplinkAddressLowByte(byte uplinkAddressHighByte) {
		this.uplinkAddressLowByte = uplinkAddressHighByte;
	}
	
	public byte[] getAllocatedAddress() {
		return allocatedAddress;
	}
	
	public void setAllocatedAddress(byte[] allocatedAddress) {
		this.allocatedAddress = allocatedAddress;
	}	
}
