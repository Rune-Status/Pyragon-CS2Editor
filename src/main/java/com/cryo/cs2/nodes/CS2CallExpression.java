package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.decompiler.util.FunctionInfo;
import com.cryo.utils.CodePrinter;
import com.cryo.utils.ScriptDAO;

import lombok.Data;

@Data
public class CS2CallExpression extends CS2Expression {

    private ScriptDAO info;
    private CS2Expression[] expressions;

    public CS2CallExpression(ScriptDAO info, CS2Expression[] expressions) {
        this.info = info;
        this.expressions = expressions;
        for(int i = 0; i < expressions.length; i++) {
            this.write(expressions[i]);
            expressions[i].setParent(this);
        }
    }

    public int getPriority() { return CS2Expression.PRIORITY_CALL; }

    public CS2Type getType() { return info.getReturnType(); }

    public CS2Expression copy() {
        CS2Expression[] copy = new CS2Expression[expressions.length];
        for(int i = 0; i < copy.length; i++)
            copy[i] = expressions[i].copy();
        return new CS2CallExpression(info, copy);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print(info.getName());
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
