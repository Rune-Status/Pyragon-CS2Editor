package com.cryo.utils;

import com.cryo.cache.Cache;
import com.cryo.cache.IndexType;
import com.cryo.cs2.CS2Instruction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class Utilities {

    public static char[] CP_1252_CHARACTERS = { '\u20ac', '\0', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6',
            '\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017d', '\0', '\0', '\u2018', '\u2019', '\u201c',
            '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153',
            '\0', '\u017e', '\u0178' };

    @SuppressWarnings({"rawtypes"})
    public static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile().replaceAll("%20", " ")));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    @SuppressWarnings("rawtypes")
    private static List<Class> findClasses(File directory, String packageName) {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                try {
                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                } catch (Throwable e) {

                }
            }
        }
        return classes;
    }

    public static String readString(byte[] buffer, int i_1, int i_2) {
        char[] arr_4 = new char[i_2];
        int offset = 0;

        for (int i_6 = 0; i_6 < i_2; i_6++) {
            int i_7 = buffer[i_6 + i_1] & 0xff;
            if (i_7 != 0) {
                if (i_7 >= 128 && i_7 < 160) {
                    char var_8 = CP_1252_CHARACTERS[i_7 - 128];
                    if (var_8 == 0) {
                        var_8 = 63;
                    }

                    i_7 = var_8;
                }

                arr_4[offset++] = (char) i_7;
            }
        }

        return new String(arr_4, 0, offset);
    }

    public static int writeString(CharSequence string, int start, int end, byte[] buffer, int offset) {
        int length = end - start;

        for (int i = 0; i < length; i++) {
            char c = string.charAt(i + start);
            if (c > 0 && c < 128 || c >= 160 && c <= 255) {
                buffer[i + offset] = (byte) c;
            } else if (c == 8364) {
                buffer[i + offset] = -128;
            } else if (c == 8218) {
                buffer[i + offset] = -126;
            } else if (c == 402) {
                buffer[i + offset] = -125;
            } else if (c == 8222) {
                buffer[i + offset] = -124;
            } else if (c == 8230) {
                buffer[i + offset] = -123;
            } else if (c == 8224) {
                buffer[i + offset] = -122;
            } else if (c == 8225) {
                buffer[i + offset] = -121;
            } else if (c == 710) {
                buffer[i + offset] = -120;
            } else if (c == 8240) {
                buffer[i + offset] = -119;
            } else if (c == 352) {
                buffer[i + offset] = -118;
            } else if (c == 8249) {
                buffer[i + offset] = -117;
            } else if (c == 338) {
                buffer[i + offset] = -116;
            } else if (c == 381) {
                buffer[i + offset] = -114;
            } else if (c == 8216) {
                buffer[i + offset] = -111;
            } else if (c == 8217) {
                buffer[i + offset] = -110;
            } else if (c == 8220) {
                buffer[i + offset] = -109;
            } else if (c == 8221) {
                buffer[i + offset] = -108;
            } else if (c == 8226) {
                buffer[i + offset] = -107;
            } else if (c == 8211) {
                buffer[i + offset] = -106;
            } else if (c == 8212) {
                buffer[i + offset] = -105;
            } else if (c == 732) {
                buffer[i + offset] = -104;
            } else if (c == 8482) {
                buffer[i + offset] = -103;
            } else if (c == 353) {
                buffer[i + offset] = -102;
            } else if (c == 8250) {
                buffer[i + offset] = -101;
            } else if (c == 339) {
                buffer[i + offset] = -100;
            } else if (c == 382) {
                buffer[i + offset] = -98;
            } else if (c == 376) {
                buffer[i + offset] = -97;
            } else {
                buffer[i + offset] = 63;
            }
        }

        return length;
    }

    public static final int packGJString2(int position, byte[] buffer, String String) {
        int length = String.length();
        int offset = position;
        for (int index = 0; length > index; index++) {
            int character = String.charAt(index);
            if (character > 127) {
                if (character > 2047) {
                    buffer[offset++] = (byte) ((character | 919275) >> 12);
                    buffer[offset++] = (byte) (128 | ((character >> 6) & 63));
                    buffer[offset++] = (byte) (128 | (character & 63));
                } else {
                    buffer[offset++] = (byte) ((character | 12309) >> 6);
                    buffer[offset++] = (byte) (128 | (character & 63));
                }
            } else
                buffer[offset++] = (byte) character;
        }
        return offset - position;
    }

    public static Object getFieldValue(Object object, Field field) throws Throwable {
        field.setAccessible(true);
        Class<?> type = field.getType();
        if (type == int[][].class) {
            return Arrays.deepToString((int[][]) field.get(object));
        } else if (type == HashMap[].class) {
            return Arrays.toString((HashMap[]) field.get(object));
        } else if (type == CS2Instruction[].class) {
            return Arrays.toString((CS2Instruction[]) field.get(object));
        } else if (type == byte[].class) {
            return Arrays.toString((byte[]) field.get(object));
        } else if (type == int[].class) {
            return Arrays.toString((int[]) field.get(object));
        } else if (type == byte[].class) {
            return Arrays.toString((byte[]) field.get(object));
        } else if (type == short[].class) {
            return Arrays.toString((short[]) field.get(object));
        } else if (type == double[].class) {
            return Arrays.toString((double[]) field.get(object));
        } else if (type == float[].class) {
            return Arrays.toString((float[]) field.get(object));
        } else if (type == boolean[].class) {
            return Arrays.toString((boolean[]) field.get(object));
        } else if (type == Object[].class) {
            return Arrays.toString((Object[]) field.get(object));
        } else if (type == String[].class) {
            return Arrays.toString((String[]) field.get(object));
        }
        return field.get(object);
    }

    public static final int getInterfaceDefinitionsSize() {
        return Cache.STORE.getIndex(IndexType.INTERFACES).getLastArchiveId() + 1;
    }

    public static final int getInterfaceDefinitionsComponentsSize(int interfaceId) {
        return Cache.STORE.getIndex(IndexType.INTERFACES).getLastFileId(interfaceId) + 1;
    }

    public static char cp1252ToChar(byte i) {
        int i_35_ = i & 0xff;
        if (0 == i_35_) {
            throw new IllegalArgumentException("Non cp1252 character 0x"+Integer.toString(i_35_, 16)+" provided");
        }
        if (i_35_ >= 128 && i_35_ < 160) {
            int i_36_ = CP_1252_CHARACTERS[i_35_ - 128];
            if (0 == i_36_) {
                i_36_ = 63;
            }
            i_35_ = i_36_;
        }
        return (char) i_35_;
    }
}
