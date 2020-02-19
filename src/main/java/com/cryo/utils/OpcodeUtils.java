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

import com.cryo.cs2.CS2Instruction;
import static com.cryo.cs2.CS2Instruction.*;

public class OpcodeUtils {

	
	public static int getTwoConditionsJumpStackType(int opcode) {
		CS2Instruction instruction = CS2Instruction.getByOpcode(opcode);
		if(instruction == INT_EQ || instruction == INT_NE || instruction == INT_LT
				|| instruction == INT_GT || instruction == INT_LE || instruction == INT_GE)
			return 0;
		else if(instruction == LONG_EQ || instruction == LONG_NE || instruction == LONG_LT
				|| instruction == LONG_GT || instruction == LONG_LE || instruction == LONG_GE)
			return 2;
		return -1;
	}
	
	public static int getTwoConditionsJumpConditional(int opcode) {
		CS2Instruction instruction = CS2Instruction.getByOpcode(opcode);
		switch (instruction) {
			case INT_NE:
			case LONG_NE:
				return 0; // !=
			case INT_EQ:
			case LONG_EQ:
				return 1; // ==
			case INT_LT:
			case LONG_LT:
				return 3; // <
			case INT_GT:
			case LONG_GT:
				return 2; // >
			case INT_LE:
			case LONG_LE:
				return 5; // <=
			case INT_GE:
			case LONG_GE:
				return 4; // >=
			default:
				return -1;
		}
	}
	
	public static int getOneConditionJumpStackType(int opcode) {
		if (opcode == BRANCH_EQ0.opcode || opcode == BRANCH_EQ1.opcode)
			return 0;
		else 
			return -1;
	}
}
