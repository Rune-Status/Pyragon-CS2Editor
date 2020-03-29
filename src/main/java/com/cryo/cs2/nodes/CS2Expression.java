package com.cryo.cs2.nodes;

import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;
import lombok.Getter;
import lombok.Setter;

public abstract class CS2Expression extends CS2Node {

    public static final int PRIORITY_STANDART = 0;
    public static final int PRIORITY_ARRAY_INDEX = 1;
    public static final int PRIORITY_CALL = 1;
    public static final int PRIORITY_MEMBER_ACCESS = 1;
    public static final int PRIORITY_UNARYPLUSMINUS = 2;
    public static final int PRIORITY_PLUSMINUSPREFIXPOSTFIX = 2;
    public static final int PRIORITY_UNARYLOGICALNOT = 2;
    public static final int PRIORITY_UNARYBITWISENOT = 2;
    public static final int PRIORITY_CAST = 2;
    public static final int PRIORITY_NEWOPERATOR = 2;
    public static final int PRIORITY_MULDIVREM = 3;
    public static final int PRIORITY_ADDSUB = 4;
    public static final int PRIORITY_CONTACTSTRING = 4;
    public static final int PRIORITY_BITSHIFTS = 5;
    public static final int PRIORITY_LELTGEGTINSTANCEOF = 6;
    public static final int PRIORITY_EQNE = 7;
    public static final int PRIORITY_BITAND = 8;
    public static final int PRIORITY_BITXOR = 9;
    public static final int PRIORITY_BITOR = 10;
    public static final int PRIORITY_LOGICALAND = 11;
    public static final int PRIORITY_LOGICALOR = 12;
    public static final int PRIORITY_TERNARY = 13;
    public static final int PRIORITY_ASSIGNMENT = 14;

    @Getter
    @Setter
    private CS2Node parent;

    public int getPriority() {
        return 0;
    }

    public abstract CS2Type getType();

    public abstract CS2Expression copy();

    public void printConstant(CodePrinter printer, Object constant, CS2Type type) {
        if (type.array() || type.structure())
            throw new RuntimeException();

        String fmt = null;
        if (type.getFormat() != null && (fmt = type.getFormat().apply(constant)) != null) {
            printer.print(fmt);
            return;
        }

        printer.print(type.getName());
        printer.print('(');
        printer.print(constant == null ? "null" : constant.toString());
        printer.print(')');
    }

}
