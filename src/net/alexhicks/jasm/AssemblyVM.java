package net.alexhicks.jasm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.alexhicks.jasm.instructions.*;

public class AssemblyVM {
	public HashMap<Integer, Cell> cells = new HashMap<>();
	public int accumulator = 0x0;
	private String lastError = "";
	private final List<Instruction> instructions;
	
	public AssemblyVM() {
		this.instructions = Arrays.asList(new Instruction[] {
			new InstructionAdd(),
			new InstructionAnd(),
			new InstructionDec(),
			new InstructionHlt(),
			new InstructionInc(),
			new InstructionJmn(),
			new InstructionJmp(),
			new InstructionJmz(),
			new InstructionLod(),
			new InstructionNot(),
			new InstructionOr(),
			new InstructionPrint(),
			new InstructionShl(),
			new InstructionShr(),
			new InstructionSto(),
			new InstructionSub(),
			new InstructionXor()
		});
	}

	public String getLastError() {
		return lastError;
	}

	public void error(int pos, String msg) {
		this.lastError = "Unable to parse cell " + pos + ": " + msg;
	}
	
	private int toNumber(String p) {
		try {
			return Integer.parseInt(p);
		} catch (NumberFormatException ex) {
			return ReturnCode.ERROR;
		}
	}
	
	private int checkLabel(String p) {
		try {
			int pos = Integer.parseInt(p);
			if (pos < cells.size()) {
				return pos;
			} else {
				return ReturnCode.ERROR;
			}
		} catch (NumberFormatException ex) {
			for (Cell cell : cells.values()) {
				if (cell.label.equals(p)) {
					return cell.position;
				}
			}
		}
		return ReturnCode.ERROR;
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
	
	public int getCellValue(int pos) {
		if (cells.get(pos).isNumber) {
			return cells.get(pos).value;
		} else {
			return checkLabel(cells.get(pos).data);
		}
	}
	
	private int handle(int index) {
		Cell cell = cells.get(index);
		if (cell.isNumber) {
			accumulator += cell.value;
			return ReturnCode.NOCHANGE;
		}
		String[] t = cell.data.split(" ");
		if (t.length == 0) {
			return ReturnCode.NOCHANGE;
		}
		String instruction = t[0].toLowerCase();
		int[] args = new int[t.length - 1];
		if (args.length > 0) {
			for (int i = 1; i < t.length; i++) {
				int val = toNumber(t[i]);
				if (val == ReturnCode.ERROR) {
					val = checkLabel(t[i]);
					if (val == ReturnCode.ERROR) {
						return val;
					}
				}
				args[i - 1] = val;
			}
		}
		for (Instruction i : instructions) {
			String iname = i.getInstructionName().toLowerCase();
			if (i.getArgumentCount()[0] <= args.length && i.getArgumentCount()[1] >= args.length) {
				if (!instruction.startsWith(iname)) {
					continue;
				}
				if (args.length == 0) {
					return i.handle(this, ReturnCode.NOCELL);
				}
				if (instruction.equals(iname + "-c")) {
					return i.handle(this, args[0]);
				}
				int val = getCellValue(args[0]);
				if (val == ReturnCode.ERROR) {
					return val;
				}
				if (instruction.equals(iname)) {
					return i.handle(this, val);
				}
				val = getCellValue(args[0]);
				if (val == ReturnCode.ERROR) {
					return val;
				}
				if (instruction.equals(iname + "-i")) {
					return i.handle(this, val);
				}
			}
		}
		return ReturnCode.INVALIDINSTRUCTION;
	}

	public boolean execute(String code) {
		parse(code.split("\n"));
		int count = 0, i;
		for (i = 0; i < cells.size() && count < 2000; i++, count++) {
			int newI = handle(i);
			if (newI == ReturnCode.HALT) {
				return true;
			} else if (newI == ReturnCode.ERROR) {
				return false;
			} else if (newI == ReturnCode.NOCHANGE) {
				continue;
			} else if (newI == ReturnCode.INVALIDINSTRUCTION) {
				this.error(i, "Invalid instruction \"" + cells.get(i).data + "\"");
				return false;
			}
			i = newI - 1;
		}
		if (count >= 2000) {
			error(ReturnCode.NOCELL, "Too many iterations (over 2000)");
		}
		System.out.println("End run");
		return true;
	}
	
	private void parse(String[] lines) {
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
}
