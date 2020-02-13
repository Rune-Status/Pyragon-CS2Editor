package com.cryo.cs2.flow;

import com.cryo.cs2.nodes.CS2Node;
import com.cryo.cs2.nodes.CS2Stack;
import com.cryo.utils.CodePrinter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CS2FlowBlock extends CS2Node {

    private int blockId;

    private List<CS2FlowBlock> successors;
    private List<CS2FlowBlock> predecessors;

    private CS2FlowBlock next;
    private CS2FlowBlock prev;

    private CS2Stack stack;

    private int startAddress;

    public CS2FlowBlock() {
        this(0, 0, new CS2Stack());
    }

    public CS2FlowBlock(int blockID, int startAddress,CS2Stack stack) {
        this.blockId = blockID;
        this.startAddress = startAddress;
        this.stack = stack;
        this.successors = new ArrayList<>();
        this.predecessors = new ArrayList<>();
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("flow_" + this.blockId + ":");
        printer.tab();
        List<CS2Node> childs = this.listChilds();
        for (CS2Node node : childs) {
            printer.print('\n');
            node.print(printer);
        }
        printer.untab();
        printer.endPrinting(this);
    }
}
