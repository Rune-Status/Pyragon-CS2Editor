package com.cryo.cs2.nodes;

import com.cryo.cs2.flow.CS2FlowBlock;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2Loop extends CS2Node implements IBreakableNode, IContinueableNode {

    public static final int LOOPTYPE_WHILE = 0;
    public static final int LOOPTYPE_DOWHILE = 1;
    public static final int LOOPTYPE_FOR = 2;

    private CS2VariableAssign[] preAssigns;

    private CS2Expression expression;

    private CS2Scope scope;

    private CS2VariableAssign[] afterAssigns;

    private CS2FlowBlock start;

    private CS2FlowBlock end;

    private String labelName;

    private int type;

    public CS2Loop(int type, CS2Scope scope, CS2Expression expr,CS2FlowBlock start,CS2FlowBlock end) {
        this.type = type;
        this.expression = expr;
        this.scope = scope;
        this.start = start;
        this.end = end;
        this.write(expr);
        this.write(scope);
        expr.setParent(this);
        this.scope.setParent(this);
    }

    public CS2Scope getScope() {
        return scope;
    }


    public CS2Expression getExpression() {
        return expression;
    }

    public CS2VariableAssign[] getPreAssigns() {
        return preAssigns;
    }

    public CS2VariableAssign[] getAfterAssigns() {
        return afterAssigns;
    }

    public void forTransform(CS2VariableAssign[] preAssigns, CS2VariableAssign[] afterAssigns) {
        this.type = LOOPTYPE_FOR;
        this.preAssigns = preAssigns;
        this.afterAssigns = afterAssigns;

        setCodeAddress(0);
        for (int i = 0; i < preAssigns.length; i++) {
            preAssigns[i].setParent(this);
            write(preAssigns[i]);
        }

        setCodeAddress(size() - 1);
        for (int i = 0; i < afterAssigns.length; i++) {
            afterAssigns[i].setParent(this);
            write(afterAssigns[i]);
        }
    }


    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if (this.labelName != null)
            printer.print(labelName + " ");
        if (this.type == LOOPTYPE_WHILE) {
            printer.print("while (");
            expression.print(printer);
            printer.print(") ");
            scope.print(printer);
        }
        else if (this.type == LOOPTYPE_DOWHILE) {
            printer.print("do ");
            scope.print(printer);
            printer.print(" while (");
            expression.print(printer);
            printer.print(");");
        }
        else if (this.type == LOOPTYPE_FOR) {
            if (preAssigns != null) {
                printer.print("for (");
                for (int i = 0; i < preAssigns.length; i++) {
                    if (i > 0)
                        printer.print(", ");
                    preAssigns[i].print(printer);
                }
                printer.print("; ");
                expression.print(printer);
                printer.print(";");
                for (int i = 0; i < afterAssigns.length; i++) {
                    printer.print(i > 0 ? ", " : " ");
                    afterAssigns[i].print(printer);
                }
                printer.print(") ");
                scope.print(printer);
            }
            else {
                printer.print("for (;");
                expression.print(printer);
                printer.print(";) ");
                scope.print(printer);
            }
        }
        else {
            throw new RuntimeException("Unknown loop type:" + this.type);
        }
        printer.endPrinting(this);
    }

    @Override
    public boolean canContinue() {
        return start != null;
    }

    @Override
    public boolean canBreak() {
        return end != null;
    }

    @Override
    public CS2FlowBlock getStart() {
        return start;
    }

    @Override
    public CS2FlowBlock getEnd() {
        return end;
    }

    @Override
    public void enableLabelName() {
        labelName = "loop_" + this.hashCode() + ":";
    }

    @Override
    public String getLabelName() {
        return labelName;
    }


}
