package net.alexhicks.jasm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class Main {
	public static MainGui gui;
	
	public static void displayReturn(HashMap<Integer, String> ret) {
		for (Integer i : ret.keySet()) {
			System.out.println(i + ": " + ret.get(i));
		}
	}
	
	public static void main(String[] args) {
		MainGui.setLookAndFeel();
		gui = new MainGui();
		gui.buttonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String code = gui.textCode.getText();
				Assembly asm = new Assembly();
				Main.displayReturn(asm.execute(code));
			}
		});
		gui.setVisible(true);
	}
}
