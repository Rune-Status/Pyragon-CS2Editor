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


package com.cryo.decompiler.ast;

import com.cryo.decompiler.CodePrinter;

public class ConditionalFlowBlockJump extends AbstractCodeNode {

    /**
     * Contains expression which type is boolean.
     */
    private ExpressionNode expression;
    /**
     * Contains target flow block.
     */
    private FlowBlock target;
    
    public ConditionalFlowBlockJump(ExpressionNode expr,FlowBlock target) {
    	this.expression = expr;
    	this.target = target;
		this.write(expr);
		expr.setParent(this);
    }
    

	public ExpressionNode getExpression() {
		return expression;
	}
	
	public FlowBlock getTarget() {
		return target;
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		printer.print("IF (");
		expression.print(printer);
		printer.print(") ");
		printer.tab();
		printer.print("\nGOTO\t" + "flow_" + target.getBlockID());
		printer.untab();
		printer.endPrinting(this);
	}
}
