package com.thefirstlineofcode.sand.client.concentrator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.thefirstlineofcode.sand.protocols.thing.CommunicationNet;

public class LanNode implements Externalizable {
	private String thingId;
	private String registrationCode;
	private Integer lanId;
	private String model;
	private CommunicationNet communicationNet;
	private String address;
	private boolean confirmed;
	
	public String getThingId() {
		return thingId;
	}

	public void setThingId(String thingId) {
		this.thingId = thingId;
	}
	
	public String getRegistrationCode() {
		return registrationCode;
	}

	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}

	public Integer getLanId() {
		return lanId;
	}

	public void setLanId(Integer lanId) {
		this.lanId = lanId;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public CommunicationNet getCommunicationNet() {
		return communicationNet;
	}

	public void setCommunicationNet(CommunicationNet communicationNet) {
		this.communicationNet = communicationNet;
	}

	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(thingId);
		out.writeObject(registrationCode);
		out.writeObject(lanId);
		out.writeObject(model);
		out.writeObject(communicationNet);
		out.writeObject(address);
		out.writeBoolean(confirmed);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		thingId = (String)in.readObject();
		registrationCode = (String)in.readObject();
		lanId = (Integer)in.readObject();
		model = (String)in.readObject();
		communicationNet = (CommunicationNet)in.readObject();
		address = (String)in.readObject();
		confirmed = in.readBoolean();
	}
}
