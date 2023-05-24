package com.thefirstlineofcode.sand.emulators.commons.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.thefirstlineofcode.sand.client.things.simple.light.ISimpleLight.LightState;
import com.thefirstlineofcode.sand.emulators.commons.ISimpleLightEmulator;
import com.thefirstlineofcode.sand.protocols.actuator.ExecutionException;
import com.thefirstlineofcode.sand.protocols.things.simple.light.SwitchState;

public class LightEmulatorPanel extends AbstractThingEmulatorPanel<ISimpleLightEmulator> implements ActionListener  {
	private static final long serialVersionUID = 7660599095831708565L;
	
	private static final String FILE_NAME_LIGHT_OFF = "light_off.png";
	private static final String FILE_NAME_LIGHT_ON = "light_on.png";

	private JLabel lightImage;
	private JButton flash;
	
	private ImageIcon lightOn;
	private ImageIcon lightOff;
	
	private ISimpleLightEmulator light;
	
	public LightEmulatorPanel(ISimpleLightEmulator light) {
		super(light);
		setPreferredSize(new Dimension(720, 360));
		
		this.light = light;
		
		updateStatus();
	}
	
	private void createLightIcons() {
		lightOn = createLightIcon(LightState.ON);
		lightOff = createLightIcon(LightState.OFF);

	}

	private ImageIcon createLightIcon(LightState lightState) {
		String path = lightState == LightState.ON ? "/images/" + FILE_NAME_LIGHT_ON : "/images/" + FILE_NAME_LIGHT_OFF;
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			throw new RuntimeException("Couldn't find file: " + path);
		}
	}

	@Override
	protected JPanel createThingCustomizedUi(ISimpleLightEmulator light) {			
		JPanel customizedUi = new JPanel(new BorderLayout());
		
		customizedUi.add(createSwitchsPanel(light), BorderLayout.NORTH);
		customizedUi.add(createLightPanel(light), BorderLayout.WEST);			
		
		customizedUi.setPreferredSize(new Dimension(640, 480));
		
		return customizedUi;
	}

	private JPanel createLightPanel(ISimpleLightEmulator light) {
		JPanel panel = new JPanel(new BorderLayout());
		
		lightImage = new JLabel(getLightImageIcon(light.getLightState()));
		panel.add(lightImage, BorderLayout.CENTER);
		panel.add(createFlashPanel(), BorderLayout.SOUTH);
		
		return panel;
	}

	private JPanel createSwitchsPanel(ISimpleLightEmulator light) {
		JRadioButton off = createOffButton(light);
		JRadioButton on = createOnButton(light);
		JRadioButton control = createControlButton(light);
		
		ButtonGroup group = new ButtonGroup();
		group.add(off);
		group.add(on);
		group.add(control);
		
		off.addActionListener(this);
		on.addActionListener(this);
		control.addActionListener(this);
		
		JPanel panel = new JPanel(new GridLayout(1, 0));
		panel.add(off);
		panel.add(on);
		panel.add(control);
		
		return panel;
	}

	private JRadioButton createControlButton(ISimpleLightEmulator light) {
		JRadioButton control = new JRadioButton("Remote Control");
		control.setMnemonic(KeyEvent.VK_R);
		control.setActionCommand("remote_control");
		control.setSelected(true);
		
		if (light.getSwitchState() == SwitchState.CONTROL)
			control.setSelected(true);
		
		return control;
	}

	private JRadioButton createOnButton(ISimpleLightEmulator light) {
		JRadioButton on = new JRadioButton("Turn On");
		on.setMnemonic(KeyEvent.VK_N);
		on.setActionCommand("on");
		
		if (light.getSwitchState() == SwitchState.ON)
			on.setSelected(true);
		
		return on;
	}

	private JRadioButton createOffButton(ISimpleLightEmulator light) {
		JRadioButton off = new JRadioButton("Turn Off");
		off.setMnemonic(KeyEvent.VK_F);
		off.setActionCommand("off");
		
		if (light.getSwitchState() == SwitchState.OFF)
			off.setSelected(true);
		
		return off;
	}

	private JPanel createFlashPanel() {
		flash = new JButton("Flash");
		flash.setPreferredSize(new Dimension(128, 48));
		
		JPanel flashPanel = new JPanel();
		flashPanel.add(flash);
		
		flash.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							light.flash(1);
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}
					
				}).start();
			}
		});
		
		return flashPanel;
	}
	
	public void flash() {
		turnOn();
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		turnOff();
	}
	
	protected ImageIcon getLightImageIcon(LightState lightState) {
		if (lightState == null) {
			throw new IllegalArgumentException("Null light state.");
		}
		
		if (lightOn == null || lightOff == null)
			createLightIcons();
		
		if (LightState.ON == lightState) {
			return lightOn;
		} else {
			return lightOff;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		
		SwitchState previous = light.getSwitchState();
		if (actionCommand.equals("off")) {
			light.changeSwitchState(SwitchState.OFF);
		} else if (actionCommand.equals("on")) {
			light.changeSwitchState(SwitchState.ON);
		} else {
			light.changeSwitchState(SwitchState.CONTROL);
		}
		
		if (previous == light.getSwitchState())
			return;
		
		fireSwitchChangedEvent(previous, light.getSwitchState());
	}
	
	private void fireSwitchChangedEvent(SwitchState previous, SwitchState current) {
		light.fireSwitchChangedEvent(previous, current);
	}

	public void updateStatus() {
		updateStatus(light.getThingStatus());
	}
	
	public void turnOn() {
		if (!light.isPowered())
			return;
		
		lightImage.setIcon(getLightImageIcon(LightState.ON));		
		lightImage.repaint();
	}
	
	public void turnOff() {
		lightImage.setIcon(getLightImageIcon(LightState.OFF));		
		lightImage.repaint();
	}
	
	public void setFlashButtonEnabled(boolean enabled) {
		flash.setEnabled(enabled);
	}
}
