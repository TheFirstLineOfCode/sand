package com.thefirstlineofcode.sand.emulators.lora.gateway.things;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import com.thefirstlineofcode.sand.emulators.lora.things.AbstractLoraThingEmulator;

public class ThingInternalFrame extends JInternalFrame {
	private static final long serialVersionUID = 4975138886817512398L;
	
	private AbstractLoraThingEmulator thing;
	
	public ThingInternalFrame(AbstractLoraThingEmulator thing, String title) {
		super(title, false, false, false, false);
		
		this.thing = thing;
		JPanel panel = thing.getPanel();
		setContentPane(panel);
	}
	
	public AbstractLoraThingEmulator getThing() {
		return thing;
	}
}
