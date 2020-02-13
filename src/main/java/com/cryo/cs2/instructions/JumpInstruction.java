package com.cryo.cs2.instructions;

import lombok.Data;

@Data
public class JumpInstruction extends Instruction {

    private final LabelInstruction target;

    public JumpInstruction(int opcode, String name, LabelInstruction target) {
        super(opcode, name);
        this.target = target;
    }
}
