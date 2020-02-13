package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Script;
import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;

public class CS2BasicExpression extends CS2Expression {

    private CS2Expression[] expressions;
    private String command;

    public CS2BasicExpression(CS2Expression expression, String command) {
        this(new CS2Expression[] { expression }, command);
    }

    public CS2BasicExpression(CS2Expression[] expressions, String command) {
        this.expressions = expressions;
        this.command = command;
        for(int i = 0; i < expressions.length; i++) {
            this.write(expressions[i]);
            expressions[i].setParent(this);
        }
    }

    public int getPriority() { return CS2Expression.PRIORITY_CALL; }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print(command);
        printer.print("(");
        for(int i = expressions.length-1; i >= 0; i--) {
            if((command.equals("if_getwidth") || command.equals("if_settext")
                || command.equals("cc_find") || command.equals("cc_deleteall")
                || command.equals("if_getheight")) && expressions[i] instanceof CS2PrimitiveExpression) {
                int hash = ((CS2PrimitiveExpression) expressions[i]).asInt();
                int[] interfaceIds = CS2Script.getInterfaceIds(hash);
                printer.print("if_gethash(");
                printer.print(Integer.toString(interfaceIds[0]));
                printer.print(", ");
                printer.print(Integer.toString(interfaceIds[1]));
                printer.print(")");
            } else
                expressions[i].print(printer);
            if(i != 0)
                printer.print(", ");
        }
        printer.print(")");
        printer.endPrinting(this);
    }

    @Override
    public CS2Type getType() {
        return CS2Type.INT;
    }

    @Override
    public CS2Expression copy() {
        CS2Expression[] copy = new CS2Expression[expressions.length];
        for(int i = 0; i < copy.length; i++)
            copy[i] = expressions[i].copy();
        return new CS2BasicExpression(copy, command);
    }
}
