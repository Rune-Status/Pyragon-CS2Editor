package com.cryo.cs2.nodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cryo.cs2.CS2Instruction;
import com.cryo.cs2.CS2Script;
import com.cryo.cs2.CS2Type;
import com.cryo.utils.DecompilerException;
import com.cryo.utils.CodePrinter;
import com.cryo.utils.ScriptDAO;
import com.cryo.utils.ScriptDBBuilder;

public class CS2AnonymousClassExpression extends CS2Node {

    private final int scriptId;
    private final Object[] expressions;
    private final CS2Instruction operation;

    public CS2AnonymousClassExpression(Object[] expressions, CS2Instruction operation) {
        this.expressions = expressions;
        this.operation = operation;
        CS2Expression[] params = (CS2Expression[]) expressions[0];
        CS2Expression scriptId = (CS2Expression) params[0];
        if(!(scriptId instanceof CS2PrimitiveExpression)) throw new DecompilerException("Script ID is not an int!");
        this.scriptId = ((CS2PrimitiveExpression) scriptId).asInt();
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
        ScriptDAO dao = ScriptDBBuilder.getScript(scriptId);
        printer.beginPrinting(this);
        printer.print(operation.name().toLowerCase());
        printer.print("(");
        CS2Expression component = (CS2Expression) expressions[3];
        if(component != null) {
            CS2Cast cast = (CS2Cast) component;
            printComponent(cast, printer);
            printer.print(", ");
        }
        if(dao == null)
            printer.print("None, ");
        else {
            Pattern pattern = Pattern.compile("script_?\\d{1,4}");
            Matcher matcher = pattern.matcher(dao.getName());
            if(matcher.matches()) {
                printer.print(Integer.toString(dao.getId()));
                printer.print(", ");
            } else {
                printer.print("\"");
                printer.print(dao.getName());
                printer.print("\", ");
            }
        }
        // printer.print("new Function<");
        // if(dao == null) printer.print("None");
        // else printer.print(dao.getName());
        // printer.print(">(");
        // if(dao != null) {
        //     for(int i = 0; i < dao.getArgumentTypes().length; i++) {
        //         printer.print(dao.getArgumentTypes()[i].toString()+" ");
        //         printer.print(dao.getArgumentNames()[i]);
        //         if(i != dao.getArgumentTypes().length-1)
        //             printer.print(", ");
        //     }
        // }
        // printer.print(") {");
        // printer.tab();
        // if(dao != null) {
        //     //script info time
        //     CS2Script script = CS2Definitions.getScript(scriptId);
        //     if(script == null) throw new DecompilerException("Unable to load script: "+scriptId);
        //     script.decompile().getScope().printInner(printer);
        // }
        // printer.untab();
        // printer.newLine();
        // printer.print("}, ");
        ((CS2Expression) expressions[2]).print(printer);
        CS2Expression[] intArr = (CS2Expression[]) expressions[1];
        if(intArr != null && intArr.length > 0) {
            printer.print(", ");
            printer.print(Integer.toString(intArr.length));
            printer.print(", ");
            for(int i = 0; i < intArr.length; i++) {
                intArr[i].print(printer);
                if(i != intArr.length-1)
                    printer.print(", ");
            }
            printer.print(", ");
        }
        CS2Expression[] params = (CS2Expression[]) expressions[0];
        if(params != null && params.length > 2) {
            printer.print(", ");
            for(int i = 1; i < params.length; i++) {
                if(dao != null && dao.getArgumentTypes() != null && dao.getArgumentTypes()[i-1] == CS2Type.COMPONENT)
                    printComponent(params[i], printer);
                else
                    params[i].print(printer);
                if(i != params.length-1)
                    printer.print(", ");
            }
        }
        printer.print(");");
        printer.endPrinting(this);
    }

    public void printComponent(CS2Expression expression, CodePrinter printer) {
        if(expression instanceof CS2Cast) {
            CS2Cast cast = (CS2Cast) expression;
            if (cast.getExpression() instanceof CS2PrimitiveExpression) {
                int value = ((CS2PrimitiveExpression) cast.getExpression()).asInt();
                int[] info = CS2Script.getInterfaceIds(value);
                printer.print("if_gethash(");
                printer.print(Integer.toString(info[0]));
                printer.print(", ");
                printer.print(Integer.toString(info[1]));
                printer.print(")");
                return;
            }
        }
        expression.print(printer);
    }

    // @Override
    // public void print(CodePrinter printer) {
    //     printer.beginPrinting(this);
    //     printer.print(operation.name().toLowerCase());
    //     printer.print("(");
    //     for(int i = 0; i < expressions.length; i++) {
    //         if(expressions[i] == null) {
    //             printer.print("null");
    //             if (i != expressions.length - 1)
    //                 printer.print(", ");
    //         } else if(expressions[i] instanceof CS2PrimitiveExpression && ((CS2PrimitiveExpression) expressions[i]).isInt() && (operation == CS2Instruction.instr6342 || operation == CS2Instruction.instr6257)) {
    //             int hash = ((CS2PrimitiveExpression) expressions[i]).asInt();
    //             int[] interfaceIds = CS2Script.getInterfaceIds(hash);
    //             printer.print("if_gethash(");
    //             printer.print(Integer.toString(interfaceIds[0]));
    //             printer.print(", ");
    //             printer.print(Integer.toString(interfaceIds[1]));
    //             printer.print(")");
    //         } else if (expressions[i] instanceof CS2Expression) {
    //             ((CS2Expression) expressions[i]).print(printer);
    //             if (i != expressions.length - 1)
    //                 printer.print(", ");
    //         } else if (expressions[i] instanceof CS2Expression[]) {
    //             CS2Expression[] expressions2 = (CS2Expression[]) expressions[i];
    //             for (int k = 0; k < expressions2.length; k++) {
    //                 if(expressions2[k] == null)
    //                     printer.print("null");
    //                 else
    //                     ((CS2Expression) expressions2[k]).print(printer);
    //                 if(i != expressions.length-1 || k != expressions2.length-1)
    //                     printer.print(", ");
    //             }
    //         }
    //     }
    //     printer.print(")");
    //     printer.endPrinting(this);
    // }

}