package com.thefirstlineofcode.sand.emulators.lora.network;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thefirstlineofcode.basalt.oxm.binary.BinaryUtils;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationChip;
import com.thefirstlineofcode.sand.client.thing.commuication.ICommunicationNetworkListener;
import com.thefirstlineofcode.sand.protocols.thing.lora.LoraAddress;

public class LoraNetwork implements ILoraNetwork {
	private static final Logger logger = LoggerFactory.getLogger(LoraNetwork.class);
	
	private static final int DEFAULT_SIGNAL_COLLISION_INTERVAL = 200;
	private static final int DEFAULT_SIGNAL_EXPIRATION_INTERVAL = 1000;
	
	protected Map<LoraAddress, LoraChip> chips;
	protected List<ILoraNetworkListener> listeners;
	protected Map<LoraChipPair, SignalQuality> signalQualities;
	protected List<LoraSignal> signals;
	
	private Random arrivedTimeRandomGenerator;
	private Random signalLostRandomGenerator;
	
	private volatile int signalCrashedInterval;
	private volatile int signalTransferTimeout;
	
	public LoraNetwork() {
		chips = new HashMap<>();
		listeners = new ArrayList<>();
		signalQualities = new HashMap<>();
		signals = new ArrayList<>();
		
		long networkInitTime = System.currentTimeMillis();
		arrivedTimeRandomGenerator = new Random(networkInitTime);
		signalLostRandomGenerator = new Random(networkInitTime);
		
		signalCrashedInterval = DEFAULT_SIGNAL_COLLISION_INTERVAL;
		
		new Thread(new LoraSignalTimeoutThread()).start();
	}
	
	@Override
	public ILoraChip createChip(LoraAddress address) {
		return createChip(address, LoraChip.PowerType.NORMAL);
	}
	
	@Override
	public ILoraChip createChip(LoraAddress address, LoraChipCreationParams params) {
		LoraChip.PowerType type = null;
		if (params != null) {
			type = params.getType();
		}
		
		if (type == null) {
			type = LoraChip.PowerType.NORMAL;
		}
		
		return createChip(address, type);
	}

	public synchronized LoraChip createChip(LoraAddress address, LoraChip.PowerType type) {
		if (address == null)
			throw new IllegalArgumentException("Null lora address.");
		
		if (chips.containsKey(address))
			throw new RuntimeException(String.format("Conflict. Lora chip which's address is %s has ready existed in network.", address));
		
		LoraChip chip = new LoraChip(this, type, address);
		chips.put(address, chip);
		
		return chip;
	}
	
	@Override
	public void setSignalCrashedInterval(int interval) {
		signalCrashedInterval = interval;
	}
	
	@Override
	public int getSignalCrashedInterval() {
		return signalCrashedInterval;
	}
	
	@Override
	public void sendData(ICommunicationChip<LoraAddress, byte[]> from, LoraAddress to, byte[] data) {
		sendData((LoraChip)from, to, data);
	}
	
