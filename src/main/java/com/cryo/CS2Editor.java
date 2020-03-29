package com.cryo;

import com.cryo.cache.Cache;
import com.cryo.cache.loaders.CS2Definitions;
import com.cryo.cache.loaders.interfaces.ComponentSetting;
import com.cryo.cs2.CS2Script;
import com.cryo.modules.WebModule;
import com.cryo.utils.InstructionDBBuilder;
import com.cryo.utils.ScriptDAO;
import com.cryo.utils.ScriptDBBuilder;
import com.cryo.utils.Utilities;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import spark.Spark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import static spark.Spark.*;
import static spark.Spark.post;

@Data
public class CS2Editor {

    @Getter
    private static CS2Editor instance;

    @Getter
    @Setter
    private static Gson gson;

    @Getter
    private static Properties properties;

    @Getter
    private static HashMap<String, HashMap<Integer, String>> loaders;

    public void start() {
        gson = buildGson();
        loadProperties();
        loadLoaders();
        InstructionDBBuilder.load();
        ScriptDBBuilder.load();
        ComponentSetting.load();
        try {
            Cache.init("F:\\workspace\\github\\darkan-server\\data\\cache\\");
            port(8087);
            staticFiles.externalLocation("client/source/");
            staticFiles.expireTime(0);
            staticFiles.header("Access-Control-Allow-Origin", "*");
            for (Class<?> c : Utilities.getClasses("com.cryo.modules")) {
                if (!WebModule.class.isAssignableFrom(c))
                    continue;
                if (c.getName().equals("com.cryo.modules.WebModule"))
                    continue;
                Object o = c.getConstructor().newInstance();
                if (!(o instanceof WebModule))
                    continue;
                WebModule module = (WebModule) o;
                int i = 0;
                while (i < module.getEndpoints().length) {
                    String method = module.getEndpoints()[i++];
                    String path = module.getEndpoints()[i++];
                    if (method.equals("GET"))
                        get(path, (req, res) -> module.decodeRequest(path, req, res));
                    else
                        post(path, (req, res) -> module.decodeRequest(path, req, res));
                }
            }
            System.out.println("Server started on " + java.net.InetAddress.getLocalHost() + ":" + Spark.port());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Gson buildGson() {
        return new GsonBuilder()
                .serializeNulls()
                .setVersion(1.0)
                .disableHtmlEscaping()
                //.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .create();
    }

    public void loadProperties() {
        File file = new File("props.json");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder json = new StringBuilder();
            while ((line = reader.readLine()) != null) json.append(line);
            properties = getGson().fromJson(json.toString(), Properties.class);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadLoaders() {
        loaders = new HashMap<>();
        loadLoader("script-names");
    }

    public static void loadLoader(String name) {
        loaders.put(name, new HashMap<>());
        BufferedReader reader;
        try  {
            reader = new BufferedReader(new FileReader("./data/"+name+".tsv"));
            String line;
            while((line = reader.readLine()) != null) {
                String[] split = line.split("\t");
                loaders.get(name).put(Integer.parseInt(split[0]), split[1]);
            }
            reader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        instance = new CS2Editor();
        instance.start();
    }
}
