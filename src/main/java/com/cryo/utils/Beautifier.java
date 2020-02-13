/*
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.cryo.utils;

import java.util.*;

import com.cryo.cs2.flow.CS2FlowBlock;
import com.cryo.cs2.nodes.CS2Comment;
import com.cryo.cs2.nodes.CS2Function;
import com.cryo.cs2.nodes.CS2Loop;
import com.cryo.cs2.nodes.CS2Node;
import com.cryo.cs2.nodes.CS2Poppable;
import com.cryo.cs2.nodes.CS2Scope;
import com.cryo.cs2.nodes.CS2Switch;
import com.cryo.cs2.nodes.CS2VariableAssign;
import com.cryo.cs2.nodes.CS2VariableLoad;
import com.cryo.cs2.nodes.LocalVariable;

public class Beautifier {
	
	private CS2Function function;

	public Beautifier(CS2Function function) {
		this.function = function;
	}
	
	
	
	public void beautify() throws DecompilerException {
		if (checkForFlowBlocks(function)) {
			function.setCodeAddress(0);
			while (function.read() instanceof CS2Comment)
				;
			function.setCodeAddress(function.getCodeAddress() - 1);
			function.write(new CS2Comment("Beautifier was aborted, because this function contains unsolved flow blocks.", CS2Comment.STANDART_STYLE));
			return; // we can't beautify 
		}
		
		transformLoops(function, function.getScope());
		moveVarScopesAndRename(function);
	}
	
	
	private void transformLoops(CS2Node parent, CS2Node n) {
		for (int i = 0; i < n.size(); i++)
			transformLoops(n, n.read(i));
		
		if (n instanceof CS2Loop && ((CS2Loop)n).getPreAssigns() == null) {
			CS2Loop loop = (CS2Loop)n;
			List<CS2Poppable> delete1 = new ArrayList<>();
			List<CS2VariableAssign> pre = new ArrayList<>();
			for (int addr = parent.addressOf(n) - 1; addr > 0; addr--) {
				CS2Node a = parent.read(addr);
				if (!(a instanceof CS2Poppable))
					break;
				CS2Poppable p = (CS2Poppable)a;
				if (!(p.getExpression() instanceof CS2VariableAssign) || !((CS2VariableAssign)p.getExpression()).isDeclaration())
					break;
				delete1.add(p);
				pre.add((CS2VariableAssign)p.getExpression());
			}
			
			if (pre.size() < 1)
				return;
			
			List<CS2Poppable> delete2 = new ArrayList<>();
			List<CS2VariableAssign> after = new ArrayList<>();
			for (int addr = loop.getScope().size() - 1; addr > 0; addr--) {
				CS2Node a = loop.getScope().read(addr);
				if (!(a instanceof CS2Poppable))
					break;
				CS2Poppable p = (CS2Poppable)a;
				
				if (!(p.getExpression() instanceof CS2VariableAssign))
					break;
				CS2VariableAssign as = (CS2VariableAssign)p.getExpression();
				if (as.isDeclaration())
					break;
				
				boolean found = false;
				for (CS2VariableAssign x : pre) {
					if (as.getVariable() == x.getVariable()) {
						found = true;
						break;
					}
				}
				
				if (!found)
					break;
				
				delete2.add(p);
				after.add(as);
			}
			
			if (pre.size() > 0 && after.size() > 0) {
				CS2VariableAssign[] p = new CS2VariableAssign[pre.size()];
				CS2VariableAssign[] a = new CS2VariableAssign[after.size()];
				
				int writep = p.length - 1;
				int writea = a.length - 1;
				for (CS2VariableAssign x : pre)
					p[writep--] = x;
				for (CS2VariableAssign x : after)
					a[writea--] = x;
				
				loop.forTransform(p, a);
				
				for (CS2Poppable d1 : delete1)
					parent.delete(parent.addressOf(d1));
				for (CS2Poppable d2 : delete2)
					loop.getScope().delete(loop.getScope().addressOf(d2));
			}
		}
	}
	
	private void moveVarScopesAndRename(CS2Function function) {
		Map<LocalVariable, Map<CS2Node, CS2Scope>> access = new HashMap<>();
		for (LocalVariable var : function.getScope().copyDeclaredVariables())
			access.put(var, new HashMap<>());
		
		collectAccess(null, function, access);
		
		Map<LocalVariable, CS2Scope> smap = new HashMap<>();
		
		for (LocalVariable var : access.keySet()) {
			if (var.isArgument())
				continue;
			
			Map<CS2Node, CS2Scope> acc = access.get(var);
			
			CS2Scope bestScope = null;
			CS2Scope[] bestScopeTree = null;
			for (CS2Scope n : acc.values()) {
				if (bestScope == null) {
					bestScope = n;
					while (bestScope.getParent() instanceof CS2Switch)
						bestScope = bestScope.getParentScope();
					bestScopeTree = bestScope.makeScopeTree();
					continue;
				}
				
				if (n == bestScope)
					continue;
				
				CS2Scope[] ntree = n.makeScopeTree();
				int amt = Math.min(bestScopeTree.length, ntree.length);
				for (int i = 0; i < amt; i++) {
					if (bestScopeTree[i] != ntree[i]) {
						bestScope = bestScopeTree[i - 1];
						while (bestScope.getParent() instanceof CS2Switch)
							bestScope = bestScope.getParentScope();
						bestScopeTree = bestScope.makeScopeTree();
						break;
					}
				}
				
				if (ntree.length < bestScopeTree.length) {
					bestScope = n;
					bestScopeTree = n.makeScopeTree();
				}
			}
			

			
			if (bestScope != function.getScope()) {
				//System.err.println("Moved " + var + " to " + bestScope);
				function.getScope().undeclare(var);
				bestScope.declare(var);
			}
			
			
			var.setName(var.getName() + "_");
			smap.put(var, bestScope);
			

		}
		
		for (int depth = 0; smap.size() > 0; depth++) {
			Iterator<LocalVariable> it$ = smap.keySet().iterator();
			while (it$.hasNext()) {
				LocalVariable var = it$.next();
				
				CS2Scope scope = smap.get(var);
				if (scope.getScopeDepth() != depth)
					continue;
								
				for (int i = 0;;i++) {
					if (scope.isDeclared("v" + i))
						continue;
					var.setName("v" + i);
					it$.remove();
					break;
				}
			}
		}
		
		
	}
	
	private void collectAccess(CS2Scope scope, CS2Node n, Map<LocalVariable, Map<CS2Node, CS2Scope>> all) {
		if (n instanceof CS2Scope)
			scope = (CS2Scope)n;
		
		for (int i = 0; i < n.size(); i++)
			collectAccess(scope, n.read(i), all);
		
		if (n instanceof CS2VariableLoad) {
			CS2VariableLoad l = (CS2VariableLoad)n;
			if (!all.containsKey(l.getVariable()))
				throw new DecompilerException("undeclared var? " + l.getVariable());
			if (scope == null)
				throw new DecompilerException("null scope?");
			
			all.get(l.getVariable()).put(l, scope);
		}
		else if (n instanceof CS2VariableAssign) {
			CS2VariableAssign a = (CS2VariableAssign)n;
			if (!all.containsKey(a.getVariable()))
				throw new DecompilerException("undeclared var? " + a.getVariable());
			if (scope == null)
				throw new DecompilerException("null scope?");
			
			all.get(a.getVariable()).put(a, scope);
		}
	}
	
	
	
	private boolean checkForFlowBlocks(CS2Node n) {
		for (int i = 0; i < n.size(); i++)
			if (checkForFlowBlocks(n.read(i)))
				return true;
		
		return n instanceof CS2FlowBlock;
	}
	

}
