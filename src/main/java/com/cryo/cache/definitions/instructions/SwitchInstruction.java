package com.cryo.cache.definitions.instructions;

public class SwitchInstruction extends Instruction {

    private int[] cases;
    private LabelInstruction[] targets;

    public SwitchInstruction(int opcode, String name, int[] cases, LabelInstruction[] targets) {
        super(opcode, name);
        this.cases = cases;
        this.targets = targets;
    }
}
