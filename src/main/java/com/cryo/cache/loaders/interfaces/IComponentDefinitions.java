package com.cryo.cache.loaders.interfaces;

import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.cache.io.InputStream;
import com.cryo.cache.io.OutputStream;
import com.cryo.cache.io.Stream;
import com.cryo.cache.loaders.ImageUtils;
import com.cryo.cache.loaders.SpriteDefinitions;
import com.cryo.utils.InterfaceBuilderException;
import com.cryo.utils.Utilities;

import de.neuland.jade4j.lexer.token.Else;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

public class IComponentDefinitions {

	private static IComponentDefinitions[][] COMPONENT_DEFINITIONS;
	private static IFTargetParams GLOBAL_SETTINGS = new IFTargetParams(0, -1);

	@SuppressWarnings("rawtypes")
	public Hashtable aHashTable4823;
	public ComponentType type;
	public String name;
	public int contentType = 0;
    public int positionX;
    public int positionY;
	public int basePositionX = 0;
	public int basePositionY = 0;
	public int baseWidth = 0;
	public int baseHeight = 0;
	public byte aspectWidthType = 0;
	public byte aspectHeightType = 0;
	public byte aspectXType = 0;
	public byte aspectYType = 0;
	public int parent = -1;
	public boolean hidden = false;
	public int scrollWidth = 0;
	public int scrollHeight = 0;
	public boolean noClickThrough = false;
	public int spriteId = -1;
	public int angle2d = 0;
	public ModelType modelType = ModelType.RAW_MODEL;
	public int modelId;
	public boolean tiling = false;
	public int fontId = -1;
	public String text = "";
	public int color = 0;
	public boolean alpha = false;
	public int transparency = 0;
	public int borderThickness = 0;
	public int anInt1324 = 0;
	public int anInt1358 = 0;
	public int textHorizontalAli = 0;
	public int textVerticalAli = 0;
	public int lineWidth = 1;
	public boolean hasOrigin;
	public boolean monospaced = true;
	public boolean filled = false;
	public byte[][] aByteArrayArray1366;
	public byte[][] aByteArrayArray1367;
	public int[] anIntArray1395;
	public int[] anIntArray1267;
	public String useOnName = "";
	public boolean vFlip;
	public boolean shadow = false;
	public boolean lineDirection = false;
	public String[] optionNames;
	public boolean usesOrthogonal = false;
	public int multiline = 0;
	public int[] opCursors;
	public boolean hFlip;
	public String opName;
	public boolean aBool1345 = false;
	public boolean aBool1424;
	public int anInt1380;
	public int anInt1381;
	public int anInt1382;
	public String useOptionString = "";
	public int originX = 0;
	public int originY = 0;
	public int spritePitch = 0;
	public int spriteRoll = 0;
	public int spriteYaw = 0;
	public int spriteScale = 100;
	public boolean clickMask = true;
	public int originZ = 0;
	public int animation = -1;
	public int targetOverCursor = -1;
	public int mouseOverCursor = -1;
	public IFTargetParams targetParams = GLOBAL_SETTINGS;
	public int aspectWidth = 0;
	public int targetLeaveCursor = -1;
	public Object[] onLoadScript;
	public Object[] onMouseHoverScript;
	public Object[] onMouseLeaveScript;
	public Object[] anObjectArray1396;
	public Object[] anObjectArray1400;
	public Object[] anObjectArray1397;
	public Object[] mouseLeaveScript;
	public Object[] anObjectArray1387;
	public Object[] anObjectArray1409;
	public Object[] params;
	public int aspectHeight = 0;
	public Object[] anObjectArray1393;
	public Object[] popupScript;
	public Object[] anObjectArray1386;
	public Object[] anObjectArray1319;
	public Object[] anObjectArray1302;
	public Object[] anObjectArray1389;
	public Object[] anObjectArray1451;
	public Object[] anObjectArray1394;
	public Object[] anObjectArray1412;
	public Object[] anObjectArray1403;
	public Object[] anObjectArray1405;
	public int[] varps;
	public int[] mouseLeaveArrayParam;
	public int[] anIntArray1402;
	public int[] anIntArray1315;
	public int[] anIntArray1406;
	public Object[] anObjectArray1413;
	public Object[] anObjectArray1292;
	public Object[] anObjectArray1415;
	public Object[] anObjectArray1416;
	public Object[] anObjectArray1383;
	public Object[] anObjectArray1419;
	public Object[] anObjectArray1361;
	public Object[] anObjectArray1421;
	public Object[] anObjectArray1346;
	public Object[] anObjectArray1353;
	public Object[] anObjectArray1271;
	public boolean usesScripts;
	public int uid = -1;
	public int anInt1288 = -1;
	public int x = 0;
	public int y = 0;
	public int width = 0;
	public int height = 0;
	public int anInt1289 = 1;
	public int anInt1375 = 1;
	public int scrollX = 0;
	public int scrollY = 0;
	public int anInt1339 = -1;
	public int anInt1293 = 0;
	public int anInt1334 = 0;
	public int anInt1335 = 2;
	public int interfaceId = -1;
	public int componentId = -1;
    public int revision;
    public int typeId;
    public boolean bool_6;
    public int oneCursor;
    public int hash;
    public int menuOptionsCount;

