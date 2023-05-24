package com.thefirstlineofcode.sand.emulators.lora.simple.light;

import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.sand.client.lora.dac.ILoraDacClient;
import com.thefirstlineofcode.sand.client.things.simple.light.ISimpleLight;
import com.thefirstlineofcode.sand.emulators.commons.Constants;
import com.thefirstlineofcode.sand.emulators.commons.ISimpleLightEmulator;
import com.thefirstlineofcode.sand.emulators.commons.ui.AbstractThingEmulatorPanel;
import com.thefirstlineofcode.sand.emulators.commons.ui.LightEmulatorPanel;
import com.thefirstlineofcode.sand.emulators.lora.things.AbstractLoraThingEmulator;
import com.thefirstlineofcode.sand.emulators.models.Sle01ModelDescriptor;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.thing.tacp.LanNotification;
import com.thefirstlineofcode.sand.protocols.thing.tacp.ThingsTinyId;
import com.thefirstlineofcode.sand.protocols.things.simple.light.Flash;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchStateChanged;

public class SimpleLight extends AbstractLoraThingEmulator implements ISimpleLightEmulator {
	private static final SwitchState DEFAULT_SWITCH_STATE = SwitchState.OFF;
	private static final LightState DEFAULT_LIGHT_STATE = LightState.OFF;
	
	private SwitchState switchState = DEFAULT_SWITCH_STATE;
	private LightState lightState = DEFAULT_LIGHT_STATE;
	
	private LightEmulatorPanel panel;
	
	public SimpleLight(ILoraDacClient dacClient) {
		this(dacClient, DEFAULT_SWITCH_STATE);
	}
	
	public SimpleLight(ILoraDacClient dacClient, SwitchState switchState) {
		this(dacClient, switchState, (switchState == SwitchState.ON) ? LightState.ON : LightState.OFF);
	}
	
	public SimpleLight(ILoraDacClient dacClient, SwitchState switchState, LightState lightState) {
		super(Sle01ModelDescriptor.MODEL_NAME, dacClient);
		
		if (switchState == null)
			throw new IllegalArgumentException("Null switch state.");
		
		if (lightState == null)
			throw new IllegalArgumentException("Null light state.");
		
		if (switchState == SwitchState.ON && lightState == LightState.OFF ||
				switchState == SwitchState.OFF && lightState == LightState.ON) {
			throw new IllegalStateException(String.format("Invalid light state. Switch state: %s. Light state: %s.", switchState, lightState));
		}
		
		this.switchState = switchState;
		this.lightState = lightState;
	}
	
	@Override
	public String getSoftwareVersion() {
		return Constants.SOFTWARE_VERSION;
	}
	
	public SwitchState getSwitchState() {
		return switchState;
	}

	@Override
	public LightState getLightState() {
		return lightState;
	}

	@Override
	public void turnOn() {
		if (!isPowered() || batteryPower == 0)
			return;
		
		panel.turnOn();

	}

	@Override
	public void turnOff() {
		if (!isPowered() || batteryPower == 0)
			return;
		
		panel.turnOff();
	}

	@Override
	public AbstractThingEmulatorPanel<?> getPanel() {
		if (panel == null) {
			panel = new LightEmulatorPanel(this);
			addBatteryPowerListener(panel);
		}
		
		return panel;
	}

	@Override
	protected void doReset() {
		turnOff();
		
		super.doReset();
	}

	@Override
	protected void doPowerOn() {
		super.doPowerOn();
		
		if (switchState == SwitchState.ON || lightState == LightState.ON) {
			lightState = LightState.ON;					
			panel.turnOn();
		}
	}

	@Override
	protected void doPowerOff() {
		if (switchState == SwitchState.OFF || lightState == LightState.OFF) {			
			lightState = LightState.OFF;
		}
		panel.turnOff();
		
		super.doPowerOff();
	}
	
	@Override
	protected void processAction(Object action) throws ExecutionException {
		if (action instanceof Flash) {
			if (switchState != SwitchState.CONTROL)
				throw new ExecutionException(ISimpleLight.ERROR_CODE_NOT_REMOTE_CONTROL_STATE);
			
			flash(((Flash)action).getRepeat());
		} else {
			super.processAction(action);
		}
	}
	
	public void flash(int repeat) throws ExecutionException {
		if (!isPowered() || batteryPower == 0)
			return;
		
		if (repeat < 0)
			throw new IllegalArgumentException("Repeat must be an positive integer.");
		
		if (repeat == 0)
			repeat = 1;
		
		doFlash(repeat);
	}
	
	private void doFlash(int repeat) {
		new Thread(new FlashRunnable(repeat)).start();
		
		try {
			synchronized(this) {					
				wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class FlashRunnable implements Runnable {
		private int repeat;
		
		public FlashRunnable(int repeat) {
			this.repeat = repeat;
		}

		@Override
		public void run() {
			panel.setFlashButtonEnabled(false);
			
			ISimpleLight.LightState oldLightState = lightState;
			if (lightState == ISimpleLight.LightState.ON) {
				panel.turnOff();
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (repeat == 1) {
				panel.flash();
			} else {
				for (int i = 0; i < repeat; i++) {
					panel.flash();
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}
			
			lightState = oldLightState;
			if (lightState == ISimpleLight.LightState.ON) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				panel.turnOn();
			}
			
			panel.setFlashButtonEnabled(true);
			
			synchronized (SimpleLight.this) {				
				SimpleLight.this.notify();
			}
		}			
	}

	@Override
	protected Map<Protocol, Class<?>> createSupportedActions() {
		return new Sle01ModelDescriptor().getSupportedActions();
	}
	
	@Override
	public void changeSwitchState(SwitchState switchState) {
		if (this.switchState == switchState)
			return;
		
		this.switchState = switchState;
		if (switchState == SwitchState.ON && lightState == ISimpleLight.LightState.OFF) {
			lightState = ISimpleLight.LightState.ON;			
			panel.turnOn();
		} else if (switchState != SwitchState.ON && lightState == ISimpleLight.LightState.ON) {
			lightState = ISimpleLight.LightState.OFF;
			panel.turnOff();
		}		
	}

	@Override
	protected Map<String, String> loadThingAttributes() {
		return null;
	}
	
	@Override
	protected void saveAttributes(Map<String, String> attributes) {}

	@Override
	public void fireSwitchChangedEvent(SwitchState previous, SwitchState current) {
		if (dacState != DacState.CONFIGURED || lanId == null)
			return;
		
		notify(new LanNotification(ThingsTinyId.createRequestId(lanId),
				new SwitchStateChanged(previous, current), true));
	}
	
	public static void main(String[] args) {
		byte[] bytes = "SL-LE01-8660B11E".getBytes();
		
		for (int i = 0; i < bytes.length; i++) {
			System.out.print(String.format("0x%02x, ", bytes[i]));
		}
	}
}
