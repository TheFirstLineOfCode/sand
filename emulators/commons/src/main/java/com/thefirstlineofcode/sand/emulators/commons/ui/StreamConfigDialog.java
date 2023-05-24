package com.thefirstlineofcode.sand.emulators.commons.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.thefirstlineofcode.chalk.core.stream.StandardStreamConfig;

public class StreamConfigDialog extends JDialog implements WindowListener, ActionListener {
	private static final boolean DEFAULT_TLS_PREFERRED = true;

	private static final long serialVersionUID = 2334251819432524828L;
	
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_PORT = "6222";

	private static final String ACTION_COMMAND_OK = "ok";
	private static final String ACTION_COMMAND_CANCEL = "cancel";
	
	private JTextField host;
	private JTextField port;
	private JCheckBox tlsPreferred;
	private JButton ok;
	private JButton cancel;
	
	private StandardStreamConfig streamConfig;
	
	public StreamConfigDialog(JFrame parent) {
		this(parent, null);
	}
	
	public StreamConfigDialog(JFrame parent, StandardStreamConfig streamConfig) {
		super(parent, "Stream Config", true);		
		this.streamConfig = streamConfig;
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		panel.add(createHostPanel());
		panel.add(createPortPanel());
		panel.add(createTlsPreferredPanel());
		panel.add(createButtonsPanel());
		panel.setPreferredSize(new Dimension(480, 320));
		
		setContentPane(panel);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		
	}

	private JPanel createButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		
		ok = new JButton("Ok");
		ok.setPreferredSize(new Dimension(100, 48));	
		ok.setActionCommand(ACTION_COMMAND_OK);
		ok.addActionListener(this);
		buttonsPanel.add(ok);
		
		cancel = new JButton("Cancel");
		cancel.setPreferredSize(new Dimension(100, 48));
		cancel.setActionCommand(ACTION_COMMAND_CANCEL);
		cancel.addActionListener(this);
		buttonsPanel.add(cancel);
		
		buttonsPanel.setPreferredSize(new Dimension(400, 48));
		return buttonsPanel;
	}

	private JPanel createTlsPreferredPanel() {
		JPanel tlsPreferredPanel = new JPanel();
		
		JLabel tlsPreferredLabel = new JLabel("TLS Preferred");
		tlsPreferredLabel.setPreferredSize(new Dimension(120, 48));
		tlsPreferredPanel.add(tlsPreferredLabel);
		
		tlsPreferred = new JCheckBox();
		tlsPreferred.setSelected(streamConfig != null ? streamConfig.isTlsPreferred() : DEFAULT_TLS_PREFERRED);
		tlsPreferred.setPreferredSize(new Dimension(240, 48));
		tlsPreferredPanel.add(tlsPreferred);
		
		tlsPreferredPanel.setPreferredSize(new Dimension(400, 48));
		return tlsPreferredPanel;
	}

	private JPanel createHostPanel() {
		JPanel hostPanel = new JPanel();
	
		JLabel hostLabel = new JLabel("Host");
		hostLabel.setPreferredSize(new Dimension(120, 48));
		hostPanel.add(hostLabel);
		
		host = new JTextField(streamConfig != null ? streamConfig.getHost() : DEFAULT_HOST);
		host.setPreferredSize(new Dimension(240, 48));
		hostPanel.add(host);
		
		hostPanel.setPreferredSize(new Dimension(400, 48));
		return hostPanel;
	}
	
	private JPanel createPortPanel() {
		JPanel portPanel = new JPanel();
		
		JLabel portLabel = new JLabel("Port");
		portLabel.setPreferredSize(new Dimension(120, 48));
		portPanel.add(portLabel);
		
		port = new JTextField(DEFAULT_PORT);
		port = new JTextField(streamConfig != null ? Integer.toString(streamConfig.getPort()) : DEFAULT_PORT);
		port.setPreferredSize(new Dimension(240, 48));
		portPanel.add(port);
		
		portPanel.setPreferredSize(new Dimension(400, 48));
		return portPanel;
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		cancel();
	}
	
	private void ok() {
		if (host.getText() == null || host.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Host mustn't be null.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (port.getText() == null || port.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Port mustn't be null.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		int iPort;
		try {
			iPort = Integer.parseInt(port.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Port must be an integer.", "Error", JOptionPane.ERROR_MESSAGE);
			return;			
		}
		
		streamConfig = new StandardStreamConfig(host.getText(), iPort);
		streamConfig.setTlsPreferred(tlsPreferred.isSelected());
		
		setVisible(false);
		dispose();
	}
	
	private void cancel() {
		if (streamConfig != null)
			streamConfig = null;
		
		setVisible(false);
		dispose();
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
	public void actionPerformed(ActionEvent e) {
		if (ACTION_COMMAND_OK.equals(e.getActionCommand())) {
			ok();
		} else if (ACTION_COMMAND_CANCEL.equals(e.getActionCommand())) {
			cancel();
		} else {
			// It's impossible.
			throw new IllegalArgumentException("Illegal action command: " + e.getActionCommand());
		}
	}
	
	public StandardStreamConfig getStreamConfig() {
		return streamConfig;
	}
}
