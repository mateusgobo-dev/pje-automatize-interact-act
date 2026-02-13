package br.jus.pje.nucleo.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {
	private static String[] pjeProperties = new String[] {"integracao.properties", 
								  					      "eureka-client.properties", 
														  "documentoUrl.properties"};
	
	private static Map<String, String> pjePropertyMap = new HashMap<String, String>();
	
	static {
		
		for(int i=0; i<pjeProperties.length; i++) {
			Properties p = getProperties(pjeProperties[i]);
			for (Object key: p.keySet()) {
				pjePropertyMap.put(key.toString(), p.getProperty(key.toString()));
			}			
		}
	}
	
	public static String getProperty(String resource, String key) {
		String value = null;
		Properties p = getProperties(resource);
		if(p != null) {
			value = p.getProperty(key);
		}
		return value;
	}
	
	public static String getPJeProperty(String key) {
		return pjePropertyMap.get(key);
	}

	public static Properties getProperties(String resource){
		Properties properties = new Properties();
		try {
			InputStream inStream = getStream(resource);
			properties.load(inStream);
			inStream.close();
		} catch (IOException e) {
			System.out.println("Resource not found");
		}
		return properties;
	}

	public static InputStream getStream(String resource) {
		return getClassLoader().getResourceAsStream(resource);
	}

	public static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

}
