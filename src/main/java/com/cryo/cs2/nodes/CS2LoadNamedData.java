package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2LoadNamedData extends CS2Expression {

    private final String name;
    private final CS2Type type;

    public int getPriority() { return CS2Expression.PRIORITY_STANDART; }

    public CS2Expression copy() { return new CS2LoadNamedData(name, type); }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print(name);
        printer.endPrinting(this);
    }
}
