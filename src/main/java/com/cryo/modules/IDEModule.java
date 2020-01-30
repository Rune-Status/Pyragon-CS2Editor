package com.cryo.modules;

import com.cryo.cache.Cache;
import spark.Request;
import spark.Response;

import java.util.HashMap;

public class IDEModule extends WebModule {

    @Override
    public String[] getEndpoints() {
        return new String[] { "GET", "/ide" };
    }

    @Override
    public Object decodeRequest(String endpoint, Request request, Response response) {
        HashMap<String, Object> model = new HashMap<>();
        switch(endpoint) {
            case "/ide":
                model.put("location", Cache.LOCATION);
                return render("./client/source/ide.jade", model, request, response);
        }
        return error("404 Page Not Found");
    }
}
