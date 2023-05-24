package com.thefirstlineofcode.sand.emulators.lora.network;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.thefirstlineofcode.sand.client.thing.commuication.AbstractCommunicator;
import com.thefirstlineofcode.sand.client.thing.commuication.CommunicationException;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraCommunicator extends AbstractCommunicator<LoraAddress, LoraAddress, byte[]> {
	protected ILoraChip chip;
	
	private boolean listening;
	private Thread dataListeningThread;
	private Lock lock;
	
	public LoraCommunicator(ILoraChip chip) {
		this.chip = chip;
		listening = false;
		lock = new ReentrantLock();
	}
	
	@Override
	public LoraAddress getAddress() {
		try {
			lock.lock();
			return chip.getAddress();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	protected void doChangeAddress(LoraAddress address, boolean temporary) throws CommunicationException {
		try {
			lock.lock();
			chip.changeAddress(address);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	protected void doSend(LoraAddress to, byte[] data) throws CommunicationException {
		try {
			lock.lock();
			chip.send(to, data);
		} finally {
			lock.unlock();
		}
	}
	
	public LoraData receive() {
		try {
			if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {				
				LoraData data = (LoraData)chip.receive();
				if (data != null) {
					received(null, data.getData());
				}
				
				return data;
			}
			
			return null;
		} catch (InterruptedException e) {
			throw new RuntimeException("????", e);
		} finally {
			lock.unlock();
		}
	}
	
	public ILoraChip getChip() {
		return chip;
	}
	
	@Override
	public void stopToListen() {
		try {
			lock.lock();
			
			listening = false;
			if (dataListeningThread == null)
				return;
			
			try {
				dataListeningThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dataListeningThread = null;			
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isListening() {
		return listening;
	}
	
	private class DataReceiver implements Runnable {		
		@Override
		public void run() {
			listening = true;
			
			while (listening) {
				receive();
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
	}

	@Override
	protected void doInitialize() {}
	
	@Override
	protected void doConfigure() {}

	@Override
	protected void doStartToListen() {
		if (dataListeningThread == null)
			dataListeningThread = new Thread(new DataReceiver(), String.format("Data Receiver Thread for Lora Communicator(%s).",
				this.getAddress()));
		
		if (!dataListeningThread.isAlive())
			dataListeningThread.start();
	}
}
