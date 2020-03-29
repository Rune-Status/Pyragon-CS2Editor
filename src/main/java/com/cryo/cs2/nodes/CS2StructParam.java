package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;

import lombok.Data;

@Data
public class CS2StructParam extends CS2Expression {

    private final int paramId;
    private final CS2Expression structId;

    public CS2StructParam(int paramId, CS2Expression structId) {
        this.paramId = paramId;
        this.structId = structId;
        this.write(structId);
        this.structId.setParent(this);
    }

    @Override
    public CS2Type getType() {
        return CS2Type.INT;
    }

    @Override
    public CS2Expression copy() {
        return new CS2StructParam(paramId, structId);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("struct_param(");
        printer.print(Integer.toString(paramId));
        printer.print(", ");
        structId.print(printer);
        printer.print(")");
        printer.endPrinting(this);
    }
    
}