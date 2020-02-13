package com.cryo.modules;

import com.cryo.CS2Editor;
import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.cs2.CS2Definitions;
import com.cryo.cs2.CS2Script;
import com.cryo.decompiler.ast.FunctionNode;
import com.cryo.decompiler.util.FunctionInfo;
import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class IDEModule extends WebModule {

    @Override
    public String[] getEndpoints() {
        return new String[]{
                "GET", "/ide",
                "POST", "/ide/load-script/:id",
                "GET", "/ide/load-script/:id"};
    }

    @Override
    public Object decodeRequest(String endpoint, Request request, Response response) {
        HashMap<String, Object> model = new HashMap<>();
        switch (endpoint) {
            case "/ide":
                try {
                    ArrayList<Properties> scripts = new ArrayList<>();
                    for (int i = 0; i < Cache.STORE.getIndex(IndexType.CS2_SCRIPTS).getLastArchiveId(); i++) {
                        Properties prop = new Properties();
                        prop.put("id", i);
                        String name = "script"+i;
                        if(CS2Editor.getLoaders().get("script-names").containsKey(i))
                            name = CS2Editor.getLoaders().get("script-names").get(i);
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
                Properties prop = new Properties();
                FunctionNode node = null;
                try {
                    CS2Script script = CS2Definitions.getScript(id);
                    if(script == null) return error("Script is null.");
//                    System.out.println("------------------------");
//                    node = CS2Editor.getInstance().loadScript(id);
//                    if(node == null) return error("Error loading node.");
                    String file = script.decompile().toString();
                    System.out.println("D: "+file);
                    prop.put("file", file);
                }   catch(Exception e) {
                    e.printStackTrace();
                }
                prop.put("name", "test");
                return CS2Editor.getGson().toJson(prop);
        }
        return error("404 Page Not Found");
    }

    private static HashMap<String, ArrayList<String>> randomClasses = new HashMap<>();

    static {
        try {
            File dir = new File("./placeholder_code/");
            for (File file : dir.listFiles()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                String name = file.getName().replace(".java", "");
                randomClasses.put(name, new ArrayList<>());
                while ((line = reader.readLine()) != null)
                    randomClasses.get(name).add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
