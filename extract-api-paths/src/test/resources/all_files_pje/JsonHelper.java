/**
 * 
 */
package br.jus.cnj.pje.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JsonHelper {
	private static Logger log = Logger.getLogger(JsonHelper.class);
	
	private JsonObject objetojson;
	
    public JsonHelper(JsonObject jsonObject) {
    	this.objetojson = jsonObject;
    }
	
	@SuppressWarnings("rawtypes")
	public static Object toJSON(Object object) throws JSONException {
        if (object instanceof Map) {
            JSONObject json = new JSONObject();
            Map map = (Map) object;
            for (Object key : map.keySet()) {
                json.put(key.toString(), toJSON(map.get(key)));
            }
            return json;
        } else if (object instanceof Iterable) {
            JSONArray json = new JSONArray();
            for (Object value : ((Iterable)object)) {
                json.put(value);
            }
            return json;
        } else {
            return object;
        }
    }

    public static boolean isEmptyObject(JSONObject object) {
        return object.names() == null;
    }

    public static Map<String, Object> getMap(JSONObject object, String key) throws JSONException {
        return toMap(object.getJSONObject(key));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

	/**
	 * Converte uma string no formato json para o objeto JsonObject.
	 * 
	 * @param json String no formato json.
	 * @return JsonObject
	 */
	public static JsonObject converterParaJsonObject(String json) {
		JsonObject resultado = new JsonObject();
		
		if (StringUtils.isNotBlank(json)) {
			String pre = !StringUtils.startsWith(json, "{") ? "{" : "";
			String pos = !StringUtils.endsWith(json, "}") ? "}" : "";
			
			json = pre + json + pos; 
			JsonParser parser = new JsonParser();
			try {
				resultado = parser.parse(json).getAsJsonObject();
			} catch (Exception e) {
				log.error(e);
			}
		}
		return resultado;
	}

	/**
	 * Converte uma string no formato json para um mapa.
	 * 
	 * @param json String no formato json.
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> converterParaMap(String json) {
		Map<String, Object> resultado = new HashMap<String, Object>();
		
		JsonObject jsonObject = converterParaJsonObject(json);
		Set<Entry<String, JsonElement>> set = jsonObject.entrySet();
		for (Entry<String, JsonElement> key : set) {
			JsonElement value = key.getValue();
			resultado.put(key.getKey(), converterJsonElementParaTipoEspecifico(key.getValue()));
		}
		return resultado;
	}

	/**
	 * Converte um JsonElement para um tipo Number, Boolean ou String.
	 * 
	 * @param jsonElement JsonElement
	 * @return Objeto do tipo Number, Boolean ou String.
	 */
	private static Object converterJsonElementParaTipoEspecifico(JsonElement jsonElement) {
		Object resultado = null;
		
		if (jsonElement.isJsonPrimitive()) {
			JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
			if (primitive.isNumber()) {
				resultado = primitive.getAsNumber();
			} else if (primitive.isBoolean()) {
				resultado = primitive.getAsBoolean();
			} else {
				resultado = primitive.getAsString();
			}
		} else {
			resultado = jsonElement.getAsString();
		}
		
		return resultado;
	}
	
	public String getObjetojson(String string) {
		String chave = null;
		JsonObject objeto = objetojson;
		String[] caminhoChave = string.split (Pattern.quote ("."));
		for (int i = 0; i < caminhoChave.length; i++) {
			if(i != caminhoChave.length - 1 && caminhoChave.length != 1 ) {
				objeto = objeto.getAsJsonObject(caminhoChave[i].toString());
			}else{
				chave = objeto.get(caminhoChave[i].toString()).toString().replaceAll("\"", "");
			};
		}
		return !chave.isEmpty() || chave != null ? chave:"";
	}
	
}