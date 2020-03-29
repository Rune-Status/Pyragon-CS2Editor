package com.cryo.cs2.flow;

import com.cryo.cs2.nodes.*;
import com.cryo.cs2.CS2Type;
import com.cryo.utils.DecompilerException;
import com.cryo.utils.DecompilerUtils;

import java.util.ArrayList;
import java.util.List;

public class FlowBlocksSolver {

	private CS2Scope scope;
	private CS2FlowBlock[] blocks;
	
	public FlowBlocksSolver(CS2Scope scope, CS2FlowBlock[] blocks) {
		this.scope = scope;
		this.blocks = blocks;
	}


	public void solve() throws DecompilerException {
		
		if (true) {
			int total = 0;
			
			do {
				total = 0;
				total += doStandartIfConditionsMergeCheck();
				total += doConnectionCheck();
			}
			while (total > 0);
				
			do {
				total = 0;
				total += doStandartIfCheck();
				total += doStandartIfElseCheck();
				total += doStandartLoopsCheck();
				total += doStandartFlowControlsCheck();
				total += doStandartSwitchesCheck();
				total += doConnectionCheck();
			}
			while (total > 0);
			
			do {
				total = 0;
				total += doUnexpectedGotosResolving();
				total += doConnectionCheck();
			}
			while (total > 0);
			
		}
		
		List<CS2FlowBlock> blocks = listBlocks();
		for (CS2FlowBlock block : blocks) {
			if (block.getSuccessors().size() <= 0 && block.getPredecessors().size() <= 0) {
				List<CS2Node> childs = block.listChilds();
				for (CS2Node node : childs)
					this.scope.write(node);
				continue;
			}
			this.scope.write(block);
		}
	}
	
	private int doConnectionCheck() {
		int connected = 0;
		for (int a = 0; a < blocks.length; a++)
			for (int i = 0; i < blocks.length; i++)
				if (blocks[a] != null && blocks[i] != null && 
						blocks[a] != blocks[i] && canConnect(blocks[a],blocks[i])) {
					connected++;
					connect(blocks[a],blocks[i]);
				}
		if (connected > 0)
			System.err.println("Connected - " + connected);
		return connected;
	}
	
