package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2ToString extends CS2Expression {

    private final CS2Expression expression;

    public CS2ToString(CS2Expression expression) {
        this.expression = expression;
        this.write(expression);
        this.expression.setParent(this);
    }

    @Override
    public CS2Type getType() {
        return CS2Type.STRING;
    }

    @Override
    public CS2Expression copy() {
        return new CS2ToString(expression.copy());
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("<tostring(");
        expression.print(printer);
        printer.print(")>");
        printer.endPrinting(this);
    }
}
