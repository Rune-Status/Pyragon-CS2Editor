package com.cryo.cs2.nodes;

import com.cryo.utils.CodePrinter;
import lombok.Getter;

public class CS2Return extends CS2Node {

    @Getter
    private CS2Expression expression;

    public CS2Return() {
        this(null);
    }

    public CS2Return(CS2Expression expression) {
        this.expression = expression;
        if(this.expression != null) {
            this.write(expression);
            expression.setParent(this);
        }
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if (this.expression != null) {
            printer.print("return ");
            expression.print(printer);
            printer.print(';');
        }
        else {
            printer.print("return;");
        }
        printer.endPrinting(this);
    }
}
