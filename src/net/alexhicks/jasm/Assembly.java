package net.alexhicks.jasm;

import java.util.HashMap;

public class Assembly {

	public HashMap<Integer, Cell> cells;
	public int accumulator = 0x0;
	private String lastError = "";

	public String getLastError() {
		return lastError;
	}

	public void error(int pos, String msg) {
		this.lastError = "Unable to parse cell " + pos + ": " + msg;
	}

	private void parse(String[] lines) {
		cells = new HashMap<>();
		int skipped = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith(";") || lines[i].isEmpty()) {
				skipped++;
				continue;
			}
			if (lines[i].contains(";")) {
				lines[i] = lines[i].substring(0, lines[i].indexOf(";"));
			}
			Cell cell = new Cell(lines[i], i - skipped);
			cells.put(i - skipped, cell);
		}
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
		if (!cells.get(ptr).isNumber) {
			error(pos, "Cell " + ptr + " is not an integer.");
			return false;
		}
		return true;
	}

	private void STO(int ptr) {
		Cell newCell;
		if (!cells.containsKey(ptr)) {
			newCell = new Cell(String.valueOf(accumulator), ptr);
		} else {
			newCell = cells.get(ptr).copy();
		}
		newCell.value = accumulator;
		newCell.isNumber = true;
		cells.put(ptr, newCell);
	}

	private int handle(int i) {
		Main.gui.outputList.setSelectedIndex(i);
		Cell cell = cells.get(i);
		if (cell.isNumber) {
			accumulator += cell.value;
			return i;
		}
		String[] t = cell.data.split(" ");
		if (t.length == 0) {
			return i;
		}
		String instruction = t[0].toUpperCase();
		if (t.length == 1) { // NOT, SHL, SHR, INC, DEC, HLT
			switch (instruction) {
				case "NOT":
					this.accumulator = ~accumulator;
					break;
				case "SHL":
					this.accumulator *= 2;
					break;
				case "SHR":
					this.accumulator /= 2;
					break;
				case "INC":
					this.accumulator += 1;
					break;
				case "DEC":
					this.accumulator -= 1;
					break;
				case "HLT":
					return -1;
				case "PRINT": // Prints the accumulator value
					System.out.println(this.accumulator);
					break;
				default:
					error(cell.position, "Instruction \"" + instruction + "\" is invalid.");
					return -2;
			}
		} else if (t.length == 2) {
			int ptr = checkLabel(t[1]);
			if (ptr < 0) {
				ptr = toNumber(t[1]);
			}
			switch (instruction) {
				case "STO":
					STO(ptr);
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
				case "XOR-C":
					this.accumulator ^= ptr;
					break;
				case "PRINT-C":
					System.out.println(ptr);
					break;
				case "JMP":
					return ptr;
				case "JMZ":
					if (accumulator == 0) {
						return ptr;
					}
					break;
				case "JMN":
					if (accumulator < 0) {
						return ptr;
					}
					break;
				default:
					boolean pointsToInteger;
					try {
						pointsToInteger = checkPointerInteger(ptr, cell.position);
					} catch (NullPointerException ex) {
						error(cell.position, "Pointer " + ptr + " is not valid!");
						return -2;
					}
					// These instructions absolutely require that their argument points at an integer value.
					if (pointsToInteger) {
						switch (instruction) {
							case "PRINT": // This is just for debugging
								System.out.println(cells.get(ptr).value);
								break;
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
							case "XOR":
								this.accumulator ^= cells.get(ptr).value;
								break;
							default:
								// XXX-I
								int optr = ptr;
								ptr = checkLabel(String.valueOf(cells.get(optr).value));
								if (ptr == -1) {
									error(cell.position, "Invalid pointer " + ptr + " in cell " + optr + ".");
									return -2;
								}
								switch (instruction) {
									case "STO-I":
										STO(ptr);
										break;
									case "LOD-I":
										this.accumulator = cells.get(ptr).value;
										break;
									case "ADD-I":
										this.accumulator += cells.get(ptr).value;
										break;
									case "SUB-I":
										this.accumulator -= cells.get(ptr).value;
										break;
									case "AND-I":
										this.accumulator &= cells.get(ptr).value;
										break;
									case "OR-I":
										this.accumulator |= cells.get(ptr).value;
										break;
									case "XOR-I":
										this.accumulator ^= cells.get(ptr).value;
										break;
									case "JMP-I":
										return cells.get(ptr).value;
									case "JMZ-I":
										if (accumulator == 0) {
											return ptr;
										}
										break;
									case "JMN-I":
										if (accumulator < 0) {
											return ptr;
										}
										break;
									default:
										error(cell.position, "Instruction \"" + instruction + "\" is invalid.");
										return -2;
								}
						}
					} else {
						error(cell.position, "Pointer " + ptr + " does not point to an integer!");
						return -2;
					}
					break;
			}
		} else {
			error(cell.position, "Argument count " + t.length + " is invalid.");
			return -2;
		}
		return i;
	}

	public boolean execute(String code) {
		parse(code.split("\n"));
		int count = 0, i;
		for (i = 0; i < cells.size() && count < 2000; i++, count++) {
			int newI = handle(i);
			if (newI == -1) {
				return true;
			} else if (newI == -2) {
				return false;
			} else if (newI == i) {
				continue;
			}
			i = newI - 1;
		}
		if (count >= 2000) {
			error(-1, "Too many iterations (over 2000)");
		}
		System.out.println("End run");
		return true;
	}
}
