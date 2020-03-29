package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2LoadConfig extends CS2Expression {

    private final int configId;
    private final String name;

    @Override
    public CS2Type getType() {
        return CS2Type.INT;
    }

    @Override
    public CS2Expression copy() {
        return new CS2LoadConfig(configId, name);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print(name);
        printer.print("(");
        printer.print(Integer.toString(configId));
        printer.print(")");
        printer.endPrinting(this);
    }
}
