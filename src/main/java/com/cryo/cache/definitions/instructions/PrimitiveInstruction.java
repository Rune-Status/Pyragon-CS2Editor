package com.cryo.cache.definitions.instructions;

import lombok.Data;

public class PrimitiveInstruction extends Instruction {

    private Object value;

    public PrimitiveInstruction(int opcode, String name, Object value) {
        super(opcode, name);
        this.value = value;
    }

}
