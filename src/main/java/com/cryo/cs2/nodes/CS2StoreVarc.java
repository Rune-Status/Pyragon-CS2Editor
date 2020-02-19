package com.cryo.cs2.nodes;

import com.cryo.utils.CodePrinter;

public class CS2StoreVarc extends CS2Node {

    private final int id;
    private final CS2Expression value;

    public CS2StoreVarc(int id, CS2Expression value) {
        this.id = id;
        this.value = value;
        this.write(value);
        this.value.setParent(this);
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("store_varc(");
        printer.print(Integer.toString(id));
        printer.print(", ");
        value.print(printer);
        printer.print(")");
        printer.endPrinting(this);
    }
}
