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

import com.cryo.decompiler.CS2Type;
import com.cryo.decompiler.CodePrinter;

public class LongExpressionNode extends ExpressionNode implements IConstantNode {

    /**
     * Contains long which this expression holds.
     */
    private long data;
    
    public LongExpressionNode(long data) {
    	this.data = data;
    }

    public long getData() {
    	return data;
    }

    @Override
    public CS2Type getType() {
    	return CS2Type.LONG;
    }
	
	@Override
	public Object getConst() {
		return this.data;
	}

	@Override
	public ExpressionNode copy() {
		return new LongExpressionNode(data);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.beginPrinting(this);
		getType().printConstant(printer, data);
		printer.endPrinting(this);
	}

}
