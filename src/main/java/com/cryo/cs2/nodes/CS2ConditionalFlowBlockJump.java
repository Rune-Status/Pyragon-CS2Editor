package com.cryo.cs2.nodes;

import com.cryo.cs2.flow.CS2FlowBlock;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2ConditionalFlowBlockJump extends CS2Node {

    private CS2Expression expression;
    private CS2FlowBlock target;

    public CS2ConditionalFlowBlockJump(CS2Expression expression, CS2FlowBlock target) {
        this.expression = expression;
        this.target = target;
        this.write(expression);
        expression.setParent(this);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("IF (");
        expression.print(printer);
        printer.print(") ");
        printer.tab();
        printer.print("\nGOTO\t" + "flow_" + target.getBlockId());
        printer.untab();
        printer.endPrinting(this);
    }
}
