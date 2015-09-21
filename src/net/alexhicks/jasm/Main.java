package net.alexhicks.jasm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class Main {
	public static MainGui gui;
	
	public static void displayReturn(int acc, HashMap<Integer, Cell> cells) {
		System.out.println("Accumulator: " + acc);
		for (Cell cell : cells.values()) {
			System.out.println(cell.position + (cell.label.isEmpty() ? "" : " (" + cell.label + ")") + ": " + (cell.isInteger ? cell.value : cell.data));
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
				if (!asm.execute(code)) {
					System.out.println("Error: " + asm.getLastError());
					return;
				}
				Main.displayReturn(asm.accumulator, asm.cells);
			}
		});
		gui.setVisible(true);
	}
}
