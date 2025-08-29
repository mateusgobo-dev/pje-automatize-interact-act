/**
 * DataHandlerJson.java.
 *
 * Data: 13/12/2019
 */
package br.com.infox.cliente.util;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.activation.DataHandler;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serializador e Deserializador de DataHandler.
 * 
 * @author Adriano Pamplona
 */
public class DataHandlerJson {

	/**
	 * Serializador DataHandler para Json.
	 * 
	 * @author Adriano Pamplona
	 */
	public static class Serializer extends JsonSerializer<DataHandler> {

		@Override
		public void serialize(DataHandler value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException {
			byte[] bytes = ProjetoUtil.converterParaBytes(value);
			String base64 = Base64.getEncoder().encodeToString(bytes);
			Tika tika = new Tika();
			String mime = tika.detect(bytes);

			gen.writeStartObject();
				gen.writeObjectFieldStart("binario");
					gen.writeFieldName("src");
					gen.writeString(String.format("data:%s;base64,%s", mime, base64));
				gen.writeEndObject();
			gen.writeEndObject();
		}
		
	}
	
	/**
	 * Deserializador Json para DataHandler.
	 * 
	 * @author Adriano Pamplona
	 */
	public static class Deserializer extends JsonDeserializer<DataHandler> {

		@Override
		public DataHandler deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			JsonNode node = jp.getCodec().readTree(jp);
			DataHandler resultado = null;

			if (node != null) {
				byte[] bytes = obterBinario(node);
				String mimeType = obterMimetype(bytes);
				resultado = ProjetoUtil.converterParaDataHandler(bytes, mimeType);
			}

			return resultado;
		}

		/**
		 * @param node
		 * @return bytes
		 */
		protected byte[] obterBinario(JsonNode node) {
			byte[] resultado = null;
			JsonNode binarioNode = node.get("binario");
			if (binarioNode != null && binarioNode.get("src") != null) {
				JsonNode srcNode = binarioNode.get("src");
				String base64 = srcNode.asText();

				if (isBase64(base64)) {
					base64 = base64.substring(base64.indexOf(";base64") + 8);
					Decoder decoder = Base64.getDecoder();
					resultado = decoder.decode(base64);
				}
			}
			return resultado;
		}

		/**
		 * @param binario
		 * @return tamanho
		 */
		protected Long obterTamanho(byte[] binario) {
			Long resultado = null;

			if (binario != null) {
				resultado = new Long(binario.length);
			}
			return resultado;
		}

		/**
		 * @param binario
		 * @return mimetype
		 */
		protected String obterMimetype(byte[] binario) {
			String resultado = null;

			if (binario != null) {
				Tika tika = new Tika();
				resultado = tika.detect(binario);
			}
			return resultado;
		}

		/**
		 * Retorna true se a string estiver no formato base64.
		 * 
		 * @param string
		 * @return boleano
		 */
		protected boolean isBase64(String string) {
			return StringUtils.isNotEmpty(string) && 
					string.startsWith("data:") && 
					string.contains(";base64");
		}
	}
}
