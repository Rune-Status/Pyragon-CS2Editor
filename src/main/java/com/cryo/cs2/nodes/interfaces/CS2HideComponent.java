package com.cryo.cs2.nodes.interfaces;

import com.cryo.cs2.CS2Script;
import com.cryo.cs2.nodes.CS2Expression;
import com.cryo.cs2.nodes.CS2Node;
import com.cryo.cs2.nodes.CS2PrimitiveExpression;
import com.cryo.utils.CodePrinter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CS2HideComponent extends CS2Node {

    private CS2Expression expression;
    private CS2Expression hidden;

    public CS2HideComponent(CS2Expression expression, CS2Expression hidden) {
        this.expression = expression;
        this.hidden = hidden;
        this.write(expression);
        this.write(hidden);
        expression.setParent(this);
        hidden.setParent(this);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("if_sethide(");
        if(expression instanceof CS2PrimitiveExpression) {
            int hash = ((CS2PrimitiveExpression) expression).asInt();
            int[] interfaceIds = CS2Script.getInterfaceIds(hash);
            printer.print("if_gethash(");
            printer.print(Integer.toString(interfaceIds[0]));
            printer.print(", ");
            printer.print(Integer.toString(interfaceIds[1]));
            printer.print(")");
        } else 
            expression.print(printer);
        printer.print(", ");
        hidden.print(printer);
        printer.print(")");
        printer.endPrinting(this);
    }
}
