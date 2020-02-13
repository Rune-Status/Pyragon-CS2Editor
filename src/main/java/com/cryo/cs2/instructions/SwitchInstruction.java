package com.cryo.cs2.instructions;

import lombok.Data;

@Data
public class SwitchInstruction extends Instruction {

    private int[] cases;
    private LabelInstruction[] targets;
    private int defaultIndex;

    public SwitchInstruction(int opcode, String name, int[] cases, LabelInstruction[] targets) {
        super(opcode, name);
        this.cases = cases;
        this.targets = targets;
        this.defaultIndex = -1;
    }

    public void attachDefault(LabelInstruction default_) {
        LabelInstruction[] nTargets = new LabelInstruction[targets.length + 1];
        int[] nCases = new int[cases.length + 1];

        System.arraycopy(targets, 0, nTargets, 0, targets.length);
        System.arraycopy(cases, 0, nCases, 0, cases.length);

        targets = nTargets;
        cases = nCases;

        targets[targets.length - 1] = default_;
        cases[cases.length - 1] = Integer.MIN_VALUE;

        defaultIndex = targets.length - 1;
    }

    public void sort() {
        int[] sCases = new int[cases.length];
        LabelInstruction[] sTargets = new LabelInstruction[targets.length];
        boolean[] usage = new boolean[cases.length];
        boolean defaultAssigned = false;
        for (int sWrite = 0; sWrite < sCases.length; sWrite++) {
            int lowestAddr = Integer.MAX_VALUE;
            int lowestIndex = -1;
            for (int i = 0; i < cases.length; i++)
                if (!usage[i] && targets[i].getAddress() < lowestAddr)
                    lowestAddr = targets[lowestIndex = i].getAddress();
            if (!defaultAssigned && defaultIndex == lowestIndex) {
                defaultAssigned = true;
                defaultIndex = sWrite;
            }
            usage[lowestIndex] = true;
            sCases[sWrite] = cases[lowestIndex];
            sTargets[sWrite] = targets[lowestIndex];
        }
        cases = sCases;
        targets = sTargets;
    }
}
