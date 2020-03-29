package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LocalVariable {

    private String name;
    private final CS2Type type;
    private int identifier = -1;
    private boolean scopeDeclarationNeeded = true;
    private final boolean isArgument;

    public LocalVariable(String name, CS2Type type) {
        this(name, type, false);
    }

    public LocalVariable(String name, CS2Type type, boolean isArgument) {
        this.name = name;
        this.type = type;
        this.isArgument = isArgument;
    }

    public int[] getInfo() {
        return new int[] { (identifier & 0xffff), (identifier >> 16) };
    }

    public static int makeIdentifier(int index, int stackType) {
        return index | stackType << 16;
    }

    public static int makeStackDumpIdentifier(int index, int stackType) {
        return index | (stackType << 16) | 0x40000000;
    }

    public String toString() {
        return type + " " + name;
    }
}
