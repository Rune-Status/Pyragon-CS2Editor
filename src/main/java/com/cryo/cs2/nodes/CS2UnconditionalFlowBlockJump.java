package com.cryo.cs2.nodes;

import com.cryo.cs2.flow.CS2FlowBlock;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2UnconditionalFlowBlockJump extends CS2Node {

    private final CS2FlowBlock target;

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("GOTO\t" + "flow_" + target.getBlockId());
        printer.endPrinting(this);
    }
}
