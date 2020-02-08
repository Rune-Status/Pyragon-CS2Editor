package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;

public class CS2FunctionExpression extends CS2Expression {

    private CS2Expression id;

    private CS2Function function;

    public CS2FunctionExpression(CS2Expression id, CS2Function function) {
        this.id = id;
        this.function = function;
        this.write(id);
        id.setParent(this);
    }

    public CS2Type getType() { return CS2Type.FUNCTION; }

    public CS2Expression copy() {
        return new CS2FunctionExpression(id.copy(), function);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if (function != null) {
            printer.print("new " + getType() + "<" + function.getName() + ">" + "(");
            LocalVariable[] locals = function.getLocalArguments();
            for (int i = 0; i < locals.length; i++) {
                printer.print(locals[i].toString());
                if ((i + 1) < locals.length)
                    printer.print(',');
            }
            printer.print(") ");
            function.getScope().print(printer);
        }
        else {
            printer.print("load " + getType() + "<");
            id.print(printer);
            printer.print('>');

        }
        printer.endPrinting(this);
    }
}
