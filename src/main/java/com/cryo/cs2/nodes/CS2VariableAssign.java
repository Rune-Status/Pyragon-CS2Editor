package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2VariableAssign extends CS2Expression {

    private LocalVariable variable;
    private CS2Expression expression;
    private boolean isDeclaration = false;

    public CS2VariableAssign(LocalVariable variable, CS2Expression expression) {
        this.variable = variable;
        this.expression = expression;
        this.write(expression);
        expression.setParent(this);
    }

    @Override
    public int getPriority() {
        return CS2Expression.PRIORITY_ASSIGNMENT;
    }

    public CS2Type getType() {
        return expression.getType();
    }

    public CS2Expression copy() {
        return new CS2VariableAssign(this.variable, this.expression.copy());
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        boolean needsParen = expression.getPriority() > getPriority();
        if (isDeclaration)
            printer.print(variable.getType() + " ");
        printer.print(variable.getName() + " = ");
        if (needsParen)
            printer.print('(');
        expression.print(printer);
        if (needsParen)
            printer.print(')');
        printer.endPrinting(this);
    }
}
