package com.cryo.cs2.nodes;

import com.cryo.utils.CodePrinter;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CS2Comment extends CS2Node {

    public static final int STANDART_STYLE = 0;
    public static final int LOGO_STYLE = 1;

    private final String comment;
    private final int style;

    public int numLines() {
        int total = 0;
        for (int i = 0; i < comment.length(); i++)
            if (comment.charAt(i) == '\n')
                total++;
        return total;
    }

    @Override
    public void print(CodePrinter printer) {
        printer.beginPrinting(this);
        if (numLines() > 0) {
            if (style == LOGO_STYLE) {
                printer.print("/* \n * ");
                printer.print(comment.replace("\n", "\n * "));
                printer.print("\n */");
            }
            else {
                printer.tab();
                printer.print("/* \n");
                printer.print(comment);
                printer.untab();
                printer.print("\n */");
            }
        }
        else {
            printer.print("// " + comment);
        }
        printer.endPrinting(this);
    }
}
