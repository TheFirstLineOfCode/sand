package com.thefirstlineofcode.sand.emulators.wifi.simple.light;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;
import com.thefirstlineofcode.sand.client.thing.BatteryPowerEvent;
import com.thefirstlineofcode.sand.client.thing.IBatteryPowerListener;
import com.thefirstlineofcode.sand.emulators.commons.Constants;
import com.thefirstlineofcode.sand.emulators.commons.ui.AboutDialog;
import com.thefirstlineofcode.sand.emulators.commons.ui.LightEmulatorPanel;
import com.thefirstlineofcode.sand.emulators.commons.ui.StreamConfigDialog;
import com.thefirstlineofcode.sand.emulators.commons.ui.UiUtils;
import com.thefirstlineofcode.sand.emulators.models.Sle02ModelDescriptor;

public class SimpleLightFrame extends JFrame implements ActionListener, WindowListener,
			IBatteryPowerListener {
	private static final long serialVersionUID = 6734911253434942398L;
	
	// File Menu
	private static final String MENU_TEXT_FILE = "File";
	private static final String MENU_NAME_FILE = "file";
	private static final String MENU_ITEM_NAME_QUIT = "quit";
	private static final String MENU_ITEM_TEXT_QUIT = "Quit";
	
	// Edit Menu
	private static final String MENU_TEXT_EDIT = "Edit";
	private static final String MENU_NAME_EDIT = "edit";
	private static final String MENU_ITEM_TEXT_POWER_ON = "Power On";
	private static final String MENU_ITEM_NAME_POWER_ON = "power_on";
	private static final String MENU_ITEM_TEXT_POWER_OFF = "Power Off";
	private static final String MENU_ITEM_NAME_POWER_OFF = "power_off";
	private static final String MENU_ITEM_TEXT_RESTART = "Restart";
	private static final String MENU_ITEM_NAME_RESTART = "restart";
	
	// Tools Menu
	private static final String MENU_TEXT_TOOLS = "Tools";
	private static final String MENU_NAME_TOOLS = "tools";
	private static final String MENU_ITEM_TEXT_SHOW_LOG_CONSOLE = "Show Log Console";
	private static final String MENU_ITEM_NAME_SHOW_LOG_CONSOLE = "show_log_console";
	
	// Help Menu
	private static final String MENU_TEXT_HELP = "Help";
	private static final String MENU_NAME_HELP = "help";
	private static final String MENU_ITEM_TEXT_ABOUT = "About";
	private static final String MENU_ITEM_NAME_ABOUT = "about";
	
	private JMenuBar menuBar;
	private LogConsoleDialog logConsolesDialog;
	
	private LightEmulatorPanel panel;
	
	private SimpleLightEmulator lightEmulator;
	
	private StandardStreamConfig streamConfig;
	
	public SimpleLightFrame() {
		super(Sle02ModelDescriptor.DESCRIPTION);
		
		setupUiWithoutLightEmulatorPanel();
		
		if (streamConfig == null) {
			StreamConfigDialog streamConfigDialog = new StreamConfigDialog(this);
			UiUtils.showDialog(this, streamConfigDialog);
			
			streamConfig = streamConfigDialog.getStreamConfig();
		}
		
		lightEmulator = new SimpleLightEmulator(this, streamConfig);
		
		addLightEmulatorPanelToUi();
		refreshPowerRelativedMenus();
	}

	private void addLightEmulatorPanelToUi() {
		panel = (LightEmulatorPanel)lightEmulator.getPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
	}

	private void setupUiWithoutLightEmulatorPanel() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		setDefaultUiFont(new javax.swing.plaf.FontUIResource("Serif", Font.PLAIN, 20));
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 800) / 2, (screenSize.height - 640) / 2, 800, 640);
		
		menuBar = createMenuBar();
		setJMenuBar(menuBar);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);		
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(createToolsMenu());
		menuBar.add(createHelpMenu());
		
		return menuBar;
    }
	
	private JMenu createEditMenu() {
		JMenu editMenu = new JMenu(MENU_TEXT_EDIT);
		editMenu.setName(MENU_NAME_EDIT);
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		editMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_POWER_ON, MENU_ITEM_TEXT_POWER_ON, -1, null, this, false));
		editMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_POWER_OFF, MENU_ITEM_TEXT_POWER_OFF, -1, null, this, false));
		
		editMenu.addSeparator();
		
		editMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_RESTART, MENU_ITEM_TEXT_RESTART, -1, null, this, false));

		return editMenu;
	}
	
	private JMenu createToolsMenu() {
		JMenu toolsMenu = new JMenu(MENU_TEXT_TOOLS);
		toolsMenu.setName(MENU_NAME_TOOLS);
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		
		toolsMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_SHOW_LOG_CONSOLE, MENU_ITEM_TEXT_SHOW_LOG_CONSOLE, -1, null, this));
		
		return toolsMenu;
	}

	private JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu(MENU_TEXT_HELP);
		helpMenu.setName(MENU_NAME_HELP);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		helpMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_ABOUT, MENU_ITEM_TEXT_ABOUT, -1, null, this));
		
		return helpMenu;
	}

	private JMenu createFileMenu() {
		JMenu fileMenu = new JMenu(MENU_TEXT_FILE);
		fileMenu.setName(MENU_NAME_FILE);
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		fileMenu.add(UiUtils.createMenuItem(MENU_ITEM_NAME_QUIT, MENU_ITEM_TEXT_QUIT, -1, null, this));
		
		return fileMenu;
	}
	
	private void setDefaultUiFont(FontUIResource fur) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put (key, fur);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		
		if (MENU_ITEM_NAME_QUIT.equals(actionCommand)) {
			quit();
		} else if (MENU_ITEM_NAME_POWER_ON.equals(actionCommand)) {
			new Thread(new Runnable() {	
				@Override
				public void run() {
					powerOn();					
				}
			}).start();
		} else if (MENU_ITEM_NAME_POWER_OFF.equals(actionCommand)) {
			new Thread(new Runnable() {	
				@Override
				public void run() {
					powerOff();
				}
			}).start();
		} else if (MENU_ITEM_NAME_RESTART.equals(actionCommand)) {
			new Thread(new Runnable() {	
				@Override
				public void run() {
					restart();
				}
			}).start();
		} else if (MENU_ITEM_NAME_SHOW_LOG_CONSOLE.equals(actionCommand)) {
			showLogConsoleDialog();
		} else if (MENU_ITEM_NAME_ABOUT.equals(actionCommand)) {
			showAboutDialog();
		} else {
			throw new IllegalArgumentException("Illegal action command: " + actionCommand);
		}
	}

	private void restart() {
		powerOff();
		powerOn();
	}
	
	private void powerOn() {
		if (lightEmulator.isPowered()) {
			refreshPowerRelativedMenus();
			return;
		}
		
		if (lightEmulator.getInternetLogListener() == null && logConsolesDialog != null)
			lightEmulator.setInternetLogListener(
					logConsolesDialog.getInternetLogListener());
		
		lightEmulator.powerOn();
		
		refreshPowerRelativedMenus();
	}
	
	private void powerOff() {
		if (!lightEmulator.isPowered())
			return;
		
		lightEmulator.powerOff();
		
		refreshPowerRelativedMenus();
	}
	
	private void refreshPowerRelativedMenus() {
		if (lightEmulator.isPowered()) {
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_POWER_ON).setEnabled(false);
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_POWER_OFF).setEnabled(true);
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_RESTART).setEnabled(true);
		} else {
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_POWER_OFF).setEnabled(false);
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_POWER_ON).setEnabled(true);
			UiUtils.getMenuItem(menuBar, MENU_NAME_EDIT, MENU_ITEM_NAME_RESTART).setEnabled(false);
		}
	}

	private void showAboutDialog() {
		UiUtils.showDialog(this, new AboutDialog(this, Sle02ModelDescriptor.DESCRIPTION, Constants.SOFTWARE_VERSION));
	}
	
	private void showLogConsoleDialog() {
		if (logConsolesDialog != null && logConsolesDialog.isVisible())
			return;
		
		if (logConsolesDialog == null) {			
			logConsolesDialog = new LogConsoleDialog(this);
			logConsolesDialog.addWindowListener(this);
		}
		
		if (lightEmulator != null)
			lightEmulator.setInternetLogListener(logConsolesDialog.getInternetLogListener());
		
		logConsolesDialog.setVisible(true);
		
		UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_SHOW_LOG_CONSOLE).setEnabled(false);
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		if (e.getSource() instanceof JFrame) {
			quit();
		} else if (e.getSource() instanceof LogConsoleDialog) {
			logConsolesDialog.setVisible(false);
			logConsolesDialog.dispose();
			logConsolesDialog = null;
			
			UiUtils.getMenuItem(menuBar, MENU_NAME_TOOLS, MENU_ITEM_NAME_SHOW_LOG_CONSOLE).setEnabled(true);
		} else {
			// NO-OP
		}
	}

	private void quit() {
		setVisible(false);
		lightEmulator.powerOff();
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void batteryPowerChanged(BatteryPowerEvent event) {
		lightEmulator.getPanel().updateStatus(lightEmulator.getThingStatus());
	}
	
	@Override
	public void windowOpened(WindowEvent e) {}
}
