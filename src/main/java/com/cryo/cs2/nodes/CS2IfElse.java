package com.cryo.cs2.nodes;

import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2IfElse extends CS2Node {

    private CS2Expression[] expressions;

    private CS2Scope[] scopes;

    private CS2Scope elseScope;

    public CS2IfElse(CS2Expression[] expressions, CS2Scope[] scopes, CS2Scope elseScope) {
        this.expressions = expressions;
        this.scopes = scopes;
        this.elseScope = elseScope;
        for (int i = 0; i < expressions.length; i++) {
            this.write(expressions[i]);
            expressions[i].setParent(this);
        }
        for (int i = 0; i < scopes.length; i++) {
            this.write(scopes[i]);
            scopes[i].setParent(this);
        }
        this.write(elseScope);
        elseScope.setParent(this);
    }

    public boolean hasElseScope() {
        return !this.elseScope.isEmpty();
    }


    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if(hasElseScope() && expressions.length == 1 && scopes[0].listChilds().size() == 1 && elseScope.listChilds().size() == 1) {
            CS2Node child1 = scopes[0].listChilds().get(0);
            CS2Node child2 = elseScope.listChilds().get(0);
            if(child1 instanceof CS2Poppable && child2 instanceof CS2Poppable) {
                CS2Poppable pop1 = (CS2Poppable) child1;
                CS2Poppable pop2 = (CS2Poppable) child2;
                if(pop1.getExpression() instanceof CS2VariableAssign && pop2.getExpression() instanceof CS2VariableAssign) {
                    CS2VariableAssign a1 = (CS2VariableAssign) pop1.getExpression();
                    CS2VariableAssign a2 = (CS2VariableAssign) pop2.getExpression();
                    printer.print(a1.getVariable().getName());
                    printer.print(" = ");
                    expressions[0].print(printer);
                    printer.print(" ? ");
                    a1.getExpression().print(printer);
                    printer.print(" : ");
                    a2.getExpression().print(printer);
                    printer.print(";");
                }
                return;
            }
        }
        for (int i = 0; i < expressions.length; i++) {
            boolean first = i == 0;
            printer.print(first ? "if (" : "\nelse if (");
            expressions[i].print(printer);
            printer.print(") ");
            scopes[i].print(printer);
        }
        if (hasElseScope()) {
            printer.print("\nelse ");
            elseScope.print(printer);
        }
        printer.endPrinting(this);
    }
}
