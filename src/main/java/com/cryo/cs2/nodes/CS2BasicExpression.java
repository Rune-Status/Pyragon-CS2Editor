package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Script;
import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import com.cryo.utils.InstructionDAO;
import com.cryo.utils.InstructionDBBuilder;

public class CS2BasicExpression extends CS2Expression {

    private int opcode;
    private CS2Expression[] expressions;
    private String command;

    public CS2BasicExpression(CS2Expression expression, String command) {
        this(-1, new CS2Expression[] { expression }, command);
    }

    public CS2BasicExpression(CS2Expression[] expressions, String command) {
        this(-1, expressions, command);
    }

    public CS2BasicExpression(int opcode, CS2Expression[] expressions, String command) {
        this.opcode = opcode;
        this.expressions = expressions;
        this.command = command;
        for(int i = 0; i < expressions.length; i++) {
            this.write(expressions[i]);
            expressions[i].setParent(this);
        }
    }

    public int getPriority() { return CS2Expression.PRIORITY_CALL; }

    public void printExtra(String popType, CS2PrimitiveExpression expression, CodePrinter printer) {
        String extra = popType.substring(1);
        switch(extra) {
            case "c":
                int hash = expression.asInt();
                int[] interfaceIds = CS2Script.getInterfaceIds(hash);
                printer.print("if_gethash(");
                printer.print(Integer.toString(interfaceIds[0]));
                printer.print(", ");
                printer.print(Integer.toString(interfaceIds[1]));
                printer.print(")");
                break;
            case "co":
                expression.print(printer);
                // hash = expression.asInt();
                // int colour = hash & 0xffffff;
                // printer.print("colour_gethash(#");
                // printer.print(Integer.toHexString(colour));
                // printer.print(")");
                break;
            case "hsv":
                hash = expression.asInt();
                int[] colours = CS2Script.getColours(hash);
                printer.print("colour_gethashedhsv(");
                printer.print(Integer.toString(colours[0]));
                printer.print(", ");
                printer.print(Integer.toString(colours[1]));
                printer.print(", ");
                printer.print(Integer.toString(colours[2]));
                printer.print(")");
                break;
            case "b":
                boolean b = expression.asInt() == 1;
                printer.print(Boolean.toString(b));
                break;
        }
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        InstructionDAO dao = InstructionDBBuilder.getInstruction(opcode);
        if(dao != null && dao.getCustomPrint() != null) {
            String print = dao.getCustomPrint();
            for(int i = 0; i < expressions.length; i++) {
                CodePrinter printer2 = new CodePrinter();
                expressions[i].print(printer2);
                String s = printer2.toString();
                print = print.replace("%"+i, s);
            }
            printer.print(print);
            return;
        }
        printer.print(command);
        printer.print("(");
        if(opcode != -1) {
            if(dao.getPopOrder() != null) {
                for(int i = dao.getPopOrder().length-1; i >= 0; i--) {
                    String popType = dao.getPopOrder()[i];
                    CS2Expression expression = expressions[i];
                    boolean hasExtra = popType.length() > 1;
                    if(hasExtra && expression instanceof CS2PrimitiveExpression)
                        printExtra(popType, (CS2PrimitiveExpression) expression, printer);
                    else
                        expression.print(printer);
                    if (i != 0)
                        printer.print(", ");
                }
            }
        } else {
            for(int i = expressions.length-1; i >= 0; i--) {
                expressions[i].print(printer);
                if(i != 0)
                    printer.print(", ");
            }
        }
        printer.print(")");
        if(dao != null && dao.getPushType() == CS2Type.VOID)
            printer.print(";");
        printer.endPrinting(this);
    }

    @Override
    public CS2Type getType() {
        return CS2Type.INT;
    }

    @Override
    public CS2Expression copy() {
        CS2Expression[] copy = new CS2Expression[expressions.length];
        for(int i = 0; i < copy.length; i++)
            copy[i] = expressions[i].copy();
        return new CS2BasicExpression(copy, command);
    }
}
