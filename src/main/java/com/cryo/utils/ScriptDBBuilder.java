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
    private static HashMap<Integer, ScriptDAO> scripts2;

    public static ScriptDAO getScript(int id) {
        if (!scripts.containsKey(id))
            return null;
        return scripts.get(id);
    }

    public static void saveScript(ScriptDAO dao) {
        scripts.put(dao.getId(), dao);
        save();
    }

    @SuppressWarnings("unchecked")
    public static void load() {
        scripts = new HashMap<>();
        scripts2 = new HashMap<>();
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
            reader = new BufferedReader(new FileReader("./data/scripts-db2.json"));
            builder = new StringBuilder();
            while ((line = reader.readLine()) != null)
                builder.append(line);
            map = CS2Editor.getGson().fromJson(builder.toString(), LinkedTreeMap.class);
            for (String key : map.keySet())
                scripts2.put(Integer.parseInt(key), ScriptDAO.fromProperties(map.get(key)));
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

    public static int[] STRUCT_SCRIPTS = { 
        13,
        983, 997, 1009, 1003, 995, 993, 1007, 
        1001, 991, 999, 979, 1015, 1021, 1017, 
        985, 989, 1011, 987, 1013, 1005, 1019, 
        3354, 
     };

    public static void main(String[] args) {
        CS2Editor.setGson(CS2Editor.buildGson());
        load();
        for(int i = 0; i < STRUCT_SCRIPTS.length; i++) {
            int id = STRUCT_SCRIPTS[i];
            String name = "script_"+id;
            CS2Type[] argTypes = { CS2Type.INT };
            String[] argNames = { "arg0" };
            String[] vNames = {};
            CS2Type returnType = CS2Type.forDesc("script_"+id+"_struct(int;string)");
            System.out.println(returnType);
            ScriptDAO dao = new ScriptDAO(id, name, argTypes, argNames, vNames, returnType);
            saveScript(dao);
        }
    }

    public static void writeProbableReturnType(int id, CS2Type[] args, String[] argNames, CS2Type returnType) {
        String name = "script_" + id;
        String[] vNames = {};
        if(scripts2.containsKey(id)) {
            if(scripts2.get(id).getVariableNames() != null)
                vNames = scripts2.get(id).getVariableNames();
        }
        ScriptDAO dao = new ScriptDAO(id, name, args, argNames, vNames, returnType);
        saveScript(dao);
    }

}