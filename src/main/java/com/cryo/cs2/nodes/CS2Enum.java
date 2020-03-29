package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;

import lombok.Data;

@Data
public class CS2Enum extends CS2Expression {

    private final CS2Expression index;
    private final CS2Expression id;
    private final CS2Expression returnType;
    private final CS2Expression keyType;

    public CS2Enum(CS2Expression index, CS2Expression id, CS2Expression returnType, CS2Expression keyType) {
        this.index = index;
        this.id = id;
        this.returnType = returnType;
        this.keyType = keyType;
        this.write(index);
        this.write(id);
        this.write(returnType);
        this.write(keyType);
        this.index.setParent(this);
        this.id.setParent(this);
        this.returnType.setParent(this);
        this.keyType.setParent(this);
    }

    @Override
    public CS2Type getType() {
        return CS2Type.ENUM;
    }

    @Override
    public CS2Expression copy() {
        return new CS2Enum(index.copy(), id.copy(), returnType.copy(), keyType.copy());
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("enum(");
        index.print(printer);
        printer.print(", ");
        id.print(printer);
        printer.print(", ");
        returnType.print(printer);
        printer.print(", ");
        keyType.print(printer);
        printer.print(")");
        printer.endPrinting(this);
    }

}