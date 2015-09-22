package net.alexhicks.jasm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.DefaultListModel;

public class Main {
	public static MainGui gui;
	
	public static void displayReturn(int acc, HashMap<Integer, Cell> cells) {
		DefaultListModel model = new DefaultListModel();
		model.addElement("Accumulator: " + acc);
		for (Cell cell : cells.values()) {
			model.addElement(cell.position + ": " + (cell.isNumber ? cell.value : cell.data));
		}
		gui.outputList.setModel(model);
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
					gui.errorLabel.setText(asm.getLastError());
					return;
				}
				Main.displayReturn(asm.accumulator, asm.cells);
			}
		});
		gui.setVisible(true);
	}
}
