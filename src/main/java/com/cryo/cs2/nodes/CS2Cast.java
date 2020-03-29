package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2Cast extends CS2Expression {

    private CS2Type type;
    private CS2Expression expression;

    public CS2Cast(CS2Type type, CS2Expression expression) {
        this.type = type;
        this.expression = expression;
        this.write(expression);
        expression.setParent(this);
    }

    public int getPriority() { return expression.getPriority(); }

    public CS2Expression copy() {
        return new CS2Cast(type, expression.copy());
    }

    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        expression.print(printer);
        printer.endPrinting(this);
    }
}