	public static void main(String[] args) throws IOException {
		Cache.init("F:\\workspace\\github\\darkan-server\\data\\cache\\");
		COMPONENT_DEFINITIONS = new IComponentDefinitions[Utilities.getInterfaceDefinitionsSize()][];

		for (int id = 0;id < COMPONENT_DEFINITIONS.length;id++) {
			IComponentDefinitions[] defs = getInterface(id);
			for (int comp = 0;comp < defs.length;comp++) {
				if (defs[comp].text != null && defs[comp].text.contains("Superheat")) {
					System.out.println("Interface: " + id + ", " + comp + " ["+defs[comp].text+"]");
				}
			}
		}
//		IComponentDefinitions[] defs = getInterface(13);
//		for (IComponentDefinitions def : defs) {
//			//if (def.baseWidth != 0)
//			//	System.out.println(def.uid + " - " + def.baseWidth);
//			if (def.onLoadScript != null)
//				System.out.println(def);
//		}	
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		IComponentDefinitions def = new IComponentDefinitions();

		result.append(this.getClass().getName());
		result.append(" {");
		result.append(newLine);

		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()))
				continue;
			try {
				Object f1 = Utilities.getFieldValue(this, field);
				Object f2 = Utilities.getFieldValue(def, field);
				if (f1 == f2 || f1.equals(f2))
					continue;
				result.append("  ");
				result.append(field.getType().getCanonicalName() + " " + field.getName() + ": ");
				result.append(Utilities.getFieldValue(this, field));
			} catch (Throwable ex) {
				System.out.println(ex);
			}
			result.append(newLine);
		}
		result.append("}");

		return result.toString();
	}

	public static IComponentDefinitions getInterfaceComponent(int id, int component) {
		IComponentDefinitions[] inter = getInterface(id);
		if (inter == null || component >= inter.length)
			return null;
		return inter[component];
	}

	public static IComponentDefinitions[] getInterface(int id) {
		if (COMPONENT_DEFINITIONS == null)
			COMPONENT_DEFINITIONS = new IComponentDefinitions[Utilities.getInterfaceDefinitionsSize()][];
		if (id >= COMPONENT_DEFINITIONS.length)
			return null;
		if (COMPONENT_DEFINITIONS[id] == null) {
			COMPONENT_DEFINITIONS[id] = new IComponentDefinitions[Utilities.getInterfaceDefinitionsComponentsSize(id)];
			for (int i = 0; i < COMPONENT_DEFINITIONS[id].length; i++) {
				byte[] data = Cache.STORE.getIndex(IndexType.INTERFACES).getFile(id, i);
				if (data != null) {
					IComponentDefinitions defs = COMPONENT_DEFINITIONS[id][i] = new IComponentDefinitions();
					defs.uid = i + (id << 16);
					defs.interfaceId = id;
					defs.componentId = i;
					if (data[0] != -1) {
						throw new IllegalStateException("if1");
					}
					defs.decode(new InputStream(data));
				}
			}
		}
		return COMPONENT_DEFINITIONS[id];
	}

    public void write() {
        Cache.STORE.getIndex(IndexType.INTERFACES).putFile(interfaceId, componentId, encode());
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	final void decode(InputStream stream) {
		revision = stream.readUnsignedByte();
		if (revision == 255) {
			revision = -1;
		}
		typeId = stream.readUnsignedByte();
		if ((typeId & 0x80) != 0) {
			typeId &= 0x7f;
			this.name = stream.readString();
		}
		this.type = ComponentType.forId(typeId);
		this.contentType = stream.readUnsignedShort();
		this.basePositionX = stream.readShort();
		this.basePositionY = stream.readShort();
		this.baseWidth = stream.readUnsignedShort();
		this.baseHeight = stream.readUnsignedShort();
		this.aspectWidthType = (byte) stream.readByte();
		this.aspectHeightType = (byte) stream.readByte();
		this.aspectXType = (byte) stream.readByte();
		this.aspectYType = (byte) stream.readByte();
		this.parent = stream.readUnsignedShort();
		if (this.parent == 65535) {
			this.parent = -1;
		} else {
			this.parent += this.uid & ~0xffff;
		}
		int i_4 = stream.readUnsignedByte();
		this.hidden = (i_4 & 0x1) != 0;
		if (revision >= 0) {
			this.noClickThrough = (i_4 & 0x2) != 0;
		}
		if (this.type == ComponentType.CONTAINER) {
			this.scrollWidth = stream.readUnsignedShort();
			this.scrollHeight = stream.readUnsignedShort();
			if (revision < 0) {
				this.noClickThrough = stream.readUnsignedByte() == 1;
			}
		}
		if (this.type == ComponentType.SPRITE) {
			this.spriteId = stream.readInt();
			this.angle2d = stream.readUnsignedShort();
			int flag2 = stream.readUnsignedByte();
			this.tiling = (flag2 & 0x1) != 0;
			this.alpha = (flag2 & 0x2) != 0;
			this.transparency = stream.readUnsignedByte();
			this.borderThickness = stream.readUnsignedByte();
			this.anInt1324 = stream.readInt();
			this.vFlip = stream.readUnsignedByte() == 1;
			this.hFlip = stream.readUnsignedByte() == 1;
			this.color = stream.readInt();
			if (revision >= 3)
				this.clickMask = stream.readUnsignedByte() == 1;
		}
		if (this.type == ComponentType.MODEL) {
			this.modelType = ModelType.RAW_MODEL;
			this.modelId = stream.readBigSmart();
			int flag2 = stream.readUnsignedByte();
			bool_6 = (flag2 & 0x1) == 1;
			this.hasOrigin = (flag2 & 0x2) == 2;
			this.usesOrthogonal = (flag2 & 0x4) == 4;
			this.aBool1345 = (flag2 & 0x8) == 8;
			if (bool_6) {
				this.originX = stream.readShort();
				this.originY = stream.readShort();
				this.spritePitch = stream.readUnsignedShort();
				this.spriteRoll = stream.readUnsignedShort();
				this.spriteYaw = stream.readUnsignedShort();
				this.spriteScale = stream.readUnsignedShort();
			} else if (this.hasOrigin) {
				this.originX = stream.readShort();
				this.originY = stream.readShort();
				this.originZ = stream.readShort();
				this.spritePitch = stream.readUnsignedShort();
				this.spriteRoll = stream.readUnsignedShort();
				this.spriteYaw = stream.readUnsignedShort();
				this.spriteScale = stream.readShort();
			}
			this.animation = stream.readBigSmart();
			if (this.aspectWidthType != 0) {
				this.aspectWidth = stream.readUnsignedShort();
			}
			if (this.aspectHeightType != 0) {
				this.aspectHeight = stream.readUnsignedShort();
			}
		}
		if (this.type == ComponentType.TEXT) {
			this.fontId = stream.readBigSmart();
			if (revision >= 2) {
				this.monospaced = stream.readUnsignedByte() == 1;
			}
			this.text = stream.readString();
			if (this.text.toLowerCase().contains("runescape")) {
				this.text = this.text.replace("runescape", "Darkan");
				this.text = this.text.replace("RuneScape", "Darkan");
				this.text = this.text.replace("Runescape", "Darkan");
			}
			this.anInt1358 = stream.readUnsignedByte();
			this.textHorizontalAli = stream.readUnsignedByte();
			this.textVerticalAli = stream.readUnsignedByte();
			this.shadow = stream.readUnsignedByte() == 1;
			this.color = stream.readInt();
			this.transparency = stream.readUnsignedByte();
			if (revision >= 0) {
				this.multiline = stream.readUnsignedByte();
			}
		}
		if (this.type == ComponentType.FIGURE) {
			this.color = stream.readInt();
			this.filled = stream.readUnsignedByte() == 1;
			this.transparency = stream.readUnsignedByte();
		}
		if (this.type == ComponentType.LINE) {
			this.lineWidth = stream.readUnsignedByte();
			this.color = stream.readInt();
			this.lineDirection = stream.readUnsignedByte() == 1;
		}
		int optionMask = stream.read24BitUnsignedInteger();
		hash = stream.readUnsignedByte();
		int optionsLength;
		if (hash != 0) {
			this.aByteArrayArray1366 = new byte[11][];
			this.aByteArrayArray1367 = new byte[11][];
			this.anIntArray1395 = new int[11];
			for (; hash != 0; hash = stream.readUnsignedByte()) {
				optionsLength = (hash >> 4) - 1;
				hash = hash << 8 | stream.readUnsignedByte();
				hash &= 0xfff;
				if (hash == 4095) {
					hash = -1;
				}
				byte b_8 = (byte) stream.readByte();
				if (b_8 != 0) {
					this.aBool1424 = true;
				}
				byte b_9 = (byte) stream.readByte();
				this.anIntArray1395[optionsLength] = hash;
				this.aByteArrayArray1366[optionsLength] = new byte[] { b_8 };
				this.aByteArrayArray1367[optionsLength] = new byte[] { b_9 };
			}
		}
		this.useOnName = stream.readString();
		optionsLength = stream.readUnsignedByte();
		menuOptionsCount = optionsLength & 0xf;
		int menuCursorMask = optionsLength >> 4;
		int i;
		if (menuOptionsCount > 0) {
			this.optionNames = new String[menuOptionsCount];
			for (i = 0; i < menuOptionsCount; i++) {
				this.optionNames[i] = stream.readString();
			}
		}
		if (menuCursorMask > 0) {
			i = stream.readUnsignedByte();
			this.opCursors = new int[i + 1];
			for (menuOptionsCount = 0; menuOptionsCount < this.opCursors.length; menuOptionsCount++)
				this.opCursors[menuOptionsCount] = -1;
			this.opCursors[i] = stream.readUnsignedShort();
		}
		if (menuCursorMask > 1) {
			oneCursor = stream.readUnsignedByte();
			this.opCursors[oneCursor] = stream.readUnsignedShort();
		}
		this.opName = stream.readString();
		if (this.opName.equals("")) {
			this.opName = null;
		}
		this.anInt1380 = stream.readUnsignedByte();
		this.anInt1381 = stream.readUnsignedByte();
		this.anInt1382 = stream.readUnsignedByte();
		this.useOptionString = stream.readString();
		i = -1;
		if (IFTargetParams.getUseOptionFlags(optionMask) != 0) {
			i = stream.readUnsignedShort();
			if (i == 65535)
				i = -1;
			this.targetOverCursor = stream.readUnsignedShort();
			if (this.targetOverCursor == 65535) {
				this.targetOverCursor = -1;
			}
			this.targetLeaveCursor = stream.readUnsignedShort();
			if (this.targetLeaveCursor == 65535) {
				this.targetLeaveCursor = -1;
			}
		}
		if (revision >= 0) {
			this.mouseOverCursor = stream.readUnsignedShort();
			if (this.mouseOverCursor == 65535) {
				this.mouseOverCursor = -1;
			}
		}
		this.targetParams = new IFTargetParams(optionMask, i);
		if (revision >= 0) {
			if (this.aHashTable4823 == null)
				this.aHashTable4823 = new Hashtable();
			menuOptionsCount = stream.readUnsignedByte();
			int i_12;
			int i_13;
			int i_14;
			for (i_12 = 0; i_12 < menuOptionsCount; i_12++) {
				i_13 = stream.read24BitUnsignedInteger();
				i_14 = stream.readInt();
				this.aHashTable4823.put(i_13, i_14);
			}
			i_12 = stream.readUnsignedByte();
			for (i_13 = 0; i_13 < i_12; i_13++) {
				i_14 = stream.read24BitUnsignedInteger();
				String string_15 = stream.readGJString();
				this.aHashTable4823.put(i_14, string_15);
			}
		}
		this.onLoadScript = this.decodeScript(stream);
		this.onMouseHoverScript = this.decodeScript(stream);
		this.onMouseLeaveScript = this.decodeScript(stream);
		this.anObjectArray1396 = this.decodeScript(stream);
		this.anObjectArray1400 = this.decodeScript(stream);
		this.anObjectArray1397 = this.decodeScript(stream);
		this.mouseLeaveScript = this.decodeScript(stream);
		this.anObjectArray1387 = this.decodeScript(stream);
		this.anObjectArray1409 = this.decodeScript(stream);
		this.params = this.decodeScript(stream);
		if (revision >= 0) {
			this.anObjectArray1393 = this.decodeScript(stream);
		}
		this.popupScript = this.decodeScript(stream);
		this.anObjectArray1386 = this.decodeScript(stream);
		this.anObjectArray1319 = this.decodeScript(stream);
		this.anObjectArray1302 = this.decodeScript(stream);
		this.anObjectArray1389 = this.decodeScript(stream);
		this.anObjectArray1451 = this.decodeScript(stream);
		this.anObjectArray1394 = this.decodeScript(stream);
		this.anObjectArray1412 = this.decodeScript(stream);
		this.anObjectArray1403 = this.decodeScript(stream);
		this.anObjectArray1405 = this.decodeScript(stream);
		this.varps = this.method4150(stream);
		this.mouseLeaveArrayParam = this.method4150(stream);
		this.anIntArray1402 = this.method4150(stream);
		this.anIntArray1315 = this.method4150(stream);
		this.anIntArray1406 = this.method4150(stream);
	}

	private final Object[] decodeScript(InputStream buffer) {
		int length = buffer.readUnsignedByte();
		if (0 == length)
			return null;
		Object[] params = new Object[length];
		for (int index = 0; index < length; index++) {
			int type = buffer.readUnsignedByte();
			if (type == 0)
				params[index] = buffer.readInt();
			else if (type == 1)
				params[index] = buffer.readString();
		}
		usesScripts = true;
		return params;
	}

	private final int[] method4150(InputStream buffer) {
		int length = buffer.readUnsignedByte();
		if (length == 0)
			return null;
		int[] arr = new int[length];
		for (int index = 0; index < length; index++)
			arr[index] = buffer.readInt();
		return arr;
	}

	final int method14502(int i) {
		return i >> 11 & 0x7f;
	}

    public void setValues(ArrayList<ComponentSetting> settings) {
        try {
            for(ComponentSetting setting : settings) {
                if(setting.getVariable().equals("type")) {
                    setting.setValue(type.ordinal());
                    continue;
                }
                if(setting.getVariable().equals("modelType")) {
                    setting.setValue(modelType.ordinal());
                    continue;
                }
                Field field = this.getClass().getDeclaredField(setting.getVariable());
                field.setAccessible(true);
                Object value = field.get(this);
                if(value instanceof String[])
                    setting.setValue(Arrays.toString((String[]) value));
                else if(value instanceof int[])
                    setting.setValue(Arrays.toString((int[]) value));
                else if(value instanceof Object[])
                    setting.setValue(Arrays.toString((Object[]) value));
                else
                    setting.setValue(value);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static int getX(IComponentDefinitions c, int inter) {
        if (c.parent == -1)
            return c.positionX;
        IComponentDefinitions parent = InterfaceUtils.getParent(c.parent);
        int positionX = c.positionX;
        while (parent != null) {
            ComponentPosition.setValues(parent);
            positionX += parent.positionX;
            parent = InterfaceUtils.getParent(parent.parent);
        }
        return positionX;
    }

    public static int getY(IComponentDefinitions c, int inter) {
        if (c.parent == -1)
            return c.positionY;
        IComponentDefinitions parent = InterfaceUtils.getParent(c.parent);
        int positionY = c.positionY;
        while (parent != null) {
            ComponentPosition.setValues(parent);
            positionY += parent.positionY;
            parent = InterfaceUtils.getParent(parent.parent);
        }

        return positionY;
    }

    /**
     * draws the interface on the panel
     *
     * @param interfaceId
     * @throws IOException TODO remove all the booleans, shitcode lol
     */
    public static BufferedImage makeInterface(int interfaceId, IComponentDefinitions[] defs, boolean showContainers, boolean showHidden, boolean showModels, int pW, int pH, int selected) {
        System.out.println("Making image");
        BufferedImage result = new BufferedImage(pW-10, pH-10, BufferedImage.TYPE_INT_RGB);
        /**
         * drawing
         **/
        Graphics g = result.getGraphics();
        /**
         * make sure you get them in the right order (containers)
         */
        List<IComponentDefinitions> orderedComponents = InterfaceUtils.getOrderedComps(interfaceId, defs);
        for (IComponentDefinitions component : orderedComponents) {
            ComponentPosition.setValues(component);
            /**
             * if hidden or no null
             */
            if (component == null || (InterfaceUtils.isHidden(component) && !showHidden)) {
                if(component != null && (InterfaceUtils.isHidden(component) && !showHidden))
                    System.out.println("Suppressing hidden component: "+component.componentId);
                continue;
            }
            /* vars */
            int width = component.width;
            int height = component.height;
            int x = getX(component, interfaceId);
            int y = getY(component, interfaceId);
            IComponentDefinitions parent = InterfaceUtils.getParent(component.parent);// ComponentDefinition.getParent(component,
                                                                                      // interfaceId);
            /*
             * if (parent == null) continue;
             */
            /* setting correct values of the parent ofcourse */
            if (parent != null) {
                ComponentPosition.setValues(parent);

                if (width > parent.width)
                    width = parent.width;
                if (height > parent.height)
                    height = parent.height;
                if (component.positionX < 0)
                    component.positionX = 0;
                if ((component.positionX + component.width) > parent.width)
                    component.positionX = (parent.width - component.width);
                if (component.positionY < 0)
                    component.positionY = 0;
                if ((component.positionY + component.height) > parent.height)
                    component.positionY = (parent.height - component.height);
            }
            /**
             * checks if it's a sprite
             */
            if (component.type == ComponentType.SPRITE && component.spriteId > -1) {
                BufferedImage sprite = null;
                try {
                    SpriteDefinitions spriteDefs = SpriteDefinitions.getSprite(component.spriteId, 0);
                    if(spriteDefs == null)
                        throw new InterfaceBuilderException("Unable to find sprite: "+component.spriteId);
                    if(spriteDefs.getImages().length < 1)
                        throw new InterfaceBuilderException("Unable to find images for sprite: "+component.spriteId);
                    sprite = ImageUtils.resize(spriteDefs.getImages()[0], width, height);
                    File file = new File("./data/images/sprites/"+component.spriteId+"_0.png");
                    if(!file.exists()) {
                        System.out.println("Dumping sprite: "+component.spriteId);
                        FileOutputStream fos = new FileOutputStream(file);
                        ImageIO.write(sprite, "PNG", fos);
                        fos.flush();
                        fos.close();
                    }
                } catch (InterfaceBuilderException | IOException e) {
                    e.printStackTrace();
                    return null;
                }
                /* horizontal flip */
                if (component.hFlip)
                    sprite = ImageUtils.horizontalFlip(sprite);
                /* vertical flip */
                if (component.vFlip)
                    sprite = ImageUtils.verticalFlip(sprite);
                g.drawImage(sprite, x, y, null);

            }

            /**
             * Rectangles
             */
            if (component.type == ComponentType.FIGURE) {
                if (component.color == 0) {
                    g.setColor(Color.black);
                } else {
                    /** Setting the color **/
                    Color color = new Color(component.color);
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    g.setColor(new Color(red, green, blue));
                }
                g.drawRect(getX(component, interfaceId), getY(component, interfaceId), component.width, component.height);
                if (component.filled)
                    g.fillRect(getX(component, interfaceId), getY(component, interfaceId), component.width, component.height);
            }
            /**
             * models
             */
            if (component.type == ComponentType.MODEL && showModels) {
                g.setColor(Color.BLUE);
                g.drawRect(getX(component, interfaceId), getY(component, interfaceId), component.width, component.height);

            }
            /**
             * Containers
             *
             *
             * ComponentDefinition.getX(comp, interafece)
             */
            if (component.type == ComponentType.CONTAINER) {
                BufferedImage sprite = null;
                if (ContainerHelper.isScrollBar(component)) {
                    try {
                        sprite = ImageUtils.resize(ImageIO.read(new File("data/scriptsprites/scrollbar.jpg")), width,
                                height);
                    } catch (IOException e) {
                        System.out.println("scrollbar.jpg not found");
                    }
                    g.drawImage(sprite, getX(component, interfaceId), getY(component, interfaceId), null);
                } else if (ContainerHelper.isButton(component)) {
                    try {
                        sprite = ImageUtils.resize(ImageIO.read(new File("data/scriptsprites/button.png")), width,
                                height);
                    } catch (IOException e) {
                        System.out.println("button.png not found");
                    }
                    g.drawImage(sprite, getX(component, interfaceId), getY(component, interfaceId), null);
                } else if (showContainers) {
                    g.setColor(Color.RED);
                    if (component.parent > 0) {
                        g.drawRect(getX(component, interfaceId), getY(component, interfaceId), component.width, component.height);
                    } else {
                        g.setColor(Color.green);
                        g.drawRect(component.positionX, component.positionY, component.width, component.height);

                    }
                }
            }
            /**
             * checks if it's text TODO make it written by container some text doesn't get
             * shown because it's under the other sprite
             */
            if (component.type == ComponentType.TEXT) {
                FontMetrics fm = g.getFontMetrics();
                Rectangle2D rect = fm.getStringBounds(component.text, g);
                /**
                 * color of the text
                 */
                Color color = new Color(component.color);
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                g.setColor(new Color(red, green, blue));
                /** setting font **/
                g.setFont(new Font("Helvetica", 0, 11));
                if (component.parent == -1) {
                    g.drawString(component.text,
                            (int) (component.positionX + component.width / 2 - rect.getWidth() / 2),
                            (int) (component.positionY + component.height / 2 + rect.getHeight() / 2));
                } else {
                    ComponentPosition.setValues(parent);

                    // text in buttons, to center it lol capypasta
                    if (component.baseWidth == 0 && component.baseHeight == 0) {
                        FontMetrics metrics = g.getFontMetrics(new Font("Helvetica", 0, 11));
                        // Determine the X coordinate for the text
                        int x2 = parent.positionX + (parent.width - metrics.stringWidth(component.text)) / 2;
                        // Determine the Y coordinate for the text (note we add the ascent, as in java
                        // 2d 0 is top of the screen)
                        int y2 = parent.positionY + ((parent.height - metrics.getHeight()) / 2) + metrics.getAscent();
                        // Set the font
                        g.setFont(new Font("Helvetica", 0, 11));
                        // Draw the String
                        g.drawString(component.text, x2, y2);
                        // System.out.println("Setting text with baseWidth/Height @ "+x2+","+y2);
                    } else {
                        /* position */
                        int positionX = getX(component, interfaceId);
                        int positionY = getY(component, interfaceId);
                        /* not drAWING OUTSIDE THE CONTAINER */
                        if (positionX > parent.width + parent.positionX)
                            positionX = parent.width - component.width;
                        // if(positionY > parent.height + parent.positionY)
                        // positionY = parent.height - component.height;
                        g.drawString(component.text, (int) (positionX + component.width / 2 - rect.getWidth() / 2),
                                (int) (positionY + component.height / 2 + rect.getHeight() / 2));

                    }
                }
                /**
                 * special
                 */
                if (component.text.contains("</u>")) {

                }
                /**
                 * testing
                 */
                // if (this.chckbxRealFonttesting.isSelected()) {
                //     int positionX = ComponentDefinition.getX(component, interfaceId);
                //     int positionY = ComponentDefinition.getY(component, interfaceId);
                //     int startX = (int) (positionX + component.width / 2 - rect.getWidth() / 2);
                //     for (BufferedImage im : FontDecoding.getTextArray(component)) {
                //         g.drawImage(ImageUtils.colorImage(im, color), startX,
                //                 (int) (positionY + component.height / 2 + rect.getHeight() / 2), null);
                //         startX += im.getWidth() / 2;
                //     }
                // }
            }
        }
        Optional<IComponentDefinitions> optional = orderedComponents.stream().filter(c -> c.componentId == selected).findFirst();
        if(optional.isPresent()) {
            IComponentDefinitions component = optional.get();
            g.setColor(Color.YELLOW);
            if (component.parent != -1)
                g.drawRect(getX(component, interfaceId), getY(component, interfaceId), component.width,
                        component.height);
            else
                g.drawRect(component.positionX, component.positionY, component.width, component.height);
        }
        File file = new File("./data/images/interfaces/"+interfaceId+".png");
        if(file.exists()) file.delete();
        try {
            System.out.println("Dumping interface pic: "+interfaceId);
            FileOutputStream fos = new FileOutputStream(file);
            ImageIO.write(result, "PNG", fos);
            fos.flush();
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] encode() {
        OutputStream stream = new OutputStream();

        stream.writeByte(revision);

        stream.writeByte(typeId);
        if((typeId & 0x80 ^ 0xffffffff) != -1)
            stream.writeString(name);
        
        stream.writeShort(contentType);
        stream.writeShort(basePositionX);
        stream.writeShort(basePositionY);
        stream.writeShort(baseWidth);
        stream.writeShort(baseHeight);

        stream.writeByte(aspectWidthType);
        stream.writeByte(aspectHeightType);
        stream.writeByte(aspectXType);
        stream.writeByte(aspectYType);
        if(parent == -1 || parent == 65535)
            stream.writeShort(65535);
        else
            stream.writeShort(parent);
        int flag = 0;
        if(hidden) flag |= 0x1;
        if(noClickThrough && revision >= 0)
            flag |= 0x2;
        stream.writeByte(flag);

        if(type == ComponentType.CONTAINER) {
            stream.writeShort(scrollWidth);
            stream.writeShort(scrollHeight);
            if(revision < 0)
                stream.writeByte(noClickThrough ? 1 : 0);
        }
        if(type == ComponentType.SPRITE) {
            stream.writeInt(spriteId);
            stream.writeShort(angle2d);
            flag = 0;
            if(tiling) flag |= 0x1;
            if(alpha) flag |= 0x2;
            stream.writeByte(flag);
            stream.writeByte(transparency);
            stream.writeByte(borderThickness);
            stream.writeInt(anInt1324);
            stream.writeByte(vFlip ? 1 : 0);
            stream.writeByte(hFlip ? 1 : 0);
            stream.writeInt(color);
            if(revision >= 3)
                stream.writeByte(clickMask ? 1 : 0);
        }
        if(type == ComponentType.MODEL) {
            stream.writeBigSmart(modelId);
            flag = 0;
            if(bool_6) flag |= 0x1;
            if(hasOrigin) flag |= 0x2;
            if(usesOrthogonal) flag |= 0x4;
            if(aBool1345) flag |= 8;
            if(bool_6 || hasOrigin) {
                stream.writeShort(originX);
                stream.writeShort(originY);
                if (!bool_6)
                    stream.writeShort(originZ);
                stream.writeShort(spritePitch);
                stream.writeShort(spriteRoll);
                stream.writeShort(spriteYaw);
                stream.writeShort(spriteScale);
            }

            stream.writeBigSmart(animation);
            if(aspectWidthType != 0)
                stream.writeShort(aspectWidth);
            
            if(aspectHeightType != 0)
                stream.writeShort(aspectHeight);
        }
        if(type == ComponentType.TEXT) {
            stream.writeBigSmart(fontId);
            if(revision >= 2)
                stream.writeByte(monospaced ? 1 : 0);
            stream.writeString(text);
            stream.writeByte(anInt1358);
            stream.writeByte(textHorizontalAli);
            stream.writeByte(textVerticalAli);
            stream.writeByte(shadow ? 1 : 0);
            stream.writeInt(color);
            stream.writeByte(transparency);
            if(revision >= 0) stream.writeByte(multiline);
        }
        if(type == ComponentType.FIGURE) {
            stream.writeInt(color);
            stream.writeByte(filled ? 1 : 0);
            stream.writeByte(transparency);
        }
        if(type == ComponentType.LINE) {
            stream.writeByte(lineWidth);
            stream.writeInt(color);
            stream.writeByte(lineDirection ? 1 : 0);
        }

        stream.write24BitInteger(targetParams.getSettings());
        stream.writeByte(hash);
        if(hash != 0) {
            for(int i = 0; i < anIntArray1395.length; i++) {
                stream.writeInt(anIntArray1395[i]);
                stream.writeByte(aByteArrayArray1366[i][0]);
                stream.writeByte(aByteArrayArray1367[i][0]);
            }
        }
        stream.writeString(useOnName);
        if(optionNames != null)
            stream.writeByte(optionNames.length);
        else
            stream.writeByte(0);
        if(optionNames != null && optionNames.length > 0)
            for(int i = 0; i < optionNames.length; i++)
                stream.writeString(optionNames[i] == null ? "" : optionNames[i]);
        int menuCursorMask = menuOptionsCount >> 4;

        if(menuCursorMask > 0) {
            stream.writeByte(opCursors.length-1);
            stream.writeShort(opCursors[opCursors.length-2]);
        }

        if(menuCursorMask > 1) {
            stream.writeByte(oneCursor);
            stream.writeShort(opCursors[oneCursor]);
        }

        stream.writeString(opName == null ? "" : opName);
        stream.writeByte(anInt1380);
        stream.writeByte(anInt1381);
        stream.writeByte(anInt1382);
        stream.writeString(useOptionString);

        if(targetParams.getUseOptionFlags() != 0) {
            stream.writeShort(targetParams.getInterfaceId());
            stream.writeShort(targetOverCursor);
            stream.writeShort(targetLeaveCursor);
        }

        if(revision >= 0)
            stream.writeShort(mouseOverCursor);

        if(revision >= 0) {
            HashMap<Integer, Integer> intVals = new HashMap<>();
            HashMap<Integer, String> strVals = new HashMap<>();
            
            for(Object key : aHashTable4823.keySet()) {
                Object val = aHashTable4823.get(key);
                if(val instanceof Integer)
                    intVals.put((int) key, (int) val);
                else
                    strVals.put((int) key, (String) val);
            }

            stream.writeByte(intVals.size());
            for(int key : intVals.keySet()) {
                int val = intVals.get(key);
                stream.write24BitInteger(key);
                stream.writeInt(val);
            }
            stream.writeByte(strVals.size());
            for(int key : strVals.keySet()) {
                String val = strVals.get(key);
                stream.write24BitInteger(key);
                stream.writeGJString(val);
            }
        }
        encodeScript(onLoadScript, stream);
        encodeScript(onMouseHoverScript, stream);
        encodeScript(onMouseLeaveScript, stream);
        encodeScript(anObjectArray1396, stream);
        encodeScript(anObjectArray1400, stream);
        encodeScript(anObjectArray1397, stream);
        encodeScript(mouseLeaveScript, stream);
        encodeScript(anObjectArray1387, stream);
        encodeScript(anObjectArray1409, stream);
        encodeScript(params, stream);
        if(revision >= 0)
            encodeScript(anObjectArray1393, stream);
        encodeScript(popupScript, stream);
        encodeScript(anObjectArray1386, stream);
        encodeScript(anObjectArray1319, stream);
        encodeScript(anObjectArray1302, stream);
        encodeScript(anObjectArray1389, stream);
        encodeScript(anObjectArray1451, stream);
        encodeScript(anObjectArray1394, stream);
        encodeScript(anObjectArray1412, stream);
        encodeScript(anObjectArray1403, stream);
        encodeScript(anObjectArray1405, stream);
        encodeIntArr(varps, stream);
        encodeIntArr(mouseLeaveArrayParam, stream);
        encodeIntArr(anIntArray1402, stream);
        encodeIntArr(anIntArray1315, stream);
        encodeIntArr(anIntArray1406, stream);

        return stream.getBuffer();
    }

    public void encodeIntArr(int[] params, OutputStream stream) {
        if(params == null) {
            stream.writeByte(0);
            return;
        }
        stream.writeByte(params.length);
        for(int i = 0; i < params.length; i++)
            stream.writeInt(params[i]);
    }

    public void encodeScript(Object[] params, OutputStream stream) {
        if (params == null) {
            stream.writeByte(0);
            return;
        }
        stream.writeByte(params.length);
        for(int i = 0; i < params.length; i++) {
            if(params[i] instanceof Integer) {
                stream.writeByte(0);
                stream.writeInt((int) params[i]);
            } else {
                stream.writeByte(1);
                stream.writeString((String) params[i]);
            }
        }
    }

    /**
     * returns all the childeren of a sprite
     * 
     * @param interfaceId
     * @param hash
     * @return
     */
    public static ArrayList<IComponentDefinitions> getChildsByParent(IComponentDefinitions[] defs, int hash) {
        IComponentDefinitions[] allComponents = defs;
        ArrayList<IComponentDefinitions> foundChilderen = new ArrayList<>();
        for (IComponentDefinitions component : allComponents) {
            if (component == null)
                continue;
            if (hash == component.parent)
                foundChilderen.add(component);
        }
        return foundChilderen;
    }

    public static boolean hasChilds(IComponentDefinitions[] defs, int parentHash) {
        for (IComponentDefinitions c : defs) {
            if (c == null)
                continue;
            if (c.parent == parentHash)
                return true;
        }
        return false;
    }

    /**
     * returns all the containers of a single interface
     * 
     * @param interfaceId
     * @return
     */
    public static ArrayList<IComponentDefinitions> getInterfaceContainers(int interfaceId, IComponentDefinitions[] defs) {
        IComponentDefinitions[] possibleParents = defs;
        ArrayList<IComponentDefinitions> containers = new ArrayList<>();
        if (possibleParents == null)
            return null;
        for (IComponentDefinitions component : possibleParents) {
            if (component == null)
                continue;
            if (component.type == ComponentType.CONTAINER && IComponentDefinitions.hasChilds(defs, component.uid)) {
                //// system.out.println("Container id: "+component.componentId);
                containers.add(component);
            }
        }

        return containers;
    }
}
