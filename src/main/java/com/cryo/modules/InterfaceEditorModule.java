package com.cryo.modules;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.cryo.CS2Editor;
import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.cache.loaders.SpriteDefinitions;
import com.cryo.cache.loaders.interfaces.ComponentSetting;
import com.cryo.cache.loaders.interfaces.ComponentType;
import com.cryo.cache.loaders.interfaces.IComponentDefinitions;
import com.cryo.cache.loaders.interfaces.ModelType;
import com.cryo.cs2.CS2Script;
import com.cryo.utils.Utilities;
import com.google.common.io.ByteStreams;
import com.google.common.net.MediaType;
import com.google.gson.internal.LinkedTreeMap;

import lombok.Cleanup;
import spark.Request;
import spark.Response;

public class InterfaceEditorModule extends WebModule {

    @Override
    public String[] getEndpoints() {
        return new String[] {
            "GET", "/interface-editor",
            "POST", "/interface-editor/load-interface/:id",
            "GET", "/interface-editor/get-render/:id",
            "POST", "/interface-editor/get-render/:id",
            "POST", "/interface-editor/load-component/:interfaceId/:componentId",
            "POST", "/interface-editor/save-defs/:interfaceId/:componentId",
            "POST", "/interface-editor/save-interface/:id"
        };
    }

    private static IComponentDefinitions[] defs;

    private static ArrayList<Integer> edited;

