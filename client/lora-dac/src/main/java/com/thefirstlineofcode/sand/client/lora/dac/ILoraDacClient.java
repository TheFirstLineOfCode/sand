package com.thefirstlineofcode.sand.client.lora.dac;

import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.client.thing.commuication.IAddressConfigurator;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicator;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public interface ILoraDacClient extends IAddressConfigurator<ICommunicator<LoraAddress,
			LoraAddress, byte[]>, LoraAddress, byte[]> {
	public static final byte DEFAULT_DAC_SERVICE_CHANNEL = 0x1f;
	public static final LoraAddress DEFAULT_DAC_SERVICE_ADDRESS = new LoraAddress(new byte[] {(byte)0xef, (byte)0xef, DEFAULT_DAC_SERVICE_CHANNEL});	
	public static final LoraAddress DEFAULT_DAC_CLIENT_ADDRESS = new LoraAddress(new byte[] {(byte)0xef, (byte)0xee, DEFAULT_DAC_SERVICE_CHANNEL});
	
	public interface Listener {
		void allocated(LoraAddress[] uplinkAddresses, LoraAddress allocatedAddress);
		void configured();
		void notConfigured();
		void occurred(CommunicationException e);
	}
	
	void setDacServiceAddress(LoraAddress dacServiceAddress);
	void setListener(Listener listener);
	void removeListener();
	void introduce(String thingId, String registrationCode);
	void isConfigured(String thingId);
	void reset();
}
