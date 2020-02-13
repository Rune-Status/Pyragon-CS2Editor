package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2StructExpression extends CS2Expression {

    private CS2Type type;
    private CS2Expression[] expressions;

    public CS2StructExpression(CS2Type type, CS2Expression[] expressions) {
        this.type = type;
        this.expressions = expressions;
        for(int i = 0; i < expressions.length; i++) {
            this.write(expressions[i]);
            expressions[i].setParent(this);
        }
    }

    public int getPriority() { return CS2Expression.PRIORITY_CALL; }

    public CS2Expression copy() {
        CS2Expression[] expressionsCopy = new CS2Expression[expressions.length];
        for(int i = 0; i < expressionsCopy.length; i++)
            expressionsCopy[i] = expressions[i].copy();
        return new CS2StructExpression(type, expressionsCopy);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print(type.name() + "");
        printer.print('(');
        for (int i = 0; i < expressions.length; i++) {
            expressions[i].print(printer);
            if ((i + 1) < expressions.length)
                printer.print(", ");
        }
        printer.print(')');
        printer.endPrinting(this);
    }
}
