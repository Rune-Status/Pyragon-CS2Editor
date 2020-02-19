package com.cryo.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import com.cryo.CS2Editor;
import com.cryo.cs2.CS2Script;
import com.cryo.cs2.nodes.CS2Function;
import com.cryo.decompiler.CS2Type;
import com.google.gson.internal.LinkedTreeMap;

public class ScriptDBBuilder {

    private static HashMap<Integer, ScriptDAO> scripts;

    public static ScriptDAO getScript(int id) {
        if (!scripts.containsKey(id))
            return null;
        return scripts.get(id);
    }

    public static void saveScript(ScriptDAO dao) {
        scripts.put(dao.getId(), dao);
        save();
    }

    public static void load() {
        scripts = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./data/scripts-db.json"));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            LinkedTreeMap<String, LinkedTreeMap<String, Object>> map = CS2Editor.getGson().fromJson(builder.toString(),
                    LinkedTreeMap.class);
            for (String key : map.keySet())
                scripts.put(Integer.parseInt(key), ScriptDAO.fromProperties(map.get(key)));
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        LinkedTreeMap<Integer, LinkedTreeMap<String, Object>> map = new LinkedTreeMap<>();
        for (Integer key : scripts.keySet())
            map.put(key, scripts.get(key).toProperties());
        String json = CS2Editor.getGson().toJson(map);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("./data/scripts-db.json"));
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
        int id = 26;
        String name = "script_26";
        CS2Type[] argTypes = {  };
        String[] argNames = {  };
        String[] vNames = { "slot", "ivar1", "ivar2", "ivar3", "ivar4", "ivar5", "ivar6", "ivar7" };
        CS2Type returnType = CS2Type.VOID;
        ScriptDAO dao = new ScriptDAO(id, name, argTypes, argNames, vNames, returnType);
        saveScript(dao);
    }

}