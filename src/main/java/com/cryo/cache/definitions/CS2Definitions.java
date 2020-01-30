package com.cryo.cache.definitions;

import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.cache.io.InputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class CS2Definitions {
	
	private static HashMap<Integer, CS2Script> scripts = new HashMap<Integer, CS2Script>();
	
//	public static void main(String[] args) throws IOException {
//		Cache.init();
//		int id = 3039;
////		for (int i = 0;i < Cache.STORE.getIndex(IndexType.CS2_SCRIPTS).getLastArchiveId();i++) {
////			CS2Script s = getScript(i);
////			if (s == null)
////				continue;
////			for (int x = 0;x < s.operations.length;x++) {
////				if (s.operations[x] == CS2Instruction.DETAIL_MUSICVOL) {
////					System.out.println(i);
////					System.out.println(Arrays.toString(s.operations));
////					id = i;
////					break;
////				}
////			}
////		}
////		if (id == 0)
////			return;
//
//
//		CS2Script script = getScript(id);
//		System.out.println(script);
//		System.out.println("script = CS2Definitions.getScript(" + id + ");");
//		System.out.println(Arrays.toString(script.arguments));
//		for (int i = 0;i < script.operations.length;i++) {
//			System.out.println("["+i+"]: " + script.getOpString(i));
//		}
//
//
////		System.out.println("script.write(Cache.STORE);");
////		printCS2RenameProgress();
//	}
	
	public static void printCS2RenameProgress() {
		int total = CS2Instruction.values().length;
		int identified = 0;
		for (CS2Instruction instr : CS2Instruction.values()) {
			if (!instr.name().contains("instr"))
				identified++;
		}
		System.out.println("-CS2 Instruction Progress-");
		System.out.println("Instruction count: " + total);
		System.out.println("Unidentified: " + (total-identified));
		System.out.println("Identified: " + identified + " ("+Math.round(((double) identified / (double) total * 100.0))+"%)");
	}
	
	public static void verify() {
		int correct = 0;
		int scriptCount = 0;
		for (int i = 0; i < Cache.STORE.getIndex(IndexType.CS2_SCRIPTS).getLastArchiveId(); i++) {
			CS2Script script = getScript(i);
			CS2Script reCoded = new CS2Script(new InputStream(script.encode()));
			if (script.equals(reCoded))
				correct++;
			scriptCount++;
		}
		System.out.println(correct+"/"+scriptCount);
	}
	
	public static CS2Script getScript(int scriptId) {
		if (scripts.containsKey(scriptId)) {
			return scripts.get(scriptId);
		}
		if (Cache.STORE.getIndex(IndexType.CS2_SCRIPTS).archiveExists(scriptId)) {
			CS2Script script = new CS2Script(new InputStream(Cache.STORE.getIndex(IndexType.CS2_SCRIPTS).getArchive(scriptId).getData()));
			script.id = scriptId;
			scripts.put(scriptId, script);
			return script;
		}
		return null;
	}

}
