package com.cryo.modules;

import com.cryo.CS2Editor;
import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.exceptions.JadeCompilerException;
import lombok.Synchronized;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public abstract class WebModule {

    public abstract String[] getEndpoints();

    public abstract Object decodeRequest(String endpoint, Request request, Response response);

    @Synchronized
    public static String render(String file, HashMap<String, Object> model, Request request, Response response) {
        try {
            return Jade4J.render(file, model);
        } catch (JadeCompilerException | IOException e) {
            e.printStackTrace();
            return error("Error loading module template.");
        }
    }

    public static String error(Object error) {
        Properties prop = new Properties();
        prop.put("success", false);
        prop.put("error", error);
        return CS2Editor.getGson().toJson(prop);
    }
}
