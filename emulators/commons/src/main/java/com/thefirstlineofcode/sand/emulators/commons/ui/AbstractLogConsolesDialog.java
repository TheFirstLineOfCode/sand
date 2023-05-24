package com.thefirstlineofcode.sand.emulators.commons.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.thefirstlineofcode.chalk.network.IConnectionListener;

public abstract class AbstractLogConsolesDialog extends JDialog {
	private static final long serialVersionUID = 5197344780011371803L;
	
	public static final String NAME_INTERNET = "Internet";
	
	protected JTabbedPane tabbedPane;
	protected Map<String, AbstractLogConsolePanel> logConsoles;
	
	public AbstractLogConsolesDialog(JFrame parent) {
		super(parent, "Log Console");
		
		logConsoles = new HashMap<>();
		
		setUi();
	}
	
	private void setUi() {
		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 640) / 2 + 240, (screenSize.height - 600) / 2, 640, 480);
	}
	
	protected abstract void createPreinstlledLogConsoles();
	
	protected void createInternetLogConsole() {
		createLogConsole(NAME_INTERNET, new InternetLogConsolePanel());
	}
	
	public void createLogConsole(String name, AbstractLogConsolePanel logConsole) {
		if (logConsoles.containsKey(name)) {
			throw new IllegalArgumentException(String.format("Logger '%s' has existed.", name));
		}
		
		tabbedPane.addTab(name, logConsole);
		logConsoles.put(name, logConsole);
		addWindowListener(logConsole);
	}
	
	public void removeLogConsole(String name) {
		AbstractLogConsolePanel logConsole = logConsoles.get(name);
		if (logConsole != null) {
			tabbedPane.remove(logConsole);
		}
	}
	
	public IConnectionListener getInternetConnectionListener() {
		return (IConnectionListener)logConsoles.get(NAME_INTERNET);
	}
}
