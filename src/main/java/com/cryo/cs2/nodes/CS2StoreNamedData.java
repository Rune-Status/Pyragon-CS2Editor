package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2StoreNamedData extends CS2Expression {

    private String name;
    private CS2Expression expression;

    public CS2StoreNamedData(String name, CS2Expression expression) {
        this.name = name;
        this.expression = expression;
        this.write(expression);
        expression.setParent(this);
    }

    public int getPriority() { return CS2Expression.PRIORITY_ASSIGNMENT; }

    public CS2Type getType() { return this.expression.getType(); }

    public CS2Expression copy() {
        return new CS2StoreNamedData(name, expression.copy());
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        boolean needsParen = expression.getPriority() > getPriority();
        printer.print(name + " = ");
        if (needsParen)
            printer.print('(');
        expression.print(printer);
        if (needsParen)
            printer.print(')');
        printer.endPrinting(this);
    }
}
