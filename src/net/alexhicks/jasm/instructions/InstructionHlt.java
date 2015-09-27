package net.alexhicks.jasm.instructions;

import net.alexhicks.jasm.AssemblyVM;
import net.alexhicks.jasm.Instruction;
import net.alexhicks.jasm.ReturnCode;

public class InstructionHlt implements Instruction {
	@Override
	public int[] getArgumentCount() {
		return new int[] {0, 0};
	}

	@Override
	public int handle(AssemblyVM vm, int val) {
		return ReturnCode.HALT;
	}

	@Override
	public String getInstructionName() {
		return "hlt";
	}
	
}