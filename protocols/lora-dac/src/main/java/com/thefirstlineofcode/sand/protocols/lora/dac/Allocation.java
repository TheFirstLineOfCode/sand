package com.thefirstlineofcode.sand.protocols.lora.dac;

import com.thefirstlineofcode.basalt.oxm.coc.annotations.ProtocolObject;
import com.thefirstlineofcode.basalt.xmpp.core.Protocol;

@ProtocolObject(namespace="urn:leps:tacp:lora-dac", localName="allocation")
public class Allocation {
	public static final Protocol PROTOCOL = new Protocol("urn:leps:tacp:lora-dac", "allocation");
	
	private byte[] gatewayUplinkAddress;
	private byte[] gatewayDownlinkAddress;
	
	private byte[] allocatedAddress;
	
	public byte[] getGatewayUplinkAddress() {
		return gatewayUplinkAddress;
	}

	public void setGatewayUplinkAddress(byte[] gatewayUplinkAddress) {
		this.gatewayUplinkAddress = gatewayUplinkAddress;
	}
	
	public byte[] getGatewayDownlinkAddress() {
		return gatewayDownlinkAddress;
	}

	public void setGatewayDownlinkAddress(byte[] gatewayDownlinkAddress) {
		this.gatewayDownlinkAddress = gatewayDownlinkAddress;
	}
	
	public byte[] getAllocatedAddress() {
		return allocatedAddress;
	}
	
	public void setAllocatedAddress(byte[] allocatedAddress) {
		this.allocatedAddress = allocatedAddress;
	}	
}
