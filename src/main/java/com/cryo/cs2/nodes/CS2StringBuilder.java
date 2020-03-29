package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2StringBuilder extends CS2Expression {

    private CS2Expression[] expressions;

    public CS2StringBuilder(CS2Expression[] expressions) {
        this.expressions = expressions;
        for(int i = 0; i < expressions.length; i++) {
            this.write(expressions[i]);
            expressions[i].setParent(this);
        }
    }

    public int getPriority() { return CS2Expression.PRIORITY_CONTACTSTRING; }

    public CS2Type getType() { return CS2Type.STRING; }

    public CS2Expression copy() {
        CS2Expression[] expressionsCopy = new CS2Expression[expressions.length];
        for(int i = 0; i < expressions.length; i++)
            expressionsCopy[i] = expressions[i].copy();
        return new CS2StringBuilder(expressionsCopy);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        for (int i = 0; i < expressions.length; i++) {
            boolean needsParen = expressions[i].getPriority() > this.getPriority();
            if (needsParen)
                printer.print('(');
            expressions[i].print(printer);
            if (needsParen)
                printer.print(')');
            if ((i + 1) < expressions.length)
                printer.print(" + ");
        }
        printer.endPrinting(this);
    }
}
