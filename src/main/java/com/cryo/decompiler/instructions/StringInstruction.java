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


package com.cryo.decompiler.instructions;

import com.cryo.decompiler.util.InstructionInfo;

public class StringInstruction extends AbstractInstruction {

	private String constant;

	public StringInstruction(InstructionInfo info, String constant) {
		super(info);
		this.constant = constant;
	}

	public String getConstant() {
		return constant;
	}

	public String toString() {
		return super.toString() + "\t" + constant;
	}

}
