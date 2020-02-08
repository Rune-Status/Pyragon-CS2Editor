package com.cryo.cs2.instructions;

import lombok.Data;

@Data
public class BooleanInstruction extends Instruction {

    private boolean value;

    public BooleanInstruction(int opcode, String name, boolean value) {
        super(opcode, name);
        this.value = value;
    }

    public String toString() {
        return "BooleanInstruction(opcode="+opcode+", value="+value+")";
    }
}
