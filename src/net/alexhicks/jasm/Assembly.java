package net.alexhicks.jasm;

import java.util.HashMap;

public class Assembly {

	public HashMap<Integer, Cell> cells;
	public int accumulator = 0;
	private String lastError = "";

	public String getLastError() {
		return lastError;
	}

	public void error(int pos, String msg) {
		this.lastError = "Unable to parse cell " + pos + ":\n" + msg;
	}

	private HashMap<Integer, Cell> parse(String[] lines) {
		HashMap<Integer, Cell> cells = new HashMap<>();
		int skipped = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith(";") || lines[i].isEmpty()) {
				skipped++;
				continue;
			}
			cells.put(i - skipped, new Cell(lines[i], i - skipped));
		}
		return cells;
	}

	private int toNumber(String p) {
		try {
			return Integer.parseInt(p);
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	private int checkLabel(String p) {
		try {
			int pos = Integer.parseInt(p);
			if (pos < cells.size()) {
				return pos;
			} else {
				return -1;
			}
		} catch (NumberFormatException ex) {
			for (Cell cell : cells.values()) {
				if (cell.label.equals(p)) {
					return cell.position;
				}
			}
		}
		return -1;
	}

	private boolean checkPointerInteger(int ptr, int pos) {
		if (!cells.get(ptr).isInteger) {
			error(pos, "Cell " + ptr + " is not an integer.");
			return false;
		}
		return true;
	}

	public boolean execute(String code) {
		cells = parse(code.split("\n"));
		for (int i = 0; i < cells.size(); i++) {
			Cell cell = cells.get(i);
			if (cell.isInteger) {
				accumulator += cell.value;
				continue;
			}
			String[] t = cell.data.split(" ");
			if (t.length == 0) {
				continue;
			}
			String instruction = t[0].toUpperCase();
			if (t.length == 1) { // NOT, SHL, SHR, INC, DEC, HLT
				switch (instruction) {
					case "NOT":
						this.accumulator = accumulator ^ 1;
						break;
					case "SHL":
						this.accumulator *= 2;
						break;
					case "SHR":
						this.accumulator /= 2;
						break;
					case "INC":
						this.accumulator++;
						break;
					case "DEC":
						this.accumulator--;
						break;
					case "HLT":
						return true;
				}
			} else if (t.length == 2) {
				int ptr = checkLabel(t[1]);
				if (ptr < 0) {
					ptr = toNumber(t[1]);
				}
				switch (instruction) {
					case "STO":
						Cell newCell;
						if (!cells.containsKey(ptr)) {
							newCell = new Cell(String.valueOf(accumulator), ptr);
						} else {
							newCell = cells.get(ptr).copy();
						}
						newCell.value = accumulator;
						newCell.isInteger = true;
						cells.put(ptr, newCell);
						break;
					case "LOD-C":
						this.accumulator = ptr;
						break;
					case "ADD-C":
						this.accumulator += ptr;
						break;
					case "SUB-C":
						this.accumulator -= ptr;
						break;
					case "AND-C":
						this.accumulator &= ptr;
						break;
					case "OR-C":
						this.accumulator |= ptr;
						break;
					case "JMP":
						i = ptr;
						break;
					case "JMZ":
						if (accumulator == 0) {
							i = ptr;
						}
						break;
					case "JMN":
						if (accumulator < 0) {
							i = ptr;
						}
						break;
				}
				// These instructions absolutely require their argument
				// TO POINT TO AN INTEGER VALUE.
				if (checkPointerInteger(ptr, cell.position)) {
					switch (instruction) {
						case "LOD":
							this.accumulator = cells.get(ptr).value;
							break;
						case "ADD":
							this.accumulator += cells.get(ptr).value;
							break;
						case "SUB":
							this.accumulator -= cells.get(ptr).value;
							break;
						case "AND":
							this.accumulator &= cells.get(ptr).value;
							break;
						case "OR":
							this.accumulator |= cells.get(ptr).value;
							break;
						case "JMP-I":
							i = cells.get(i).value;
							break;
					}
				}
			} else {
				error(cell.position, "Argument count " + t.length + " is invalid.");
				return false;
			}
		}
		return true;
	}
}
