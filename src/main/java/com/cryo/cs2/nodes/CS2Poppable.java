package com.cryo.cs2.nodes;

import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2Poppable extends CS2Node {

    private CS2Expression expression;

    public CS2Poppable(CS2Expression expression) {
        this.expression = expression;
        this.write(expression);
        expression.setParent(this);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        expression.print(printer);
        printer.print(';');
        printer.endPrinting(this);
    }
}
