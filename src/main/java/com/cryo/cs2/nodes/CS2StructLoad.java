package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2StructLoad extends CS2Expression {

    private String name;
    private CS2Type type;
    private CS2Expression expression;

    public CS2StructLoad(String name, CS2Type type, CS2Expression expression) {
        this.name = name;
        this.type = type;
        this.expression = expression;
        this.write(expression);
        expression.setParent(this);
    }

    public CS2Expression copy() {
        return new CS2StructLoad(name, type, expression.copy());
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        boolean needsParen = expression.getPriority() > CS2Expression.PRIORITY_MEMBER_ACCESS;
        if (needsParen)
            printer.print('(');
        expression.print(printer);
        if (needsParen)
            printer.print(')');
        printer.print("." + name);
        printer.endPrinting(this);
    }
}
