package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CS2VariableLoad extends CS2Expression {

    private LocalVariable variable;

    public CS2Type getType() { return variable.getType(); }

    public CS2Expression copy() { return new CS2VariableLoad(variable); }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print(variable.getName());
        printer.endPrinting(this);
    }
}
