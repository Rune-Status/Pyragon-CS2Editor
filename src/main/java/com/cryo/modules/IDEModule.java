package com.cryo.modules;

import com.cryo.CS2Editor;
import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.cs2.CS2Definitions;
import com.cryo.cs2.CS2Script;
import com.cryo.decompiler.CS2Type;
import com.cryo.utils.InstructionDAO;
import com.cryo.utils.InstructionDBBuilder;
import com.cryo.utils.ScriptDAO;
import com.cryo.utils.ScriptDBBuilder;

import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class IDEModule extends WebModule {

    @Override
    public String[] getEndpoints() {
        return new String[]{
                "GET", "/ide",
                "POST", "/ide/load-script/:id",
                "GET", "/ide/load-script/:id",
                "POST", "/ide/auto-completion/get-instr",
                "POST", "/ide/edit-script-info",
                "POST", "/ide/save-script-info",
                "POST", "/ide/reload-instruction-info",
                "POST", "/ide/reload-script-info",
                "POST", "/ide/recompile"
                };
    }

    @Override
    public Object decodeRequest(String endpoint, Request request, Response response) {
        HashMap<String, Object> model = new HashMap<>();
        Properties prop = new Properties();
        switch (endpoint) {
            case "/ide":
                try {
                    ArrayList<Properties> scripts = new ArrayList<>();
                    for (int i = 0; i < Cache.STORE.getIndex(IndexType.CS2_SCRIPTS).getLastArchiveId(); i++) {
                        prop = new Properties();
                        prop.put("id", i);
                        String name = "script"+i;
                        ScriptDAO dao = ScriptDBBuilder.getScript(i);
                        if(dao != null) name = dao.getName();
                        prop.put("name", name);
                        scripts.add(prop);
                    }
                    model.put("location", Cache.LOCATION);
                    model.put("scripts", scripts);
                    return render("./client/source/ide.jade", model, request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                    return error("Error loading page.");
                }
            case "/ide/load-script/:id":
                String idString = request.params(":id");
                int id;
                try {
                    id = Integer.parseInt(idString);
                } catch (Exception e) {
                    return error("Error parsing id.");
                }
                try {
                    CS2Script script = CS2Definitions.getScript(id);
                    if(script == null) return error("Script is null.");
                    ScriptDAO info = ScriptDBBuilder.getScript(id);
                    if (info == null) {
                        info = ScriptDAO.fromScript(script);
                        if (info == null)
                            return error("Error calling script: " + id);
                        ScriptDBBuilder.saveScript(info);
                    }
                    String file = script.decompile().getScope().printNoBrace();
                    prop.put("file", file);
                }   catch(Exception e) {
                    e.printStackTrace();
                }
                break;
            case "/ide/auto-completion/get-instr":
                String prefix = request.queryParams("prefix");
                if(prefix == null) return error("No prefix sent.");
                ArrayList<String> results = new ArrayList<>();
                for(InstructionDAO dao : InstructionDBBuilder.getInstructions().values()) {
                    if(!dao.getName().startsWith(prefix)) continue;
                    results.add(dao.getName());
                }
                prop.put("results", results);
                break;
            case "/ide/save-script-info":
                try {
                    idString = request.queryParams("id");
                    try {
                        id = Integer.parseInt(idString);
                    } catch (Exception e) {
                        return error("Error parsing script ID");
                    }
                    String name = request.queryParams("name");
                    String arguments = request.queryParams("arguments");
                    String variables = request.queryParams("variables");
                    String returnTypeS = request.queryParams("returnType");
                    CS2Type[] argumentTypes;
                    String[] argumentNames;
                    String[] variableNames;
                    if(name == null || name.replaceAll("\\s", "").equals(""))
                        name = "script"+id;
                    if(arguments == null || arguments.replaceAll("\\s", "").equals("")) {
                        argumentTypes = new CS2Type[0];
                        argumentNames = new String[0];
                    } else {
                        String[] split = arguments.split(", ?");
                        argumentTypes = new CS2Type[split.length];
                        argumentNames = new String[split.length];
                        for(int i = 0; i < split.length; i++) {
                            String[] tN = split[i].split(" ");
                            if(tN.length != 2) return error("Invalid argument2: "+split[i]);
                            argumentTypes[i] = CS2Script.getCS2Type(tN[0]);
                            argumentNames[i] = tN[1];
                        }
                    }
                    if(variables == null || variables.replaceAll("\\s", "").equals(""))
                        variableNames = new String[0];
                    else {
                        String[] split = arguments.split(", ?");
                        variableNames = new String[split.length];
                        for(int i = 0; i < variableNames.length; i++) {
                            if(split[i].contains(" ")) return error("Invalid variable: "+ split[i]);
                            variableNames[i] = split[i];
                        }
                    }
                    CS2Type returnType = CS2Script.getCS2Type(returnTypeS);
                    ScriptDAO dao = new ScriptDAO(id, name, argumentTypes, argumentNames, variableNames, returnType);
                    ScriptDBBuilder.saveScript(dao);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
            case "/ide/edit-script-info":
                idString = request.queryParams("id");
                try {
                    id = Integer.parseInt(idString);
                } catch(Exception e) {
                    return error("Error parsing script ID");
                }
                ScriptDAO dao = ScriptDBBuilder.getScript(id);
                if(dao == null) return error("Error loading script.");
                model.put("script", dao);
                try {
                    prop.put("html", render("./client/source/edit_script_info.jade", model, request, response));
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
            case "/ide/reload-instruction-info":
                InstructionDBBuilder.load();
                break;
            case "/ide/reload-script-info":
                ScriptDBBuilder.load();
                break;
            case "/ide/recompile":
                String contents = request.queryParams("contents");
                idString = request.queryParams("id");
                try {
                    id = Integer.parseInt(idString);
                } catch(Exception e) {
                    return error("Error parsing script ID");
                }
                if(contents == null || contents.equals(""))
                    return error("Invalid contents");
                CS2Script script;
                try {
                    script = CS2Definitions.getScript(id);
                } catch(Exception e) {
                    e.printStackTrace();
                    return error("Error getting script.");
                }
                if (script == null)
                    return error("Script is null.");
                try {
                    String result = CS2Script.recompile(contents);
                    if (result != null)
                        return error(result);
                } catch(Exception e) {
                    e.printStackTrace();
                    return error("Error compiling script. Check console.");
                }
                prop.put("success", true);
                break;
            default: return error("404 Page Not Found");
        }
        return CS2Editor.getGson().toJson(prop);
    }
}
