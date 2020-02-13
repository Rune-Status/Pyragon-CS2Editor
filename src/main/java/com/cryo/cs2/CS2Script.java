package com.cryo.cs2;

import com.cryo.CS2Editor;
import com.cryo.cache.IndexType;
import com.cryo.cache.Store;
import com.cryo.cs2.flow.CS2FlowGenerator;
import com.cryo.cs2.flow.FlowBlocksSolver;
import com.cryo.cs2.flow.LocalVariablesAnalyzerT1;
import com.cryo.cs2.flow.LocalVariablesAnalyzerT2;
import com.cryo.cs2.instructions.*;
import com.cryo.cache.io.InputStream;
import com.cryo.cache.io.OutputStream;
import com.cryo.cs2.nodes.CS2Comment;
import com.cryo.cs2.nodes.CS2Function;
import com.cryo.cs2.nodes.LocalVariable;
import com.cryo.decompiler.util.FunctionInfo;
import com.cryo.decompiler.CS2Type;
import com.cryo.utils.Beautifier;
import com.cryo.utils.Utilities;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

import static com.cryo.cs2.CS2Instruction.*;

@Data
public class CS2Script {
    public String[] stringOpValues;
    public String name;
    public CS2Instruction[] operations;
    public Instruction[] instructions;
    public CS2Type[] arguments;
    public String[] argumentNames;
    public int[] operationOpcodes;
    public int[] intOpValues;
    public long[] longOpValues;
    public int longArgsCount;
    public int intLocalsCount;
    public int stringLocalsCount;
    public int intArgsCount;
    public int stringArgsCount;
    public int longLocalsCount;
    public HashMap<Integer, Integer>[] switchMaps;
    public int id;
    public CS2Type returnType;

    private static ArrayList<Integer> decompiling = new ArrayList<>();

    public CS2Script(InputStream buffer) {
        int instructionLength = decodeHeader(buffer);
        int opCount = 0;
        while (buffer.getOffset() < instructionLength) {
            CS2Instruction op = getOpcode(buffer);
            decodeInstruction(buffer, opCount, op);
            opCount++;
        }
        postDecode();
        loadInstructions();
    }

    CS2Instruction getOpcode(InputStream buffer) {
        int opcode = buffer.readUnsignedShort();
        if (opcode < 0 || opcode >= CS2Instruction.values().length) {
            throw new RuntimeException("Invalid operation code: " + opcode);
        }
        CS2Instruction op = CS2Instruction.getByOpcode(opcode);
        return op;
    }

