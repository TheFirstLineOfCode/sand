package com.thefirstlineofcode.sand.client.thing.commuication;

public class Data<A, D> {
	private A address;
	private D data;
	
	public Data(A address, D data) {
		this.address = address;
		this.data = data;
	}
	
	public A getAddress() {
		return address;
	}
	
	public D getData() {
		return data;
	}
}
