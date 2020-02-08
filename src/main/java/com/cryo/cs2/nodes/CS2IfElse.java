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
