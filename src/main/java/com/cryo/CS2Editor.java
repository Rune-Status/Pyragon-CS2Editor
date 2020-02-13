package com.cryo;

import com.cryo.cache.Cache;
import com.cryo.cs2.CS2Definitions;
import com.cryo.cs2.CS2Script;
import com.cryo.decompiler.CS2;
import com.cryo.decompiler.CS2Decoder;
import com.cryo.decompiler.CS2Decompiler;
import com.cryo.decompiler.ICS2Provider;
import com.cryo.decompiler.ast.FunctionNode;
import com.cryo.decompiler.util.*;
import com.cryo.modules.WebModule;
import com.cryo.utils.Utilities;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.Getter;
import spark.Spark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

import static spark.Spark.*;
import static spark.Spark.post;

@Data
public class CS2Editor {

    @Getter
    private static CS2Editor instance;

    @Getter
    private static Gson gson;

    @Getter
    private static Properties properties;

    @Getter
    private static HashMap<String, HashMap<Integer, String>> loaders;
    private UnsafeSerializer serializer;

    private InstructionsDatabase instructionsDB;
    private ConfigsDatabase configsDB;
    private FunctionDatabase opcodesDB;
    private FunctionDatabase scriptsDB;
    private CS2Decompiler decompiler;

    public void start() {
        gson = buildGson();
        loadProperties();
        loadLoaders();
        serializer = new UnsafeSerializer();
        reloadDatabases();
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
            CS2Script script = CS2Definitions.getScript(99);
            if (script == null)
                System.out.println("Error getting.");
            else
                System.out.println(script.decompile().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadDatabases() {
        instructionsDB = new InstructionsDatabase(new File("./instructions_db.ini"));
        configsDB = new ConfigsDatabase(new File("./configs_db.ini"), new File("./bitconfigs_db.ini")); // TODO
        opcodesDB = new FunctionDatabase(new File("./opcodes_db.ini"));
        scriptsDB = new FunctionDatabase(new File("./scripts_db.ini"));

        decompiler = new CS2Decompiler(instructionsDB, configsDB, opcodesDB, scriptsDB, new ICS2Provider() {
            @Override
            public CS2 getCS2(InstructionsDatabase idb, ConfigsDatabase cdb, FunctionDatabase sdb, FunctionDatabase odb, int id) {
                try {
                    return CS2Decoder.readScript(idb, cdb, id);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    return null;
                }
            }
        });
        printUnder150();
    }

    public void printUnder150() {
        for(int i = 0; i < 150; i++) {
            InstructionInfo info = instructionsDB.getByUnscrampled(i);
            if(info != null)
                System.out.println(i+" - "+info.getName());
        }
    }

    public FunctionNode loadScript(int scriptID) {
        try {
            return decompiler.decompile(scriptID);
        }
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static Gson buildGson() {
        return new GsonBuilder()
                .serializeNulls()
                .setVersion(1.0)
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
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
