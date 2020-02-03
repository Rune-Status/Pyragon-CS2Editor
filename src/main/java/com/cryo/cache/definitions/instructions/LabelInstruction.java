package com.cryo.cache.definitions.instructions;

import lombok.Data;

@Data
public class LabelInstruction extends Instruction {

    private int labelId;

    public LabelInstruction() {
        super(-1, null);
    }

    public LabelInstruction(int opcode, int labelId) {
        super(opcode, null);
        this.labelId = labelId;
    }
}
