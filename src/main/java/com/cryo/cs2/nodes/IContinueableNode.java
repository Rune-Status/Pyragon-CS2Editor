package com.cryo.cs2.nodes;

import com.cryo.cs2.flow.CS2FlowBlock;

public interface IContinueableNode extends IControllableFlowNode {

    CS2FlowBlock getStart();
    boolean canContinue();
}
