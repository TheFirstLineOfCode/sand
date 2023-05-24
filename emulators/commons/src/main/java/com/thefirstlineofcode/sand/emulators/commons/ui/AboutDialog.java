package com.thefirstlineofcode.sand.emulators.commons.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutDialog extends JDialog {
	private static final long serialVersionUID = -1872492975053174389L;

	public AboutDialog(JFrame parent, String softwareName, String softwareVersion) {
		super(parent, "About Dialog", true);
		
		Box box = Box.createVerticalBox();
		JPanel softwarePanel = new JPanel();
		softwarePanel.setLayout(new BorderLayout());
		JLabel softwareLabel = new JLabel(String.format("%s %s", softwareName, softwareVersion));
		softwareLabel.setHorizontalAlignment(JLabel.CENTER);
		softwarePanel.add(softwareLabel, BorderLayout.CENTER);
		box.add(softwarePanel);
		
		JPanel creatorPanel = new JPanel();
		creatorPanel.setLayout(new BorderLayout());
		creatorPanel.add(new JLabel("Created by TheFirstLineOfCode."), BorderLayout.EAST);
		box.add(creatorPanel);
		
		getContentPane().add(box, "Center");
		
		JPanel okPanel = new JPanel();
		JButton ok = new JButton("Ok");
		okPanel.add(ok);
		getContentPane().add(okPanel, "South");
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});
		
		setPreferredSize(new Dimension(360, 200));
	}
}
