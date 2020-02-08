package com.cryo.cs2.nodes;

import com.cryo.cs2.flow.CS2FlowBlock;
import com.cryo.utils.CodePrinter;
import com.cryo.utils.DecompilerException;

import java.util.ArrayList;
import java.util.List;

public class CS2Scope extends CS2Node {


    /**
     * Contains scope in which this scope is
     * declared or null if this scope is first.
     */
    private CS2Scope parentScope;
    /**
     * Contains parent node or null if this scope doesn't have
     * parent node.
     */
    private CS2Node parent;
    /**
     * Contains list of declared local variables.
     */
    private List<LocalVariable> declaredLocalVariables;


    public CS2Scope() {
        this(null);
    }

    public CS2Scope(CS2Scope parent) {
        this.parentScope = parent;
        this.declaredLocalVariables = new ArrayList<>();
    }


    /**
     * Removes local variable from declared variables list.
     * @param variable
     * @throws DecompilerException
     * If variable does not belong to this scope.
     */
    public void undeclare(LocalVariable variable) throws DecompilerException {
        if (!declaredLocalVariables.contains(variable))
            throw new DecompilerException("Variable (" + variable.toString() + ") is not declared!");
        declaredLocalVariables.remove(variable);
    }

    /**
     * Declare's given local variable to this scope.
     * @param variable
     * @throws DecompilerException
     * If variable there's variable with the same name declared.
     */
    public void declare(LocalVariable variable) throws DecompilerException {
        if (this.isDeclared(variable.getName())) {
            throw new DecompilerException("Variable (" + variable.toString() + ") is already declared!");
        }
        this.declaredLocalVariables.add(variable);
    }

    /**
     * Get's declared local variable from this scope or one of the parent scopes.
     * @param localName
     * Local name of the variable that should be returned.
     * @return
     * Returns local variable with given localName.
     * @throws DecompilerException
     * If the given local variable is not declared.
     */
    public LocalVariable getLocalVariable(String localName) throws DecompilerException {
        for (LocalVariable var : this.declaredLocalVariables) {
            if (var.getName().equals(localName)) {
                return var;
            }
        }
        if (this.parentScope != null) {
            return this.parentScope.getLocalVariable(localName);
        }
        throw new DecompilerException("Variable " + localName + " is not declared!");
    }

    /**
     * Get's declared local variable from this scope or one of the parent scopes.
     * @param identifier
     * Identifier of the local variable
     * @return
     * Returns local variable with given localName.
     * @throws DecompilerException
     * If the given local variable is not declared.
     */
    public LocalVariable getLocalVariable(int identifier) throws DecompilerException {
        for (LocalVariable var : this.declaredLocalVariables) {
            if (var.getIdentifier() != -1 && var.getIdentifier() == identifier) {
                return var;
            }
        }
        if (this.parentScope != null) {
            return this.parentScope.getLocalVariable(identifier);
        }
        throw new DecompilerException("Variable " + identifier + " is not declared!");
    }

    /**
     * Get's if given local variable is declared in this
     * scope or in parent scopes.
     * @param localName
     * Name of the local variable
     * @return
     * Wheter given local variable is declared.
     */
    public boolean isDeclared(String localName) {
        for (LocalVariable var : this.declaredLocalVariables) {
            if (var.getName().equals(localName)) {
                return true;
            }
        }
        if (this.parentScope != null) {
            return this.parentScope.isDeclared(localName);
        }
        return false;
    }


    /**
     * Get's if given local variable is declared in this
     * scope or in parent scopes.
     * @param identifier
     * Identifier of the local variable.
     * @return
     * Wheter given local variable is declared.
     */
    public boolean isDeclared(int identifier) {
        for (LocalVariable var : this.declaredLocalVariables) {
            if (var.getIdentifier() != -1 && var.getIdentifier() == identifier) {
                return true;
            }
        }
        if (this.parentScope != null) {
            return this.parentScope.isDeclared(identifier);
        }
        return false;
    }

    /**
     * Copies declared variables in this scope only.
     */
    public List<LocalVariable> copyDeclaredVariables() {
        return new ArrayList<LocalVariable>(this.declaredLocalVariables);
    }

