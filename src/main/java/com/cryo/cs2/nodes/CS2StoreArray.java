package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2StoreArray extends CS2Expression {

    private CS2Expression array;
    private CS2Expression index;
    private CS2Expression value;

    public CS2StoreArray(CS2Expression array, CS2Expression index, CS2Expression value) {
        this.array = array;
        this.index = index;
        this.value = value;
        this.write(array);
        this.write(index);
        this.write(value);
        array.setParent(this);
        index.setParent(this);
        value.setParent(this);
    }

    public int getPriority() { return CS2Expression.PRIORITY_ASSIGNMENT; }

    public CS2Type getType() { return value.getType(); }

    public CS2Expression copy() {
        return new CS2StoreArray(array.copy(), index.copy(), value.copy());
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        boolean needsParen = array.getPriority() > CS2Expression.PRIORITY_ARRAY_INDEX;
        if (needsParen)
            printer.print('(');
        array.print(printer);
        if (needsParen)
            printer.print(')');
        printer.print('[');
        index.print(printer);
        printer.print(']');
        printer.print(" = ");
        value.print(printer);
        printer.endPrinting(this);
    }


}
