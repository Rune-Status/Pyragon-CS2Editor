package com.cryo.cs2.flow;

import com.cryo.cs2.nodes.*;
import com.cryo.utils.DecompilerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalVariablesAnalyzerT2 {
	
	private CS2Function function;
	private CS2FlowBlock[] blocks;
	

	private boolean[] processed;
	private FlowBlockState[] states;
	
	private Map<LocalVariable, List<Object>> assigns;

	public LocalVariablesAnalyzerT2(CS2Function function, CS2FlowBlock[] blocks) {
		this.function = function;
		this.blocks = blocks;
	}
	
	
	
	public void analyze() throws DecompilerException {
		init();
		process();
		end();
	}
	
	
	private void init() {
		processed = new boolean[blocks.length];
		states = new FlowBlockState[blocks.length];
		assigns = new HashMap<LocalVariable, List<Object>>();
		
		FlowBlockState s = new FlowBlockState();
		for (LocalVariable arg : function.getLocalArguments()) {
			Object o = s.set(null, arg);
			if (!assigns.containsKey(arg))
				assigns.put(arg, new ArrayList<Object>());
			List<Object> l = assigns.get(arg);
			if (!l.contains(o))
				l.add(o);
		}
		
		
		queue(s, blocks[0]);
	}
	
	private void process() {
		int count;
		do {
			count = 0;
			for (int i = 0; i < states.length; i++) {
				if (states[i] == null || processed[i])
					continue;
				processed[i] = true;
				
				processNode(states[i], blocks[i]);
				count++;
			}
		}
		while (count > 0);
		
	}
	
	private void end() {	
		for (LocalVariable var : assigns.keySet()) {
			List<Object> objs = assigns.get(var);
			if (objs.size() != 1 || !(objs.get(0) instanceof CS2VariableAssign))
				continue;
			
			CS2VariableAssign fassign = (CS2VariableAssign)objs.get(0);
			
			fassign.setDeclaration(true);
			var.setScopeDeclarationNeeded(false);
		}
	}
	
	
	
	private boolean processNode(FlowBlockState state, CS2Node n) {
		boolean quit = false;
		for (int i = 0; i < n.size(); i++) {
			boolean q = processNode(state, n.read(i));
			quit = quit || q;
		}
		
		if (n instanceof CS2ConditionalFlowBlockJump) {
			CS2ConditionalFlowBlockJump jmp = (CS2ConditionalFlowBlockJump)n;
			queue(state, jmp.getTarget());
		}
		else if (n instanceof CS2UnconditionalFlowBlockJump) {
			CS2UnconditionalFlowBlockJump jmp = (CS2UnconditionalFlowBlockJump)n;
			queue(state, jmp.getTarget());
			return true;
		}
		else if (n instanceof CS2SwitchFlowBlockJump) {
			CS2SwitchFlowBlockJump jmp = (CS2SwitchFlowBlockJump)n;
			for (int i = 0; i < jmp.getTargets().length; i++)
				queue(state, jmp.getTargets()[i]);
			return true;
		}
		else if (n instanceof CS2VariableAssign) {
			CS2VariableAssign ldr = (CS2VariableAssign)n;
			Object o = state.set(ldr, ldr.getVariable());
			if (!assigns.containsKey(ldr.getVariable()))
				assigns.put(ldr.getVariable(), new ArrayList<Object>());
			List<Object> l = assigns.get(ldr.getVariable());
			if (!l.contains(o))
				l.add(o);
		}
		else if (n instanceof CS2Return) {
			return true;
		}
		else if (n instanceof CS2FlowBlock) {
			CS2FlowBlock f = (CS2FlowBlock)n;
			if (!quit && f.getNext() != null)
				queue(state, f.getNext());
			return true;
		}
		return quit;
	}
	
	
	private void queue(FlowBlockState current, CS2FlowBlock target) {
		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i] != target)
				continue;
			if (states[i] == null) {
				states[i] = current.copy();
				return;
			}
			
			if (states[i].merge(current))
				processed[i] = false;
			return;
		}
		throw new DecompilerException("logic error");
	}
	
	
	private class FlowBlockState {
		private Map<LocalVariable, Object> hits;
		
		public FlowBlockState() {
			hits = new HashMap<LocalVariable, Object>();
		}
		
		public Object set(Object obj, LocalVariable var) {
			if (!hits.containsKey(var))
				hits.put(var, obj);			
			return hits.get(var);
		}
		
		public boolean merge(FlowBlockState other) {
			boolean updated = false;
			for (LocalVariable ovar : other.hits.keySet()) {
				if (!hits.containsKey(ovar))
					continue;
				
				if (hits.get(ovar) != other.hits.get(ovar)) {
					hits.put(ovar, null);
					updated = true;
				}			
			}
			return updated;
		}
		
		public FlowBlockState copy() {
			FlowBlockState s = new FlowBlockState();
			for (LocalVariable n : hits.keySet())
				s.hits.put(n, hits.get(n));
			return s;
		}
	}
}
