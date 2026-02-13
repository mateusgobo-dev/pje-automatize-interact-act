/**
 * JSonUtil.java
 *
 * Data: 21/10/2019
 */
package br.com.infox.cliente.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;


/**
 * Classe utilitária para manipular JSON.
 * 
 * @author Adriano Pamplona
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JSONUtil {

	@Logger
	private static Log logger;
	
	private static ObjectMapper mapper = novoObjectMapper();
	
	/**
	 * Construtor.
	 */
	private JSONUtil() {
		// Construtor.
	}

	/**
	 * Converte um objeto para uma string no formato json.
	 * 
	 * @param objeto Objeto que será convertido.
	 * @return String json.
	 */
	public static String converterObjetoParaString(Object objeto) {
		String resultado = null;
		try {
			if (objeto instanceof String) {
				resultado = (String) objeto;
			} else {
				resultado = mapper.writeValueAsString(objeto);
			}
		} catch (JsonProcessingException e) {
			String mensagem = "Erro ao converter Objeto para Json. Erro: {0}";
			logger.error(mensagem, e.getLocalizedMessage());
		}
		
		return resultado;
	}
	
	/**
	 * Converte um objeto para um array de bytes de uma string no formato json.
	 * 
	 * @param objeto Objeto que será convertido.
	 * @return byte[] de uma string json.
	 */
	public static byte[] converterObjetoParaBytes(Object objeto) {
		byte[] resultado = null;
		try {
			if (objeto instanceof byte[]) {
				resultado = (byte[]) objeto;
			} else {
				resultado = mapper.writeValueAsBytes(objeto);
			}
		} catch (JsonProcessingException e) {
			String mensagem = "Erro ao converter Objeto para Json. Erro: {0}";
			logger.error(mensagem, e.getLocalizedMessage());
		}
		
		return resultado;
	}

    /**
     * Converte uma string JSON para um objeto do tipo especificado.
     *
     * @param json A string JSON a ser convertida.
     * @param tipo A classe do tipo para o qual a string JSON deve ser convertida.
     * @param <T> O tipo de retorno.
     * @return Um objeto do tipo especificado, ou null se a conversão falhar.
     */
	public static <T> T converterStringParaObjeto(String json, Class<T> tipo) {
		T resultado = null;

		try {
			if (StringUtils.isNotBlank(json)) {
				JavaType javaType;

				// Verifica se o tipo foi informado
				if (tipo == null) {
					throw new IllegalArgumentException("O tipo de retorno não pode ser nulo");
				}

				// Determina o tipo de JavaType com base no conteúdo do JSON e na classe fornecida
				if (json.trim().charAt(0) == '[') {
					// Se o JSON começa com '[', trata-se de uma coleção
					javaType = mapper.getTypeFactory().constructCollectionType(Collection.class, tipo);
				} else if (tipo.isAssignableFrom(Map.class)) {
					javaType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
				} else {
					// Para outros tipos, usa o tipo fornecido
					javaType = mapper.getTypeFactory().constructType(tipo);
				}

				if (tipo.isAssignableFrom(JSONObject.class)) {
					resultado = (T) new JSONObject(json);
				} else {
					// Converte a string JSON para o tipo especificado usando ObjectMapper
					resultado = mapper.readValue(json, javaType);
				}
			}
		} catch (Exception e) {
			String mensagem = "Erro ao converter Json para Objeto. Erro: {0}";
			logger.error(mensagem, e.getLocalizedMessage());
		}

		return resultado;
	}

	/**
	 * @return Novo ObjectMapper.
	 */
	public static ObjectMapper novoObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
		mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		
		mapper.registerModule(new JSR353Module());
		mapper.registerModule(novoModuleSerializer(DataHandler.class, new DataHandlerJson.Serializer()));
    	mapper.registerModule(novoModuleDeserializer(DataHandler.class, new DataHandlerJson.Deserializer()));
    	mapper.registerModule(novoModuleBeanSerializerIgnorePrimitives());
		return mapper;
	}
	
	/**
	 * Retorna um SimpleModule com um Serializer para o tipo definido.
	 * 
	 * @param classe Tipo da classe.
	 * @param serializer JsonSerializer.
	 * @return SimpleModule.
	 */
	public static SimpleModule novoModuleSerializer(Class classe, JsonSerializer serializer) {
		SimpleModule module = new SimpleModule();
		module.addSerializer(classe, serializer);

		return module;
	}

	/**
	 * Retorna um SimpleModule com um Deserializer para o tipo definido.
	 * 
	 * @param classe Tipo da classe.
	 * @param serializer JsonDeserializer.
	 * @return SimpleModule.
	 */
	public static SimpleModule novoModuleDeserializer(Class classe, JsonDeserializer deserializer) {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(classe, deserializer);

		return module;
	}
	
	/**
	 * Retorna um SimpleModule com BeanSerializer que ignora os atributos primitivos nulos.
	 * 
	 * @return SimpleModule.
	 */
	private static SimpleModule novoModuleBeanSerializerIgnorePrimitives() {
		return new SimpleModule() {

            public void setupModule(SetupContext context) {
                super.setupModule(context);
                BeanSerializerModifier modifier = new BeanSerializerModifier(){
                	@Override
                    public  JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                        if (serializer instanceof BeanSerializerBase) {
                            return new BeanSerializer((BeanSerializerBase) serializer);
                        }
                        return serializer;

                    }
                };
                context.addBeanSerializerModifier(modifier);
            }           
        };
	}
	
	public static <T> T converterStringParaObjeto(HttpEntity entity, Class<T> tipo) throws ParseException, IOException {
		return converterStringParaObjeto(EntityUtils.toString(entity), tipo);
	}
	
	private static boolean isNull(JSONObject json, String key) {
		boolean ret = false;
		if(!json.has(key) || json.isNull(key)) {
			ret = true;
		}
		return ret;
		
	}

	public static JSONObject getJsonObject(JSONObject json, String key) {
		JSONObject ret = null;
		if(!isNull(json, key)) {
			ret = json.getJSONObject(key);
		}
		return ret;
	}

	public static String getJsonString(JSONObject json, String key) {
		String ret = null;
		if(!isNull(json, key)) {
			ret = json.getString(key);
		}
		return ret;
	}
	
	public static Integer getJsonInteger(JSONObject json, String key) {
		Integer ret = null;
		if(!isNull(json, key)) {
			ret = json.getInt(key);
		}
		return ret;
	}

	public static Long getJsonLong(JSONObject json, String key) {
		Long ret = null;
		if(!isNull(json, key)) {
			ret = json.getLong(key);
		}
		return ret;
	}

	public static Boolean getJsonBoolean(JSONObject json, String key) {
		Boolean ret = null;
		if(!isNull(json, key)) {
			ret = json.getBoolean(key);
		}
		return ret;
	}

}
