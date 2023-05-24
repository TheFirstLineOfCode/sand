package com.thefirstlineofcode.sand.emulators.commons.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.thefirstlineofcode.sand.client.thing.BatteryPowerEvent;
import com.thefirstlineofcode.sand.client.thing.IBatteryPowerListener;
import com.thefirstlineofcode.sand.emulators.commons.IThingEmulator;

public abstract class AbstractThingEmulatorPanel<T extends IThingEmulator> extends JPanel implements IBatteryPowerListener {	
	private static final long serialVersionUID = 388916038961904955L;
	
	protected T thingEmulator;
	protected JLabel statusBar;
	
	public AbstractThingEmulatorPanel(T thingEmulator) {
		super(new BorderLayout());
		
		this.thingEmulator = thingEmulator;
		add(createThingCustomizedUi(thingEmulator), BorderLayout.CENTER);
		add(creatStatusBarPanel(), BorderLayout.SOUTH);
		
	}
	
	private JPanel creatStatusBarPanel() {
		JPanel statusBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statusBarPanel.add(new CopyThingIdOrShowQrCodeButton(thingEmulator));
		
		statusBar = new JLabel();
		statusBar.setHorizontalAlignment(SwingConstants.LEFT);
		Font font = statusBar.getFont();
		if (font.getSize() > 16)
			statusBar.setFont(new Font("Status Text Font", font.getStyle(), font.getSize() - 4));	
		
		statusBarPanel.add(statusBar);
		statusBarPanel.setPreferredSize(new Dimension(760, 48));
		
		return statusBarPanel;
	}

	public void updateStatus(String status) {
		statusBar.setText(status);
	}
	
	@Override
	public void batteryPowerChanged(BatteryPowerEvent event) {
		updateStatus(thingEmulator.getThingStatus());
	}

	protected abstract JPanel createThingCustomizedUi(T thingEmulator);
}
