package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.decompiler.ast.ExpressionNode;
import com.cryo.decompiler.ast.NotExpressionNode;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2NotExpression extends CS2Expression {

    private CS2Expression expression;

    public CS2NotExpression(CS2Expression expression) {
        this.expression = expression;
        this.write(expression);
        this.expression.setParent(this);
    }

    @Override
    public int getPriority() {
        return ExpressionNode.PRIORITY_UNARYLOGICALNOT;
    }

    @Override
    public CS2Type getType() {
        return CS2Type.BOOLEAN;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        boolean needsParen = expression.getPriority() > this.getPriority();
        printer.print('!');
        if (needsParen)
            printer.print("(");
        this.expression.print(printer);
        if (needsParen)
            printer.print(")");
        printer.endPrinting(this);
    }

    @Override
    public CS2Expression copy() {
        return new CS2NotExpression(expression.copy());
    }

}
