package com.cryo.cs2.nodes;

import com.cryo.utils.CodePrinter;
import lombok.Data;

@Data
public class CS2Case extends CS2Node {

    private int caseNumber;
    private boolean isDefault;

    public CS2Case(int caseNumber) {
        this.caseNumber = caseNumber;
        this.isDefault = false;
    }

    public CS2Case() {
        this.caseNumber = 0;
        this.isDefault = true;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if (isDefault)
            printer.print("default:");
        else
            printer.print("case " + caseNumber + ":");
        printer.endPrinting(this);
    }

}