    /**
     * Get's if this scopeNode is empty.
     * @return
     */
    public boolean isEmpty() {
        return this.size() <= 0;
    }

    /**
     * Return's all scopes in order.
     */
    public CS2Scope[] makeScopeTree() {
        CS2Scope[] tree = new CS2Scope[getScopeDepth() + 1];
        fillScopeTree(tree, tree.length - 1);
        return tree;
    }

    private void fillScopeTree(CS2Scope[] tree, int index) {
        tree[index] = this;
        if (parentScope != null)
            parentScope.fillScopeTree(tree, index - 1);
    }

    /**
     * Return's depth of this scope.
     */
    public int getScopeDepth() {
        if (parentScope == null)
            return 0;
        return parentScope.getScopeDepth() + 1;
    }

    /**
     * Get's root (first) scope.
     */
    public CS2Scope getRootScope() {
        if (this.parentScope != null)
            return this.parentScope.getRootScope();
        return this;
    }

    public CS2Scope getParentScope() {
        return parentScope;
    }

    /**
     * Find's controllable flow node to which target belongs.
     * Return's null if nothing was found.
     */
    public IControllableFlowNode findControllableNode(CS2FlowBlock target) {
        if (this instanceof IControllableFlowNode) {
            if (this instanceof IBreakableNode && ((IBreakableNode)this).canBreak() && ((IBreakableNode)this).getEnd() == target)
                return (IControllableFlowNode)this;
            else if (this instanceof IContinueableNode && ((IContinueableNode)this).canContinue() && ((IContinueableNode)this).getStart() == target)
                return (IControllableFlowNode)this;
        }
        if (this.parent != null && this.parent instanceof IControllableFlowNode) {
            IControllableFlowNode parent = (IControllableFlowNode)this.parent;
            if (parent instanceof IBreakableNode && ((IBreakableNode)parent).canBreak() && ((IBreakableNode)parent).getEnd() == target)
                return parent;
            else if (parent instanceof IContinueableNode && ((IContinueableNode)parent).canContinue() && ((IContinueableNode)parent).getStart() == target)
                return parent;
        }
        if (this.parentScope != null)
            return this.parentScope.findControllableNode(target);
        return null;
    }


    public void setParent(CS2Node parentInstruction) {
        this.parent = parentInstruction;
    }

    public CS2Node getParent() {
        return parent;
    }


    private boolean needsBraces() {
        if (!(getParent() instanceof CS2Loop) && !(getParent() instanceof CS2IfElse))
            return true;

        int cElements = size();
        for (int i = 0; i < cElements; i++) {
            if (read(i) instanceof CS2Loop || read(i) instanceof CS2IfElse || read(i) instanceof CS2Switch)
                return true;
        }


        for (LocalVariable var : this.declaredLocalVariables)
            if (var.isScopeDeclarationNeeded())
                cElements++;

        return cElements > 1;
    }


    @Override
    public void print(CodePrinter printer) {
        boolean braces = needsBraces();

        printer.beginPrinting(this);
        printer.tab();
        if (braces)
            printer.print('{');
        for (LocalVariable var : this.declaredLocalVariables) {
            if (var.isScopeDeclarationNeeded())
                printer.print("\n" + var.toString() + ";");
        }
        boolean caseAnnotationTabbed = false;
        List<CS2Node> childs = this.listChilds();
        for (CS2Node node : childs) {
            if (node instanceof CS2Case && !caseAnnotationTabbed) {
                printer.print('\n');
                node.print(printer);
                printer.tab();
                caseAnnotationTabbed = true;
            }
            else if (node instanceof CS2Case && caseAnnotationTabbed) {
                printer.untab();
                printer.print('\n');
                node.print(printer);
                printer.tab();
            }
            else {
                printer.print('\n');
                node.print(printer);
            }

        }
        if (caseAnnotationTabbed)
            printer.untab();
        printer.untab();
        if (braces)
            printer.print("\n}");
        printer.endPrinting(this);
    }

}
