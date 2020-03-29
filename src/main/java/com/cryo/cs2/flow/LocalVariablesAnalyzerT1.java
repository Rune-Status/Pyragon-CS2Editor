package com.cryo.cs2.flow;

import com.cryo.cs2.nodes.*;
import com.cryo.cs2.CS2Type;
import com.cryo.utils.CodePrinter;
import com.cryo.utils.DecompilerException;
import lombok.Data;

import java.util.*;

@Data
public class LocalVariablesAnalyzerT1 {
	
	private final CS2Function function;
	private final CS2FlowBlock[] blocks;
	

	private boolean[] processed;
	private FlowBlockState[] states;
	
	private Map<Integer, LocalVariable> variables;
	private Map<CS2Node, List<Integer>> accesses;
	
	public void analyze() throws DecompilerException {
		init();
		process();
		end();
	}
	
	
	private void init() {
		processed = new boolean[blocks.length];
		states = new FlowBlockState[blocks.length];
		variables = new HashMap<Integer, LocalVariable>();
		accesses = new HashMap<CS2Node, List<Integer>>();
		
		FlowBlockState s = new FlowBlockState();
		for (LocalVariable arg : function.getLocalArguments()) {
			CS2Node dummy = new CS2Node() {
				@Override
				public void print(CodePrinter printer) {
					printer.beginPrinting(this);
					printer.print("dummy");
					printer.endPrinting(this);
				}
				
				@Override
				public int getCodeAddress() {
					return Integer.MIN_VALUE;
				}
			};
			
			accesses.put(dummy, s.set(dummy, arg, true));
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
		for (CS2Node n : accesses.keySet())
			merge(new ArrayList<Integer>(accesses.get(n)));		
		
		
		List<LocalVariable> old = function.getScope().copyDeclaredVariables();
		for (LocalVariable v : old)
			function.getScope().undeclare(v);
		
		List<LocalVariable> nvars = new ArrayList<LocalVariable>();
		List<LocalVariable> nargs = new ArrayList<LocalVariable>();
		
		int count = 0;
		int argcount = 0;
		for (List<Integer> vars : accesses.values()) {
			if (vars.size() != 1)
				throw new DecompilerException("Not merged");
			
			LocalVariable v = variables.get(vars.get(0));
			if (nvars.contains(v))
				continue;
			
			nvars.add(v);
			if (v.isArgument()) {
				v.setScopeDeclarationNeeded(false);
				nargs.add(v);
			}
			v.setName(v.isArgument() ? ("a" + argcount++) : ("v" + count++));
		}
		
		System.err.println(nargs.size() + "," + function.getLocalArguments().length);
		if (nargs.size() != function.getLocalArguments().length)
			throw new DecompilerException("something failed");
		
		int write = 0;
		for (LocalVariable v : nargs)
			function.getLocalArguments()[write++] = v;
		
		for (LocalVariable v : nvars)
			function.getScope().declare(v);
		
		for (CS2Node n : accesses.keySet()) {
			LocalVariable nvar = variables.get(accesses.get(n).get(0));
			if (n instanceof CS2VariableLoad)
				((CS2VariableLoad)n).setVariable(nvar);
			else if (n instanceof CS2VariableAssign)
				((CS2VariableAssign)n).setVariable(nvar);
			else if (n.getCodeAddress() == Integer.MIN_VALUE)
				; // XXX this is our dummy node
			else
				throw new DecompilerException("logic error");
		}
	}
	
	private void merge(List<Integer> vars) {
		if (vars.size() < 2)
			return;
		
		int main = vars.get(0);
		LocalVariable vmain = variables.get(main);
		String name = vmain.getName();
		CS2Type type = vmain.getType();
		boolean arg = vmain.isArgument();
		for (int var : vars) {
			if (var == main)
				continue;
			
			LocalVariable vother = variables.get(var);
			if (!vother.getName().equals(name) || !vother.getType().equals(type))
				throw new DecompilerException("Can't merge " + vmain + " with " + vother);
			arg = arg || vother.isArgument();
		}
		
		LocalVariable merged = new LocalVariable(name, type, arg);
		variables.put(main, merged);
		
		for (CS2Node n : accesses.keySet()) {
			List<Integer> varlist = accesses.get(n);
			
			boolean has = false;
			Iterator<Integer> it$ = varlist.iterator();
			while (it$.hasNext()) {
				Integer var = it$.next();
				if (vars.contains(var)) {
					it$.remove();
					has = true;
				}
			}
			
			if (has)
				varlist.add(main);
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
		else if (n instanceof CS2VariableLoad) {
			CS2VariableLoad ldr = (CS2VariableLoad)n;
			accesses.put(ldr, state.get(n, ldr.getVariable()));
		}
		else if (n instanceof CS2VariableAssign) {
			CS2VariableAssign ldr = (CS2VariableAssign)n;
			accesses.put(ldr, state.set(n, ldr.getVariable(), false));
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
		private Map<LocalVariable, List<Integer>> state;
		
		public FlowBlockState() {
			state = new HashMap<LocalVariable, List<Integer>>();
		}
		
		public List<Integer> set(Object obj, LocalVariable var, boolean arg) {
			if (!variables.containsKey(obj.hashCode()))
				variables.put(obj.hashCode(), new LocalVariable(var.getName(), var.getType(), arg));
			if (!state.containsKey(var))
				state.put(var, new ArrayList<Integer>());
			
			List<Integer> v = state.get(var);
			v.clear();
						
			v.add(obj.hashCode());
			return new ArrayList<Integer>(v);
		}
		
		
		public List<Integer> get(Object obj, LocalVariable var) {
			if (!state.containsKey(var))
				throw new DecompilerException("Accessing unassigned variable!");
			return new ArrayList<Integer>(state.get(var));
		}
		
		public boolean merge(FlowBlockState other) {
			boolean updated = false;
			for (LocalVariable ovar : other.state.keySet()) {
				if (!state.containsKey(ovar))
					continue;
				
				List<Integer> l = state.get(ovar);
				for (Integer i : other.state.get(ovar)) {
					if (!l.contains(i)) {
						l.add(i);
						updated = true;
					}
				}
				
			}
			return updated;
		}
		
		public FlowBlockState copy() {
			FlowBlockState s = new FlowBlockState();
			for (LocalVariable n : state.keySet())
				s.state.put(n, new ArrayList<Integer>(state.get(n)));
			return s;
		}
	}
}
