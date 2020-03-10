package com.cryo.cache.loaders.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.cryo.CS2Editor;
import com.google.gson.internal.LinkedTreeMap;

import lombok.Data;
import lombok.Getter;

@Data
public class ComponentSetting {

    @Getter
    private static ArrayList<ComponentSetting> settings;

    private final String name;
    private final String variable;
    private final String type;

    private Object value;

    private HashMap<Integer, String> options;

    public static void load() {
        settings = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(new File("./data/editable_component_variables.json")));
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<LinkedTreeMap<String, Object>> list = CS2Editor.getGson().fromJson(builder.toString(), ArrayList.class);
        for (LinkedTreeMap<String, Object> prop : list)
            settings.add(fromProperties(prop));
    }

    public static ComponentSetting fromProperties(LinkedTreeMap<String, Object> prop) {
        String name = (String) prop.get("name");
        String variable = (String) prop.get("variable");
        String type = (String) prop.get("type");
        ComponentSetting setting = new ComponentSetting(name, variable, type);
        if (type.equals("options")) {
            ArrayList<String> list = (ArrayList<String>) prop.get("options");
            setting.options = new HashMap<>();
            for(int i = 0; i < list.size(); i++)
                setting.options.put(i, list.get(i));
        }
        return setting;
    }

}