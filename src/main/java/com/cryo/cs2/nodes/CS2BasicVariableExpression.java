package com.cryo.cs2.nodes;

import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CodePrinter;

public class CS2BasicVariableExpression extends CS2Expression {

    private String name;

    @Override
    public CS2Type getType() {
        return CS2Type.INT;
    }

    @Override
    public CS2Expression copy() {
        return null;
    }

    @Override
    public void print(CodePrinter printer) {
        // TODO Auto-generated method stub

    }
    
}