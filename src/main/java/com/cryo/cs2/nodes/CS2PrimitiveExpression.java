package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CS2PrimitiveExpression extends CS2Expression implements CS2Constant {

    private final Object value;
    private final CS2Type type;

    public Object getConstant() {
        return value;
    }

    public CS2Expression copy() {
        return new CS2PrimitiveExpression(value, type);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printConstant(printer, value, type);
        printer.endPrinting(this);
    }

    public boolean isInt() {
        return value instanceof Integer;
    }

    public int asInt() {
        return (int) value;
    }

    public String asString() {
        return (String) value;
    }

    public boolean asBoolean() {
        return (boolean) value;
    }
}
