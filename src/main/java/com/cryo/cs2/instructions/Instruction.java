package com.cryo.cs2.instructions;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public abstract class Instruction {

    protected final int opcode;
    private final String name;
    private int address;
}
