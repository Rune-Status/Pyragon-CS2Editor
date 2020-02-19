package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Script;
import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2Function extends CS2Node {

    private int id;
    private String name;
    private CS2Type[] argumentTypes;
    private String[] argumentNames;
    private LocalVariable[] localArguments;
    private CS2Type returnType;
    private CS2Scope scope;
    private CS2Script script;

    public CS2Function(int id, String name, CS2Type[] args, String[] argNames, CS2Type returnType, CS2Script script) {
        this.id = id;
        this.name = name;
        this.argumentTypes = args;
        this.argumentNames = argNames;
        this.returnType = returnType;
        this.localArguments = new LocalVariable[args.length];
        this.scope = new CS2Scope();
        this.script = script;
        this.write(scope);
        scope.setParent(this);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        int precount = 0;
        for (int i = 0; i < size(); i++) {
            if (read(i) == scope)
                break;
            read(i).print(printer);
            printer.print('\n');
            precount++;
        }

        if (precount > 0)
            printer.print("\n\n");

        printer.print(this.returnType.toString());
        printer.print(' ');
        printer.print(this.name);
        printer.print('(');
        for (int i = 0; i < localArguments.length; i++) {
            printer.print(localArguments[i].toString());
            if ((i + 1) < argumentTypes.length)
                printer.print(',');
        }
        printer.print(')');
        printer.print(' ');
        this.scope.print(printer);
        printer.endPrinting(this);
    }

}
