package com.cryo.cs2.nodes;

import com.cryo.cs2.flow.CS2FlowBlock;
import com.cryo.utils.CodePrinter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CS2Switch extends CS2Node implements IBreakableNode {

    private CS2Expression expression;

    private CS2Scope scope;

    private CS2FlowBlock end;

    private String labelName;

    public CS2Switch(CS2FlowBlock end, CS2Scope scope, CS2Expression expression) {
        this.end = end;
        this.expression = expression;
        this.scope = scope;
        this.write(expression);
        this.write(scope);
        expression.setParent(this);
        scope.setParent(this);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if (this.labelName != null)
            printer.print(this.labelName + " ");
        printer.print("switch (");
        expression.print(printer);
        printer.print(") ");
        scope.print(printer);
        printer.endPrinting(this);
    }

    @Override
    public boolean canBreak() {
        return this.end != null;
    }

    @Override
    public CS2FlowBlock getEnd() {
        return this.end;
    }

    @Override
    public void enableLabelName() {
        this.labelName = "switch_" + this.hashCode() + ":";
    }

    @Override
    public String getLabelName() {
        return this.labelName;
    }
}
