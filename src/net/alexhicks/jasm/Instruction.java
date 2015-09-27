package net.alexhicks.jasm;



public interface Instruction {
	public abstract int handle(AssemblyVM vm, int val);
	public abstract String getInstructionName();
	/**
	 * @return {minArgCount, maxArgCount}
	 */
	public abstract int[] getArgumentCount();
}
