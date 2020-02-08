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

import com.cryo.cs2.flow.CS2FlowBlock;
import com.cryo.cs2.nodes.CS2Case;
import com.cryo.cs2.nodes.CS2SwitchFlowBlockJump;

import java.util.ArrayList;
import java.util.List;

public class DecompilerUtils {

	public static class SwitchCase {
		private CS2Case[] annotations;
		private CS2FlowBlock block;
		
		public SwitchCase(CS2Case[] annotations,CS2FlowBlock block) {
			this.annotations = annotations;
			this.block = block;
		}
	
		public CS2Case[] getAnnotations() {
			return annotations;
		}
	
		public CS2FlowBlock getBlock() {
			return block;
		}
		
		public void setBlock(CS2FlowBlock block) {
			this.block = block;
		}
		
		public String toString() {
			StringBuilder bld = new StringBuilder();
			for (int i = 0; i < annotations.length; i++) {
				bld.append(annotations[i]);
				if ((i + 1) < annotations.length)
					bld.append(" AND ");
			}
			bld.append("\t GOTO flow_" + block.getBlockId());
			return bld.toString();
		}
		
	}

	public static SwitchCase[] makeSwitchCases(CS2SwitchFlowBlockJump sbj) {
		SwitchCase[] buff = new SwitchCase[sbj.getCases().length];
		CS2FlowBlock lastBlock = null;
		List<CS2Case> annotations = new ArrayList<>();
		int count = 0;
		for (int i = 0; i < sbj.getCases().length; i++) {
			if (sbj.getTargets()[i] == lastBlock)
				annotations.add(sbj.getDefaultIndex() == i ? new CS2Case() : new CS2Case(sbj.getCases()[i]));
			else {
				if (lastBlock != null) {
					CS2Case[] ann = new CS2Case[annotations.size()];
					int aWrite = 0;
					for (CS2Case a : annotations)
						ann[aWrite++] = a;
					buff[count++] = new SwitchCase(ann,lastBlock);
				}
				lastBlock = sbj.getTargets()[i];
				annotations.clear();
				annotations.add(sbj.getDefaultIndex() == i ? new CS2Case() : new CS2Case(sbj.getCases()[i]));
			}
		}
		if (lastBlock != null) {
			CS2Case[] ann = new CS2Case[annotations.size()];
			int aWrite = 0;
			for (CS2Case a : annotations)
				ann[aWrite++] = a;
			buff[count++] = new SwitchCase(ann,lastBlock);
		}
		
		if (count == buff.length)
			return buff;
		
		SwitchCase[] full = new SwitchCase[count];
		System.arraycopy(buff, 0, full, 0, count);
		return full;
	}
	
}
