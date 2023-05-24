package com.thefirstlineofcode.sand.emulators.commons.ui;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

public class UiUtils {
	private static final int DEFAULT_NOTIFICATION_DELAY_TIME = 1000 * 2;
	
	public static void showNotification(final Window window, final String title, final String message) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				final JDialog dialog = new JDialog(window, title, ModalityType.MODELESS);
				dialog.setBounds(getParentCenterBounds(window, 400, 160));
				dialog.add(new JLabel(message));
				dialog.setVisible(true);
				
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						dialog.setVisible(false);
						dialog.dispose();
						
						timer.cancel();
					}
				}, DEFAULT_NOTIFICATION_DELAY_TIME);
			}
			
		}).start();
	}
	
	private static Rectangle getParentCenterBounds(Window window, int width, int height) {
		int parentX = window.getX();
		int parentY = window.getY();
		int parentWidth = window.getWidth();
		int parentHeight = window.getHeight();
		
		if (width > parentWidth || height > parentHeight)
			return new Rectangle(parentX, parentY, width, height);
		
		return new Rectangle((parentX + (parentWidth - width) / 2), (parentY + (parentHeight - height) / 2), width, height);
	}
	
	public static void showDialog(Window parent, JDialog dialog) {
		Dimension size = dialog.getPreferredSize();
		Rectangle bounds = parent.getBounds();
		int x = (int)(bounds.x + (bounds.getWidth() - size.width) / 2);
		int y = (int)(bounds.y + (bounds.getHeight() - size.height) / 2);
		
		dialog.setBounds(x, y, size.width, size.height);
		dialog.setVisible(true);
	}
	
	public static JMenuItem createMenuItem(String name, String text, int mnemonic, KeyStroke accelerator, ActionListener actionListener) {
		return createMenuItem(name, text, mnemonic, accelerator, actionListener, true);
	}
	
	public static JMenuItem createMenuItem(String name, String text, int mnemonic, KeyStroke accelerator, ActionListener actionListener, boolean enabled) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.setName(name);
		
		if (mnemonic != -1)
			menuItem.setMnemonic(mnemonic);
		
		if (accelerator != null) {			
			menuItem.setAccelerator(accelerator);
		}
		
		menuItem.setActionCommand(name);
		menuItem.addActionListener(actionListener);
		
		menuItem.setEnabled(enabled);
		
		return menuItem;
	}
	
	public static JMenuItem getMenuItem(JMenuBar menuBar, String menuName, String menuItemName) {
		JMenu menu = getMenu(menuBar, menuName);
		
		for (MenuElement child : menu.getSubElements()[0].getSubElements()) {
			JMenuItem menuItem = (JMenuItem)child;
			if (menuItemName.equals(menuItem.getName()))
				return menuItem;
		}
		
		throw new IllegalArgumentException(String.format("Menu item '%s->%s' not existed.", menuName, menuItemName));
	}

	public static JMenu getMenu(JMenuBar menuBar, String menuName) {
		for (MenuElement child : menuBar.getSubElements()) {
			JMenu menu = (JMenu)child;
			if (menuName.equals(menu.getName())) {
				return menu;
			}
		}
		
		throw new IllegalArgumentException(String.format("Menu '%s' not existed.", menuName));
	}
}