	public synchronized void sendData(LoraChip from, LoraAddress to, byte[] data) {
		try {
			LoraChip toChip = getChip(to);
			LoraChipPair pair = new LoraChipPair(from, toChip);
			if (!signalQualities.containsKey(pair)) {
				SignalQuality quality = null;
				int randomNumber = new Random().nextInt(10);
				if (randomNumber < 3) {
					quality = SignalQuality.BAD;
				} else if (randomNumber >= 3 && randomNumber < 9) {
					quality = SignalQuality.MEDUIM;
				} else {
					quality = SignalQuality.GOOD;
				}
				
				signalQualities.put(pair, quality);
			}
			
			long currentTime = System.currentTimeMillis();
			long arrivedTime = getArrivedTime(from, toChip, currentTime);
			signals.add(new LoraSignal(from, toChip, data, arrivedTime));
			
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Lora signal[%s, %s, %d, %d: %s] is sent to network.", from.address,
						toChip.getAddress(), currentTime, arrivedTime, BinaryUtils.getHexStringFromBytes(data)));
			}
			
			for (ILoraNetworkListener listener : listeners) {
				listener.sent(from.getAddress(), to, data);
			}
		} catch (AddressNotFoundException e) {
			for (ILoraNetworkListener listener : listeners) {
				listener.lost(from.getAddress(), to, data);
			}
		}
	}
	
	protected long getArrivedTime(ILoraChip from, ILoraChip to, long sentTime) {
		int randomTime = arrivedTimeRandomGenerator.nextInt(1000);
		
		return sentTime + 1500 - randomTime;
	}
	
	private class AddressNotFoundException extends Exception {
		private static final long serialVersionUID = 8173716761032756998L;		
	}

	protected synchronized LoraChip getChip(LoraAddress address) throws AddressNotFoundException {
		LoraChip chip = chips.get(address);
		
		if (chip == null)
			throw new AddressNotFoundException();
		
		return chip;
	}
	
	@Override
	public LoraData receiveData(ICommunicationChip<LoraAddress, byte[]> target) {
		return doReceiveData((LoraChip)target);
	}
	
	public synchronized LoraData doReceiveData(LoraChip target) {
		LoraSignal received = null;
		for (LoraSignal signal : signals) {
			if (isSendToTarget(signal, target) && isArrived(signal.arrivedTime)) {
				received = signal;
				break;
			}
		}
		
		if (received == null)
			return null;
		
		if (isExpired(received)) {
			signals.remove(received);
			return null;
		}
		
		List<LoraSignal> collisions = findCollisions(received);
		if (!collisions.isEmpty()) {
			if (logger.isTraceEnabled()) {
				for (LoraSignal collision : collisions) {					
					logger.trace(String.format("Lora signal[%s, %s, %s] is collided.",
							BinaryUtils.getHexStringFromBytes(collision.data),
							collision.from, collision.to));
				}
			}
			
			collisions.add(received);
			signals.removeAll(collisions);
			
			for (ILoraNetworkListener listener : listeners) {
				for (LoraSignal signal : collisions) {
					listener.collided(signal.from.getAddress(), signal.to.getAddress(), signal.data);
				}
			}
			
			return null;
		}
		
		signals.remove(received);
		if (isLost(received)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Lora signal[{}, {}, {}] is lost.",
							BinaryUtils.getHexStringFromBytes(received.data),
							received.from, received.to);
			}
			
			for (ILoraNetworkListener listener : listeners) {
				listener.lost(received.from.getAddress(), received.to.getAddress(), received.data);
			}
			
			return null;
		}
		
		for (ILoraNetworkListener listener : listeners) {
			listener.received(received.from.getAddress(), received.to.getAddress(), received.data);
		}
		return new LoraData(received.from.getAddress(), received.data);
	}
	
	private boolean isExpired(LoraSignal signal) {
		return (Calendar.getInstance().getTimeInMillis() - signal.arrivedTime) >
				DEFAULT_SIGNAL_EXPIRATION_INTERVAL;
	}

	private synchronized boolean isLost(LoraSignal received) {
		SignalQuality quality = signalQualities.get(new LoraChipPair(received.from, received.to));
		if (received.from.getPowerType() == LoraChip.PowerType.HIGH_POWER) {
			quality = adjustHighPowerThingSignalQuality(quality);
		}
		
		int randomSignalLostNumber = signalLostRandomGenerator.nextInt(100);
		return randomSignalLostNumber < quality.getPacketLossRate();
	}

	private SignalQuality adjustHighPowerThingSignalQuality(SignalQuality quality) {
		if (quality == SignalQuality.MEDUIM || quality == SignalQuality.BAD) {
			quality = SignalQuality.GOOD;
		} else if (quality == SignalQuality.BADDEST) {
			quality = SignalQuality.BAD;
		} else { // quality == SignalQuality.GOOD
			// no-op
		}
		
		return quality;
	}

	private List<LoraSignal> findCollisions(LoraSignal received) {
		List<LoraSignal> collisions = new ArrayList<>();
		for (LoraSignal signal : signals) {
			if (signal == received)
				continue;
			
			if (isCollided(signal, received)) {
				collisions.add(signal);
			}
		}
		
		return collisions;
	}

	private boolean isCollided(LoraSignal signal, LoraSignal received) {
		return Math.abs(signal.arrivedTime - received.arrivedTime) < signalCrashedInterval;
	}

	private boolean isSendToTarget(LoraSignal signal, ILoraChip target) {
		return signal.to.getAddress().equals(target.getAddress());
	}
	
	private boolean isArrived(long arrivedTime) {
		return System.currentTimeMillis() - arrivedTime > 0;
	}
	
	@Override
	public void addListener(ICommunicationNetworkListener<LoraAddress, byte[]> listener) {
		addListener((ILoraNetworkListener)listener);
	}
	
	public void addListener(ILoraNetworkListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public boolean removeListener(ICommunicationNetworkListener<LoraAddress, byte[]> listener) {
		return removeListener((ILoraNetworkListener)listener);
	}
	
	public boolean removeListener(ILoraNetworkListener listener) {
		return listeners.remove(listener);
	}
	
	private class LoraChipPair {
		public LoraChip chip1;
		public LoraChip chip2;
		
		public LoraChipPair(LoraChip chip1, LoraChip chip2) {
			if (chip1 == null || chip2 == null)
				throw new IllegalArgumentException("Null lora chip.");
			
			int result = compare(chip1, chip2);
			if (result == 0) {
				throw new RuntimeException("Two lora addresses are same.");
			}
			
			if (result < 0) {
				this.chip1 = chip2;
				this.chip2 = chip1;
			} else {
				this.chip1 = chip1;
				this.chip2 = chip2;
			}
		}

		private int compare(LoraChip chip1, LoraChip chip2) {
			return chip1.getAddress().hashCode() - chip2.getAddress().hashCode();
		}
		
		@Override
		public int hashCode() {
			return 7 + 31 * chip1.hashCode() + chip2.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LoraChipPair) {
				LoraChipPair other = (LoraChipPair)obj;
				
				return chip1.equals(other.chip1) && chip2.equals(other.chip2);
			}
			
			return false;
		}
	}
	
	private class LoraSignal {
		public LoraChip from;
		public LoraChip to;
		public byte[] data;
		public long arrivedTime;
		
		public LoraSignal(LoraChip from, LoraChip to, byte[] message, long arrivedTime) {
			this.from = from;
			this.to = to;
			this.data = message;
			this.arrivedTime = arrivedTime;
		}
	}
	
	private class LoraSignalTimeoutThread implements Runnable {

		@Override
		public void run() {
			synchronized (LoraNetwork.this) {
				long currentTime = System.currentTimeMillis();
				List<LoraSignal> timeouts = new ArrayList<>();
				for (LoraSignal signal : signals) {
					if (isTimeout(currentTime, signal)) {
						timeouts.add(signal);
					}
				}
				
				if (!timeouts.isEmpty()) {
					signals.removeAll(timeouts);
					
					for (LoraSignal signal : timeouts) {
						for (ILoraNetworkListener listener : listeners) {
							listener.lost(signal.from.getAddress(), signal.to.getAddress(), signal.data);
						}
					}
				}
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private boolean isTimeout(long currentTime, LoraSignal signal) {
			return currentTime - signal.arrivedTime > signalTransferTimeout;
		}
		
	}
	
	@Override
	public void changeAddress(ICommunicationChip<LoraAddress, byte[]> chip, LoraAddress address) {
		doChangeAddress((LoraChip)chip, address);
	}
	
	public synchronized void doChangeAddress(LoraChip chip, LoraAddress address) {
		LoraAddress oldAddress = chip.getAddress();
		LoraChip newChip = createChip(address, chip.getPowerType());
		
		LoraChipPair oldPair = null;
		LoraChipPair newPair = null;
		for (LoraChipPair pair : signalQualities.keySet()) {
			if (pair.chip1.equals(chip)) {
				oldPair = pair;
				newPair = new LoraChipPair(newChip, pair.chip2);
				break;
			}
			
			if (pair.chip2.equals(chip)) {
				oldPair = pair;
				newPair = new LoraChipPair(pair.chip1, newChip);
				break;
			}
		}
		
		if (oldPair != null) {
			SignalQuality quality = signalQualities.remove(oldPair);
			if (quality != null)
				signalQualities.put(newPair, quality);
		}
		
		chips.remove(chip.getAddress());
		
		for (ICommunicationNetworkListener<LoraAddress, byte[]> listener : listeners) {
			listener.addressChanged(address, oldAddress);
		}
	}

	@Override
	public void removeChip(LoraAddress address) {
		int removed = 0;
		for (LoraChipPair pair : signalQualities.keySet()) {
			if (pair.chip1.getAddress().equals(address)) {
				signalQualities.remove(pair);
				removed++;
			}
			
			if (removed == 2)
				break;
		}
		
		chips.remove(address);
	}
}
