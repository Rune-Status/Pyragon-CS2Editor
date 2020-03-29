package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;

public class CS2LoadArray extends CS2Expression {

    private CS2Expression array;
    private CS2Expression index;

    public CS2LoadArray(CS2Expression array, CS2Expression index) {
        this.array = array;
        this.index = index;
        this.write(array);
        this.write(index);
        array.setParent(this);
        index.setParent(this);
    }

    public int getPriority() { return CS2Expression.PRIORITY_ARRAY_INDEX; }

    public CS2Type getType() { return array.getType().getElementType(); }

    public CS2Expression copy() {
        return new CS2LoadArray(array.copy(), index.copy());
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        boolean needsParen = array.getPriority() > this.getPriority();
        if (needsParen)
            printer.print('(');
        array.print(printer);
        if (needsParen)
            printer.print(')');
        printer.print('[');
        index.print(printer);
        printer.print(']');
        printer.endPrinting(this);
    }

}
