package com.thefirstlineofcode.sand.emulators.commons;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.thefirstlineofcode.sand.client.thing.AbstractThing;
import com.thefirstlineofcode.sand.client.thing.BatteryPowerEvent;
import com.thefirstlineofcode.sand.client.thing.IBatteryPowerListener;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;

public abstract class AbstractThingEmulator extends AbstractThing implements IThingEmulator {
	private static final int BATTERY_POWER_DOWN_INTERVAL = 1000 * 10;
	
	protected BatteryTimer batteryTimer;
	protected long startTime;
	
	public AbstractThingEmulator(String model) {
		super(model);
		
		batteryPower = 100;
		powered = false;
		
		startTime = -1;
	}

	protected void startBatteryTimer() {
		if (batteryTimer == null)
			batteryTimer = new BatteryTimer(model, thingId);
		
		batteryTimer.start();
	}
	
	protected void stopBatteryTimer() {
		if (batteryTimer != null) {
			batteryTimer.stop();
			
			batteryTimer = null;
		}
	}

	protected String generateThingId() {
		return getThingModel() + "-" + ThingsUtils.generateRandomId(8);
	}
	
	public String getThingStatus() {
		StringBuilder sb = new StringBuilder();
		if (isPowered() || batteryPower != 0) {
			sb.append("Power On, ");
		} else {
			sb.append("Power Off, ");
		}
		
		sb.append("Battery: ").append(getBatteryPower()).append("%, ");
		
		sb.append("Thing ID: ").append(thingId);
		
		return sb.toString();

	}
	
	private class BatteryTimer {
		private String thingModel;
		private String thingId;
		private Timer timer;
		
		public BatteryTimer(String thingModel, String thingId) {
			this.thingModel = thingModel;
			this.thingId = thingId;
		}
		
		public boolean isWorking() {
			return timer != null;
		}
		
		public void start() {
			timer = new Timer(String.format("%s '%s' Battery Timer", thingModel, thingId));
			timer.schedule(new BatteryPowerTimerTask(), BATTERY_POWER_DOWN_INTERVAL, BATTERY_POWER_DOWN_INTERVAL);
		}
		
		public void stop() {
			timer.cancel();
			timer = null;
		}
	}
	
	private class BatteryPowerTimerTask extends TimerTask {
		@Override
		public void run() {
			if (powered && downBatteryPower()) {
				for (IBatteryPowerListener batteryPowerListener : batteryPowerlisteners) {
					batteryPowerListener.batteryPowerChanged(new BatteryPowerEvent(AbstractThingEmulator.this, batteryPower));
				}
			}
		}
	}
	
	protected boolean downBatteryPower() {
		if (batteryPower == 0)
			return false;
		
		if (batteryPower != 10) {
			batteryPower -= 2;
		} else {
			batteryPower = 100;
		}
		
		return true;
	}
	
	@Override
	public int getBatteryPower() {
		return batteryPower;
	}

	@Override
	public void powerOn() {
		if (powered)
			return;
		
		startTime = Calendar.getInstance().getTimeInMillis();
		
		this.powered = true;
		doPowerOn();
		
		if (batteryTimer == null || !batteryTimer.isWorking())
			startBatteryTimer();
		
		getPanel().updateStatus(getThingStatus());
	}
	
	@Override
	public long getElapsedTime() {
		return Calendar.getInstance().getTimeInMillis() - startTime;
	}

	@Override
	public void powerOff() {
		if (!powered)
			return;
		
		stopBatteryTimer();
		
		doPowerOff();
		this.powered = false;
		
		getPanel().updateStatus(getThingStatus());
	}

	@Override
	public boolean isPowered() {
		return powered && batteryPower != 0;
	}
	
	@Override
	public void reset() {
		doReset();
		
		getPanel().updateStatus(getThingStatus());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!getClass().equals(obj.getClass()))
			return false;
		
		return thingId.equals(((IThingEmulator)obj).getThingId());
	}
	
	public void start() {}
	public boolean isStarted() {return false;}
	public void stop() throws ExecutionException {}
	public boolean isStopped() {return false;}
	public void restart() throws ExecutionException {}
	public void shutdownSystem(boolean restart) throws ExecutionException {}
	
	protected abstract void doPowerOn();
	protected abstract void doPowerOff();
	protected abstract void doReset();
}
