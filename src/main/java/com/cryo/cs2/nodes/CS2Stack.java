package com.cryo.cs2.nodes;

public class CS2Stack {

    private CS2Expression[][] stack;
    private int[][] pushOrders;
    private int[] size;
    private int timesPushed;
    public static final int STACK_TYPES_COUNT = 3;
    public static final int BUFFER_SIZE = 500;

    public CS2Stack() {
        stack = new CS2Expression[STACK_TYPES_COUNT][BUFFER_SIZE];
        pushOrders = new int[STACK_TYPES_COUNT][BUFFER_SIZE];
        size = new int[STACK_TYPES_COUNT];
    }

    public CS2Expression pop() {
        if (getSize() <= 0)
            throw new RuntimeException("Stack underflow");
        int order = -1;
        int stackType = -1;
        for (int a = 0; a < STACK_TYPES_COUNT; a++) {
            if (size[a] > 0 && pushOrders[a][size[a] - 1] > order)
                stackType = a;
        }
        return pop(stackType);
    }

    public CS2Expression peek() {
        if (getSize() <= 0)
            throw new RuntimeException("Stack underflow");
        int order = -1;
        int stackType = -1;
        for (int a = 0; a < STACK_TYPES_COUNT; a++) {
            if (size[a] > 0 && pushOrders[a][size[a] - 1] > order)
                stackType = a;
        }
        return peek(stackType);
    }

    public CS2Expression pop(int stackType) {
        if (size[stackType] <= 0)
            throw new RuntimeException("Stack underflow");
        return stack[stackType][--size[stackType]];
    }

    public CS2Expression peek(int stackType) {
        if (size[stackType] <= 0)
            throw new RuntimeException("Stack underflow");
        return stack[stackType][size[stackType] - 1];
    }

    public void push(CS2Expression expr,int stackType) {
        if ((size[stackType] + 1) >= BUFFER_SIZE)
            throw new RuntimeException("Stack overflow");
        pushOrders[stackType][size[stackType]] = timesPushed++;
        stack[stackType][size[stackType]++] = expr;
    }

    public CS2Stack copy() {
        CS2Stack stack = new CS2Stack();
        for (int a = 0; a < STACK_TYPES_COUNT; a++) {
            stack.size[a] = this.size[a];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                if (this.stack[a][i] != null)
                    stack.stack[a][i] = this.stack[a][i].copy();
                stack.pushOrders[a][i] = this.pushOrders[a][i];
            }
        }
        return stack;
    }


    public int getSize() {
        int total = 0;
        for (int i = 0; i < STACK_TYPES_COUNT; i++)
            total += size[i];
        return total;
    }

    public int getSize(int stackType) {
        return size[stackType];
    }

    public void clear() {
        for (int i = 0; i < STACK_TYPES_COUNT; i++)
            size[i] = 0;
    }



}
