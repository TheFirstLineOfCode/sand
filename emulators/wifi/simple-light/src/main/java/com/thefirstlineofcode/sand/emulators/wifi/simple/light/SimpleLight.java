package com.thefirstlineofcode.sand.emulators.wifi.simple.light;

import java.nio.file.Path;
import java.util.Map;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.basalt.xmpp.core.stanza.Iq;
import com.thefirstlineofcode.chalk.core.AuthFailureException;
import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.chalk.network.ConnectionException;
import com.thefirstlineofcode.sand.client.actuator.ActuatorPlugin;
import com.thefirstlineofcode.sand.client.actuator.IActuator;
import com.thefirstlineofcode.sand.client.actuator.IExecutor;
import com.thefirstlineofcode.sand.client.actuator.IExecutorFactory;
import com.thefirstlineofcode.sand.client.edge.AbstractEdgeThing;
import com.thefirstlineofcode.sand.client.ibtr.RegistrationException;
import com.thefirstlineofcode.sand.client.thing.IAckListener;
import com.thefirstlineofcode.sand.client.thing.INotificationService;
import com.thefirstlineofcode.sand.client.thing.INotifier;
import com.thefirstlineofcode.sand.client.thing.ThingsUtils;
import com.thefirstlineofcode.sand.client.things.simple.light.FlashExecutor;
import com.thefirstlineofcode.sand.client.things.simple.light.ISimpleLight;
import com.thefirstlineofcode.sand.client.things.simple.light.TurnOffExecutor;
import com.thefirstlineofcode.sand.client.things.simple.light.TurnOnExecutor;
import com.thefirstlineofcode.sand.emulators.commons.Constants;
import com.thefirstlineofcode.sand.emulators.commons.ui.LightEmulatorPanel;
import com.thefirstlineofcode.sand.emulators.models.Sle02ModelDescriptor;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.thing.RegisteredEdgeThing;
import com.thefirstlineofcode.sand.protocols.things.simple.light.Flash;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchStateChanged;
import com.thefirstlineofcode.sand.protocols.things.simple.light.TurnOff;
import com.thefirstlineofcode.sand.protocols.things.simple.light.TurnOn;

public class SimpleLight extends AbstractEdgeThing implements ISimpleLight, IAckListener {	
	private static final SwitchState DEFAULT_SWITCH_STATE = SwitchState.OFF;
	private static final LightState DEFAULT_LIGHT_STATE = LightState.OFF;
	
	private SwitchState switchState;
	private LightState lightState;
	
	private LightEmulatorPanel panel;
	
	private IActuator actuator;
	private INotifier notifier;
	
	public SimpleLight(StandardStreamConfig streamConfig) {
		super(Sle02ModelDescriptor.MODEL_NAME, streamConfig);
		
		switchState = DEFAULT_SWITCH_STATE;
		lightState = DEFAULT_LIGHT_STATE;
	}
	
	@Override
	protected boolean doProcessAttributes(Map<String, String> attributes) {
		return false;
	}
	
	public void setLightEmulatorPanel(LightEmulatorPanel panel) {
		this.panel = panel;
	}

	@Override
	public String getSoftwareVersion() {
		return Constants.SOFTWARE_VERSION;
	}

	@Override
	public SwitchState getSwitchState() {
		return switchState;
	}

	@Override
	public LightState getLightState() {
		return lightState;
	}
	
	public void changeSwitchState(SwitchState switchState) {
		if (this.switchState == switchState)
			return;
		
		this.switchState = switchState;		
		if (switchState == SwitchState.ON && lightState == ISimpleLight.LightState.OFF) {
			lightState = ISimpleLight.LightState.ON;
			try {
				turnOn();
			} catch (ExecutionException e) {
				throw new RuntimeException("Can't turn on light.", e);
			}
		} else if (switchState != SwitchState.ON && lightState == ISimpleLight.LightState.ON) {
			lightState = ISimpleLight.LightState.OFF;
			try {
				turnOff();
			} catch (ExecutionException e) {
				throw new RuntimeException("Can't turn off light.", e);
			}
		}
	}

