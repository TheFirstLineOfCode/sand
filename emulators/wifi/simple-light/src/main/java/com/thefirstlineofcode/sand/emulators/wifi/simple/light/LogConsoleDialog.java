package com.thefirstlineofcode.sand.emulators.wifi.simple.light;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.thefirstlineofcode.chalk.network.IConnectionListener;

public class LogConsoleDialog extends JDialog {
	private static final long serialVersionUID = -2688096815046190393L;
	
	private InternetConsolePanel consolePanel;
	
	public LogConsoleDialog(JFrame parent) {
		super(parent);
		
		setUi();
	}
	
	private void setUi() {
		setTitle("Internet");
		
		consolePanel = createInternetConsolePanel();
		getContentPane().add(consolePanel);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 640) / 2 + 240, (screenSize.height - 600) / 2, 640, 480);
	}

	private InternetConsolePanel createInternetConsolePanel() {
		return new InternetConsolePanel();
	}
	
	public IConnectionListener getInternetLogListener() {
		if (consolePanel == null)
			throw new RuntimeException("Null internet console panel.");
		
		return (IConnectionListener)consolePanel;
	}

}
