package com.cryo.cs2.flow;

import com.cryo.CS2Editor;
import com.cryo.cs2.CS2Definitions;
import com.cryo.cs2.CS2Instruction;
import com.cryo.cs2.CS2ParamDefs;
import com.cryo.cs2.CS2Script;
import com.cryo.cs2.instructions.*;
import com.cryo.cs2.nodes.*;
import com.cryo.cs2.nodes.interfaces.CS2HideComponent;
import com.cryo.decompiler.CS2Type;
import com.cryo.decompiler.util.FunctionInfo;
import com.cryo.utils.ArrayQueue;
import com.cryo.utils.DecompilerException;
import com.cryo.utils.IOUtils;
import com.cryo.utils.OpcodeUtils;
import lombok.Data;

import static com.cryo.cs2.CS2Instruction.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class CS2FlowGenerator {

    private CS2Script script;
    private CS2Function function;
    private CS2FlowBlock[] blocks;
    private List<CS2FlowBlock> processed;
    private int counter;

    public CS2FlowGenerator(CS2Script script, CS2Function function) {
        this.processed = new ArrayList<>();
        this.script = script;
        this.function = function;
        this.counter = 0;
    }

    public void generate() throws DecompilerException {
        this.initGeneration();
        this.processGeneration();
        this.endGeneration();
    }

    private void initGeneration() {
        blocks = new CS2FlowBlock[script.countOf(LabelInstruction.class) + 1];
        blocks[0] = new CS2FlowBlock();
    }

    private void processGeneration() {
        int numProcessed;
        do {
            numProcessed = 0;
            for (int i = 0; i < blocks.length; i++)
                if (blocks[i] != null && !processed.contains(blocks[i])) {
                    processed.add(blocks[i]);
                    numProcessed++;
                    processFlowBlock(blocks[i]);
                }
        }
        while (numProcessed > 0);
    }

    private void processFlowBlock(CS2FlowBlock block) {
        int ptr = block.getStartAddress();
        CS2Stack stack = block.getStack().copy();

        try {
            for (;;ptr++) {
                if (ptr >= script.getInstructions().length)
                    throw new DecompilerException("Error:Code out bounds.");
                Instruction instruction = script.getInstructions()[ptr];
                CS2Instruction operation = CS2Instruction.getByOpcode(instruction.getOpcode());
                System.out.println("Instruction: "+operation);
                int opcode = instruction.getOpcode();
                if (instruction instanceof LabelInstruction) {
                    // new flow block
                    this.dumpStack(block, stack);
                    this.generateFlowBlock((LabelInstruction)instruction, stack.copy());
                    break;
                }
                else if (instruction instanceof JumpInstruction) {
                    JumpInstruction jmp = (JumpInstruction)instruction;

                    if (OpcodeUtils.getTwoConditionsJumpStackType(opcode) != -1) {
                        int stackType = OpcodeUtils.getTwoConditionsJumpStackType(opcode);
                        CS2Type type = defaultType(stackType);
                        CS2Expression v2 = this.cast(stack.pop(stackType), type);
                        CS2Expression v1 = this.cast(stack.pop(stackType), type);
                        CS2Expression conditional = new CS2ConditionalExpression(v1,v2,OpcodeUtils.getTwoConditionsJumpConditional(opcode));
                        this.dumpStack(block, stack);
                        CS2FlowBlock target = generateFlowBlock(jmp.getTarget(),stack.copy());
                        target.getPredecessors().add(block);
                        block.getSuccessors().add(target);
                        block.write(new CS2ConditionalFlowBlockJump(conditional,target));
                    }
                    else if (OpcodeUtils.getOneConditionJumpStackType(opcode) != -1) {
                        CS2Expression expr = this.cast(stack.pop(0), CS2Type.BOOLEAN);
                        if (operation == BRANCH_EQ0)
                            expr = new CS2NotExpression(expr);
                        this.dumpStack(block, stack);
                        CS2FlowBlock target = generateFlowBlock(jmp.getTarget(),stack.copy());
                        target.getPredecessors().add(block);
                        block.getSuccessors().add(target);
                        block.write(new CS2ConditionalFlowBlockJump(expr,target));
                    }
                    else {
                        this.dumpStack(block, stack);
                        CS2FlowBlock target = generateFlowBlock(jmp.getTarget(),stack.copy());
                        target.getPredecessors().add(block);
                        block.getSuccessors().add(target);
                        block.write(new CS2UnconditionalFlowBlockJump(target));
                        break;
                    }
                }
                else if(instruction instanceof PrimitiveInstruction
                        && (((PrimitiveInstruction) instruction).getType() == CS2Type.STRING
                        || ((PrimitiveInstruction) instruction).getType() == CS2Type.LONG)) {
                    CS2PrimitiveExpression expression = new CS2PrimitiveExpression(((PrimitiveInstruction) instruction).getValue(), ((PrimitiveInstruction) instruction).getType());
                    if(expression.getType() == CS2Type.STRING)
                        stack.push(expression, 1);
                    else if(expression.getType() == CS2Type.LONG)
                        stack.push(expression, 2);
                }
//                else if (instruction instanceof ConfigInstruction) {
//                    ConfigInstruction cfg = (ConfigInstruction)instruction;
//                    int stackType = cfg.getConfig().getType().intSS() == 0 ? (cfg.getConfig().getType().longSS() != 0 ? 2 : 1) : 0;
//                    if (opcode == Opcodes.LOAD_CONFIG) {
//                        stack.push(new ConfigurationLoadNode(cfg.getConfig(), cfg.getConstant()), stackType);
//                    }
//                    else if (opcode == Opcodes.STORE_CONFIG) {
//                        CS2Expression expr = cast(stack.pop(stackType), cfg.getConfig().getType());
//                        this.dumpStack(block, stack);
//                        block.write(new CS2Poppable(new ConfigurationStoreNode(cfg.getConfig(), cfg.getConstant(), expr)));
//                    }
//                    else {
//                        throw new DecompilerException("Unknown opcode:" + opcode);
//                    }
//                }
//                else if (instruction instanceof BitConfigInstruction) {
//                    BitConfigInstruction cfg = (BitConfigInstruction)instruction;
//                    int stackType = cfg.getConfig().getBase().getType().intSS() == 0 ? (cfg.getConfig().getBase().getType().longSS() != 0 ? 2 : 1) : 0;
//                    if (opcode == Opcodes.LOAD_BITCONFIG) {
//                        stack.push(new BitConfigurationLoadNode(cfg.getConfig(), cfg.getConstant()), stackType);
//                    }
//                    else if (opcode == Opcodes.STORE_BITCONFIG) {
//                        CS2Expression expr = cast(stack.pop(stackType), cfg.getConfig().getBase().getType());
//                        this.dumpStack(block, stack);
//                        block.write(new CS2Poppable(new BitConfigurationStoreNode(cfg.getConfig(), cfg.getConstant(), expr)));
//                    }
//                    else {
//                        throw new DecompilerException("Unknown opcode:" + opcode);
//                    }
//                }
                else if (instruction instanceof PrimitiveInstruction && ((PrimitiveInstruction) instruction).getType() == CS2Type.INT) {
                    PrimitiveInstruction intInstr = (PrimitiveInstruction)instruction;
                    if(operation == PUSH_INT)
                        stack.push(new CS2PrimitiveExpression(intInstr.getValue(), intInstr.getType()), 0);
                    else if(operation == ADD || operation == SUBTRACT
                        || operation == DIVIDE || operation == MULTIPLY
                        || operation == MODULO) {
                        CS2Expression[] expressions = new CS2Expression[2];
                        expressions[0] = cast(stack.pop(0), CS2Type.INT);
                        expressions[1] = cast(stack.pop(0), CS2Type.INT);
                        stack.push(new CS2DMAS(expressions, operation), 0);
                    } else if(operation == ENUM) {
                        CS2Expression index = cast(stack.pop(0), CS2Type.INT);
                        CS2Expression id = cast(stack.pop(0), CS2Type.INT);
                        CS2PrimitiveExpression returnType = (CS2PrimitiveExpression) stack.pop(0);
                        CS2Expression keyType = cast(stack.pop(0), CS2Type.INT);
                        stack.push(new CS2Enum(index, id, cast(returnType, CS2Type.INT), keyType), ((int) returnType.getValue()) == 's' ? 1 : 0);
                    } else if(operation == ITEM_NAME) {
                        CS2Expression expression = cast(stack.pop(0), CS2Type.INT);
                        stack.push(new CS2BasicExpression(expression, operation.name().toLowerCase()), 1);
                    } else if(operation == SCALE || operation == MIN || operation == MAX) {
                        int expressionSize = CS2Script.getArgumentSize(operation);
                        CS2Expression[] expressions = new CS2Expression[expressionSize];
                        for(int i = 0; i < expressionSize; i++)
                            expressions[i] = cast(stack.pop(0), CS2Type.INT);
                        stack.push(new CS2BasicExpression(expressions, operation.name().toLowerCase()), CS2Script.getStackType(operation));
                    } else if(operation == IF_GETWIDTH || operation == INV_SIZE || operation == RANDOM
                        || operation == IF_GETNEXTSUBID || operation == GET_PLAYER_X || operation == GET_PLAYER_Y 
                        || operation == GET_PLAYER_PLANE || operation == IF_GETHEIGHT) {
                        CS2Expression expression = stack.pop(0);
                        stack.push(new CS2BasicExpression(expression, operation.name().toLowerCase()), 0);
                    } else if(operation == instr6801 || operation == instr6152) {
                        CS2Expression[] expressions = new CS2Expression[3];
                        expressions[0] = cast(stack.pop(1), CS2Type.STRING);
                        expressions[1] = cast(stack.pop(0), CS2Type.INT);
                        expressions[2] = cast(stack.pop(0), CS2Type.INT);
                        stack.push(new CS2BasicExpression(expressions, operation.name().toLowerCase()), 0);
                    } else if(operation == instr6519) {
                        CS2Expression expression = cast(stack.pop(1), CS2Type.STRING);
                        stack.push(new CS2BasicExpression(expression, operation.name().toLowerCase()), 1);
                    } else if(operation == INV_GETITEM || operation == INV_GETNUM || operation == INV_TOTAL
                        || operation == QUEST_STATREQ_LEVEL || operation == CC_FIND || operation == INVOTHER_GETITEM
                        || operation == INVOTHER_GETNUM) {
                        CS2Expression[] expressions = new CS2Expression[2];
                        expressions[0] = cast(stack.pop(0), CS2Type.INT);
                        expressions[1] = stack.pop(0);
                        if (operation != CC_FIND)
                            expressions[1] = cast(expressions[1], CS2Type.INT);
                        stack.push(new CS2BasicExpression(expressions, operation.name().toLowerCase()), 0);
                    } else if(operation == STRING_LENGTH) {
                        CS2Expression expression = cast(stack.pop(1), CS2Type.STRING);
                        stack.push(new CS2BasicExpression(expression, operation.name().toLowerCase()), 0);
                    } else if(operation == IF_SETHIDE) {
                        CS2Expression expression = stack.pop(0);
                        if(!(expression instanceof CS2PrimitiveExpression))
                            expression = cast(expression, CS2Type.COMPONENT);
                        CS2Expression hidden = cast(stack.pop(0), CS2Type.BOOLEAN);
                        block.write(new CS2HideComponent(expression, hidden));
                    } else if(operation == TO_STRING) {
                        CS2Expression expression = cast(stack.pop(0), CS2Type.INT);
                        stack.push(new CS2ToString(expression), 1);
                    } else if(operation == CC_SETOP || operation == IF_SETTEXT) {
                        CS2Expression op = stack.pop(0);
                        if(operation != IF_SETTEXT || !(op instanceof CS2PrimitiveExpression))
                            op = cast(op, CS2Type.INT);
                        CS2Expression str = cast(stack.pop(1), CS2Type.STRING);
                        block.write(new CS2BasicExpression(new CS2Expression[] { str, op }, operation.name().toLowerCase()));
                    } else if(operation == STRUCT_PARAM || operation == ITEM_PARAM) {
                        CS2PrimitiveExpression expression = (CS2PrimitiveExpression) stack.pop(0);
                        CS2Expression expression2 = cast(stack.pop(0), CS2Type.INT);
                        int paramId = (int) expression.getValue();
                        CS2ParamDefs defs = CS2ParamDefs.getParams(paramId);
                        stack.push(new CS2StructParam(paramId, expression2), defs.isString() ? 1 : 0);
                    } else if(operation == instr6135 || operation == instr6150 || operation == GET_PLAYER_POS) {
                        stack.push(new CS2BasicExpression(new CS2Expression[] { }, operation.name().toLowerCase()), 0);
                    } else if(operation == MOVE_COORD) {
                        CS2Expression plane = cast(stack.pop(0), CS2Type.INT);
                        CS2Expression x = cast(stack.pop(0), CS2Type.INT);
                        CS2Expression y = cast(stack.pop(0), CS2Type.INT);
                        CS2Expression startPos = cast(stack.pop(0), CS2Type.INT);
                        stack.push(new CS2MoveCoord(startPos, x, y, plane), 0);
                    } else if(operation == instr6342 || operation == instr6452 || operation == instr6257
                        || operation == instr6237 || operation == HOOK_MOUSE_PRESS || operation == HOOK_MOUSE_RELEASE) {
                        System.out.println("Testing unknown instruction.");
                        CS2Expression component = null;
                        if(operation == instr6342 || operation == instr6257 || operation == instr6237
                            || operation == HOOK_MOUSE_PRESS || operation == HOOK_MOUSE_RELEASE)
                            component = cast(stack.pop(0), CS2Type.INT);
                        CS2PrimitiveExpression paramTypesE = (CS2PrimitiveExpression) stack.pop(1);
                        String paramTypes = (String) paramTypesE.getValue();
                        CS2Expression[] intArr = null;
                        if(paramTypes.length() > 0 && paramTypes.charAt(paramTypes.length()-1) == 'Y') {
                            int size = (int) ((CS2PrimitiveExpression) stack.pop(0)).getValue();
                            if(size > 0) {
                                intArr = new CS2Expression[size];
                                while(size-- > 0)
                                    intArr[size] = cast(stack.pop(0), CS2Type.INT);
                            }
                        }
                        CS2Expression[] params = new CS2Expression[paramTypes.length() + 1];
                        for(int i = params.length - 1; i >= 1; --i) {
                            if(paramTypes.charAt(i - 1) == 's')
                                params[i] = cast(stack.pop(1), CS2Type.STRING);
                            else if(paramTypes.charAt(i-1) == 'I')
                                params[i] = cast(stack.pop(0), CS2Type.INT);
                            else
                                System.out.println("paramTypes.charAt: "+paramTypes.charAt(i - 1));
                        }
                        CS2Expression scriptId = cast(stack.pop(0), CS2Type.INT);
                        block.write(new CS2UnknownExpression(new Object[] { scriptId, params, intArr, cast(paramTypesE, CS2Type.STRING), component }, operation));
                    } else if(operation == CC_DELETEALL) {
                        CS2Expression expression = stack.pop(0);
                        block.write(new CS2BasicExpression(expression, operation.name().toLowerCase()));
                    } else if(operation == instr6212) {
                        CS2Expression expression = cast(stack.pop(0), CS2Type.INT);
                        stack.push(new CS2BasicExpression(expression, "test_ex_1"), 0);
                        stack.push(new CS2BasicExpression(expression, "test_ex_2"), 0);
                    } else if(CS2Script.isBasicInstruction(operation)) {
                        int size = CS2Script.getArgumentSize(operation);
                        int stackType = CS2Script.getStackType(operation);
                        if(size == -1 || stackType == -1)
                            throw new DecompilerException("Unknown opcode: " + opcode + " " + operation);
                        CS2Expression[] expressions = new CS2Expression[size];
                        for(int i = 0; i < size; i++)
                            expressions[i] = cast(stack.pop(stackType), stackType == 0 ? CS2Type.INT : stackType == 1 ? CS2Type.STRING : CS2Type.LONG);
                        block.write(new CS2BasicExpression(expressions, operation.name().toLowerCase()));
                    }
                    else if (operation == CS2Instruction.RETURN) {
                        if (stack.getSize() <= 0) {
                            this.function.setReturnType(CS2Type.merge(this.function.getReturnType(), CS2Type.VOID));
                            block.write(new CS2Return());
                        } else if (stack.getSize() == 1) {
                            this.function.setReturnType(CS2Type.merge(this.function.getReturnType(), stack.peek().getType()));
                            block.write(new CS2Return(stack.pop()));
                        }
                        else {
                            CS2Type[] types = new CS2Type[stack.getSize()];
                            CS2Expression[] args = new CS2Expression[stack.getSize()];
                            for (int i = args.length - 1; i >= 0; i--) {
                                CS2Expression expr = stack.pop();
                                args[i] = cast(expr, expr.getType());
                                types[i] = args[i].getType();
                            }

                            CS2Type struct = CS2Type.makeAdvancedStruct(this.function.getName() + "_struct", false, types);
                            this.function.setReturnType(CS2Type.merge(this.function.getReturnType(), struct));
                            block.write(new CS2Return(new CS2StructExpression(struct, args)));
                        }
                        break;
                    }
                    else if(operation == CS2Instruction.LOAD_INT || operation == CS2Instruction.LOAD_STRING || operation == CS2Instruction.LOAD_LONG) {
                        int stackType = operation == CS2Instruction.LOAD_INT ? 0 : operation == CS2Instruction.LOAD_STRING ? 1 : 2;
                        LocalVariable variable = function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.asInt(), stackType));
                        stack.push(new CS2VariableLoad(variable), stackType);
                    }
                    else if(operation == CS2Instruction.STORE_INT || operation == CS2Instruction.STORE_STRING || operation == CS2Instruction.STORE_LONG) {
                        int stackType = operation == CS2Instruction.STORE_INT ? 0 : operation == CS2Instruction.STORE_STRING ? 1 : 2;
                        LocalVariable var = function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.asInt(), stackType));
                        CS2Expression expr = cast(stack.pop(stackType),var.getType());
                        this.dumpStack(block, stack);
                        block.write(new CS2Poppable(new CS2VariableAssign(var,expr)));
                    }
                    else if(operation == STORE_VARC || operation == STORE_VARC_STRING) {
                        int id = intInstr.asInt();
                        boolean isString = operation == STORE_VARC_STRING;
                        CS2Expression expression = cast(stack.pop(isString ? 1 : 0), isString ? CS2Type.STRING : CS2Type.INT);
                        block.write(new CS2StoreVarc(id, expression));
                    }
                    else if(operation == LOAD_VARPBIT || operation == LOAD_VARC || operation == LOAD_VARC_STRING) {
                        int id = intInstr.asInt();
                        boolean isString = operation == LOAD_VARC_STRING;
                        stack.push(new CS2LoadConfig(id, operation.name().toLowerCase()), isString ? 1 : 0);
                    }
                    else if (operation == CS2Instruction.MERGE_STRINGS) {
                        int amount = intInstr.asInt();
                        CS2Expression[] exprs = new CS2Expression[amount];
                        for (int i = amount - 1; i >= 0; i--)
                            exprs[i] = cast(stack.pop(1),CS2Type.STRING);
                        stack.push(new CS2StringBuilder(exprs), 1);
                    }
                    else if (operation == CS2Instruction.POP_INT || operation == CS2Instruction.POP_STRING || operation == CS2Instruction.POP_LONG) {
                        CS2Expression expr = stack.pop(operation == CS2Instruction.POP_LONG ? 2 : (operation == CS2Instruction.POP_STRING ? 1 : 0));
                        this.dumpStack(block, stack);
                        block.write(new CS2Poppable(expr));
                    }
                    else if (operation == CS2Instruction.CALL_CS2) {
                        FunctionInfo info = CS2Editor.getInstance().getScriptsDB().getInfo(intInstr.asInt());
                        if (info == null)
                            throw new DecompilerException("No documentation for:" + instruction);
                        int ret = this.analyzeCall(info, block, stack, ptr);
                        if (ret != -1)
                            ptr = ret;
                    }
                    else if (operation == CS2Instruction.ARRAY_NEW) {
                        int arrayID = intInstr.asInt() >> 16;
                        char type = (char)(intInstr.asInt() & 0xFFFF);
                        CS2Type array = CS2Type.forJagexChar(type).getArrayType();
                        CS2Expression length = cast(stack.pop(0),CS2Type.INT);
                        this.dumpStack(block, stack);
                        block.write(new CS2Poppable(new CS2StoreNamedData("globalarray_" + arrayID, new CS2NewArray(length,array))));
                    }
                    else if (operation == CS2Instruction.ARRAY_LOAD) {
                        stack.push(new CS2LoadArray(new CS2LoadNamedData("globalarray_" + intInstr.asInt(),CS2Type.INT.getArrayType()),cast(stack.pop(0),CS2Type.INT)), 0);
                    }
                    else if (operation == CS2Instruction.ARRAY_STORE) {
                        int arrayID = intInstr.asInt() >> 16;
                        CS2Expression value = cast(stack.pop(0), CS2Type.INT);
                        CS2Expression index = cast(stack.pop(0), CS2Type.INT);
                        this.dumpStack(block, stack);
                        block.write(new CS2Poppable(new CS2StoreArray(new CS2LoadNamedData("globalarray_" + arrayID,CS2Type.INT.getArrayType()),index,value)));
                    }
                    else {
                        throw new DecompilerException("Unknown opcode: " + opcode+" "+operation);
                    }
                }
                else if (instruction instanceof PrimitiveInstruction && ((PrimitiveInstruction) instruction).getType() == CS2Type.BOOLEAN) {
                    FunctionInfo info = CS2Editor.getInstance().getOpcodesDB().getInfo(instruction.getOpcode());
                    if (info == null)
                        throw new DecompilerException("No documentation for:" + instruction);
                    if (instruction.getOpcode() >= 20000 && instruction.getOpcode() < 30000)
                        info = this.analyzeDelegate(info, block, stack.copy());
                    else if (instruction.getOpcode() >= 30000 && instruction.getOpcode() < 40000)
                        info = this.analyzeSpecialCall(instruction, info, block, stack.copy());
                    else if (instruction.getOpcode() >= 40000)
                        throw new DecompilerException("A call to disabled function: " + info);
                    int ret = this.analyzeCall(info, block, stack, ptr);
                    if (ret != -1)
                        ptr = ret;
                }
                else if (instruction instanceof SwitchInstruction) {
                    SwitchInstruction sw = (SwitchInstruction)instruction;
                    CS2Expression value = cast(stack.pop(0), CS2Type.INT);
                    this.dumpStack(block, stack);
                    int[] cases = sw.getCases();
                    CS2FlowBlock[] targets = new CS2FlowBlock[cases.length];
                    for (int i = 0; i < targets.length; i++) {
                        targets[i] = generateFlowBlock(sw.getTargets()[i], stack.copy());
                        targets[i].getPredecessors().add(block);
                        block.getSuccessors().add(targets[i]);
                    }

                    block.write(new CS2SwitchFlowBlockJump(value, cases, targets, sw.getDefaultIndex()));
                    break;
                }
                else
                    throw new DecompilerException("Error:Unknown instruction type:" + instruction.getClass().getName());
            }
        }
        catch (DecompilerException ex) {
            this.dumpStack(block, stack);
            block.write(new CS2Comment("AT " + script.getInstructions()[ptr] + "\n" + IOUtils.getStackTrace(ex), CS2Comment.STANDART_STYLE));
        }
        catch (RuntimeException ex) {
            this.dumpStack(block, stack);
            block.write(new CS2Comment("AT " + script.getInstructions()[ptr] + "\n" + IOUtils.getStackTrace(ex), CS2Comment.STANDART_STYLE));
        }
    }


    private int analyzeCall(FunctionInfo info, CS2FlowBlock block, CS2Stack stack, int ptr) {
        CS2Type returnType = info.getReturnType();
        for (int i = 0; i < info.getArgumentTypes().length; i++)
            if (!info.getArgumentTypes()[i].usable() || info.getArgumentTypes()[i].structure() || info.getArgumentTypes()[i].totalSS() > 1)
                throw new DecompilerException(returnType + " is not supported in function arguments");

        if (!returnType.usable())
            throw new DecompilerException(returnType + " is not supported in function return type");

        if (returnType.totalSS() <= 1) {
            CS2Expression[] args = new CS2Expression[info.getArgumentTypes().length];
            for (int i = args.length - 1; i >= 0; i--) {
                CS2Type type = info.getArgumentTypes()[i];
                args[i] = cast(stack.pop(type.intSS() == 0 ? (type.longSS() != 0 ? 2 : 1) : 0),type);
            }

            if (returnType.totalSS() <= 0) { // void
                this.dumpStack(block, stack);
                block.write(new CS2Poppable(new CS2CallExpression(info,args)));
            }
            else { // standart
                int stackType = returnType.intSS() == 0 ? (returnType.longSS() != 0 ? 2 : 1) : 0;
                stack.push(new CS2CallExpression(info,args), stackType);
            }
            return -1;
        }
        else {
            this.dumpStack(block, stack);
            CS2Expression[] args = new CS2Expression[info.getArgumentTypes().length];
            for (int i = args.length - 1; i >= 0; i--) {
                CS2Type type = info.getArgumentTypes()[i];
                args[i] = cast(stack.pop(type.intSS() == 0 ? (type.longSS() != 0 ? 2 : 1) : 0),type);
            }

            CS2Expression expr = new CS2CallExpression(info,args);
            LocalVariable dump = new LocalVariable("dmp_" + counter++,expr.getType());
            function.getScope().declare(dump);
            block.write(new CS2Poppable(new CS2VariableAssign(dump,expr)));

            CS2VariableAssign[] assignations = new CS2VariableAssign[returnType.totalSS()];
            int intsLeft = returnType.intSS();
            int stringsLeft = returnType.stringSS();
            int longsLeft = returnType.longSS();

            int readptr;
            int write;
            for (write = 0, readptr = ptr + 1; (intsLeft + stringsLeft + longsLeft) > 0; readptr++) {
                if (readptr >= script.getInstructions().length || !(script.getInstructions()[readptr] instanceof PrimitiveInstruction) || ((PrimitiveInstruction) script.getInstructions()[readptr]).getType() != CS2Type.INT)
                    break;
                PrimitiveInstruction intInstr = (PrimitiveInstruction)script.getInstructions()[readptr];
                if (script.getOperations()[readptr] == CS2Instruction.STORE_INT) {
                    if (intsLeft <= 0)
                        break;
                    assignations[write++] = new CS2VariableAssign(function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.asInt(), 0)),
                            new CS2StructLoad("ip_" + (--intsLeft),CS2Type.INT,new CS2VariableLoad(dump)));
                }
                else if (script.getOperations()[readptr] == CS2Instruction.STORE_STRING) {
                    if (stringsLeft <= 0)
                        break;
                    assignations[write++] = new CS2VariableAssign(function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.asInt(), 1)),
                            new CS2StructLoad("sp_" + (--stringsLeft),CS2Type.STRING,new CS2VariableLoad(dump)));
                }
                else if (script.getOperations()[readptr] == CS2Instruction.STORE_LONG) {
                    if (longsLeft <= 0)
                        break;
                    assignations[write++] = new CS2VariableAssign(function.getScope().getLocalVariable(LocalVariable.makeIdentifier(intInstr.asInt(), 2)),
                            new CS2StructLoad("lp_" + (--longsLeft),CS2Type.LONG,new CS2VariableLoad(dump)));
                }
                if ((intsLeft + stringsLeft + longsLeft) <= 0)
                    break;
            }

            if ((intsLeft + stringsLeft + longsLeft) <= 0) {
                for (int i = 0; i < assignations.length; i++)
                    block.write(new CS2Poppable(assignations[i]));
                return readptr;
            }
            else {
                for (int i = 0; i < expr.getType().intSS(); i++) {
                    stack.push(new CS2StructLoad("ip_" + i,CS2Type.INT,new CS2VariableLoad(dump)), 0);
                }
                for (int i = 0; i < expr.getType().stringSS(); i++) {
                    stack.push(new CS2StructLoad("sp_" + i,CS2Type.STRING,new CS2VariableLoad(dump)), 1);
                }
                for (int i = 0; i < expr.getType().longSS(); i++) {
                    stack.push(new CS2StructLoad("lp_" + i,CS2Type.LONG,new CS2VariableLoad(dump)), 2);
                }
                return -1;
            }
        }
    }

    private FunctionInfo analyzeDelegate(FunctionInfo info, CS2FlowBlock block, CS2Stack stack) {
        String name = info.getName();
        CS2Type[] argsBuff = new CS2Type[100];
        int argsCount = 0;

        if (info.getArgumentTypes().length > 1) {
            stack.pop(0);
            argsBuff[argsCount++] = CS2Type.INTERFACE;
        }

        CS2Expression stringExpr = stack.pop(1);
        argsBuff[argsCount++] = CS2Type.STRING;
        if (!(stringExpr instanceof CS2PrimitiveExpression) || stringExpr.getType() != CS2Type.STRING)
            throw new DecompilerException("Dynamic delegate - impossible to decompile.");
        String descriptor = ((CS2PrimitiveExpression)stringExpr).asString();
        if (descriptor.length() > 0 && descriptor.charAt(descriptor.length() - 1) == 'Y') {
            CS2Expression length = stack.pop(0);
            if (!(length instanceof CS2PrimitiveExpression) || length.getType() != CS2Type.INT)
                throw new DecompilerException("Dynamic delegate - impossible to decompile.");
            int len = ((CS2PrimitiveExpression)length).asInt();
            argsBuff[argsCount++] = CS2Type.INT;
            while (len-- > 0) {
                stack.pop(0);
                argsBuff[argsCount++] = CS2Type.INT;
            }
            descriptor = descriptor.substring(0, descriptor.length() - 1);
        }
        for (int argument = descriptor.length() - 1; argument >= 0; argument--) {
            CS2Type type = CS2Type.forJagexChar(descriptor.charAt(argument));
            CS2Type basic = CS2Type.INT;
            if (descriptor.charAt(argument) == 's')
                basic = CS2Type.STRING;
            else if (descriptor.charAt(argument) == 'l')
                basic = CS2Type.LONG;

            if (basicType(type) != basic)
                type = basic;

            stack.pop(basic == CS2Type.LONG ? 2 : (basic == CS2Type.STRING ? 1 : 0));
            argsBuff[argsCount++] = type;
        }
        stack.pop(0);
        argsBuff[argsCount++] = CS2Type.FUNCTION;

        CS2Type[] args = new CS2Type[argsCount];
        String[] names = new String[argsCount];
        int write = args.length - 1;
        for (int i = 0; i < argsCount; i++) {
            args[write--] = argsBuff[i];
            names[i] = "arg" + i;
        }
        return new FunctionInfo(name, args, CS2Type.VOID, names);

    }

    private int[] paramtypes = { 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 105, 0, 0, 105, 0, 0, 0, 0, 0, 0, 121, 0, 105, 0, 105, 105, 0, 0, 105, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 103, 0, 0, 0, 105, 0, 0, 105, 0, 0, 0, 0, 0, 110, 111, 0, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 171, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 100, 105, 105, 100, 100, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 73, 73, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 105, 111, 105, 111, 105, 111, 105, 111, 105, 111, 105, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 103, 111, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 105, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 105, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 115, 115, 111, 111, 111, 111, 0, 171, 0, 105, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 74, 109, 65, 105, 105, 105, 105, 105, 111, 111, 111, 111, 111, 0, 0, 100, 115, 105, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 8364, 105, 100, 100, 100, 100, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 105, 105, 115, 100, 105, 105, 0, 0, 0, 0, 73, 73, 74, 103, 103, 103, 103, 105, 105, 105, 0, 0, 105, 103, 105, 105, 74, 105, 105, 105, 115, 110, 0, 105, 105, 99, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 108, 108, 0, 0, 0, 0, 0, 115, 115, 115, 115, 115, 0, 0, 0, 0, 171, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 100, 100, 105, 115, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 103, 0, 0, 0, 0, 0, 75, 105, 75, 75, 115, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 0, 105, 105, 0, 0, 0, 0, 111, 0, 111, 111, 0, 0, 0, 0, 0, 0, 105, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 105, 105, 115, 99, 99, 99, 99, 99, 105, 105, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 83, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 74, 115, 115, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 109, 105, 109, 65, 105, 115, 109, 105, 65, 0, 0, 0, 115, 115, 115, 115, 100, 0, 105, 105, 110, 110, 105, 0, 0, 0, 0, 105, 0, 105, 0, 105, 105, 105, 0, 0, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 105, 105, 83, 105, 83, 105, 115, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 111, 49, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 115, 115, 105, 65, 105, 105, 105, 80, 105, 100, 105, 115, 115, 115, 100, 105, 105, 116, 64, 108, 108, 115, 100, 100, 118, 65, 65, 109, 65, 65, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 100, 75, 75, 75, 75, 75, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 115, 115, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 111, 111, 111, 111, 115, 115, 115, 105, 105, 105, 105, 100, 105, 115, 115, 115, 115, 115, 115, 115, 115, 115, 99, 99, 99, 99, 99, 99, 99, 99, 105, 115, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 115, 100, 0, 0, 0, 0, 0, 105, 105, 105, 0, 105, 111, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 105, 105, 105, 110, 115, 115, 105, 105, 105, 105, 100, 105, 105, 100, 105, 65, 74, 74, 74, 74, 0, 0, 105, 115, 115, 0, 0, 0, 0, 0, 105, 105, 105, 0, 0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 74, 74, 74, 100, 100, 100, 100, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 100, 100, 105, 105, 105, 105, 105, 115, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 105, 111, 111, 111, 111, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 100, 105, 0, 0, 0, 103, 103, 103, 0, 105, 105, 105, 105, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 74, 74, 74, 74, 74, 74, 74, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 100, 115, 105, 105, 105, 115, 105, 100, 105, 115, 105, 100, 105, 115, 105, 100, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 99, 99, 105, 105, 105, 99, 99, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 99, 105, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 105, 105, 99, 99, 105, 105, 99, 105, 105, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 103, 103, 103, 103, 103, 103, 103, 103, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 105, 105, 105, 99, 99, 99, 99, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 0, 105, 105, 0, 105, 105, 115, 115, 100, 105, 100, 172, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 115, 115, 105, 115, 115, 105, 115, 115, 105, 115, 115, 110, 110, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 105, 105, 49, 111, 105, 105, 111, 111, 111, 111, 0, 0, 0, 0, 74, 74, 74, 74, 74, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 115, 115, 105, 105, 108, 108, 108, 108, 108, 100, 0, 115, 115, 115, 49, 111, 111, 111, 111, 105, 115, 115, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 0, 0, 0, 115, 105, 105, 65, 105, 116, 100, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 171, 171, 171, 171, 171, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 105, 111, 100, 105, 115, 49, 49, 49, 105, 105, 105, 105, 105, 115, 105, 105, 105, 105, 105, 99, 105, 105, 0, 105, 105, 105, 105, 105, 0, 111, 111, 111, 111, 111, 111, 0, 0, 0, 0, 115, 115, 115, 115, 115, 115, 115, 115, 115, 115, 0, 0, 105, 111, 105, 105, 105, 105, 105, 105, 111, 83, 105, 105, 111, 111, 111, 111, 111, 111, 74, 111, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 108, 108, 108, 105, 105, 105, 105, 115, 105, 105, 105, 115, 115, 105, 115, 115, 115, 0, 115, 115, 105, 105, 115, 111, 111, 109, 105, 105, 105, 105, 105, 105, 105, 105, 115, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 0, 74, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 115, 0, 115, 105, 105, 105, 105, 105, 105, 105, 105, 115, 100, 65, 65, 109, 65, 109, 105, 105, 103, 105, 0, 0, 105, 49, 115, 105, 0, 115, 0, 0, 0, 0, 0, 100, 105, 115, 115, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 109, 109, 109, 109, 109, 109, 109, 109, 109, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 105, 105, 115, 0, 0, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 111, 111, 111, 105, 105, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 105, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 105, 105, 105, 105, 99, 105, 105, 105, 103, 74, 111, 105, 111, 105, 111, 105, 111, 105, 111, 105, 111, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 105, 115, 115, 105, 105, 105, 105, 105, 105, 100, 64, 105, 105, 105, 105, 115, 105, 0, 105, 105, 105, 105, 105, 74, 103, 0, 105, 109, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 65, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 65, 103, 65, 65, 65, 65, 116, 103, 171, 171, 171, 171, 171, 171, 171, 171, 171, 171, 80, 116, 80, 171, 171, 171, 171, 171, 116, 99, 105, 105, 105, 105, 105, 105, 0, 0, 0, 105, 105, 105, 8364, 8364, 0, 0, 0, 0, 0, 105, 74, 105, 74, 105, 74, 105, 74, 0, 105, 0, 105, 0, 0, 105, 105, 74, 105, 0, 105, 111, 111, 111, 111, 105, 115, 105, 116, 111, 74, 74, 74, 105, 105, 105, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 49, 100, 103, 105, 105, 49, 0, 115, 115, 115, 115, 115, 164, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 109, 109, 115, 105, 105, 100, 105, 105, 105, 105, 105, 105, 105, 105, 105, 115, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 105, 0, 0, 0, 115, 105, 105, 105, 105, 105, 105, 115, 100, 105, 105, 105, 115, 108, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 109, 110, 109, 110, 109, 110, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 74, 105, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 49, 111, 105, 105, 105, 105, 105, 105, 105, 105, 105, 116, 105, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 105, 105, 100, 115, 115, 115, 115, 115, 115, 115, 105, 65, 116, 116, 105, 115, 74, 0, 0, 99, 99, 115, 105, 111, 100, 105, 105, 0, 116, 0, 105, 0, 105, 116, 105, 0, 0, 105, 105, 105, 105, 105, 105, 105, 99, 99, 105, 105, 105, 103, 99, 110, 105, 74, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 0, 65, 105, 105, 105, 111, 105, 105, 105, 0, 105, 0, 0, 0, 0, 105, 0, 105, 0, 0, 0, 111, 111, 111, 111, 111, 111, 105, 115, 105, 105, 105, 105, 105, 0, 0, 0, 0, 100, 100, 111, 115, 115, 115, 115, 115, 105, 105, 105, 105, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 74, 115, 83, 105, 100, 105, 76, 100, 100, 100, 74, 74, 74, 74, 74, 74, 74, 115, 76, 105, 105, 105, 105, 76, 105, 105, 105, 105, 76, 105, 105, 105, 105, 76, 105, 105, 105, 105, 76, 105, 105, 105, 105, 103, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 115, 74, 100, 100, 100, 74, 105, 105, 105, 105, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 74, 49, 105, 49, 49, 49, 49, 49, 49, 105, 49, 49, 49, 49, 105, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 49, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 0, 105, 105, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 65, 103, 105, 0, 105, 105, 107, 101, 49, 0, 105, 105, 0, 0, 0, 115, 105, 111, 100, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, 105, 100, 100, 100, 100, 0, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 65, 65, 109, 65, 109, 105, 111, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 111, 105, 74, 74, 105, 105, 100, 100, 100, 100, 100, 100, 100, 100, 100, 105, 105, 105, 102, 105, 105, 0, 105, 115, 105, 0, 118, 73, 49, 103, 105, 102, 115, 115, 105, 115, 100, 111, 105, 100, 105, 115, 115, 105, 115, 110, 103, 103, 105, 105, 83, 83, 83, 83, 83, 105, 105, 105, 105, 105, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 115, 105, 105, 105, 105, 99, 115, 115, 105, 99, 99, 99, 99, 99, 99, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 100, 100, 100, 100, 100, 100, 100, 100, 100, 115, 115, 115, 115, 115, 115, 115, 100, 100, 100, 100, 100, 100, 105, 105, 0, 0, 0, 105, 105, 0, 0, 0, 0, 99, 0, 0, 0, 0, 111, 111, 111, 111, 111, 111, 105, 115, 105, 105, 105, 105, 105, 0, 0, 0, 0, 100, 100, 111, 115, 115, 105, 105, 105, 105, 115, 105, 105, 105, 105, 115, 105, 105, 105, 105, 115, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 115, 0, 115, 0, 115, 105, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 115, 105, 111, 0, 0, 105, 0, 0, 0, 111, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 100, 100, 105, 105, 105, 100, 100, 100, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 105, 105, 105, 100, 100, 105, 105, 115, 105, 115, 105, 74, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 111, 0, 105, 105, 105, 105, 105, 105, 0, 0, 111, 115, 0, 105, 105, 115, 49, 49, 0, 0, 0, 0, 110, 49, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 110, 105, 103, 103, 100, 115, 105, 49, 105, 100, 100, 100, 105, 105, 105, 102, 105, 74, 105, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 105, 105, 74, 105, 99, 115, 105, 0, 0, 105, 100, 105, 105, 105, 105, 100, 100, 73, 73, 73, 73, 73, 73, 73, 105, 0, 0, 0, 0, 65, 65, 65, 116, 116, 116, 116, 116, 0, 0, 116, 116, 116, 105, 105, 115, 115, 0, 0, 0, 0, 115, 105, 0, 115, 105, 105, 105, 105, 102, 105, 105, 105, 115, 115, 100, 100, 105, 105, 105, 105, 65, 116, 65, 116, 116, 105, 0, 0, 0, 103, 83, 83, 83, 83, 105, 105, 105, 105, 111, 111, 111, 111, 105, 105, 105, 105, 105, 105, 83, 103, 105, 105, 0, 105, 105, 0, 0, 74, 105, 100, 105, 115, 105, 115, 115, 99, 115, 105, 115, 115, 65, 0, 105, 105, 116, 8364, 105, 105, 105, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 0, 105, 0, 0, 65, 109, 65, 109, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 171, 171, 115, 115, 115, 115, 105, 0, 105, 105, 105, 105, 105, 115, 115, 111, 111, 105, 111, 105, 116, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8364, 8364, 65, 65, 65, 65, 65, 65, 116, 0, 103, 100, 100, 100, 105, 74, 74, 74, 74, 74, 74, 74, 74, 105, 105, 100, 100, 100, 49, 115, 115, 74, 115, 100, 115, 115, 74, 105, 105, 105, 105, 105, 74, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 100, 105, 105, 0, 100, 100, 100, 100, 100, 100, 100, 100, 105, 105, 105, 105, 105, 105, 105, 105, 100, 100, 100, 100, 73, 115, 0, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 73, 111, 0, 0, 83, 105, 111, 0, 0, 105, 105, 49, 105, 105, 105, 105, 105, 115, 99, 49, 103, 103, 103, 49, 49, 49, 49, 49, 73, 103, 105, 105, 105, 105, 49, 105, 105, 102, 105, 105, 105, 105, 105, 105, 102, 105, 105, 74, 102, 105, 105, 115, 105, 111, 111, 105, 105, 74, 74, 74, 0, 0, 0, 105, 105, 0, 0, 0, 0, 0, 0, 0, 74, 0, 105, 105, 0, 0, 105, 0, 0, 116, 116, 116, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 0, 0, 115, 115, 105, 105, 115, 103, 0, 0, 105, 0, 105, 0, 0, 103, 105, 115, 105, 105, 105, 110, 110, 115, 115, 115, 103, 74, 74, 74, 74, 115, 105, 115, 0, 105, 99, 105, 105, 105, 49, 99, 99, 105, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 100, 100, 100, 100, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 49, 105, 105, 105, 105, 105, 105, 105, 105, 0, 105, 0, 0, 105, 105, 0, 0, 111, 105, 111, 105, 0, 111, 0, 0, 111, 99, 99, 110, 121, 111, 111, 105, 0, 0, 0, 105, 110, 109, 8364, 105, 0, 0, 0, 0, 0, 0, 0, 0, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 105, 0, 0, 103, 110, 105, 115, 105, 115, 65, 65, 65, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 74, 0, 0, 105, 115, 105, 105, 115, 115, 115, 115, 115, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 105, 105, 105, 105, 105, 105, 0, 0, 0, 105, 74, 105, 0, 74, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 49, 0, 0, 0, 0, 0, 0, 105, 0, 111, 105, 0, 105, 105, 111, 105, 74, 105, 103, 115, 115, 110, 65, 65, 109, 0, 0, 105, 0, 103, 115, 115, 115, 115, 115, 115, 115, 105, 111, 100, 105, 105, 105, 105, 105, 115, 115, 105, 105, 105, 0, 0, 0, 111, 105, 105, 99, 99, 105, 111, 105, 111, 105, 111, 105, 105, 105, 0, 105, 105, 0, 115, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 0, 0, 0, 105, 115, 105, 105, 105, 105, 105, 105, 105, 105, 49, 115, 115, 105, 49, 115, 105, 115, 115, 115, 115, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 0, 105, 115, 115, 105, 105, 105, 105, 115, 83, 83, 83, 83, 83, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 103, 115, 115, 115, 115, 115, 115, 115, 103, 103, 103, 103, 105, 105, 105, 105, 65, 65, 65, 115, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 105, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 73, 105, 105, 105, 100, 115, 105, 115, 105, 111, 105, 105, 115, 111, 111, 111, 111, 111, 111, 83, 105, 105, 105, 0, 0, 105, 105, 105, 105, 74, 105, 105, 105, 105, 105, 105, 115, 115, 100, 100, 105, 105, 0, 0, 115, 111, 105, 105, 115, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 0, 105, 105, 115, 115, 115, 105, 105, 115, 100, 0, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 103, 111, 0, 0, 105, 49, 103, 103, 105, 0, 105, 0, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 105, 111, 105, 0, 0, 0, 0, 0, 74, 105, 0, 0, 0, 100, 0, 74, 0, 105, 111, 105, 111, 105, 111, 105, 111, 105, 0, 115, 105, 76, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 110, 105, 108, 105, 111, 105, 65, 0, 0, 0, 0, 0, 0, 105, 105, 105, 105, 105, 105, 105, 105, 111, 105, 105, 105, 115, 105, 115, 105, 105, 105, 0, 0, 0, 0, 105, 105, 115, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 111, 115, 105, 105, 105, 115, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 0, 0, 0, 0, 0, 105, 105, 49, 49, 105, 0, 0, 65, 0, 0, 171, 105, 0, 0, 105, 105, 49, 0, 105, 0, 103, 103, 103, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, 0, 105, 0, 115, 105, 105, 105, 105, 105, 111, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 111, 111, 111, 111, 115, 115, 115, 115, 100, 115, 115, 115, 105, 105, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 103, 105, 115, 82, 73, 0, 0, 0, 0, 0, 99, 99, 99, 105, 108, 105, 0, 105, 105, 105, 105, 105, 105, 105, 0, 105, 115, 105, 105, 208, 105, 105, 0, 0, 105, 105, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 49, 49, 49, 49, 49, 111, 111, 111, 111, 111, 105, 105, 105, 105, 105, 100, 0, 100, 0, 0, 0, 0, 105, 105, 0, 0, 0, 105, 0, 111, 111, 111, 111, 111, 83, 105, 83, 105, 83, 105, 0, 0, 0, 0, 0, 0, 0, 0, 105, 0, 0, 111, 105, 208, 208, 105, 105, 105, 105, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 108, 115, 115, 109, 111, 105, 105, 0, 0, 0, 105, 105, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 111, 0, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 105, 105, 0, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 73, 105, 105, 105, 105, 115, 0, 105 };
    //private int[][][] db1 = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, { 17, 0, }, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, { 0, }, null, null, }, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, null, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, null, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, null, { { 0, }, { 36, }, null, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, }, { { 0, }, { 36, }, { 23, }, { 36, }, null, { 0, 0, 0, }, }, null, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, { 0, }, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, null, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, { 0, }, null, null, null, null, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, { 0, }, null, null, null, null, } };
    private int[][][] db2 = { null, null, null, null, { { 0, }, { 36, }, { 36, }, { 36, }, { 23, }, { 57, }, { 0, }, { 0, }, null, { 0, }, }, { { 0, }, { 36, }, { 36, }, { 23, }, { 0, }, null, { 0, }, { 0, }, { 0, }, { 17, 0, }, { 0, }, { 0, }, { 0, }, }, null, null, { { 0, }, { 36, }, { 23, }, { 36, }, { 36, }, { 0, 0, 0, }, } };

    private FunctionInfo analyzeSpecialCall(Instruction instruction, FunctionInfo info, CS2FlowBlock block, CS2Stack stack) {
        if (instruction.getName().equals("_db_getfield")) {
            CS2Expression[] args = new CS2Expression[3];
            for (int i = 2; i >= 0; i--)
                args[i] = stack.pop(0);

            if (!(args[1] instanceof CS2PrimitiveExpression) || args[1].getType() == CS2Type.INT)
                throw new DecompilerException("Dynamic type");

            int target = ((CS2PrimitiveExpression)args[1]).asInt();

            int t1 = target >>> 8;
            int t2 = target & 0xFF;

            if (t1 < 0 || t1 >= db2.length || db2[t1] == null)
                throw new DecompilerException("Invalid type");

            if (t2 < 0 || t2 >= db2[t1].length || db2[t1][t2] == null)
                throw new DecompilerException("Invalid type");

            CS2Type[] rtypes = new CS2Type[db2[t1][t2].length];
            for (int i = 0; i < rtypes.length; i++)
                rtypes[i] = CS2Type.forJagexId(db2[t1][t2][i]);

            if (rtypes.length == 0)
                return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.VOID, info.getArgumentNames());
            else if (rtypes.length == 1)
                return new FunctionInfo(info.getName(), info.getArgumentTypes(), rtypes[0], info.getArgumentNames());
            else {
                String s = "(";
                for (int i = 0; i < rtypes.length; i++)
                    s += rtypes[i].toString() + ((i+1)<rtypes.length?";":"");
                s += ")";
                return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.forDesc("dbfield" + s), info.getArgumentNames());
            }
        }
        else if (instruction.getName().equals("_enum")) { // enum
            CS2Expression[] args = new CS2Expression[4];
            for (int i = 3; i >= 0; i--)
                args[i] = stack.pop(0);
            if (!(args[0] instanceof CS2PrimitiveExpression) || !(args[1] instanceof CS2PrimitiveExpression)
                || args[0].getType() != CS2Type.INT || args[1].getType() != CS2Type.INT)
                throw new DecompilerException("Dynamic type");
            CS2Type[] atypes = Arrays.copyOf(info.getArgumentTypes(), info.getArgumentTypes().length);
            atypes[3] = CS2Type.forJagexId(((CS2PrimitiveExpression)args[0]).asInt());
            return new FunctionInfo(info.getName(), atypes, CS2Type.forJagexId(((CS2PrimitiveExpression)args[1]).asInt()), info.getArgumentNames());
        }
        else if (instruction.getName().equals("_random_sound_pitch")) { // random_pitch_sound
            CS2Expression[] args = new CS2Expression[2];
            for (int i = 1; i >= 0; i--)
                args[i] = stack.pop(0);
            if (!(args[0] instanceof CS2PrimitiveExpression) || !(args[1] instanceof CS2PrimitiveExpression)
                || args[0].getType() != CS2Type.INT || args[1].getType() != CS2Type.INT)
                throw new DecompilerException("Dynamic type");
            if (((CS2PrimitiveExpression)args[0]).asInt() > 700 || ((CS2PrimitiveExpression)args[1]).asInt() > 700)
                return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.forDesc("random_pitch_sound(2,0,0)"), info.getArgumentNames());
            else
                return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.INT, info.getArgumentNames());
        }
        else if (instruction.getName().contains("_param")) {
            CS2Expression arg = stack.pop(0);
            if (!(arg instanceof CS2PrimitiveExpression) || arg.getType() != CS2Type.INT)
                throw new DecompilerException("Dynamic type");

            int paramId = ((CS2PrimitiveExpression)arg).asInt();
            if (paramId < 0 || paramId >= paramtypes.length)
                throw new DecompilerException("unknown param id: " + paramId);

            return new FunctionInfo(info.getName(), info.getArgumentTypes(), CS2Type.forJagexChar((char)paramtypes[paramId]), info.getArgumentNames());
        }
        else if (instruction.getName().equals("_runjavascript")) {
            // TODO we don't know the return type though
            CS2Type rtype = CS2Type.VOID;
            CS2Type[] argtypes = new CS2Type[stack.getSize()];
            String[] argnames = new String[stack.getSize()];
            for (int i = argtypes.length - 1; i >= 0; i--) {
                argtypes[i] = stack.pop().getType();
                argnames[i] = "arg" + i;
            }

            return new FunctionInfo(info.getName(), argtypes, rtype, argnames);
        }
        else
            throw new DecompilerException("TODO unimplemented special instruction: " + instruction.getOpcode() + ", name=" + instruction.getName());
    }


    private void endGeneration() {
        List<CS2FlowBlock> validBlocks = new ArrayList<>();
        for (int i = 0; i < blocks.length; i++)
            if (blocks[i] != null)
                validBlocks.add(blocks[i]);
        blocks = new CS2FlowBlock[validBlocks.size()];
        int write = 0;
        for (CS2FlowBlock block : validBlocks)
            blocks[write++] = block;
        for (int i = 1; i < blocks.length; i++) {
            blocks[i - 1].setNext(blocks[i]);
            blocks[i].setPrev(blocks[i - 1]);
        }
    }




    private CS2FlowBlock generateFlowBlock(LabelInstruction label,CS2Stack variableStack) {
        int blockID = label.getLabelId() + 1;
        if (blocks[blockID] == null)
            return (blocks[blockID] = new CS2FlowBlock(blockID, label.getAddress() + 1,variableStack));
        if (!checkMerging(blocks[blockID].getStack(),variableStack))
            throw new DecompilerException("Can't merge two stacks (Code is invalid).");
        return blocks[blockID];
    }


    /**
     * Dump's stack contents to local variables.
     * Write's assignation expressions on specific block.
     */
    private void dumpStack(CS2FlowBlock block, CS2Stack stack) {
        ArrayQueue<CS2Expression> s = new ArrayQueue<CS2Expression>(stack.getSize());
        while (stack.getSize() > 0)
            s.insert(stack.pop());
        int ic = 0,sc = 0,lc = 0;
        while (s.size() > 0) {
            CS2Expression value = s.take();
            int stackType = value.getType().intSS() == 0 ? (value.getType().longSS() != 0 ? 2 : 1) : 0;
            int identifier = LocalVariable.makeStackDumpIdentifier(stackType != 0 ? (stackType == 2 ? lc : sc) : ic, stackType);
            LocalVariable variable;
            if (function.getScope().isDeclared(identifier)) {
                variable = function.getScope().getLocalVariable(identifier);
            }
            else {
                variable = new LocalVariable("stack_dump" + counter++, defaultType(stackType));
                variable.setIdentifier(identifier);
                function.getScope().declare(variable);
            }
            block.write(new CS2Poppable(new CS2VariableAssign(variable,cast(value,variable.getType()))));
            stack.push(new CS2VariableLoad(variable), stackType);
            if (stackType == 0)
                ic++;
            else if (stackType == 1)
                sc++;
            else
                lc++;
        }
    }

    /**
     * Casts expression node to specific type.
     * If expression type is same then returned value is expr,
     * otherwise on most cases CastCS2Expression is returned with one child
     * which is expr.
     */
    private CS2Expression cast(CS2Expression expr, CS2Type type) {
        if (type == CS2Type.FUNCTION) {
            CS2Function n = null;
            if (expr instanceof CS2PrimitiveExpression && expr.getType() == CS2Type.INT) {
                CS2PrimitiveExpression iexpr = (CS2PrimitiveExpression) expr;
                try {
                    if (iexpr.asInt() != -1) {
                        CS2Script script = CS2Definitions.getScript(iexpr.asInt());
                        n = script.decompile();
                    } else
                        n = new CS2Function(-1, "none", new CS2Type[0], new String[0], CS2Type.VOID);
                }
                catch (DecompilerException ex) {
                }
            }
            return new CS2FunctionExpression(expr, n);
        }

        return new CS2Cast(type, expr);
    }


    /**
     * Check's if two stacks can be merged.
     * false is returned if stack sizes doesn't match or
     * one of the elements in the first or second stack is not dumped
     * to same local variables.
     */
    private boolean checkMerging(CS2Stack v0, CS2Stack v1) {
        if (v0.getSize() != v1.getSize()) {
            System.out.println("Invalid size1");
            return false;
        }
        for (int i = 0; i < 3; i++)
            if (v0.getSize(i) != v1.getSize(i)) {
                System.out.println("Invalid size2");
                return false;
            }
        CS2Stack c0 = v0.copy();
        CS2Stack c1 = v1.copy();
        for (int i = 0; i < 3; i++) {
            while (c0.getSize(i) > 0) {
                CS2Expression e0 = c0.pop(i);
                CS2Expression e1 = c1.pop(i);
                if (!(e0 instanceof CS2VariableLoad) || !(e1 instanceof CS2VariableLoad)) {
                    System.out.println("Invalid CS2VariableLoad");
                    return false;
                }
                if (((CS2VariableLoad)e0).getVariable() != ((CS2VariableLoad)e1).getVariable()) {
                    System.out.println("Invalid variable matching");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Find's default CS2Type for given stack type.
     */
    private CS2Type defaultType(int stackType) {
        if (stackType == 0)
            return CS2Type.INT;
        else if (stackType == 1)
            return CS2Type.STRING;
        else if (stackType == 2)
            return CS2Type.LONG;
        throw new DecompilerException("Wrong stack type.");
    }

    /**
     * Find's basic CS2Type for given advanced type.
     */
    private CS2Type basicType(CS2Type advanced) {
        if ((advanced.intSS() + advanced.stringSS() + advanced.longSS()) == 1)
            return advanced.intSS() == 1 ? CS2Type.INT : (advanced.stringSS() == 1 ? CS2Type.STRING : CS2Type.LONG);
        throw new DecompilerException("Wrong advanced type.");
    }
}
