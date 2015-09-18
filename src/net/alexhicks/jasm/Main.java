package net.alexhicks.jasm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;

public class Main {
	public static JFileChooser fileChooser;
	public static MainGui gui;
	public static void main(String[] args) {
		MainGui.setLookAndFeel();
		fileChooser = new JFileChooser();
		gui = new MainGui();
		gui.buttonFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Main.fileChooser.showOpenDialog(Main.gui);
			}
		});
		gui.setVisible(true);
	}
}
