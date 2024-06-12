package com.thefirstlineofcode.sand.protocols.thing.lora;

import com.thefirstlineofcode.sand.protocols.thing.BadAddressException;
import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;
import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;

public class BleAddress implements ILanAddress {
	private String address;
	
	public BleAddress(String address) {
		if (address == null || "".equals(address))
			throw new IllegalArgumentException("Null address.");
		
		this.address = address;
	}
	
	@Override
	public String toAddressString() {
		return String.format("ble$%s", address);
	}

	@Override
	public CommunicationNet getCommunicationNet() {
		return CommunicationNet.BLE;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BleAddress) {
			BleAddress other = (BleAddress)obj;
			
			return address.equals(other.address);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return address.hashCode();
	}
	
	public static BleAddress parse(String addressString) throws BadAddressException {
		if (!addressString.startsWith("ble$")) {
			throw new BadAddressException("Invalid BLE address.");
		}
		
		return  new BleAddress(addressString.substring(4, addressString.length()));
	}
}