	private int doStandartIfCheck() {
		int solved = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartIfCheck(blocks[i]))
				solved++;
		if (solved > 0)
			System.err.println("Solved standart ifs - " + solved);
		return solved;
	}
	
	private boolean doStandartIfCheck(CS2FlowBlock block) {
		if (block.size() < 2)
			return false;
		/**
		 * What we solve here is check last two nodes
		 * for structure like this
		 * IF (condition)
		 * 		GOTO if_start_block
		 * GOTO if_end_block
		 * if_start_block:
		 * whatever here
		 * if_end_block:
		 */
		CS2Node v0 = block.read(block.size() - 2);
		CS2Node v1 = block.read(block.size() - 1);
		if (!(v0 instanceof CS2ConditionalFlowBlockJump) || !(v1 instanceof CS2UnconditionalFlowBlockJump))
			return false;
		CS2ConditionalFlowBlockJump condition = (CS2ConditionalFlowBlockJump)v0;
		CS2UnconditionalFlowBlockJump jumpOut = (CS2UnconditionalFlowBlockJump)v1;
		if (condition.getTarget().getBlockId() <= block.getBlockId() ||
				jumpOut.getTarget().getBlockId() <= condition.getTarget().getBlockId())
			return false;
		List<CS2FlowBlock> inJumps = new ArrayList<>();
		inJumps.add(block);
		List<CS2FlowBlock> outJumps = new ArrayList<>();
		if (!canCut(block,jumpOut.getTarget(),inJumps,outJumps))
			return false;
		CS2FlowBlock[] blocks = this.cut(block, jumpOut.getTarget());
		block.setCodeAddress(block.size() - 2);
		for (int i = 0; i < 2; i++)
			block.delete();
		block.getSuccessors().remove(condition.getTarget());
		block.getSuccessors().remove(jumpOut.getTarget());
		condition.getTarget().getPredecessors().remove(block);
		jumpOut.getTarget().getPredecessors().remove(block);
		CS2IfElse ifElse = new CS2IfElse(new CS2Expression[] { condition.getExpression() },new CS2Scope[] { new CS2Scope(this.scope) },new CS2Scope(this.scope));
		FlowBlocksSolver solver = new FlowBlocksSolver(ifElse.getScopes()[0], blocks);
		solver.solve();
		block.write(ifElse);
		return true;
	}
	
	private int doStandartIfElseCheck() {
		int solved = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartIfElseCheck(blocks[i]))
				solved++;
		if (solved > 0)
			System.err.println("Solved standart if else's - " + solved);
		return solved;
	}
	
	private boolean doStandartIfElseCheck(CS2FlowBlock block) {
		if (block.size() < 2)
			return false;

		CS2Node v0 = block.read(block.size() - 2);
		CS2Node v1 = block.read(block.size() - 1);
		if (!(v0 instanceof CS2ConditionalFlowBlockJump) || !(v1 instanceof CS2UnconditionalFlowBlockJump))
			return false;
		CS2ConditionalFlowBlockJump condition = (CS2ConditionalFlowBlockJump)v0;
		CS2UnconditionalFlowBlockJump jumpOut = (CS2UnconditionalFlowBlockJump)v1;
		if (block.getNext() != condition.getTarget() || 
				jumpOut.getTarget().getBlockId() <= condition.getTarget().getBlockId())
			return false;
		
		List<CS2FlowBlock> outJumps = getAllOutjumps(block, jumpOut.getTarget());
		if (outJumps.size() != 1)
			return false;
		CS2FlowBlock end = outJumps.get(0);
		if (!jumpOut.getTarget().getPrev().getSuccessors().contains(end))
			return false;
		if (jumpOut.getTarget().getPrev().size() < 1)
			return false;
		CS2Node v2 = jumpOut.getTarget().getPrev().read(jumpOut.getTarget().getPrev().size() - 1);
		if (!(v2 instanceof CS2UnconditionalFlowBlockJump))
			return false;
		CS2UnconditionalFlowBlockJump jumpEnd = (CS2UnconditionalFlowBlockJump) v2;
		if (jumpEnd.getTarget() != end)
			return false;
		//if (this.getFirstJumpingBlock(end) != jumpOut.getTarget().getPrev())
		//	return false;
		
		{
			List<CS2FlowBlock> allowedInJumpers = new ArrayList<>();
			allowedInJumpers.add(block);
			List<CS2FlowBlock> allowedOutJumps = new ArrayList<>();
			allowedOutJumps.add(end);
			if (!canCut(block, jumpOut.getTarget(), allowedInJumpers, allowedOutJumps))
				return false;
		}
		
		
		int bufferWrite = 0;
		CS2FlowBlock[] startBlocks = new CS2FlowBlock[100];
		CS2FlowBlock[] endBlocks = new CS2FlowBlock[100];
		CS2FlowBlock[] jumpEndBlocks = new CS2FlowBlock[100];
		CS2ConditionalFlowBlockJump[] jumpins = new CS2ConditionalFlowBlockJump[100];
		CS2UnconditionalFlowBlockJump[] jumpouts = new CS2UnconditionalFlowBlockJump[100];
		CS2UnconditionalFlowBlockJump[] jumpends = new CS2UnconditionalFlowBlockJump[100];
		
		startBlocks[bufferWrite] = block;
		endBlocks[bufferWrite] = jumpOut.getTarget();
		jumpEndBlocks[bufferWrite] = jumpOut.getTarget().getPrev();
		jumpins[bufferWrite] = condition;
		jumpouts[bufferWrite] = jumpOut;
		jumpends[bufferWrite++] = jumpEnd;
		
		while(true) {
			block = jumpOut.getTarget();
			if (block == end) 
				return false; // we reached end block?! wtf
			
			if (block.size() < 2)
				break; // we reached end, there's else block
			CS2Node v3 = block.read(block.size() - 2);
			CS2Node v4 = block.read(block.size() - 1);
			if (!(v3 instanceof CS2ConditionalFlowBlockJump) || !(v4 instanceof CS2UnconditionalFlowBlockJump))
				break; // we reached end, there's else block 
			condition = (CS2ConditionalFlowBlockJump)v3;
			jumpOut = (CS2UnconditionalFlowBlockJump)v4;
			if (block.getNext() != condition.getTarget() || 
					jumpOut.getTarget().getBlockId() <= condition.getTarget().getBlockId())
				return false;
			outJumps = getAllOutjumps(block, jumpOut.getTarget());
			if (outJumps.size() == 0) { // there's no else block
				if (jumpOut.getTarget() != end)
					return false;
				List<CS2FlowBlock> allowedInJumpers = new ArrayList<>();
				allowedInJumpers.add(block);
				List<CS2FlowBlock> allowedOutJumps = new ArrayList<>();
				if (!canCut(block, jumpOut.getTarget(), allowedInJumpers, allowedOutJumps))
					return false;
				startBlocks[bufferWrite] = block;
				endBlocks[bufferWrite] = jumpOut.getTarget();
				jumpins[bufferWrite] = condition;
				jumpouts[bufferWrite] = jumpOut;
			}
			else if (outJumps.size() == 1 && outJumps.get(0) == end) {
				CS2Node v5 = jumpOut.getTarget().getPrev().read(jumpOut.getTarget().getPrev().size() - 1);
				if (!(v5 instanceof CS2UnconditionalFlowBlockJump))
					return false;
				jumpEnd = (CS2UnconditionalFlowBlockJump) v5;
				if (jumpEnd.getTarget() != end)
					return false;
				List<CS2FlowBlock> allowedInJumpers = new ArrayList<>();
				allowedInJumpers.add(block);
				List<CS2FlowBlock> allowedOutJumps = new ArrayList<>();
				allowedOutJumps.add(end);
				if (!canCut(block, jumpOut.getTarget(), allowedInJumpers, allowedOutJumps))
					return false;			
				startBlocks[bufferWrite] = block;
				endBlocks[bufferWrite] = jumpOut.getTarget();
				jumpEndBlocks[bufferWrite] = jumpOut.getTarget().getPrev();
				jumpins[bufferWrite] = condition;
				jumpouts[bufferWrite] = jumpOut;
				jumpends[bufferWrite++] = jumpEnd;
			}
			else
				return false;
		}
		
		boolean hasElse = jumpouts[bufferWrite - 1].getTarget() != end;
		List<CS2FlowBlock> allowedElseInJumpers = new ArrayList<>();
		allowedElseInJumpers.add(startBlocks[bufferWrite - 1]); // last start block
		if (hasElse && !this.canCut(block.getPrev(), end, allowedElseInJumpers, new ArrayList<>()))
			return false;
		CS2Expression[] conditions = new CS2Expression[bufferWrite];
		CS2Scope[] scopes = new CS2Scope[bufferWrite];
		CS2Scope elseScope = new CS2Scope(this.scope);
		
		for (int i = 0; i < bufferWrite; i++) {
			CS2FlowBlock[] blocks = cut(startBlocks[i], endBlocks[i]);
			if (jumpEndBlocks[i] != null) {
				jumpEndBlocks[i].setCodeAddress(jumpEndBlocks[i].addressOf(jumpends[i]));
				jumpEndBlocks[i].delete();
				jumpEndBlocks[i].getSuccessors().remove(jumpends[i].getTarget());
				jumpends[i].getTarget().getPredecessors().remove(jumpEndBlocks[i]);
			}
			startBlocks[i].setCodeAddress(startBlocks[i].addressOf(jumpins[i]));
			startBlocks[i].delete();
			startBlocks[i].getSuccessors().remove(jumpins[i].getTarget());
			jumpins[i].getTarget().getPredecessors().remove(startBlocks[i]);
			startBlocks[i].setCodeAddress(startBlocks[i].addressOf(jumpouts[i]));
			startBlocks[i].delete();
			startBlocks[i].getSuccessors().remove(jumpouts[i].getTarget());
			jumpouts[i].getTarget().getPredecessors().remove(startBlocks[i]);
			conditions[i] = jumpins[i].getExpression();
			scopes[i] = new CS2Scope(this.scope);
			FlowBlocksSolver solver = new FlowBlocksSolver(scopes[i],blocks);
			solver.solve();
		}
		
		if (hasElse) {
			CS2FlowBlock[] blocks = cut(block.getPrev(),end);
			FlowBlocksSolver solver = new FlowBlocksSolver(elseScope,blocks);
			solver.solve();
		}
		
		startBlocks[0].setCodeAddress(startBlocks[0].size());
		startBlocks[0].write(new CS2IfElse(conditions,scopes,elseScope));
		
		return true;
	}
	


	
	
	private int doStandartIfConditionsMergeCheck() {
		int merged = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && (doIfANDConditionsMerge(blocks[i]) || doIfORConditionsMerge1(blocks[i]) || doIfORConditionsMerge2(blocks[i])))
				merged++;
		if (merged > 0)
			System.err.println("Merged if conditions:" + merged);
		return merged;
	}
	
	private boolean doIfORConditionsMerge1(CS2FlowBlock block) {
		if (block.size() < 2)
			return false;
		block.setCodeAddress(0);
		for (CS2Node node = block.read(); node != null; node = block.read()) {
			if (node instanceof CS2ConditionalFlowBlockJump) {
				CS2ConditionalFlowBlockJump jmp = (CS2ConditionalFlowBlockJump) node;
				int nextAddr = block.addressOf(jmp) + 1;
				if (nextAddr >= block.size())
					return false;
				if (!(block.read(nextAddr) instanceof CS2ConditionalFlowBlockJump))
					return false;
				CS2ConditionalFlowBlockJump jmp2 = (CS2ConditionalFlowBlockJump) block.read(nextAddr);
				if (jmp.getTarget() == jmp2.getTarget()) {
					block.getSuccessors().remove(jmp.getTarget());
					jmp.getTarget().getPredecessors().remove(block);
					block.setCodeAddress(nextAddr - 1);
					block.delete();
					block.delete();
					block.write(new CS2ConditionalFlowBlockJump(new CS2ConditionalExpression(jmp.getExpression(),jmp2.getExpression(),6),jmp.getTarget()));
					System.err.println("AHAHAHAHAH");
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean doIfORConditionsMerge2(CS2FlowBlock block) {
		if (block.size() < 2)
			return false;
		
		CS2Node v0 = block.read(block.size() - 2);
		CS2Node v1 = block.read(block.size() - 1);
		if (!(v0 instanceof CS2ConditionalFlowBlockJump) || !(v1 instanceof CS2UnconditionalFlowBlockJump))
			return false;

		CS2ConditionalFlowBlockJump condition1 = (CS2ConditionalFlowBlockJump)v0;
		CS2UnconditionalFlowBlockJump jmp = (CS2UnconditionalFlowBlockJump)v1;

		CS2FlowBlock target = jmp.getTarget();
		if (target.getBlockId() <= block.getBlockId() || target.getPredecessors().size() != 1 || target.size() < 2)
			return false;
		
		CS2Node v3 = target.read(0);
		CS2Node v4 = target.read(1);
		if (!(v3 instanceof CS2ConditionalFlowBlockJump) || !(v4 instanceof CS2UnconditionalFlowBlockJump))
			return false;

		CS2ConditionalFlowBlockJump condition2 = (CS2ConditionalFlowBlockJump)v3;
		CS2UnconditionalFlowBlockJump jmp2 = (CS2UnconditionalFlowBlockJump)v4;

		CS2FlowBlock realTarget = jmp2.getTarget();
		
		if (condition1.getTarget() != condition2.getTarget())
			return false;
		
		block.setCodeAddress(block.size() - 2);
		block.delete();
		block.delete();
		
		block.write(new CS2ConditionalFlowBlockJump(new CS2ConditionalExpression(condition1.getExpression(), condition2.getExpression(), 6), condition1.getTarget()));
		block.write(new CS2UnconditionalFlowBlockJump(realTarget));
		
		block.getSuccessors().remove(target);
		target.getPredecessors().remove(block);
		
		block.getSuccessors().add(realTarget);
		realTarget.getPredecessors().add(block);
		
		
		target.setCodeAddress(0);
		target.delete();
		target.delete();
		
		target.getSuccessors().remove(condition2.getTarget());
		condition2.getTarget().getPredecessors().remove(target);
		target.getSuccessors().remove(realTarget);
		realTarget.getPredecessors().remove(target);
		
		return true;
	}
	
	private boolean doIfANDConditionsMerge(CS2FlowBlock block) {
		if (block.size() < 2)
			return false;
		CS2Node v0 = block.read(block.size() - 2);
		CS2Node v1 = block.read(block.size() - 1);
		if (!(v0 instanceof CS2ConditionalFlowBlockJump) || !(v1 instanceof CS2UnconditionalFlowBlockJump))
			return false;
		CS2ConditionalFlowBlockJump conditionPart = (CS2ConditionalFlowBlockJump)v0;
		CS2UnconditionalFlowBlockJump jumpOut = (CS2UnconditionalFlowBlockJump)v1;
		CS2FlowBlock condition = conditionPart.getTarget();
		CS2FlowBlock out = jumpOut.getTarget();
		if (condition.getBlockId() <= block.getBlockId() || out.getBlockId() <= condition.getBlockId())
			return false;	
		if (condition.getPredecessors().size() != 1 || condition.size() < 1 || condition.size() > 2)
			return false;
		
		if (condition.size() == 1) {
			if (condition.getNext() != out)
				return false;
			
			CS2Node v3 = condition.read(condition.size() - 1);
			if (!(v3 instanceof CS2ConditionalFlowBlockJump))
				return false;

			CS2ConditionalFlowBlockJump realjmp = (CS2ConditionalFlowBlockJump)v3;
			CS2FlowBlock target = realjmp.getTarget();
			
			block.setCodeAddress(block.size() - 2);
			block.delete();
			block.write(new CS2ConditionalFlowBlockJump(new CS2ConditionalExpression(conditionPart.getExpression(), realjmp.getExpression(), 7), target));
			block.getSuccessors().remove(condition);
			block.getSuccessors().add(target);
			condition.getPredecessors().remove(block);
			target.getPredecessors().add(block);
			
			condition.setCodeAddress(condition.size() - 1);
			condition.delete();
			condition.getSuccessors().remove(target);
			target.getPredecessors().remove(condition);
			return true;
			
		}
		else {
			CS2Node v3 = condition.read(condition.size() - 2);
			CS2Node v4 = condition.read(condition.size() - 1);
			if (!(v3 instanceof CS2ConditionalFlowBlockJump) || !(v4 instanceof CS2UnconditionalFlowBlockJump))
				return false;
			CS2ConditionalFlowBlockJump realjmp = (CS2ConditionalFlowBlockJump)v3;
			CS2UnconditionalFlowBlockJump jumpOut2 = (CS2UnconditionalFlowBlockJump)v4;
			
			if (jumpOut2.getTarget() != out)
				return false;

			CS2FlowBlock target = realjmp.getTarget();
			
			block.setCodeAddress(block.size() - 2);
			block.delete();
			block.write(new CS2ConditionalFlowBlockJump(new CS2ConditionalExpression(conditionPart.getExpression(), realjmp.getExpression(), 7), target));
			block.getSuccessors().remove(condition);
			block.getSuccessors().add(target);
			condition.getPredecessors().remove(block);
			target.getPredecessors().add(block);
			
			condition.setCodeAddress(condition.size() - 2);
			for (int i = 0; i < 2; i++)
				condition.delete();
			condition.getSuccessors().remove(target);
			condition.getSuccessors().remove(out);
			target.getPredecessors().remove(condition);
			out.getPredecessors().remove(condition);
			return true;
		}
	}
	
	private int doStandartLoopsCheck() {
		int flowsFound = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartLoopsCheck(blocks[i]))
				flowsFound++;
		if (flowsFound > 0)
			System.err.println("Loops found:" + flowsFound);
		return flowsFound;
	}
	
	private boolean doStandartLoopsCheck(CS2FlowBlock block) {
		/**
		 * What we solve here is check the last jumper to this 
		 * block does jump unconditionally and only block that is jumped
		 * from inside the loop is the next block from the backjumper.
		 * example
		 * doSomething();
		 * flow_1:
		 * 		IF (someVariable > 0)
		 * 			GOTO flow_2
		 * 		GOTO flow_3;
		 * flow_2:
		 * 		doSomethingElse();
		 *		 GOTO flow_1
		 * flow_3:
		 * 		blah();
		 * This code could be translated to
		 * doSomething();
		 * for (;someVariable > 0;) {
		 * 		doSomethingElse();
		 * }
		 * blah();
		 */
		CS2FlowBlock last = this.getLastJumpingBlock(block);
		if (last == null || last.getBlockId() < block.getBlockId() || last.size() < 1)
			return false;
		if (!(last.read(last.size() - 1) instanceof CS2UnconditionalFlowBlockJump))
			return false;
		CS2UnconditionalFlowBlockJump jumpBack = (CS2UnconditionalFlowBlockJump) last.read(last.size() - 1);
		if (jumpBack.getTarget() != block)
			return false;
		if (block.getPrev() == null)
			this.attachSynthethicBlockBefore(block);
		if (last.getNext() == null)
			this.attachSynthethicBlockAfter(last);
		CS2FlowBlock start = block.getPrev();
		CS2FlowBlock end = last.getNext();
		List<CS2FlowBlock> inJumps = new ArrayList<>();
		List<CS2FlowBlock> outJumps = new ArrayList<>();
		outJumps.add(end);
		if (!this.canCut(start, end, inJumps, outJumps))
			return false;
		CS2FlowBlock[] blocks = this.cut(start, end);
		CS2Loop loop = new CS2Loop(CS2Loop.LOOPTYPE_WHILE,new CS2Scope(scope),new CS2Cast(CS2Type.BOOLEAN, new CS2PrimitiveExpression(1, CS2Type.INT)),block,end);
		block.setCodeAddress(0);
		if (block.read() instanceof CS2ConditionalFlowBlockJump && block.read() instanceof CS2UnconditionalFlowBlockJump) {
			CS2ConditionalFlowBlockJump v0 = (CS2ConditionalFlowBlockJump)block.read(0);
			CS2UnconditionalFlowBlockJump v1 = (CS2UnconditionalFlowBlockJump)block.read(1);
			if (v0.getTarget() == block.getNext() && v1.getTarget() == end) {
				block.setCodeAddress(0);
				for (int i = 0; i < 2; i++)
					block.delete();
				loop = new CS2Loop(CS2Loop.LOOPTYPE_WHILE,new CS2Scope(scope), v0.getExpression(), block,end);
				
				block.getSuccessors().remove(v0.getTarget());
				block.getSuccessors().remove(v1.getTarget());
				v0.getTarget().getPredecessors().remove(block);
				v1.getTarget().getPredecessors().remove(block);
			}
		}
		start.setCodeAddress(start.size());
		start.write(loop);
		
		last.setCodeAddress(last.size() - 1);
		last.delete();
		block.getPredecessors().remove(last);
		last.getSuccessors().remove(block);
		FlowBlocksSolver solver = new FlowBlocksSolver(loop.getScope(),blocks);
		solver.solve();

		return true;
	}
	
	private int doStandartFlowControlsCheck() {
		int controlsFound = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartFlowControlsCheck(blocks[i]))
				controlsFound++;
		if (controlsFound > 0)
			System.err.println("Controls found:" + controlsFound);
		return controlsFound;
	}
	
	private boolean doStandartFlowControlsCheck(CS2FlowBlock block) {
		if (block.size() < 1)
			return false;
		/**
		 * What we solve here is check the last node
		 * if it goes to start (continue) or end(break) of parent controllable
		 * flow node.
		 */
		CS2Node n0 = block.read(block.size() - 1);
		if (!(n0 instanceof CS2UnconditionalFlowBlockJump))
			return false;
		CS2UnconditionalFlowBlockJump jmp = (CS2UnconditionalFlowBlockJump)n0;
		IControllableFlowNode node = scope.findControllableNode(jmp.getTarget());
		if (node == null)
			return false;
		if (node instanceof IContinueableNode && ((IContinueableNode)node).getStart() == jmp.getTarget()) {
			block.setCodeAddress(block.size() - 1);
			block.delete();
			block.write(new CS2Continue(scope,(IContinueableNode)node));
			block.getSuccessors().remove(jmp.getTarget());
			jmp.getTarget().getPredecessors().remove(block);
			return true;
		}
		else if (node instanceof IBreakableNode && ((IBreakableNode)node).getEnd() == jmp.getTarget()) {
			block.setCodeAddress(block.size() - 1);
			block.delete();
			block.write(new CS2Break(scope,(IBreakableNode)node));
			block.getSuccessors().remove(jmp.getTarget());
			jmp.getTarget().getPredecessors().remove(block);
			return true;
		}
		else
			throw new DecompilerException("Unexpected type node:" + node);
		
	}
	
	
	private int doStandartSwitchesCheck() {
		int switchesFound = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doStandartSwitchesCheck(blocks[i]))
				switchesFound++;
		if (switchesFound > 0)
			System.err.println("Switches found:" + switchesFound);
		return switchesFound;
	}
	
	private boolean doStandartSwitchesCheck(CS2FlowBlock block) {
		if (block.size() < 1)
			return false;
		
		CS2Node v0 = block.read(block.size() - 1);
		if (!(v0 instanceof CS2SwitchFlowBlockJump))
			return false;
		CS2SwitchFlowBlockJump sw = (CS2SwitchFlowBlockJump)v0;
		if (sw.getCases().length <= 0)
			return false;
		if (sw.getTargets()[0].getBlockId() <= block.getBlockId())
			return false;
		CS2FlowBlock start = sw.getTargets()[0];
		CS2FlowBlock end = sw.getTargets()[sw.getTargets().length - 1];
		if (start.getPrev() == null)
			attachSynthethicBlockBefore(start);
		if (end.getNext() == null)
			attachSynthethicBlockAfter(end);
		start = start.getPrev();
		end = end.getNext();
		
		main: while (true) {
			List<CS2FlowBlock> outJumps = this.getAllOutjumps(start, end);
			for (CS2FlowBlock out : outJumps) {
				if (out.getBlockId() < end.getBlockId())
					return false;
				if (end != out) {
					end = out;
					continue main;
				}
			}
			break;
		}
		
		
		DecompilerUtils.SwitchCase[] cases = DecompilerUtils.makeSwitchCases(sw);
		List<CS2FlowBlock> allowedInJumpers = new ArrayList<>();
		for (int i = 0; i < sw.getCases().length; i++)
			allowedInJumpers.add(block);
		
		List<CS2FlowBlock> allowedOutJumps = this.getAllOutjumps(start, end);
		for (CS2FlowBlock out : allowedOutJumps) {
			if (out != end)
				return false;
		}
		if (!canCut(start, end, allowedInJumpers, allowedOutJumps))
			return false;
		CS2FlowBlock[] blocks = cut(start, end);
		start.setCodeAddress(start.size() - 1);
		start.delete();
		CS2Switch swi = new CS2Switch(end, new CS2Scope(this.scope), sw.getExpression());
		start.write(swi);
		for (int i = 0; i < sw.getCases().length; i++) {
			sw.getTargets()[i].getPredecessors().remove(block);
			block.getSuccessors().remove(sw.getTargets()[i]);
		}
		
		for (int i = 0; i < cases.length; i++) {
			boolean def = false;
			for (CS2Case a : cases[i].getAnnotations()) {
				if (a.isDefault()) {
					def = true;
					break;
				}
			}
			
			if (!def)
				continue;
			
			int blockIndex = -1;
			for (int x = 0; x < blocks.length; x++) {
				if (blocks[x] == cases[i].getBlock()) {
					blockIndex = x;
					break;
				}
			}
			
			if (blockIndex == -1)
				throw new DecompilerException("logic error");

			CS2FlowBlock b = blocks[blockIndex];
			
			if (b.size() != 1 || b.getPredecessors().size() != 0 || b.getSuccessors().size() != 1)
				continue;
			
			CS2Node b0 = b.read(0);
			if (!(b0 instanceof CS2UnconditionalFlowBlockJump))
				continue;

			CS2UnconditionalFlowBlockJump j = (CS2UnconditionalFlowBlockJump)b0;
			if (j.getTarget() != end)
				cases[i].setBlock(j.getTarget());
			else
				cases[i] = null;
			
			b.getSuccessors().remove(j.getTarget());
			j.getTarget().getPredecessors().remove(b);
			
			b.setCodeAddress(0);
			b.delete();
		}

		for (int i = 0; i < cases.length; i++) {
			if (cases[i] == null)
				continue;
			
			cases[i].getBlock().setCodeAddress(0);
			for (int a = 0; a < cases[i].getAnnotations().length; a++)
				cases[i].getBlock().write(cases[i].getAnnotations()[a]);
		}
		FlowBlocksSolver solver = new FlowBlocksSolver(swi.getScope(),blocks);
		solver.solve();
		return true;
	}
	
	
	
	private int doUnexpectedGotosResolving() {
		int unexpectedGotosResolved = 0;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && doUnexpectedGotosResolving(blocks[i]))
				unexpectedGotosResolved++;
		if (unexpectedGotosResolved > 0)
			System.err.println("Unexpected gotos resolved:" + unexpectedGotosResolved);
		return unexpectedGotosResolved;
	}
	
	private boolean doUnexpectedGotosResolving(CS2FlowBlock start) {
		for (int i = 0; i < blocks.length; i++) {		
			if (blocks[i] == null || blocks[i].getPrev() == null)
				continue;
			CS2FlowBlock firstJumper = this.getFirstJumpingBlock(blocks[i]);
			CS2FlowBlock lastJumper = this.getLastJumpingBlock(blocks[i]);
			if (firstJumper == null || lastJumper == null || firstJumper.getBlockId() <= start.getBlockId() || lastJumper.getBlockId() >= blocks[i].getBlockId())
				continue;
			CS2FlowBlock end = blocks[i];
			List<CS2FlowBlock> allowedOutJumps = new ArrayList<>();
			for (CS2FlowBlock current = start.getNext(); current != end && current != null; current = current.getNext()) {
				for (CS2FlowBlock successor : current.getSuccessors()) {
					if (successor == end)
						allowedOutJumps.add(end); 
				}
			}
			if (!this.canCut(start,end, new ArrayList<>(), allowedOutJumps ))
				continue;
			CS2FlowBlock[] blocks = this.cut(start,end);
			CS2Loop loop = new CS2Loop(CS2Loop.LOOPTYPE_DOWHILE,new CS2Scope(scope), new CS2Cast(CS2Type.BOOLEAN, new CS2PrimitiveExpression(0, CS2Type.INT)),blocks[0],end);
			start.setCodeAddress(0);
			start.write(loop);
			FlowBlocksSolver solver = new FlowBlocksSolver(loop.getScope(),blocks);
			solver.solve();
			return true;
		}
		return false;
	}
	
	/**
	 * Decides if two blocks can be connected.
	 */
	private boolean canConnect(CS2FlowBlock b1,CS2FlowBlock b2) {
		if (b1.getNext() != b2 || b2.getPrev() != b1)
			return false;
		if (b2.getPredecessors().size() > 0 || (b2.size() > 0 && b1.getSuccessors().size() > 0))
			return false;
		return true;
	}
	
	/**
	 * Connect's b2 to b1's end.
	 */
	private void connect(CS2FlowBlock b1,CS2FlowBlock b2) {
		if (!canConnect(b1,b2))
			throw new RuntimeException("Unchecked connection.");
		b1.setNext(b2.getNext());
		if (b2.getNext() != null) {
			b2.getNext().setPrev(b1);
		}
		b1.setCodeAddress(0);
		do {} while (b1.read() != null);
		b2.setCodeAddress(0);
		for (CS2Node node = b2.read(); node != null; node = b2.read())
			b1.write(node);
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] == b2)
				blocks[i] = null;
		for (CS2FlowBlock successor : b2.getSuccessors()) {
			successor.getPredecessors().remove(b2);
			successor.getPredecessors().add(b1);
			b1.getSuccessors().add(successor);
		}
		for (CS2FlowBlock predecesor : b2.getPredecessors()) {
			predecesor.getSuccessors().remove(b2);
			predecesor.getSuccessors().add(b1);
			b1.getPredecessors().add(predecesor);
		}
	}
	
	/**
	 * Decides if flow blocks starting after from and ending before to 
	 * can be cut.
	 */
	private boolean canCut(CS2FlowBlock from,CS2FlowBlock to,List<CS2FlowBlock> allowedInJumps,List<CS2FlowBlock> allowedOutJumps) {
		if (from.getBlockId() >= to.getBlockId() || from.getNext() == to || to.getPrev() == from)
			return false;
		List<CS2FlowBlock> leftIn = new ArrayList<>(allowedInJumps);
		List<CS2FlowBlock> leftOut = new ArrayList<>(allowedOutJumps);
		for (CS2FlowBlock current = from.getNext(); current != to && current != null; current = current.getNext()) {
			//if (current == null)
			//	return false;
			for (CS2FlowBlock inJump : current.getPredecessors()) {
				if (leftIn.contains(inJump)) {
					leftIn.remove(inJump);
					continue;
				}
				if (inJump.getBlockId() <= from.getBlockId() || inJump.getBlockId() >= to.getBlockId())
					return false;
			}
			for (CS2FlowBlock outJump : current.getSuccessors()) {
				if (leftOut.contains(outJump)) {
					leftOut.remove(outJump);
					continue;
				}
				if (outJump.getBlockId() <= from.getBlockId() || outJump.getBlockId() >= to.getBlockId())
					return false;
			}
		}
		return true;
	}
	

	private CS2FlowBlock[] cut(CS2FlowBlock from,CS2FlowBlock to) {
		CS2FlowBlock[] buffer = new CS2FlowBlock[to.getBlockId() * 2];
		int bufferWrite = 0;
		for (CS2FlowBlock current = from.getNext(); current != to && current != null; current = current.getNext())
			buffer[bufferWrite++] = current;	
		from.setNext(to);
		to.setPrev(from);
		buffer[0].setPrev(null);
		buffer[bufferWrite - 1].setNext(null);

		CS2FlowBlock[] blocks = new CS2FlowBlock[bufferWrite];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = buffer[i];
			for (int a = 0; a < this.blocks.length; a++)
				if (blocks[i] == this.blocks[a])
					this.blocks[a] = null;
		}
		
		return blocks;
	}
	
	/**
	 * Attache's synthethic block before given block.
	 * if (next.getPrev() != null)
	 * then this method throws IllegalArgumentException
	 * The ID of the synthethic flow block is always negative -(next.getBlockId() - 1);
	 */
	private void attachSynthethicBlockBefore(CS2FlowBlock next) {
		if (next.getPrev() != null)
			throw new IllegalArgumentException("next.getPrev() != null");
		int blockID = next.getBlockId() - 1;
		if (blockID > 0)
			blockID = -blockID;
		CS2FlowBlock block = new CS2FlowBlock(blockID,-1,new CS2Stack());
		CS2FlowBlock[] rebuff = new CS2FlowBlock[blocks.length + 1];
		for (int i = 0,write = 0; i < blocks.length; i++) {
			if (blocks[i] != null && blocks[i].getNext() == next)
				throw new IllegalArgumentException("block " + i + " next flow block is argument!");
			if (blocks[i] == next)
				rebuff[write++] = block;
			rebuff[write++] = blocks[i];
		}
		block.setNext(next);
		next.setPrev(block);
		blocks = rebuff;
	}
	
	
	/**
	 * Attache's synthethic block after given block.
	 * if (prev.getNext() != null)
	 * then this method throws IllegalArgumentException
	 * The ID of the synthethic flow block is always higher than prev by 5000
	 */
	private void attachSynthethicBlockAfter(CS2FlowBlock prev) {
		if (prev.getNext() != null)
			throw new IllegalArgumentException("next.getPrev() != null");
		int blockID = prev.getBlockId() + 5000;
		CS2FlowBlock block = new CS2FlowBlock(blockID,-1,new CS2Stack());
		CS2FlowBlock[] rebuff = new CS2FlowBlock[blocks.length + 1];
		for (int i = 0,write = 0; i < blocks.length; i++) {
			if (blocks[i] != null && blocks[i].getPrev() == prev)
				throw new IllegalArgumentException("block " + i + " prev flow block is argument!");
			rebuff[write++] = blocks[i];
			if (blocks[i] == prev)
				rebuff[write++] = block;
		}
		block.setPrev(prev);
		prev.setNext(block);
		blocks = rebuff;
	}
	
	private List<CS2FlowBlock> getAllOutjumps(CS2FlowBlock from,CS2FlowBlock to) {
		List<CS2FlowBlock> outJumps = new ArrayList<>();
		if (from.getBlockId() >= to.getBlockId() || from.getNext() == to || to.getPrev() == from)
			throw new IllegalArgumentException("from -> | nothing | <- to");
		for (CS2FlowBlock current = from.getNext(); current != to && current != null; current = current.getNext()) {
			for (CS2FlowBlock outJump : current.getSuccessors()) {
				if (outJump.getBlockId() <= from.getBlockId() || outJump.getBlockId() >= to.getBlockId())
					outJumps.add(outJump);
			}
		}
		return outJumps;
	}
	
	
	
	
	public CS2FlowBlock getFirstJumpingBlock(CS2FlowBlock target) {
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && blocks[i].getSuccessors().contains(target))
				return blocks[i];
		return null;
	}
	
	public CS2FlowBlock getLastJumpingBlock(CS2FlowBlock target) {
		int lastBlockID = -1;
		CS2FlowBlock last = null;
		for (int i = 0; i < blocks.length; i++)
			if (blocks[i] != null && blocks[i].getBlockId() > lastBlockID && blocks[i].getSuccessors().contains(target)) {
				lastBlockID = blocks[i].getBlockId();
				last = blocks[i];
			}
		return last;
	}


	
	public List<CS2FlowBlock> listBlocks() {
		List<CS2FlowBlock> blocks = new ArrayList<>();
		for (int i = 0; i < this.blocks.length; i++)
			if (this.blocks[i] != null)
				blocks.add(this.blocks[i]);
		return blocks;
	}

	

	public CS2Scope getScope() {
		return scope;
	}
	


	
	
}