    @Override
    public Object decodeRequest(String endpoint, Request request, Response response) {
        HashMap<String, Object> model = new HashMap<>();
        Properties prop = new Properties();
        sw: switch(endpoint) {
            case "/interface-editor":
                ArrayList<Integer> interfaces = new ArrayList<>();
                for(int i = 0; i < Utilities.getInterfaceDefinitionsSize(); i++) interfaces.add(i);
                model.put("interfaces", interfaces);
                return render("./client/source/interface_editor.jade", model, request, response);
            case "/interface-editor/load-interface/:id":
                String idString = request.params(":id");
                int id;
                try {
                    id = Integer.parseInt(idString);
                } catch(Exception e) {
                    e.printStackTrace();
                    return error("Error parsing id.");
                }
                try {
                    IComponentDefinitions[] defs = IComponentDefinitions.getInterface(id);
                    if(defs == null) return error("Error loading defs for: "+id);
                    InterfaceEditorModule.defs = defs;
                    InterfaceEditorModule.edited = new ArrayList<>();
                    prop.put("defs", defs);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
            case "/interface-editor/load-component/:interfaceId/:componentId":
                int componentId;
                idString = request.params(":componentId");
                try {
                    componentId = Integer.parseInt(idString);
                } catch (Exception e) {
                    e.printStackTrace();
                    return error("Error parsing id.");
                }
                IComponentDefinitions iDefs = InterfaceEditorModule.defs[componentId];
                iDefs.setValues(ComponentSetting.getSettings());
                model.put("settings", ComponentSetting.getSettings());
                prop.put("html", render("./client/source/component_settings.jade", model, request, response));
                break;
            case "/interface-editor/save-defs/:interfaceId/:componentId":
                idString = request.params(":interfaceId");
                String componentString = request.params(":componentId");
                try {
                    id = Integer.parseInt(idString);
                    componentId = Integer.parseInt(componentString);
                } catch(Exception e) {
                    e.printStackTrace();
                    return error("Error parsing ids.");
                }
                if(componentId < 0 || componentId >= InterfaceEditorModule.defs.length) return error("Invalid component id.");
                iDefs = InterfaceEditorModule.defs[componentId];
                String variable = request.queryParams("variable");
                String type = request.queryParams("type");
                String valueString = request.queryParams("value");
                try {
                    for (Field field : iDefs.getClass().getFields()) {
                        if (field.getName().equals(variable)) {
                            field.setAccessible(true);
                            if(!hasChanged(field.get(iDefs), type, variable, valueString)) {
                                prop.put("nochange", true);
                                System.out.println("Did not change");
                                break sw;
                            } else System.out.println("Has changed!");
                            System.out.println(variable+":"+type+" -> "+ valueString);
                            switch (type) {
                                case "int":
                                    field.set(iDefs, Integer.parseInt(valueString));
                                    break;
                                case "string":
                                case "boolean":
                                    System.out.println("Set "+field.getName()+" to "+valueString);
                                    field.set(iDefs, type.equals("boolean") ? Boolean.parseBoolean(valueString) : valueString);
                                    break;
                                case "string[]":
                                    if (valueString.equals("")) {
                                        field.set(iDefs, null);
                                        break;
                                    }
                                    if(valueString.startsWith("["))
                                        valueString = valueString.substring(1, valueString.length()-1);
                                    field.set(iDefs, valueString.split(", ?"));
                                    break;
                                case "int[]":
                                    if (valueString.equals("")) {
                                        field.set(iDefs, null);
                                        break;
                                    }
                                    if (valueString.startsWith("["))
                                        valueString = valueString.substring(1, valueString.length() - 1);
                                    String[] spl = valueString.split(", ?");
                                    field.set(iDefs, Stream.of(spl).map(Integer::parseInt).toArray());
                                    break;
                                case "object[]":
                                    if (valueString.equals("")) {
                                        field.set(iDefs, null);
                                        break;
                                    }
                                    if (valueString.startsWith("["))
                                        valueString = valueString.substring(1, valueString.length() - 1);
                                    spl = valueString.split(", ?");
                                    Object[] arr = new Object[spl.length];
                                    for(int i = 0; i < spl.length; i++) {
                                        if(CS2Script.isInt(spl[i]))
                                            arr[i] = Integer.parseInt(spl[i]);
                                        else if(CS2Script.isLong(spl[i]))
                                            arr[i] = Long.parseLong(spl[i]);
                                        else arr[i] = spl[i];
                                    }
                                    field.set(iDefs, arr);
                                    break;
                                case "options":
                                    Object value = null;
                                    int index = Integer.parseInt(valueString);
                                    if (variable.equals("modelType"))
                                        value = ModelType.values()[index];
                                    else
                                        value = ComponentType.values()[index];
                                    field.set(iDefs, value);
                                    break;
                            }
                        }
                    }
                    edited.add(componentId);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                prop.put("success", true);
                break;
            case "/interface-editor/save-interface/:id":
                idString = request.params(":id");
                try {
                    id = Integer.parseInt(idString);
                } catch (Exception e) {
                    e.printStackTrace();
                    return error("Error parsing id.");
                }
                try {
                    edited.forEach(i -> InterfaceEditorModule.defs[i].write());
                    Cache.STORE.getIndex(IndexType.INTERFACES).rewriteTable();
                    edited.clear();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                prop.put("success", true);
                break;
            case "/interface-editor/get-render/:id":
                System.out.println("called get render");
                boolean showContainers;
                boolean showHidden;
                boolean showModels;
                int panelWidth;
                int panelHeight;
                int selected = -1;
                try {
                    showContainers = Boolean.parseBoolean(request.queryParams("showContainers"));
                    showHidden = Boolean.parseBoolean(request.queryParams("showHidden"));
                    showModels = Boolean.parseBoolean(request.queryParams("showModels"));
                    panelWidth = Integer.parseInt(request.queryParams("panelWidth"));
                    panelHeight = Integer.parseInt(request.queryParams("panelHeight"));
                    id = Integer.parseInt(request.params(":id"));
                    if(request.queryParams().contains("selected"))
                        selected = Integer.parseInt(request.queryParams("selected"));
                } catch(Exception e) {
                    e.printStackTrace();
                    return error("Error loading one of the parameters. Please check console.");
                }
                System.out.println("Creating image for inter: "+id+" size: "+ panelWidth+"x"+panelHeight);
                BufferedImage rendered = IComponentDefinitions.makeInterface(id, 
                        InterfaceEditorModule.defs, showContainers, showHidden, showModels, panelWidth, panelHeight, selected);
                if(rendered == null) return error("Error rendering image.");
                try {
                    ImageIO.write(rendered, "PNG", response.raw().getOutputStream());
                    response.status(200);
                    return "";
                } catch (IOException e) {
                    e.printStackTrace();
                    return error("Error writing to output stream.");
                }
            default: return error("404: Not Found. "+endpoint);
        }
        return CS2Editor.getGson().toJson(prop);
    }

    public boolean hasChanged(Object value, String type, String variable, String valueString) {
        switch(type.toLowerCase()) {
            case "int":
                return Integer.parseInt(valueString) != (int) value;
            case "string":
                return !valueString.equals((String) value);
            case "long":
                return Long.parseLong(valueString) != (long) value;
            case "boolean":
                return Boolean.parseBoolean(valueString) != (boolean) value;
            case "string[]":
                return Arrays.equals((String[]) value, valueString.split(", ?"));
            case "int[]":
                int[] val = Stream.of(valueString.split(", ?")).mapToInt(Integer::parseInt).toArray();
                return Arrays.equals((int[]) value, val);
            case "object[]":
                String[] spl = valueString.split(", ?");
                Object[] vals = new Object[spl.length];
                for(int i = 0; i < spl.length; i++) {
                    if(CS2Script.isInt(spl[i]))
                        vals[i] = Integer.parseInt(spl[i]);
                    else if(CS2Script.isLong(spl[i]))
                        vals[i] = Long.parseLong(spl[i]);
                    else
                        vals[i] = spl[i];
                }
                return Arrays.equals((Object[]) value, vals);
            case "options":
                int ordinal = Integer.parseInt(valueString);
                if(variable.equals("modelType"))
                    return ((ModelType) value).ordinal() == ordinal;
                else
                    return ((ComponentType) value).ordinal() == ordinal;
        }
        return true;
    }

}