package com.cryo.cs2.instructions;

import com.cryo.decompiler.CS2Type;
import lombok.Data;

@Data
public class PrimitiveInstruction extends Instruction {

    private Object value;
    private CS2Type type;

    public PrimitiveInstruction(int opcode, String name, Object value, CS2Type type) {
        super(opcode, name);
        this.value = value;
        this.type = type;
    }

    public String asString() {
        return (String) value;
    }

    public int asInt() {
        return (int) value;
    }

    public long asLong() {
        return (long) value;
    }

    public String toString() {
        return "PrimitiveInstruction(opcode="+opcode+", value="+value+", type="+type+")";
    }

}
