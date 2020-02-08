package com.cryo.cs2.nodes;

import com.cryo.cs2.flow.CS2FlowBlock;

public interface IBreakableNode extends IControllableFlowNode {

    CS2FlowBlock getEnd();
    boolean canBreak();
}