    public Object getParam(int i) {
        CS2Instruction op = operations[i];
        if (op == CS2Instruction.PUSH_LONG) {
            return longOpValues[i];
        }
        if (op == CS2Instruction.PUSH_STRING) {
            return "\"" + stringOpValues[i] + "\"";
        }
        if (op.hasIntConstant()) {
            return intOpValues[i];
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private int decodeHeader(InputStream buffer) {
        buffer.setOffset(buffer.getLength() - 2);
        int switchBlockSize = buffer.readUnsignedShort();
        int instructionLength = buffer.getBuffer().length - 2 - switchBlockSize - 16;
        buffer.setOffset(instructionLength);
        int codeSize = buffer.readInt();
        intLocalsCount = buffer.readUnsignedShort();
        stringLocalsCount = buffer.readUnsignedShort();
        longLocalsCount = buffer.readUnsignedShort();
        intArgsCount = buffer.readUnsignedShort();
        stringArgsCount = buffer.readUnsignedShort();
        longArgsCount = buffer.readUnsignedShort();
        int switchesCount = buffer.readUnsignedByte();
        if (switchesCount > 0) {
            switchMaps = new HashMap[switchesCount];
            for (int i = 0; i < switchesCount; i++) {
                int numCases = buffer.readUnsignedShort();
                switchMaps[i] = new HashMap<Integer, Integer>(numCases);
                while (numCases-- > 0) {
                    switchMaps[i].put(buffer.readInt(), buffer.readInt());
                }
            }
        }
        buffer.setOffset(0);
        name = buffer.readNullString();
        instructions = new Instruction[codeSize * 2];
        operations = new CS2Instruction[codeSize];
        operationOpcodes = new int[codeSize];
        System.out.println(codeSize+" "+operations.length);
        return instructionLength;
    }

    private void decodeInstruction(InputStream buffer, int opIndex, CS2Instruction operation) {
        int opLength = operations.length;
        if (operation == CS2Instruction.PUSH_STRING) {
            if (stringOpValues == null)
                stringOpValues = new String[opLength];
            stringOpValues[opIndex] = buffer.readString();
        } else if (CS2Instruction.PUSH_LONG == operation) {
            if (null == longOpValues)
                longOpValues = new long[opLength];
            longOpValues[opIndex] = buffer.readLong();
        } else {
            if (null == intOpValues)
                intOpValues = new int[opLength];
            if (operation.hasIntConstant())
                intOpValues[opIndex] = buffer.readInt();
            else
                intOpValues[opIndex] = buffer.readUnsignedByte();
        }
        operations[opIndex] = operation;
        operationOpcodes[opIndex] = operation.getOpcode();
    }

    public void prepareInstructions() {
        int nonLabelsCount = instructions.length / 2;
        for (int i = 0; i < nonLabelsCount; i++) {
            if (instructions[i * 2 + 1] instanceof SwitchInstruction) {
                SwitchInstruction instruction = (SwitchInstruction) instructions[i * 2 + 1];
                if (((i + 1) * 2 + 1) >= instructions.length) {
                    Instruction[] buf = new Instruction[(nonLabelsCount + 1) * 2];
                    System.arraycopy(instructions, 0, buf, 0, instructions.length);
                    this.instructions = buf;
                }
                if (instructions[(i + 1) * 2] == null)
                    instructions[(i + 1) * 2] = new LabelInstruction();
                instruction.attachDefault((LabelInstruction) instructions[(i + 1) * 2]);
            }
        }
        List<Instruction> buffer = new ArrayList<>();
        for (int i = 0; i < instructions.length; i++)
            if (instructions[i] != null)
                buffer.add(instructions[i]);

        instructions = new Instruction[buffer.size()];
        int write = 0;
        for(Instruction instruction : buffer)
            instructions[write++] = instruction;
        for(int i = 0; i < instructions.length; i++)
            instructions[i].setAddress(i);
        for(int i = 0, found = 0; i < instructions.length; i++)
            if(instructions[i] instanceof LabelInstruction)
                ((LabelInstruction) instructions[i]).setLabelId(found++);
            for(int i = 0; i < instructions.length; i++)
                if(instructions[i] instanceof SwitchInstruction)
                    ((SwitchInstruction) instructions[i]).sort();
    }

    public void postDecode() {
        arguments = new CS2Type[intArgsCount + stringArgsCount + longArgsCount];
        int write = 0;
        for (int i = 0; i < intArgsCount; i++)
            arguments[write++] = CS2Type.INT;
        for (int i = 0; i < stringArgsCount; i++)
            arguments[write++] = CS2Type.STRING;
        for (int i = 0; i < longArgsCount; i++)
            arguments[write++] = CS2Type.LONG;
    }

    public void loadInstructions() {
        for (int i = 0; i < operations.length; i++) {
            CS2Instruction instruction = operations[i];
            if (instruction == CS2Instruction.PUSH_STRING || instruction == CS2Instruction.PUSH_LONG) {
                Object value = instruction == CS2Instruction.PUSH_STRING ? stringOpValues[i] : longOpValues[i];
                CS2Type type = instruction == CS2Instruction.PUSH_STRING ? CS2Type.STRING : CS2Type.LONG;
                instructions[(i * 2) + 1] = new PrimitiveInstruction(instruction.opcode, instruction.name(), value, type);
            } else if (instruction == CS2Instruction.SWITCH) {
                Map block = switchMaps[intOpValues[i]];
                int[] cases = new int[block.size()];
                LabelInstruction[] targets = new LabelInstruction[block.size()];
                int w = 0;
                for (Object key : block.keySet()) {
                    cases[w] = (Integer) key;
                    Object addr = block.get(key);
                    int full = i + ((Integer) addr).intValue() + 1;
                    if (instructions[full * 2] == null)
                        instructions[full * 2] = new LabelInstruction();
                    targets[w++] = (LabelInstruction) instructions[full * 2];
                }
                instructions[(i * 2) + 1] = new SwitchInstruction(instruction.getOpcode(), instruction.name(), cases, targets);
            } else if (isJump(instruction)) {
                int full = i + intOpValues[i] + 1;
                if (instructions[full * 2] == null)
                    instructions[full * 2] = new LabelInstruction();
                instructions[(i * 2) + 1] = new JumpInstruction(instruction.opcode, instruction.name(), (LabelInstruction) instructions[full * 2]);
            } else if(isBasicInstruction(instruction) || instruction.hasIntConstant())
                instructions[(i * 2) + 1] = new PrimitiveInstruction(instruction.opcode, instruction.name(), intOpValues[i], CS2Type.INT);
            else
                instructions[(i*2)+1] = new PrimitiveInstruction(instruction.getOpcode(), instruction.name(), intOpValues[i] == 1, CS2Type.BOOLEAN);
        }
        prepareInstructions();
    }

    public static boolean isBasicInstruction(CS2Instruction instruction) {
        int i = 0;
        while(i < BASIC_INSTRUCTIONS.length) {
            CS2Instruction instr = (CS2Instruction) BASIC_INSTRUCTIONS[i++];
            if(instr.opcode == instruction.opcode) return true;
            i += 2;
        }
        return false;
    }

    public static int getArgumentSize(CS2Instruction instruction) {
        int i = 0;
        while(i < BASIC_INSTRUCTIONS.length) {
            CS2Instruction instr = (CS2Instruction) BASIC_INSTRUCTIONS[i++];
            int argumentSize = (int) BASIC_INSTRUCTIONS[i++];
            if(instr.opcode == instruction.opcode) return argumentSize;
            i++;
        }
        return 0;
    }

    public static int getStackType(CS2Instruction instruction) {
        if(!isBasicInstruction(instruction)) return -1;
        int i = 0;
        while(i < BASIC_INSTRUCTIONS.length) {
            CS2Instruction instr = (CS2Instruction) BASIC_INSTRUCTIONS[i++];
            i++;
            int stackType = (int) BASIC_INSTRUCTIONS[i++];
            if(instr.opcode == instruction.opcode) return stackType;
        }
        return -1;
    }

    //cs2instruction, numberOfArguments, stackType(0=int, 1=string, 2=long)
    public static Object[] BASIC_INSTRUCTIONS = {
            ADD, -1, -1, SUBTRACT, -1, -1, DIVIDE, -1, -1, MULTIPLY, -1, -1, MODULO, -1, -1,
            INV_GETITEM, 2, 0, IF_GETWIDTH, -1, -1, INV_SIZE, 1, 0, CC_CREATE, 3, 0,
            CC_SETSIZE, 4, 0, CC_SETPOSITION, 4, 0, IF_SETPOSITION, 5, 0,
            INV_GETNUM, 2, 0, CC_SETITEM, 2, 0, CC_SETGRAPHICSHADOW, 1, 0,
            ITEM_NAME, -1, -1, CC_SETOPBASE, 1, 1, CC_SETOUTLINE, 1, 0, CC_SETOP, -1, -1,
            IF_SETHIDE, -1, -1, TO_STRING, -1, -1, IF_SETTEXT, -1, -1, SOUND_VORBIS_VOLUME, 4, 0,
            ENUM, -1, -1, RANDOM, -1, -1, instr6342, -1, -1, STRUCT_PARAM, -1, -1, IF_SETGRAPHIC, 2, 0,
            ITEM_PARAM, -1, -1, instr6135, -1, -1, INV_TOTAL, -1, -1, QUEST_STATREQ_LEVEL, -1, -1,
            instr6185, 3, 0, instr6236, 0, 0, instr6150, -1, -1, GET_PLAYER_POS, -1, -1, instr6452, -1, -1,
            instr6257, -1, -1, instr6237, -1, -1, CC_FIND, -1, -1, CC_SETTRANS, 1, 0, HOOK_MOUSE_PRESS, -1, -1,
            HOOK_MOUSE_RELEASE, -1, -1, CC_DELETEALL, 1, 0, IF_GETNEXTSUBID, -1, -1, CC_SETGRAPHIC, 1, 0, 
            CC_SETHFLIP, 1, 0, instr6212, -1, -1, GET_PLAYER_X, -1, -1, GET_PLAYER_Y, -1, -1, GET_PLAYER_PLANE, -1, -1,
            IF_GETHEIGHT, -1, -1, MOVE_COORD, -1, -1, SCALE, 3, 0, MIN, 2, 0, MAX, 2, 0, STRING_LENGTH, -1, -1,
            CC_DELETE, 0, 1, IF_SETSIZE, 5, 0, instr6519, -1, -1, instr6801, -1, -1, instr6152, -1, -1,
            INVOTHER_GETITEM, -1, -1, INVOTHER_GETNUM, -1, -1
    };

    public CS2Function decompile() {
        if(decompiling.contains(id))
            throw new RuntimeException("Stuck in decompiling loop.");
        decompiling.add(id);
        FunctionInfo info = CS2Editor.getInstance().getScriptsDB().getInfo(id);
        if(info != null) {
            returnType = info.getReturnType();
            CS2Type[] arguments = new CS2Type[info.getArgumentTypes().length];
            String[] argNames = new String[info.getArgumentNames().length];
            System.arraycopy(info.getArgumentTypes(), 0, arguments, 0, arguments.length);
            System.arraycopy(info.getArgumentNames(), 0, argNames, 0, argNames.length);
            this.arguments = arguments;
            this.argumentNames = argNames;
        }
        if(argumentNames == null) {
            argumentNames = new String[arguments.length];
            for(int i = 0; i < argumentNames.length; i++)
                argumentNames[i] = "arg"+i;
        }

        CS2Function function = new CS2Function(id, name, arguments, argumentNames, returnType);

        //add script id as comment
        function.setCodeAddress(0);
        function.write(new CS2Comment(Integer.toString(id), CS2Comment.LOGO_STYLE));

        declareAllVariables(function);

        CS2FlowGenerator generator = new CS2FlowGenerator(this, function);
        generator.generate();

    //    LocalVariablesAnalyzerT1 a1 = new LocalVariablesAnalyzerT1(function, generator.getBlocks());
    //    a1.analyze();

    //    LocalVariablesAnalyzerT2 a2 = new LocalVariablesAnalyzerT2(function, generator.getBlocks());
    //    a2.analyze();
               
        FlowBlocksSolver solver = new FlowBlocksSolver(function.getScope(), generator.getBlocks());
        solver.solve();

        // Beautifier beautifier = new Beautifier(function);
        // beautifier.beautify();
 
        decompiling.remove(Integer.valueOf(id));
        return function;
    }


    private void declareAllVariables(CS2Function function) {

        int ic = 0, oc = 0, lc = 0;
        for (int i = 0; i < function.getArgumentTypes().length; i++) {
            CS2Type atype = function.getArgumentTypes()[i];
            String aname = function.getArgumentNames()[i];
            if (atype.intSS() == 1 && atype.stringSS() == 0 && atype.longSS() == 0) {
                LocalVariable var = new LocalVariable(aname, atype, true);
                var.setIdentifier(LocalVariable.makeIdentifier(ic++, 0));
                var.setScopeDeclarationNeeded(false);
                function.getScope().declare(function.getLocalArguments()[i] = var);
            }
            else if (atype.intSS() == 0 && atype.stringSS() == 1 && atype.longSS() == 0) {
                LocalVariable var = new LocalVariable(aname, atype, true);
                var.setIdentifier(LocalVariable.makeIdentifier(oc++, 1));
                var.setScopeDeclarationNeeded(false);
                function.getScope().declare(function.getLocalArguments()[i] = var);
            }
            else if (atype.intSS() == 0 && atype.stringSS() == 0 && atype.longSS() == 1) {
                LocalVariable var = new LocalVariable(aname, atype, true);
                var.setIdentifier(LocalVariable.makeIdentifier(lc++, 2));
                var.setScopeDeclarationNeeded(false);
                function.getScope().declare(function.getLocalArguments()[i] = var);
            }
            else {
                throw new RuntimeException("structs in args?");
            }
        }
        if (ic != getIntArgsCount() || oc != getStringArgsCount() || lc != getLongArgsCount())
            throw new RuntimeException("badargs");

        for (int i = getIntArgsCount(); i < getIntLocalsCount(); i++) {
            LocalVariable var = new LocalVariable("ivar" + i,CS2Type.INT);
            var.setIdentifier(LocalVariable.makeIdentifier(i, 0));
            function.getScope().declare(var);
        }
        for (int i = getStringArgsCount(); i < getStringLocalsCount(); i++) {
            LocalVariable var = new LocalVariable("svar" + i,CS2Type.STRING);
            var.setIdentifier(LocalVariable.makeIdentifier(i, 1));
            function.getScope().declare(var);
        }
        for (int i = getLongArgsCount(); i < getLongLocalsCount(); i++) {
            LocalVariable var = new LocalVariable("lvar" + i,CS2Type.LONG);
            var.setIdentifier(LocalVariable.makeIdentifier(i, 2));
            function.getScope().declare(var);
        }
    }

    public static CS2Instruction[] JUMP_INSTRUCTIONS = {
            GOTO, INT_EQ, INT_NE, INT_LT, INT_GT,
            INT_LE, INT_GE, BRANCH_EQ0, BRANCH_EQ1, LONG_EQ,
            LONG_NE, LONG_LT, LONG_GT, LONG_LE, LONG_GE};

    public static boolean isJump(CS2Instruction instruction) {
        if(instruction == null) {
            System.out.println("Instruction is null?");
            return false;
        }
        Optional<CS2Instruction> optional = Stream.of(JUMP_INSTRUCTIONS)
                .filter(instr -> instr != null)
                .filter(instr -> instr.opcode == instruction.opcode)
                .findFirst();
        return optional.isPresent();
    }

    public static int[] getInterfaceIds(int hash) {
        int interfaceId = hash >> 16;
        int componentId = hash >>> 16;
        return new int[] { interfaceId, hash & 0xffff };
    }

    public void write(Store store) {
        store.getIndex(IndexType.CS2_SCRIPTS).putArchive(id, encode());
    }

    public byte[] encode() {
        OutputStream out = new OutputStream();

        if (name == null)
            out.writeByte(0);
        else
            out.writeString(name);

        for (int i = 0; i < operations.length; i++) {
            CS2Instruction op = operations[i];
            out.writeShort(op.getOpcode());
            if (op == CS2Instruction.PUSH_STRING) {
                out.writeString((String) stringOpValues[i]);
            } else if (CS2Instruction.PUSH_LONG == op) {
                out.writeLong(longOpValues[i]);
            } else {
                if (op.hasIntConstant()) {
                    out.writeInt(intOpValues[i]);
                } else {
                    out.writeByte(intOpValues[i]);
                }
            }
        }

        out.writeInt(operations.length);
        out.writeShort(intLocalsCount);
        out.writeShort(stringLocalsCount);
        out.writeShort(longLocalsCount);
        out.writeShort(intArgsCount);
        out.writeShort(stringArgsCount);
        out.writeShort(longArgsCount);

        OutputStream switchBlock = new OutputStream();
        if (switchMaps == null) {
            switchBlock.writeByte(0);
        } else {
            switchBlock.writeByte(switchMaps.length);
            if (switchMaps.length > 0) {
                for (int i = 0; i < switchMaps.length; i++) {
                    HashMap<Integer, Integer> map = switchMaps[i];
                    switchBlock.writeShort(map.size());
                    for (int key : map.keySet()) {
                        switchBlock.writeInt(key);
                        switchBlock.writeInt(map.get(key));
                    }
                }
            }
        }

        byte[] switchBytes = switchBlock.toByteArray();
        out.writeBytes(switchBytes);

        out.writeShort(switchBytes.length);
        return out.toByteArray();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CS2Script))
            return false;
        CS2Script script = (CS2Script) other;
        if (script.operationOpcodes != null) {
            if (this.operationOpcodes == null) {
                System.out.println("Mismatching operation opcodes.");
                return false;
            }
            if (!Arrays.equals(script.operationOpcodes, this.operationOpcodes)) {
                System.out.println("Mismatching operation opcodes");
                return false;
            }
        }
        if (script.intOpValues != null) {
            if (this.intOpValues == null) {
                System.out.println("int op values null shouldn't be");
                return false;
            }
            if (!Arrays.equals(script.intOpValues, this.intOpValues)) {
                System.out.println("Mismatching int op values");
                return false;
            }
        }
        if (script.longOpValues != null) {
            if (this.longOpValues == null) {
                System.out.println("long op values null shouldn't be");
                return false;
            }
            if (!Arrays.equals(script.longOpValues, this.longOpValues)) {
                System.out.println("Mismatching long op values");
                return false;
            }
        }
        if (script.stringOpValues != null) {
            if (this.stringOpValues == null) {
                System.out.println("String op values null shouldn't be");
                return false;
            }
            if (!Arrays.equals(script.stringOpValues, this.stringOpValues)) {
                System.out.println("Mismatching string op values");
                System.out.println(Arrays.toString(this.stringOpValues));
                System.out.println(Arrays.toString(script.stringOpValues));
                return false;
            }
        }
        if (script.switchMaps != null) {
            if (this.switchMaps == null) {
                System.out.println("Switchmap null shouldn't be");
                return false;
            }
            if (this.switchMaps.length != script.switchMaps.length) {
                System.out.println("Mismatching switch map lengths");
                return false;
            }
            for (int i = 0; i < this.switchMaps.length; i++) {
                HashMap<Integer, Integer> map1 = this.switchMaps[i];
                HashMap<Integer, Integer> map2 = script.switchMaps[i];
                for (int key : map1.keySet()) {
                    if (map2.get(key).intValue() != map1.get(key).intValue()) {
                        System.out.println("Mismatching map keys: " + map1.get(key) + " - " + map2.get(key));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String getName() {
        String name = this.name;
        if (name == null || name.equals("")) {
            if (CS2Editor.getLoaders().get("script-names").containsKey(id)) {
                name = CS2Editor.getLoaders().get("script-names").get(id);
                if (name != null && !name.equals(""))
                    return name;
            }
            return "script" + id;
        }
        return name;
    }

    public int countOf(Class<? extends Instruction> type) {
        int total = 0;
        for (int i = 0; i < instructions.length; i++)
            if (instructions[i].getClass() == type)
                total++;
        return total;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(this.getClass().getName());
        result.append(" {");
        result.append(newLine);

        // determine fields declared in this class only (no fields of
        // superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        // print field names paired with their values
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            result.append("  ");
            try {
                result.append(field.getType().getCanonicalName() + " " + field.getName() + ": ");
                result.append(Utilities.getFieldValue(this, field));
            } catch (Throwable ex) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }

    public String getOpString(int i) {
        String str = operations[i] + "(";
        if (intOpValues != null && (stringOpValues == null || stringOpValues[i] == null)) {
            str += intOpValues[i];
        }
        if (longOpValues != null) {
            str += longOpValues[i];
        }
        if (stringOpValues != null) {
            str += (stringOpValues[i] != null ? stringOpValues[i] : "");
        }
        return str + ")";
    }
}
