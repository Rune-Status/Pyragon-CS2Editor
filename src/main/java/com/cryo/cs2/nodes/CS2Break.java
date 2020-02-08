package com.cryo.cs2.nodes;

import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2Break extends CS2Node implements CS2FlowControl {

    private IBreakableNode node;
    private CS2Scope scope;

    public CS2Break(CS2Scope scope, IBreakableNode node) {
        this.node = node;
        this.scope = scope;
        if(this.scope.getParent() != node && node.getLabelName() == null)
            node.enableLabelName();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if (scope.getParent() == node)
            printer.print("break;");
        else
            printer.print("break " + node.getLabelName() + ";");
        printer.endPrinting(this);
    }
}
