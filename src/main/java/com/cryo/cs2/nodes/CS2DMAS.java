package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Instruction;
import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;

public class CS2DMAS extends CS2Expression {

    private CS2Expression[] expressions;
    private CS2Instruction instruction;

    public CS2DMAS(CS2Expression[] expressions, CS2Instruction instruction) {
        this.expressions = expressions;
        this.instruction = instruction;
        for(int i = 0; i < expressions.length; i++) {
            this.write(expressions[i]);
            expressions[i].setParent(this);
        }
    }

    public int getPriority() {
        return CS2Expression.PRIORITY_MULDIVREM;
    }

    public CS2Type getType() { return CS2Type.INT; }

    public CS2Expression copy() {
        CS2Expression[] copy = new CS2Expression[expressions.length];
        for(int i = 0; i < expressions.length; i++)
            copy[i] = expressions[i].copy();
        return new CS2DMAS(copy, instruction);
    }

    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("calc(");
        expressions[1].print(printer);
        printer.print(" "+getSign()+" ");
        expressions[0].print(printer);
        printer.print(")");
        printer.endPrinting(this);
    }

    private String getSign() {
        switch(instruction) {
            case ADD: return "+";
            case SUBTRACT: return "-";
            case DIVIDE: return "/";
            case MULTIPLY: return "*";
            case MODULO: return "%";
            default: return "?";
        }
    }

}
