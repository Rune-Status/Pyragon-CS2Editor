package com.cryo.utils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cryo.cs2.CS2Script;
import com.cryo.cs2.nodes.CS2Function;
import com.cryo.decompiler.CS2Type;
import com.google.gson.internal.LinkedTreeMap;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScriptDAO {

    private int id;
    private String name;
    private CS2Type[] argumentTypes;
    private String[] argumentNames;
    private String[] variableNames;
    private CS2Type returnType;

    public LinkedTreeMap<String, Object> toProperties() {
        LinkedTreeMap<String, Object> map = new LinkedTreeMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("argTypes", Stream.of(argumentTypes).map(t -> t.toString()).collect(Collectors.joining(",")));
        map.put("argNames", String.join(",", argumentNames));
        if(variableNames != null)
            map.put("vNames", String.join(",", variableNames));
        map.put("returnType", returnType.toString());
        return map;
    }

    public static String rtToString(CS2Type returnType) {
        if(returnType == CS2Type.INT)
            return "i";
        else if(returnType == CS2Type.STRING)
            return "s";
        else if(returnType == CS2Type.LONG)
            return "l";
        else if(returnType == CS2Type.VOID)
            return "v";
        return "i";
    }

    public static CS2Type getRTFromString(String rt) {
        switch(rt) {
            case "i": return CS2Type.INT;
            case "s": return CS2Type.STRING;
            case "l": return CS2Type.LONG;
            case "v": return CS2Type.VOID;
            default: return CS2Type.INT;
        }
    }

    public static ScriptDAO fromProperties(LinkedTreeMap<String, Object> map) {
        int id = (int) ((double) map.get("id"));
        String name = (String) map.get("name");
        String argTypesE = (String) map.get("argTypes");
        CS2Type[] argTypes;
        if(argTypesE.equals("")) argTypes = new CS2Type[0];
        else {
            String[] argTypesS = argTypesE.split(",");
            argTypes = new CS2Type[argTypesS.length];
            for(int i = 0; i < argTypes.length; i++) {
                argTypes[i] = CS2Type.forDesc(argTypesS[i]);
                if(id == 5372)
                    System.out.println("Argument type for "+argTypesS[i]+": "+argTypes[i]);
            }
        }
        String[] argNames = ((String) map.get("argNames")).split(",");
        String vNamesS = map.containsKey("vNames") ? (String) map.get("vNames") : null;
        String[] vNames = (vNamesS == null || vNamesS.equals("")) ? null : vNamesS.split(",");
        CS2Type returnType = CS2Type.forDesc((String) map.get("returnType"));
        return new ScriptDAO(id, name, argTypes, argNames, vNames, returnType);
    }

    public String getJoinedArguments() {
        StringBuilder builder = new StringBuilder();
        if(argumentTypes == null) return "";
        for(int i = 0; i < argumentTypes.length; i++) {
            builder.append(argumentTypes[i].toString());
            builder.append(" "+argumentNames[i]);
            if(i != argumentTypes.length-1)
                builder.append(", ");
        }
        return builder.toString();
    }

    public String getJoinedVariableNames() {
        StringBuilder builder = new StringBuilder();
        if(variableNames == null) return "";
        for (int i = 0; i < variableNames.length; i++) {
            builder.append(variableNames[i]);
            if (i != variableNames.length - 1)
                builder.append(", ");
        }
        return builder.toString();
    }

    public static ScriptDAO fromScript(CS2Script script) {
        CS2Function function = script.decompile();
        return new ScriptDAO(script.getId(), script.getName(), function.getArgumentTypes(), function.getArgumentNames(),
                new String[0], function.getReturnType());
    }

}