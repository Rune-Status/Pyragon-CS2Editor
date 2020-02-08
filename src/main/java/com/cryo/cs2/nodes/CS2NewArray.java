package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2NewArray extends CS2Expression {

    private CS2Expression expression;
    private CS2Type type;

    public CS2NewArray(CS2Expression expression, CS2Type type) {
        this.expression = expression;
        this.type = type;
        this.write(expression);
        expression.setParent(this);
    }

    public int getPriority() { return CS2Expression.PRIORITY_ARRAY_INDEX; }

    public CS2Expression copy() {
        return new CS2NewArray(expression.copy(), type);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("new " + type.name());
        printer.print('[');
        expression.print(printer);
        printer.print(']');
        printer.endPrinting(this);
    }
}
