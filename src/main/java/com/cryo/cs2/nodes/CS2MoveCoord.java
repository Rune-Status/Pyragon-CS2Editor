package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;

public class CS2MoveCoord extends CS2Expression {

    private final CS2Expression startPos;
    private final CS2Expression x;
    private final CS2Expression y;
    private final CS2Expression plane;

    public CS2MoveCoord(CS2Expression startPos, CS2Expression x, CS2Expression y, CS2Expression plane) {
        this.startPos = startPos;
        this.x = x;
        this.y = y;
        this.plane = plane;
        this.write(startPos);
        this.write(x);
        this.write(y);
        this.write(plane);
        this.startPos.setParent(this);
        this.x.setParent(this);
        this.y.setParent(this);
        this.plane.setParent(this);
    }

    @Override
    public CS2Type getType() {
        return CS2Type.INT;
    }

    @Override
    public CS2Expression copy() {
        return new CS2MoveCoord(startPos.copy(), x.copy(), y.copy(), plane.copy());
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        printer.print("move_coord(");
        startPos.print(printer);
        printer.print(", ");
        x.print(printer);
        printer.print(", ");
        y.print(printer);
        printer.print(", ");
        plane.print(printer);
        printer.print(")");
        printer.endPrinting(this);
    }

}