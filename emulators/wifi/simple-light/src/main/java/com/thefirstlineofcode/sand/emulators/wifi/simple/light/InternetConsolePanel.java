package com.thefirstlineofcode.sand.emulators.wifi.simple.light;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.thefirstlineofcode.basalt.xmpp.core.Protocol;
import com.thefirstlineofcode.chalk.network.ConnectionException;
import com.thefirstlineofcode.chalk.network.IConnectionListener;

public class InternetConsolePanel extends JPanel implements IConnectionListener {
	private static final long serialVersionUID = -1225545939299160156L;

	protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

	protected final Map<Protocol, Class<?>> protocolToTypes = new HashMap<>();
	
	private JTextArea logConsole;
	private JButton clear;
	
	public InternetConsolePanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		logConsole = new JTextArea();
		logConsole.setAutoscrolls(true);
		Font font = logConsole.getFont();
		if (font.getSize() > 8)
			logConsole.setFont(new Font("LogFont", font.getStyle(), font.getSize() - 8));
		
		add(new JScrollPane(logConsole));
		
		clear = new JButton("Clear Console");
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				logConsole.setText(null);
			}
		});
		add(clear);
	}
	
	protected synchronized void log(Exception e) {
		StringWriter out = new StringWriter();
		e.printStackTrace(new PrintWriter(out));
		logConsole.append(out.getBuffer().toString());
		logConsole.append(LINE_SEPARATOR);
		
		logConsole.setCaretPosition(logConsole.getDocument().getLength() - 1);
	}
	
	protected synchronized void log(String message) {
		logConsole.append(message);
		logConsole.append(LINE_SEPARATOR);
		
		logConsole.setCaretPosition(logConsole.getDocument().getLength());
	}
	
	@Override
	public void exceptionOccurred(ConnectionException exception) {
		log(exception);
	}

	@Override
	public void messageReceived(String message) {
		log("G<--S: " + message);
	}

	@Override
	public void messageSent(String message) {
		log("G-->S: " + message);
	}

	@Override
	public void heartBeatsReceived(int length) {
		// Do nothing.
	}
}
