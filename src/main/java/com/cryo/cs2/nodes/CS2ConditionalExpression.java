package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2ConditionalExpression extends CS2Expression {

    private CS2Expression left;
    private CS2Expression right;

    private int conditional;

    public CS2ConditionalExpression(CS2Expression left, CS2Expression right, int conditional) {
        this.left = left;
        this.right = right;
        this.conditional = conditional;

        this.write(left);
        this.write(right);
        this.left.setParent(this);
        this.right.setParent(this);
    }

    @Override
    public int getPriority() {
        if (conditional == 0 || conditional == 1)
            return CS2Expression.PRIORITY_EQNE;
        else if (conditional < 6)
            return CS2Expression.PRIORITY_LELTGEGTINSTANCEOF;
        else if (conditional == 6)
            return CS2Expression.PRIORITY_LOGICALOR;
        else if (conditional == 7)
            return CS2Expression.PRIORITY_LOGICALAND;
        else
            return super.getPriority();
    }

    public CS2Type getType() { return CS2Type.BOOLEAN; }

    private String conditionalToString() {
        switch (this.conditional) {
            case 0:
                return "!=";
            case 1:
                return "==";
            case 2:
                return ">";
            case 3:
                return "<";
            case 4:
                return ">=";
            case 5:
                return "<=";
            case 6:
                return "||";
            case 7:
                return "&&";
            default:
                return "??";
        }
    }

    public CS2Expression copy() {
        return new CS2ConditionalExpression(left.copy(), right.copy(), conditional);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        boolean needsLeftParen = left.getPriority() > this.getPriority();
        boolean needsRightParen = right.getPriority() > this.getPriority();
        if (needsLeftParen)
            printer.print("(");
        this.left.print(printer);
        if (needsLeftParen)
            printer.print(")");
        printer.print(" " + this.conditionalToString() + " ");
        if (needsRightParen)
            printer.print("(");
        this.right.print(printer);
        if (needsRightParen)
            printer.print(")");
        printer.endPrinting(this);
    }
}
