package com.cryo.modules;

import com.cryo.CS2Editor;
import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.cs2.CS2Definitions;
import com.cryo.cs2.CS2Script;
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
                "POST", "/ide/auto-completion/get-instr"};
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
            default: return error("404 Page Not Found");
        }
        return CS2Editor.getGson().toJson(prop);
    }
}
