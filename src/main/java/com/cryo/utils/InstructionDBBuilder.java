package com.cryo.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

import com.google.gson.internal.LinkedTreeMap;

import lombok.Getter;

import com.cryo.CS2Editor;
import com.cryo.cs2.CS2Instruction;
import static com.cryo.cs2.CS2Instruction.*;
import com.cryo.decompiler.CS2Type;

public class InstructionDBBuilder {

    @Getter
    private static HashMap<Integer, InstructionDAO> instructions;

    public static InstructionDAO getInstruction(int opcode) {
        if (!instructions.containsKey(opcode))
            return null;
        return instructions.get(opcode);
    }

    public static InstructionDAO getInstruction(String name) {
        Optional<InstructionDAO> optional = instructions.values()
                                            .stream()
                                            .filter(i -> i.getName().equalsIgnoreCase(name))
                                            .findFirst();
        if(!optional.isPresent()) return null;
        return optional.get();
    }

    public static void saveInstruction(InstructionDAO dao) {
        instructions.put(dao.getOpcode(), dao);
        save();
    }

    public static void load() {
        instructions = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./data/instructions-db.json"));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            LinkedTreeMap<String, LinkedTreeMap<String, Object>> map = CS2Editor.getGson().fromJson(builder.toString(),
                    LinkedTreeMap.class);
            for (String key : map.keySet())
                instructions.put(Integer.parseInt(key), InstructionDAO.fromProperties(map.get(key)));
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        LinkedTreeMap<Integer, LinkedTreeMap<String, Object>> map = new LinkedTreeMap<>();
        for (Integer key : instructions.keySet())
            map.put(key, instructions.get(key).toProperties());
        String json = CS2Editor.getGson().toJson(map);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("./data/instructions-db.json"));
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CS2Editor.setGson(CS2Editor.buildGson());
        load();
        CS2Instruction instruction = CS2Instruction.IF_ISOPEN;
        int opcode = instruction.getOpcode();
        String name = instruction.name().toLowerCase();
        String[] popOrder = { "ic" };
        String[] argumentNames = { "hash" };
        CS2Type pushType = CS2Type.INT;
        InstructionDAO dao = new InstructionDAO(opcode, name, popOrder, argumentNames, pushType);
        // dao.setCustomPrint("calc(%0 | 1 << %1)");
        saveInstruction(dao);
    }


}