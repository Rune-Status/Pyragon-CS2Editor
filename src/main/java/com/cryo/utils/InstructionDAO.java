package com.cryo.utils;

import java.util.Properties;

import com.cryo.decompiler.CS2Type;
import com.google.gson.internal.LinkedTreeMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class InstructionDAO {

    private final int opcode;
    private final String name;
    private final String[] popOrder;
    private final String[] argumentNames;
    private final CS2Type pushType;

    private String customPrint;

    public CS2Type[] getArgumentTypes() {
        CS2Type[] argumentTypes = new CS2Type[popOrder.length];
        for(int i = 0; i < popOrder.length; i++)
            argumentTypes[i] = ScriptDAO.getRTFromString(popOrder[i]);
        return argumentTypes;
    }

    public LinkedTreeMap<String, Object> toProperties() {
        LinkedTreeMap<String, Object> map = new LinkedTreeMap<String, Object>();
        map.put("opcode", opcode);
        map.put("name", name);
        map.put("popOrder", popOrder == null ? null : String.join(",", popOrder));
        map.put("argNames", argumentNames == null ? null : String.join(",", argumentNames));
        map.put("pushType", ScriptDAO.rtToString(pushType));
        if(customPrint != null)
            map.put("customPrint", customPrint);
        return map;
    }

    public static InstructionDAO fromProperties(LinkedTreeMap<String, Object> props) {
        int opcode = (int) ((double) props.get("opcode"));
        String name = (String) props.get("name");
        String popOrderS = props.containsKey("popOrder") ? (String) props.get("popOrder") : null;
        String[] popOrder = popOrderS == null || popOrderS.equals("") ? new String[] {} : popOrderS.split(",");
        String argumentNamesS = props.containsKey("argNames") ? (String) props.get("argNames") : null;
        String[] argumentNames = argumentNamesS == null || argumentNamesS.equals("") ? new String[] {} : argumentNamesS.split(",");
        CS2Type pushType = ScriptDAO.getRTFromString((String) props.get("pushType"));
        InstructionDAO dao = new InstructionDAO(opcode, name, popOrder, argumentNames, pushType);
        if(props.containsKey("customPrint"))
            dao.setCustomPrint((String) props.get("customPrint"));
        return dao;
    }

    public String getJoinedPopOrder() {
        return String.join(",", popOrder);
    }

    public String getJoinedArgumentNames() {
        return String.join(",", argumentNames);
    }

}