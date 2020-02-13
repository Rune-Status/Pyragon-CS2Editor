package com.cryo.cs2.nodes;

import com.cryo.cs2.flow.CS2FlowBlock;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2SwitchFlowBlockJump extends CS2Node {

    private CS2Expression expression;
    private int[] cases;
    private CS2FlowBlock[] targets;
    private int defaultIndex;

    public CS2SwitchFlowBlockJump(CS2Expression expression, int[] cases, CS2FlowBlock[] targets, int defaultIndex) {
        this.expression = expression;
        this.cases = cases;
        this.targets = targets;
        this.defaultIndex = defaultIndex;
        this.write(expression);
        expression.setParent(this);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("SWITCH (");
        expression.print(printer);
        printer.print(") {");
        printer.tab();
        for (int i = 0; i < cases.length; i++) {
            if (i == defaultIndex)
                printer.print("\ndefault:\n\t GOTO flow_" + targets[i].getBlockId());
            else
                printer.print("\ncase " + cases[i] + ":\n\t GOTO flow_" + targets[i].getBlockId());
        }

        printer.untab();
        printer.print("\n}");
        printer.endPrinting(this);

    }
}
