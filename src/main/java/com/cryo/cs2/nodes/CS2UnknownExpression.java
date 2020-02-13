package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Instruction;
import com.cryo.cs2.CS2Script;
import com.cryo.utils.CodePrinter;

public class CS2UnknownExpression extends CS2Node {

    private final Object[] expressions;
    private final CS2Instruction operation;

    public CS2UnknownExpression(Object[] expressions, CS2Instruction operation) {
        this.expressions = expressions;
        this.operation = operation;
        for(int i = 0; i < expressions.length; i++) {
            if(expressions[i] == null) {
                System.out.println("hi "+i);
                continue;
            }
            if(expressions[i] instanceof CS2Expression) {
                this.write((CS2Expression) expressions[i]);
                ((CS2Expression) expressions[i]).setParent(this);
            } else if(expressions[i] instanceof CS2Expression[]) {
                CS2Expression[] expressions2 = (CS2Expression[]) expressions[i];
                for(int k = 0; k < expressions2.length; k++) {
                    if (expressions2[k] == null)
                        continue;
                    this.write(expressions2[k]);
                    expressions2[k].setParent(this);
                }
            }
        }
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print(operation.name().toLowerCase());
        printer.print("(");
        for(int i = 0; i < expressions.length; i++) {
            if(expressions[i] == null) {
                printer.print("null");
                if (i != expressions.length - 1)
                    printer.print(", ");
            } else if(expressions[i] instanceof CS2PrimitiveExpression && ((CS2PrimitiveExpression) expressions[i]).isInt() && (operation == CS2Instruction.instr6342 || operation == CS2Instruction.instr6257)) {
                int hash = ((CS2PrimitiveExpression) expressions[i]).asInt();
                int[] interfaceIds = CS2Script.getInterfaceIds(hash);
                printer.print("if_gethash(");
                printer.print(Integer.toString(interfaceIds[0]));
                printer.print(", ");
                printer.print(Integer.toString(interfaceIds[1]));
                printer.print(")");
            } else if (expressions[i] instanceof CS2Expression) {
                ((CS2Expression) expressions[i]).print(printer);
                if (i != expressions.length - 1)
                    printer.print(", ");
            } else if (expressions[i] instanceof CS2Expression[]) {
                CS2Expression[] expressions2 = (CS2Expression[]) expressions[i];
                for (int k = 0; k < expressions2.length; k++) {
                    if(expressions2[k] == null)
                        printer.print("null");
                    else
                        ((CS2Expression) expressions2[k]).print(printer);
                    if(i != expressions.length-1 || k != expressions2.length-1)
                        printer.print(", ");
                }
            }
        }
        printer.print(")");
        printer.endPrinting(this);
    }

}