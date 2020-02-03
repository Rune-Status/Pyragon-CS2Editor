package com.cryo.cache.definitions.instructions;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public abstract class Instruction {

    private final int opcode;
    private final String name;
    private int address;
}
