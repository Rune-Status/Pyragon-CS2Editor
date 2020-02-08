package com.cryo.cs2.nodes;


import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2Continue extends CS2Node implements CS2FlowControl {

    private IContinueableNode node;
    private CS2Scope scope;

    public CS2Continue(CS2Scope scope, IContinueableNode node) {
        this.node = node;
        this.scope = scope;
        if(this.scope.getParent() != node && node.getLabelName() == null)
            node.enableLabelName();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if (scope.getParent() == node)
            printer.print("continue;");
        else
            printer.print("continue " + node.getLabelName() + ";");
        printer.endPrinting(this);
    }
}
