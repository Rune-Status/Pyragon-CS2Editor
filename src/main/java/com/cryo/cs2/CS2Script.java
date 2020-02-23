package com.cryo.cs2;

import com.cryo.CS2Editor;
import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.cache.Store;
import com.cryo.cs2.flow.CS2FlowGenerator;
import com.cryo.cs2.flow.FlowBlocksSolver;
import com.cryo.cs2.instructions.*;
import com.cryo.cache.io.InputStream;
import com.cryo.cache.io.OutputStream;
import com.cryo.cs2.nodes.CS2Comment;
import com.cryo.cs2.nodes.CS2Expression;
import com.cryo.cs2.nodes.CS2Function;
import com.cryo.cs2.nodes.LocalVariable;
import com.cryo.decompiler.util.FunctionInfo;
import com.cryo.decompiler.CS2Type;
import com.cryo.utils.CompilerException;
import com.cryo.utils.InstructionDBBuilder;
import com.cryo.utils.ScriptDAO;
import com.cryo.utils.ScriptDBBuilder;
import com.cryo.utils.Utilities;

import org.apache.commons.lang3.ArrayUtils;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private ScriptDAO DAO;

    private static ArrayList<Integer> decompiling = new ArrayList<>();

    public CS2Script(int id, String name, CS2Type[] arguments, String[] argNames, CS2Instruction[] operations, 
        int[] intOpValues, String[] stringOpValues, long[] longOpValues, int intLocalsCount, int stringLocalsCount, int longLocalsCount) {
            this.id = id;
            this.name = name;
            this.arguments = arguments;
            this.argumentNames = argNames;
            this.operations = operations;
            this.intOpValues = intOpValues;
            this.stringOpValues = stringOpValues;
            this.longOpValues = longOpValues;
            this.intLocalsCount = intLocalsCount;
            this.stringLocalsCount = stringLocalsCount;
            this.longLocalsCount = longLocalsCount;
            for(CS2Type argument : arguments) {
                if(argument == CS2Type.STRING) stringLocalsCount++;
                else if(argument == CS2Type.LONG) longLocalsCount++;
                else intLocalsCount++;
            }
        }

    public CS2Script(int id, InputStream buffer) {
        int instructionLength = decodeHeader(buffer);
        int opCount = 0;
        this.id = id;
        DAO = ScriptDBBuilder.getScript(id);
        while (buffer.getOffset() < instructionLength) {
            int opcode = buffer.readUnsignedShort();
            if (opcode < 0 || opcode >= CS2Instruction.values().length) {
                System.out.println("Invalid operation code: " + opcode);
                break;
            }
            CS2Instruction op = CS2Instruction.getByOpcode(opcode);
            if(op == null) {
                System.out.println("Null operation: "+opcode+" "+opCount);
                continue;
            }
            decodeInstruction(buffer, opCount, op);
            opCount++;
        }
        postDecode();
        loadInstructions();
    }

    CS2Instruction getInstruction(int opcode) {
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
        System.out.println("Decompiling. int locals: "+intLocalsCount);
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
        return instructionLength;
    }

    private void decodeInstruction(InputStream buffer, int opIndex, CS2Instruction operation) {
        int opLength = operations.length;
        System.out.print(operation);
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
            if (operation.hasIntConstant()) {
                intOpValues[opIndex] = buffer.readInt();
                System.out.print(" "+intOpValues[opIndex]);
            } else
                intOpValues[opIndex] = buffer.readUnsignedByte();
        }
        System.out.println();
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
            if(instruction == null) continue; //no idea why some scripts have differing codeSizes and instruction lengths
            if (instruction == CS2Instruction.PUSH_STRING || instruction == CS2Instruction.PUSH_LONG) {
                Object value = instruction == CS2Instruction.PUSH_STRING ? stringOpValues[i] : longOpValues[i];
                CS2Type type = instruction == CS2Instruction.PUSH_STRING ? CS2Type.STRING : CS2Type.LONG;
                instructions[(i * 2) + 1] = new PrimitiveInstruction(instruction.opcode, instruction.name(), value, type);
            } else if (instruction == CS2Instruction.SWITCH) {
                Map<Integer, Integer> block = switchMaps[intOpValues[i]];
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
                    System.out.println("Switch block target set.");
                }
                instructions[(i * 2) + 1] = new SwitchInstruction(instruction.getOpcode(), instruction.name(), cases, targets);
            } else if (isJump(instruction)) {
                int full = i + intOpValues[i] + 1;
                System.out.println("Decoding jump instruction "+instruction+". Full: "+full+" i: "+i+" value: "+intOpValues[i]);
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
            CC_DELETE, 0, 1, IF_SETSIZE, 5, 0, REMOVETAGS, -1, -1, PARAMWIDTH, -1, -1, PARAMHEIGHT, -1, -1,
            INVOTHER_GETITEM, -1, -1, INVOTHER_GETNUM, -1, -1, IF_SETONMOUSELEAVE, -1, -1, IF_SETONMOUSEOVER, -1, -1,
            HOOK_MOUSE_EXIT, -1, -1, instr6376, -1, -1, instr6527, -1, -1, instr6393, -1, -1, HOOK_MOUSE_ENTER, -1, -1,
            instr6239, -1, -1, instr6687, -1, -1, instr6091, -1, -1, instr6092, -1, -1, instr6088, -1, -1,
            instr6224, -1, -1, instr6499, -1, -1, instr5957, -1, -1, instr6246, -1, -1, instr6771, -1, -1,
            RETURN, -1, -1, instr6253, -1, -1
    };

    public CS2Function decompile() {
        if(decompiling.contains(id))
            throw new RuntimeException("Stuck in decompiling loop.");
        decompiling.add(id);

        ScriptDAO dao = ScriptDBBuilder.getScript(id);

        FunctionInfo info = CS2Editor.getInstance().getScriptsDB().getInfo(id);

        CS2Type returnType = info.getReturnType();

        if(dao != null) {
            argumentNames = dao.getArgumentNames();
            returnType = dao.getReturnType();
        }

        if(argumentNames == null) {
            argumentNames = new String[arguments.length];
            for(int i = 0; i < argumentNames.length; i++)
                argumentNames[i] = "arg"+i;
        }

        CS2Function function = new CS2Function(id, getName(), arguments, argumentNames, returnType, this);

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

        ScriptDAO dao = ScriptDBBuilder.getScript(id);
        int index = 0;
        for (int i = getIntArgsCount(); i < getIntLocalsCount(); i++) {
            String name = "ivar"+i;
            if(dao != null && dao.getVariableNames() != null && dao.getVariableNames().length > 0) {
                String rName = dao.getVariableNames()[index++];
                if(rName != null) name = rName;
            }
            LocalVariable var = new LocalVariable(name,CS2Type.INT);
            var.setIdentifier(LocalVariable.makeIdentifier(i, 0));
            function.getScope().declare(var);
        }
        for (int i = getStringArgsCount(); i < getStringLocalsCount(); i++) {
            String name = "svar" + i;
            if (dao != null && dao.getVariableNames() != null && dao.getVariableNames().length > 0) {
                String rName = dao.getVariableNames()[index++];
                if (rName != null)
                    name = rName;
            }
            LocalVariable var = new LocalVariable(name, CS2Type.STRING);
            var.setIdentifier(LocalVariable.makeIdentifier(i, 1));
            function.getScope().declare(var);
        }
        for (int i = getLongArgsCount(); i < getLongLocalsCount(); i++) {
            String name = "lvar" + i;
            if (dao != null && dao.getVariableNames() != null && dao.getVariableNames().length > 0) {
                String rName = dao.getVariableNames()[index++];
                if (rName != null)
                    name = rName;
            }
            LocalVariable var = new LocalVariable(name,CS2Type.LONG);
            var.setIdentifier(LocalVariable.makeIdentifier(i, 2));
            function.getScope().declare(var);
        }
    }

    public static CS2Instruction[] JUMP_INSTRUCTIONS = {
            GOTO, INT_EQ, INT_NE, INT_LT, INT_GT,
            INT_LE, INT_GE, BRANCH_EQ0, BRANCH_EQ1, LONG_EQ,
            LONG_NE, LONG_LT, LONG_GT, LONG_LE, LONG_GE};

    public static boolean isJump(CS2Instruction instruction) {
        Optional<CS2Instruction> optional = Stream.of(JUMP_INSTRUCTIONS)
                .filter(instr -> instr != null)
                .filter(instr -> instr.opcode == instruction.opcode)
                .findFirst();
        return optional.isPresent();
    }

    public static int[] getInterfaceIds(int hash) {
        int interfaceId = hash >> 16;
        return new int[] { interfaceId, hash & 0xffff };
    }

    public static int getHash(int interfaceId, int componentId) {
        return interfaceId << 16 | componentId;
    }

    public static int[] getColours(int hash) {
        int r = hash >> 16 & 0xff;
        int g = hash >> 8 & 0xff;
        int b = hash & 0xff;
        return new int[] { r, g, b };
    }

    public void write(Store store) {
        store.getIndex(IndexType.CS2_SCRIPTS).putArchive(id, encode());
    }

    @SuppressWarnings("unchecked")
    public static String recompile(String content) {
        ArrayList<CS2Instruction> instructions = new ArrayList<>();
        int[] intOpValues = new int[10000];
        String[] stringOpValues = new String[10000];
        long[] longOpValues = new long[10000];
        int intLocals = 0;
        int stringLocals = 0;
        int longLocals = 0;
        int opCount = 0;
        int scriptId = -1;
        String name = null;
        CS2Type[] arguments = null;
        String[] argumentNames = null;
        String[] variableNames = null;
        CS2Type returnType = null;
        HashMap<String, LocalVariable> variables = new HashMap<>();
        String[] lines = content.split("\n");
        int index = 0;
        while(index < lines.length) {
            String line = lines[index++];
            if(line.contains("//")) {
                name = line.substring(line.indexOf("//")+2, line.indexOf("("));
                String idString = line.substring(line.indexOf("(")+1, line.indexOf(")"));
                try {
                    scriptId = Integer.parseInt(idString);
                } catch(Exception e) {
                    e.printStackTrace();
                   return e.getMessage();
                }
                line = line.substring(line.indexOf(")")+1);
                String argumentsS = line.substring(line.indexOf("(")+1, line.indexOf(")"));
                if(argumentsS.equals("")) {
                    arguments = new CS2Type[0];
                    argumentNames = new String[0];
                } else {
                    String[] argumentsA = argumentsS.split(", ?");
                    arguments = new CS2Type[argumentsA.length];
                    argumentNames = new String[argumentsA.length];
                    for (int i = 0; i < argumentsA.length; i++) {
                        String argument = argumentsA[i];
                        System.out.println("A: " + argument);
                        String[] split = argument.split(" ");
                        CS2Type type = getCS2Type(split[0]);
                        arguments[i] = type;
                        argumentNames[i] = split[1];
                    }
                }
                line = line.substring(line.indexOf(")")+1);
                String returnTypeS = line.substring(line.indexOf("(")+1, line.indexOf(")"));
                returnType = getCS2Type(returnTypeS);
            } else if(line.contains("return")) {
                instructions.add(CS2Instruction.RETURN);
            } else if(line.contains("if(") || line.startsWith("if (")) {
                Object[] values = evaluateIfExpression(line, opCount, intOpValues, stringOpValues, longOpValues,
                        intLocals, stringLocals, longLocals, variableNames, variables, lines, index);
                instructions.addAll((ArrayList<CS2Instruction>) values[0]);
                opCount = (int) values[1];
                intOpValues = (int[]) values[2];
                stringOpValues = (String[]) values[3];
                longOpValues = (long[]) values[4];
                intLocals = (int) values[5];
                stringLocals = (int) values[6];
                longLocals = (int) values[7];
                if (values[8] != null)
                    variableNames = (String[]) values[8];
                variables = (HashMap<String, LocalVariable>) values[9];
                index = (int) values[10];
            } else {
                try {
                    //line = line.replaceAll("\\s", "");
                    Object[] values = evaluateExpression(line, opCount, intOpValues, stringOpValues, longOpValues, intLocals, stringLocals, longLocals, variableNames, variables, lines, index);
                    instructions.addAll((ArrayList<CS2Instruction>) values[0]);
                    opCount = (int) values[1];
                    intOpValues = (int[]) values[2];
                    stringOpValues = (String[]) values[3];
                    longOpValues = (long[]) values[4];
                    intLocals = (int) values[5];
                    stringLocals = (int) values[6];
                    longLocals = (int) values[7];
                    if(values[8] != null)
                        variableNames = (String[]) values[8];
                    variables = (HashMap<String, LocalVariable>) values[9];
                    index = (int) values[10];
                } catch(Exception e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
            }
        }

        ScriptDAO dao = new ScriptDAO(scriptId, name, arguments, argumentNames, variableNames, returnType);
        ScriptDBBuilder.saveScript(dao);

        int[] iCopy = new int[instructions.size()];
        String[] sCopy = new String[instructions.size()];
        long[] lCopy = new long[instructions.size()];
        System.arraycopy(intOpValues, 0, iCopy, 0, instructions.size());
        System.arraycopy(stringOpValues, 0, sCopy, 0, instructions.size());
        System.arraycopy(longOpValues, 0, lCopy, 0, instructions.size());

        for(int i = 0; i < iCopy.length; i++)
            System.out.println("IntOpValues @ "+i+": "+iCopy[i]);
        
        for(int i = 0; i < instructions.size(); i++) {
            CS2Instruction instruction = instructions.get(i);
            System.out.println(instruction+" "+(instruction.hasIntConstant() ? Integer.toString(intOpValues[i]) : ""));
        }
        CS2Instruction[] copy = new CS2Instruction[instructions.size()];
        index = 0;
        for(CS2Instruction instruction : instructions)
            copy[index++] = instruction;
        CS2Script script = new CS2Script(scriptId, name, arguments, argumentNames, copy, iCopy, sCopy, lCopy, intLocals, stringLocals, longLocals);
        try {
            System.out.println("Attempting to save to cache.");
            script.write(Cache.STORE);
            Cache.STORE.getIndices()[IndexType.CS2_SCRIPTS.getId()].rewriteTable();
            System.out.println("Probably succeeded.");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object[] evaluateIfExpression(String expression, int opCount, int[] iValues, String[] sValues, long[] lValues, 
        int intLocals, int stringLocals, int longLocals, String[] variableNames, HashMap<String, LocalVariable> variables, String[] lines, int index) {
        ArrayList<CS2Instruction> instructions = new ArrayList<>();
        boolean hasBlock = expression.endsWith("{");
        Pattern pattern = Pattern.compile("else( \\{)?");
        Matcher matcher = pattern.matcher(expression);
        if(!matcher.matches()) {
            String ifExpression = expression.substring(expression.indexOf("(") + 1, expression.lastIndexOf(")"));
            Object[] comparison = getComparison(ifExpression);
            if (comparison == null)
                throw new CompilerException("Unable to evaluate if statement: " + ifExpression);
            CS2Instruction instruction = (CS2Instruction) comparison[1];
            String[] split = ifExpression.split(" ?" + (String) comparison[0] + " ?");
            for (String ex : split) {
                Object[] values = evaluateParameter(ex, opCount, iValues, 
                        sValues, lValues, intLocals,
                        stringLocals, longLocals, variableNames, variables, lines, index);
                instructions.addAll((ArrayList<CS2Instruction>) values[0]);
                opCount = (int) values[1];
                iValues = (int[]) values[2];
                sValues = (String[]) values[3];
                lValues = (long[]) values[4];
                intLocals = (int) values[5];
                stringLocals = (int) values[6];
                longLocals = (int) values[7];
                if (values[8] != null)
                    variableNames = (String[]) values[8];
                variables = (HashMap<String, LocalVariable>) values[9];
                index = (int) values[10];
            }
            instructions.add(instruction);
            iValues[opCount++] = 1;
        }
        instructions.add(CS2Instruction.GOTO);
        int sizeIndex = opCount++;
        if (!hasBlock) {
            String nextLine = lines[index++];
            Object[] values = evaluateExpression(
                    nextLine, opCount, 
                    iValues, sValues, lValues,
                    intLocals, stringLocals, longLocals, variableNames, variables, lines, index);
            instructions.addAll((ArrayList<CS2Instruction>) values[0]);
            opCount = (int) values[1];
            iValues = (int[]) values[2];
            sValues = (String[]) values[3];
            lValues = (long[]) values[4];
            intLocals = (int) values[5];
            stringLocals = (int) values[6];
            longLocals = (int) values[7];
            if (values[8] != null)
                variableNames = (String[]) values[8];
            variables = (HashMap<String, LocalVariable>) values[9];
            index = (int) values[10];
            iValues[sizeIndex] = opCount - sizeIndex - 1;
        } else {
            while(!(expression = lines[index++]).equals("}")) {
                Object[] values = evaluateExpression(expression, opCount, iValues, sValues, lValues, intLocals,
                        stringLocals, longLocals, variableNames, variables, lines, index);
                instructions.addAll((ArrayList<CS2Instruction>) values[0]);
                opCount = (int) values[1];
                iValues = (int[]) values[2];
                sValues = (String[]) values[3];
                lValues = (long[]) values[4];
                intLocals = (int) values[5];
                stringLocals = (int) values[6];
                longLocals = (int) values[7];
                if (values[8] != null)
                    variableNames = (String[]) values[8];
                variables = (HashMap<String, LocalVariable>) values[9];
                index = (int) values[10];
            }
            iValues[sizeIndex] = opCount - sizeIndex;
        }
        String nextLine = lines[index].replaceAll("\\t", "").replaceAll(" {4}", "");
        System.out.println("Next Line: "+nextLine);
        if(nextLine.startsWith("else")) {
            Object[] values = evaluateIfExpression(lines[index++], opCount, iValues, sValues, lValues, intLocals,
                    stringLocals, longLocals, variableNames, variables, lines, index);
            instructions.addAll((ArrayList<CS2Instruction>) values[0]);
            opCount = (int) values[1];
            iValues = (int[]) values[2];
            sValues = (String[]) values[3];
            lValues = (long[]) values[4];
            intLocals = (int) values[5];
            stringLocals = (int) values[6];
            longLocals = (int) values[7];
            if (values[8] != null)
                variableNames = (String[]) values[8];
            variables = (HashMap<String, LocalVariable>) values[9];
            index = (int) values[10];
        }
        return new Object[] { instructions, opCount, iValues, sValues, lValues, intLocals, stringLocals, longLocals,
                variableNames, variables, index };
    }

    public static CS2Type getCS2Type(String string) {
        switch(string.toLowerCase()) {
            case "void": return CS2Type.VOID;
            case "string": return CS2Type.STRING;
            case "long": return CS2Type.LONG;
            case "boolean": return CS2Type.BOOLEAN;
            default: return CS2Type.INT;
        }
    }

    public static CS2Type getCS2TypeOrNull(String string) {
        switch (string.toLowerCase()) {
            case "void": return CS2Type.VOID;
            case "string": return CS2Type.STRING;
            case "long": return CS2Type.LONG;
            case "boolean": return CS2Type.BOOLEAN;
            case "int": return CS2Type.INT;
            default: return null;
        }
    }

    public static Object[] assignVariable(String name, String expression, int opCount, int[] iValues, String[] sValues, long[] lValues, 
        int intLocals, int stringLocals, int longLocals, String[] variableNames, HashMap<String, LocalVariable> variables, String[] lines, int index) {
        ArrayList<CS2Instruction> instructions = new ArrayList<>();
        String[] split = expression.split(" ?= ?");
        if (split.length < 2)
            throw new CompilerException("Unable to evaluate assignation: " + expression);
        if (split.length > 2)
            throw new CompilerException("Multiple assignations on one line is not yet supported.");
        if (!variables.containsKey(name))
            throw new CompilerException(name + " is undefined.");
        String assignation = split[1].replace(";", "");
        System.out.println("Attempting to decode assignation: " + assignation);
        if (isInt(assignation)) {
            instructions.add(CS2Instruction.PUSH_INT);
            iValues[opCount++] = Integer.parseInt(assignation);
            System.out.println("Push Int: " + Integer.parseInt(assignation));
        } else if (isString(assignation)) {
            instructions.add(CS2Instruction.PUSH_STRING);
            sValues[opCount++] = assignation;
        } else if (isLong(assignation)) {
            instructions.add(CS2Instruction.PUSH_LONG);
            lValues[opCount++] = Long.parseLong(assignation);
        } else if(variables.containsKey(assignation)) {
            LocalVariable variable = variables.get(assignation);
            CS2Instruction instruction = variable.getType() == CS2Type.STRING ? CS2Instruction.LOAD_STRING : variable.getType() == CS2Type.LONG ? CS2Instruction.LOAD_LONG : CS2Instruction.LOAD_INT;
            instructions.add(instruction);
            iValues[opCount++] = variable.getInfo()[0];
        } else {
            Object[] values = evaluateExpression(assignation, opCount, iValues, sValues, lValues, intLocals,
                    stringLocals, longLocals, variableNames, variables, lines, index);
            instructions.addAll((ArrayList<CS2Instruction>) values[0]);
            opCount = (int) values[1];
            iValues = (int[]) values[2];
            sValues = (String[]) values[3];
            lValues = (long[]) values[4];
            intLocals = (int) values[5];
            stringLocals = (int) values[6];
            longLocals = (int) values[7];
            if (values[8] != null)
                variableNames = (String[]) values[8];
            variables = (HashMap<String, LocalVariable>) values[9];
            index = (int) values[10];
        }
        LocalVariable variable = variables.get(name);
        instructions.add(variable.getType() == CS2Type.STRING ? CS2Instruction.STORE_STRING
                : variable.getType() == CS2Type.LONG ? CS2Instruction.STORE_LONG : CS2Instruction.STORE_INT);
        iValues[opCount++] = variable.getInfo()[0];
        return new Object[] { instructions, opCount, iValues, sValues, lValues, intLocals, stringLocals, longLocals,
                variableNames, variables, index };
    }

    public static Object[] COMPARISONS = {
        "<", CS2Instruction.INT_LT, ">", CS2Instruction.INT_GT,
        "<=", CS2Instruction.INT_LE, ">=", CS2Instruction.INT_GE,
        "==", CS2Instruction.INT_EQ, "!=", CS2Instruction.INT_NE
    };

    public static Object[] getComparison(String expression) {
        int index = 0;
        while(index < COMPARISONS.length) {
            String comparison = (String) COMPARISONS[index++];
            CS2Instruction instruction = (CS2Instruction) COMPARISONS[index++];
            if(expression.contains(comparison)) return new Object[] { comparison, instruction };
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Object[] evaluateExpression(String expression, int opCount, int[] iValues, String[] sValues, long[] lValues, 
        int intLocals, int stringLocals, int longLocals, String[] variableNames, HashMap<String, LocalVariable> variables, String[] lines, int index) {
        ArrayList<CS2Instruction> instructions = new ArrayList<>();
        // if line startsWith variable
        // if line startsWith loop/if
        // else
        expression = expression.replaceAll("\\t", "").replaceAll(" {4}", "");
        System.out.println("Evaluating: "+expression);
        String[] split = expression.split(" ");
        if(expression.startsWith("if(") || expression.startsWith("if (")) {
            Object[] values = evaluateIfExpression(expression, opCount, iValues, sValues, lValues, intLocals,
                    stringLocals, longLocals, variableNames, variables, lines, index);
            instructions.addAll((ArrayList<CS2Instruction>) values[0]);
            opCount = (int) values[1];
            iValues = (int[]) values[2];
            sValues = (String[]) values[3];
            lValues = (long[]) values[4];
            intLocals = (int) values[5];
            stringLocals = (int) values[6];
            longLocals = (int) values[7];
            if (values[8] != null)
                variableNames = (String[]) values[8];
            variables = (HashMap<String, LocalVariable>) values[9];
            index = (int) values[10];
            return new Object[] { instructions, opCount, iValues, sValues, lValues, intLocals, stringLocals, longLocals,
                    variableNames, variables, index };
        } else if(expression.contains("=")) {
            CS2Type type = CS2Script.getCS2TypeOrNull(split[0]);
            String name;
            if(type != null) { //we're creating a variable too
                name = split[1].replace(";", "");
                if (variableNames == null) {
                    variableNames = new String[1];
                    variableNames[0] = name;
                } else
                    variableNames = ArrayUtils.add(variableNames, name);
                int variableIndex;
                if (type == CS2Type.STRING)
                    variableIndex = stringLocals++;
                else if (type == CS2Type.LONG)
                    variableIndex = longLocals++;
                else
                    variableIndex = intLocals++;
                LocalVariable variable = new LocalVariable(name, type);
                int stackType = type == CS2Type.STRING ? 1 : type == CS2Type.LONG ? 2 : 0;

                variable.setIdentifier(LocalVariable.makeIdentifier(variableIndex, stackType));
                variables.put(name, variable);
            } else
                name = split[0];
            Object[] values = assignVariable(name, expression, opCount, iValues, sValues, lValues, intLocals, stringLocals,
                    longLocals, variableNames, variables, lines, index);
            instructions.addAll((ArrayList<CS2Instruction>) values[0]);
            opCount = (int) values[1];
            iValues = (int[]) values[2];
            sValues = (String[]) values[3];
            lValues = (long[]) values[4];
            intLocals = (int) values[5];
            stringLocals = (int) values[6];
            longLocals = (int) values[7];
            if (values[8] != null)
                variableNames = (String[]) values[8];
            variables = (HashMap<String, LocalVariable>) values[9];
            index = (int) values[10];
            return new Object[] { instructions, opCount, iValues, sValues, lValues, intLocals, stringLocals, longLocals,
                    variableNames, variables, index };
        } else if(CS2Script.getCS2TypeOrNull(split[0]) != null) { //variable creation (and possible assignation)
            CS2Type type = CS2Script.getCS2TypeOrNull(split[0]);
            String name = split[1].replace(";", "");
            if(variableNames == null) {
                variableNames = new String[1];
                variableNames[0] = name;
            } else
                variableNames = ArrayUtils.add(variableNames, name);
            int variableIndex;
            if(type == CS2Type.STRING)
                variableIndex = stringLocals++;
            else if(type == CS2Type.LONG)
                variableIndex = longLocals++;
            else
                variableIndex = intLocals++;
            LocalVariable variable = new LocalVariable(name, type);
            int stackType = type == CS2Type.STRING ? 1 : type == CS2Type.LONG ? 2 : 0;

            variable.setIdentifier(LocalVariable.makeIdentifier(variableIndex, stackType));
            variables.put(name, variable);
            System.out.println("Defining "+type.toString()+" "+name+" "+Arrays.toString(variableNames));
            return new Object[] { instructions, opCount, iValues, sValues, lValues, intLocals, stringLocals, longLocals,
                    variableNames, variables, index };
        }
        String instructionName = expression.substring(0, expression.indexOf("("));
        if(instructionName.equals("if_gethash")) {
            String[] parameters = expression.substring(expression.indexOf("(") + 1, expression.lastIndexOf(")"))
                    .split(", ?");
            int interfaceId = Integer.parseInt(parameters[0]);
            int componentId = Integer.parseInt(parameters[1]);
            instructions.add(CS2Instruction.PUSH_INT);
            iValues[opCount++] = getHash(interfaceId, componentId);
            return new Object[] { instructions, opCount, iValues, sValues, lValues, intLocals, stringLocals, longLocals, variableNames, variables, index };
        }
        expression = expression.replaceAll("\\s", "");
        CS2Instruction instruction = CS2Instruction.getByName(instructionName);
        if(instruction == null) throw new CompilerException("Invalid instruction: "+instructionName);
        String[] parameters = expression.substring(expression.indexOf("(") + 1, expression.lastIndexOf(")")).split(",(?![^()]*\\))");
        System.out.println("Params: "+Arrays.toString(parameters));
        for(int i = parameters.length-1; i >= 0; i--) {
            String parameter = parameters[i];
            Object[] values = evaluateParameter(parameter, opCount, iValues, sValues, lValues, intLocals,
                    stringLocals, longLocals, variableNames, variables, lines, index);
            instructions.addAll((ArrayList<CS2Instruction>) values[0]);
            opCount = (int) values[1];
            iValues = (int[]) values[2];
            sValues = (String[]) values[3];
            lValues = (long[]) values[4];
            intLocals = (int) values[5];
            stringLocals = (int) values[6];
            longLocals = (int) values[7];
            if (values[8] != null)
                variableNames = (String[]) values[8];
            variables = (HashMap<String, LocalVariable>) values[9];
            index = (int) values[10];
        }
        instructions.add(instruction);
        iValues[opCount++] = 0;
        return new Object[] { instructions, opCount, iValues, sValues, lValues, intLocals, stringLocals, longLocals, variableNames, variables, index };
    }

    public static Object[] evaluateParameter(String expression, int opCount, int[] iValues, String[] sValues, long[] lValues, 
        int intLocals, int stringLocals, int longLocals, String[] variableNames, HashMap<String, LocalVariable> variables, String[] lines, int index) {
        ArrayList<CS2Instruction> instructions = new ArrayList<>();
        if (isInt(expression)) {
            instructions.add(CS2Instruction.PUSH_INT);
            iValues[opCount++] = Integer.parseInt(expression);
            System.out.println("Push Int: " + Integer.parseInt(expression));
        } else if (isString(expression)) {
            instructions.add(CS2Instruction.PUSH_STRING);
            sValues[opCount++] = expression;
        } else if (isLong(expression)) {
            instructions.add(CS2Instruction.PUSH_LONG);
            lValues[opCount++] = Long.parseLong(expression);
        } else if (variables.containsKey(expression)) {
            LocalVariable variable = variables.get(expression);
            CS2Instruction instr = variable.getType() == CS2Type.STRING ? CS2Instruction.LOAD_STRING
                    : variable.getType() == CS2Type.LONG ? CS2Instruction.LOAD_LONG : CS2Instruction.LOAD_INT;
            instructions.add(instr);
            iValues[opCount++] = variable.getInfo()[0];
        } else {
            Object[] values = evaluateExpression(
                    expression, opCount, iValues, sValues, lValues, intLocals, stringLocals,
                    longLocals, variableNames, variables, lines, index);
            instructions.addAll((ArrayList<CS2Instruction>) values[0]);
            opCount = (int) values[1];
            iValues = (int[]) values[2];
            sValues = (String[]) values[3];
            lValues = (long[]) values[4];
            intLocals = (int) values[5];
            stringLocals = (int) values[6];
            longLocals = (int) values[7];
            if (values[8] != null)
                variableNames = (String[]) values[8];
            variables = (HashMap<String, LocalVariable>) values[9];
            index = (int) values[10];
        }
        return new Object[] { instructions, opCount, iValues, sValues, lValues, intLocals, stringLocals, longLocals,
                variableNames, variables, index };
    }

    public static boolean isString(String expression) {
        return expression.contains("\"") && !expression.contains("(");
    }

    public static boolean isInt(String expression) {
        try {
            Integer.parseInt(expression);
            return true;
        } catch(Exception e) { return false; }
    }

    public static boolean isLong(String expression) {
        try {
            Long.parseLong(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        CS2Editor.setGson(CS2Editor.buildGson());
        Cache.init("F:\\workspace\\github\\darkan-server\\data\\cache\\");
        ScriptDBBuilder.load();
        InstructionDBBuilder.load();
        byte[] o = Cache.STORE.getIndex(IndexType.CS2_SCRIPTS).getArchive(15).getData();
        CS2Script script = CS2Definitions.getScript(15);
        byte[] n = script.encode();
        System.out.println("id: 15, cache data length: "+o.length+", encoded data length: "+n.length);
        System.out.println(Arrays.toString(o));
        System.out.println(Arrays.toString(n));

        OutputStream test = new OutputStream();

        test.writeInt(155);

        System.out.println(Arrays.toString(test.toByteArray()));
    }

    public byte[] encode() {
        OutputStream out = new OutputStream();

        if (name == null) {
            out.writeByte(0);
            System.out.println("Writing byte name: 0");
        } else
            out.writeString(name);

        for (int i = 0; i < operations.length; i++) {
            CS2Instruction op = operations[i];
            if(op == null) continue;
            out.writeShort(op.getOpcode());
            System.out.println("Writing short opcode: "+op.getOpcode()+" result: "+getShortBytes(op.getOpcode()));
            if (op == CS2Instruction.PUSH_STRING) {
                out.writeString((String) stringOpValues[i]);
                System.out.println("Writing string opValue: "+stringOpValues[i]+" shouldn't get here.");
            } else if (CS2Instruction.PUSH_LONG == op) {
                out.writeLong(longOpValues[i]);
                System.out.println("Writing long opValue: " + longOpValues[i] + " shouldn't get here.");
            } else {
                if (op.hasIntConstant()) {
                    out.writeInt(intOpValues[i]);
                    System.out.println("Writing int constant: "+intOpValues[i]+" result: "+getIntBytes(intOpValues[i]));
                } else {
                    out.writeByte(intOpValues[i]);
                    System.out.println("Writing byte opValue: "+((byte) intOpValues[i]));
                }
            }
        }

        out.writeInt(operations.length);
        System.out.println("Writing operations length int "+operations.length+" result: "+getIntBytes(operations.length));
        out.writeShort(intLocalsCount);
        System.out.println("Writing int locals short "+intLocalsCount+" result: "+getShortBytes(intLocalsCount));
        out.writeShort(stringLocalsCount);
        System.out.println("Writing string locals short " + stringLocalsCount + " result: " + getShortBytes(
                stringLocalsCount));
        out.writeShort(longLocalsCount);
        System.out.println("Writing long locals short " + longLocalsCount + " result: " + getShortBytes(longLocalsCount));
        out.writeShort(intArgsCount);
        System.out.println("Writing int args short " + intArgsCount + " result: " + getShortBytes(intArgsCount));
        out.writeShort(stringArgsCount);
        System.out.println("Writing string args short " + stringArgsCount + " result: " + getShortBytes(
                stringArgsCount));
        out.writeShort(longArgsCount);
        System.out.println("Writing long args short " + longArgsCount + " result: " + getShortBytes(longArgsCount));

        OutputStream switchBlock = new OutputStream();
        if (switchMaps == null) {
            switchBlock.writeByte(0);
            System.out.println("Writing switchBlock byte 0");
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
        System.out.println("Writing switchBytes: "+Arrays.toString(switchBytes));

        out.writeShort(switchBytes.length);
        System.out.println("Writing switchBytes length short: "+switchBytes.length+" result: "+getShortBytes(
                switchBytes.length));
        System.out.println(out.toByteArray().length);
        return out.toByteArray();
    }

    public String getIntBytes(int i) {
        return ((byte) (i >> 24))+" "+((byte) (i >> 16))+" "+((byte) (i >> 8))+" "+((byte) i);
    }

    public String getShortBytes(int i) {
        return ((byte) (i >> 8))+" "+((byte) i);
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
        ScriptDAO dao = ScriptDBBuilder.getScript(id);
        if (dao != null)
            return dao.getName();
        return "script" + id;
    }

    public int countOf(Class<? extends Instruction> type) {
        int total = 0;
        for (int i = 0; i < instructions.length; i++)
            if (instructions[i].getClass() == type)
                total++;
        return total;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
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
