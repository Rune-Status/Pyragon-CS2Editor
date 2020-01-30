package com.cryo.modules;

import com.cryo.CS2Editor;
import com.cryo.cache.Cache;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Properties;

public class CacheSelectModule extends WebModule {

    private boolean cacheLoaded;

    @Override
    public String[] getEndpoints() {
        return new String[] { "GET", "/", "POST", "/cache/load",
            "GET", "/cache/loaded"};
    }

    @Override
    public Object decodeRequest(String endpoint, Request request, Response response) {
        HashMap<String, Object> model = new HashMap<>();
        switch(endpoint) {
            case "/":
                if(!request.requestMethod().equals("GET")) return error("Only GET requests are allowed to /");
                model.put("title", "Select Cache Location");
                return render("./client/source/cache_select.jade", model, request, response);
            case "/cache/load":
                if(!request.requestMethod().equals("POST")) return error("Only POST requests are allowed to /cache/load");
                String path = request.queryParams("path");
                if(path == null) {
                    Cache.CACHE_ERROR = true;
                    break;
                }
                try {
                    Cache.init(path);
                } catch(Exception e) {
                    Cache.CACHE_ERROR = true;
                    e.printStackTrace();
                }
                break;
            case "/cache/loaded":
                Properties prop = new Properties();
                if(Cache.CACHE_ERROR) {
                    prop.put("error", true);
                    Cache.CACHE_ERROR = false;
                } else
                    prop.put("loaded", Cache.IS_LOADED);
                return CS2Editor.getGson().toJson(prop);
        }
        return error("404 Page Not Found");
    }
}