	@Override
	public void turnOn() throws ExecutionException {
		panel.turnOn();
		lightState = ISimpleLight.LightState.ON;
	}

	@Override
	public void turnOff() throws ExecutionException {
		panel.turnOff();
		lightState = ISimpleLight.LightState.OFF;
	}

	@Override
	public void flash(int repeat) throws ExecutionException {
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
	protected void registrationExceptionOccurred(RegistrationException e) {}

	@Override
	protected void saveAttributes(Map<String, String> attributes) {}

	@Override
	protected void registerIotPlugins() {
		chatClient.register(ActuatorPlugin.class);
	}

	@Override
	protected void FailedToConnect(ConnectionException e) {}

	@Override
	protected void failedToAuth(AuthFailureException e) {}

	@Override
	protected void startIotComponents() {
		startActuator();
	}
	
	protected void startActuator() {
		if (actuator == null) {
			actuator = createActuator();
		}
		
		actuator.start();
	}

	protected IActuator createActuator() {
		IActuator actuator = chatClient.createApi(IActuator.class);
		actuator.registerExecutorFactory(new IExecutorFactory<Flash>() {
			
			@Override
			public IExecutor<Flash> create() {
				return new FlashExecutor(SimpleLight.this);
			}

			@Override
			public Protocol getProtocol() {
				return Flash.PROTOCOL;
			}

			@Override
			public Class<Flash> getActionType() {
				return Flash.class;
			}
			
		});
		actuator.registerExecutorFactory(new IExecutorFactory<TurnOn>() {

			@Override
			public IExecutor<TurnOn> create() {
				return new TurnOnExecutor(SimpleLight.this);
			}

			@Override
			public Protocol getProtocol() {
				return TurnOn.PROTOCOL;
			}

			@Override
			public Class<TurnOn> getActionType() {
				return TurnOn.class;
			}

		});
		actuator.registerExecutorFactory(new IExecutorFactory<TurnOff>() {

			@Override
			public IExecutor<TurnOff> create() {
				return new TurnOffExecutor(SimpleLight.this);
			}

			@Override
			public Protocol getProtocol() {
				return TurnOff.PROTOCOL;
			}

			@Override
			public Class<TurnOff> getActionType() {
				return TurnOff.class;
			}

		});
		
		return actuator;
	}

	@Override
	protected void stopIotComponents() {
		stopActuator();
	}
	
	private void stopActuator() {
		if (actuator != null) {
			actuator.stop();
			actuator = null;
		}
	}

	@Override
	protected void disconnected() {}

	@Override
	protected Map<String, String> loadThingAttributes() {
		return null;
	}

	@Override
	protected String loadThingId() {
		return getThingModel() + "-" + ThingsUtils.generateRandomId(8);
	}
	
	@Override
	protected StandardStreamConfig getStreamConfig(Map<String, String> attributes) {
		return null;
	}

	@Override
	protected RegisteredEdgeThing getRegisteredEdgeThing(Map<String, String> attributes) {
		return null;
	}
	
	@Override
	protected Path getAttributesFilePath() {
		return null;
	}

	@Override
	public void fireSwitchChangedEvent(final SwitchState previous, final SwitchState current) {
		if (notifier == null) {
			INotificationService notificationService = chatClient.createApi(INotificationService.class);
			notifier = notificationService.getNotifier();
			notifier.registerSupportedEvent(SwitchStateChanged.class);
		}
		
		notifier.notifyWithAck(new SwitchStateChanged(previous, current), this);
	}

	@Override
	public void acked(Iq iq) {
		// NOOP
	}

	@Override
	public void noAck(Iq iq) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String loadRegistrationCode() {
		return "abcdefghigkl";
	}
}
