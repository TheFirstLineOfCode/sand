package com.thefirstlineofcode.sand.protocols.thing.lora;

import java.io.Serializable;
import java.util.StringTokenizer;

import com.thefirstlineofcode.sand.protocols.thing.BadAddressException;
import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;
import com.thefirstlineofcode.sand.protocols.thing.ILanAddress;

public class LoraAddress implements ILanAddress, Serializable {
	private static final long serialVersionUID = -2095123770025458417L;
	
	private byte addressHighByte;
	private byte addressLowByte;
	private byte channel;
	
	public LoraAddress() {}
	
	public LoraAddress(byte[] bytes) {
		if (bytes == null)
			throw new IllegalArgumentException("Null bytes.");
		
		if (bytes.length != 3)
			throw new IllegalArgumentException("Length of LoRa address bytes must be 3.");
		
		init(bytes[0], bytes[1], bytes[2]);
	}
	
	public LoraAddress(byte addressHighByte, byte addressLowByte, byte channel) {
		init(addressHighByte, addressLowByte, channel);
	}

	private void init(byte addressHighByte, byte addressLowByte, byte channel) {
		if ((channel & 0xff) < 0 || (channel & 0xff) > 31)
			throw new IllegalArgumentException("Channel must be in range of 0~31.");
		
		this.addressHighByte = addressHighByte;
		this.addressLowByte = addressLowByte;
		this.channel = channel;
	}
	
	public byte[] getAddressBytes() {
		return new byte[] {addressHighByte, addressLowByte};
	}
	
	public void setAddressBytes(byte[] address) {
		if (address.length != 2)
			throw new IllegalArgumentException("Length of address bytes of LoRa address must be 2.");
		
		addressHighByte = address[0];
		addressLowByte = address[1];
	}
		
	public byte getAddressHighByte() {
		return addressHighByte;
	}

	public byte getAddressLowByte() {
		return addressLowByte;
	}

	public byte getChannel() {
		return channel;
	}
	
	public void setChannel(byte channel) {
		this.channel = channel;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LoraAddress) {
			LoraAddress other = (LoraAddress)obj;
			
			return addressHighByte == other.addressHighByte &&
					addressLowByte == other.addressLowByte &&
					channel == other.channel;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash += 31 * hash + addressHighByte;
		hash += 31 * hash + addressLowByte;		
		hash += 31 * hash + channel;
		
		return hash;
	}
	
	public static LoraAddress parse(String addressString) throws BadAddressException {
		if (!addressString.startsWith("la$")) {
			throw new BadAddressException("Invalid LORA address.");
		}
		
		int conlonIndex = addressString.indexOf(':');
		if (conlonIndex == -1)
			throw new BadAddressException("Invalid LORA address.");
		
		String addressPart = addressString.substring(3, conlonIndex);
		String channelPart = addressString.substring(conlonIndex + 1);
		
		try {
			byte[] addressBytes = parseAddressBytes(addressPart);
			return  new LoraAddress(new byte[] {addressBytes[0], addressBytes[1], Byte.parseByte(channelPart)});
		} catch (NumberFormatException e) {
			throw new BadAddressException("Invalid LORA address.", e);
		}
	}

	private static byte[] parseAddressBytes(String sAddress) {
		StringTokenizer st = new StringTokenizer(sAddress, ",");
		if (st.countTokens() != 2)
			throw new IllegalArgumentException("Length of address bytes of LoRa address must be 2.");
		
		return new byte[] {Byte.parseByte(st.nextToken()), Byte.parseByte(st.nextToken())};
	}

	@Override
	public String toAddressString() {
		return String.format("la$%d,%d:%d", addressHighByte & 0xff, addressLowByte & 0xff, channel);
	}

	@Override
	public CommunicationNet getCommunicationNet() {
		return CommunicationNet.LORA;
	}

	public byte[] getBytes() {
		return new byte[] {addressHighByte, addressLowByte, channel};
	}
	
	@Override
	public String toString() {
		return String.format("LoraAddress[%s]", toAddressString());
	}
}
