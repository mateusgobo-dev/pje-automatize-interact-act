package br.jus.cnj.pje.util.formatadorLista;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;

public class PropUtils {
	
	private static SimpleDateFormat dateFormater = new SimpleDateFormat("dd/MM/yyyy");
	
	public static String getPropertyString(Object obj, String property) {
		Object value = getProperty(obj, property);
		
		if(value == null) return "";
		
		if(value instanceof Date) {
			return dateFormater.format((Date)value);
		}

		return value.toString();
	}
	
	public static Object getProperty(Object obj, String property) {
		try {
			return PropertyUtils.getProperty(obj, property);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(e);
		} catch (Exception e) {
			return null;
		}
	}

}
