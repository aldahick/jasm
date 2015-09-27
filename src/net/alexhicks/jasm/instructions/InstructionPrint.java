package net.alexhicks.jasm.instructions;

import net.alexhicks.jasm.AssemblyVM;
import net.alexhicks.jasm.Instruction;
import net.alexhicks.jasm.ReturnCode;

public class InstructionPrint implements Instruction {
	@Override
	public int[] getArgumentCount() {
		return new int[] {1, 1};
	}

	@Override
	public int handle(AssemblyVM vm, int val) {
		System.out.println(val);
		return ReturnCode.NOCHANGE;
	}

	@Override
	public String getInstructionName() {
		return "print";
	}
	
}