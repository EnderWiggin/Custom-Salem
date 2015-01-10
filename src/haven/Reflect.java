package haven;

import java.lang.reflect.Field;

public class Reflect {
    public static Object getFieldValue(Object obj, String name){
	Object v = null;
	try {
	    Field f = getField(obj, name);
	   v = f.get(obj);
	} catch (NoSuchFieldException ignored) {
	} catch (IllegalAccessException ignored) {}
	return v;
    }

    public static int getFieldValueInt(Object obj, String name){
	int v = 0;
	try {
	    Field f = getField(obj, name);
	    v = f.getInt(obj);
	} catch (NoSuchFieldException ignored) {
	} catch (IllegalAccessException ignored) {}
	return v;
    }

    public static double getFieldValueDouble(Object obj, String name){
	double v = 0;
	try {
	    Field f = getField(obj, name);
	    v = f.getDouble(obj);
	} catch (NoSuchFieldException ignored) {
	} catch (IllegalAccessException ignored) {}
	return v;
    }

    private static Field getField(Object obj, String name) throws NoSuchFieldException {
	Field f = obj.getClass().getDeclaredField(name);
	f.setAccessible(true);
	return f;
    }
}
