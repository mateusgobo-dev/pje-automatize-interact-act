package br.jus.cnj.pje.util.formatadorLista;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {
	
	private static String firstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static Field getField(Class<?> clazz, String name) {
		while(clazz != null) {
			try {
				return clazz.getDeclaredField(name);		
			} catch (Exception e) { }
			clazz = clazz.getSuperclass();
		}
		return null;
	}
	
	public static Method getGet(Class<?> clazz, String fieldName) {
		try {
			return clazz.getMethod("get" + firstUpper(fieldName));
		} catch (Exception e) {
			try {
				return clazz.getMethod("is" + firstUpper(fieldName));
			} catch (Exception e2) {
				return null;
			}
		}
	}
	
	public static Method getSet(Class<?> clazz, String fieldName) throws SecurityException, NoSuchMethodException {
		Field f = getField(clazz, fieldName);
		if(f == null) return null;
		try {
			return clazz.getMethod("set" + firstUpper(fieldName), f.getType());
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Object invoke(Object target, String methodName, Object[] parameters) {
		if(parameters == null) {
			parameters = new Object[0];
		}
		
	    for (Method method : target.getClass().getMethods()) {
	        if (!method.getName().equals(methodName)) {
	            continue;
	        }
	        
	        Class<?>[] parameterTypes = method.getParameterTypes();
	        if(parameterTypes.length != parameters.length) {
	        	continue;
	        }
	        
	        boolean matches = true;
	        for (int i = 0; i < parameterTypes.length; i++) {
	            if (parameters[i] != null && !parameterTypes[i].isAssignableFrom(parameters[i].getClass())) {
	                matches = false;
	                break;
	            }
	        }
	        if (matches) {
	            try {
					return method.invoke(target, parameters);
				} catch (Exception e) {
				}
	        }
	    }
	    
	    throw new IllegalArgumentException("Não foi possível localizar o método '" + methodName + "' na classe '" + target.getClass().getCanonicalName() + "'.");
	}
	
}
